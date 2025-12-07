/* Copyright (C) 2013-2025 TU Dortmund University
 * This file is part of LearnLib <https://learnlib.de>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.learnlib.algorithm.lstar.mmlt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.learnlib.datastructure.observationtable.ObservationTable;
import de.learnlib.datastructure.observationtable.Row;
import de.learnlib.datastructure.observationtable.RowImpl;
import de.learnlib.filter.FilterResponse;
import de.learnlib.filter.RefutableSymbolFilter;
import de.learnlib.oracle.TimedQueryOracle;
import de.learnlib.oracle.TimedQueryOracle.TimerQueryResult;
import de.learnlib.query.DefaultQuery;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.mmlt.TimerInfo;
import net.automatalib.common.util.HashUtil;
import net.automatalib.common.util.collection.IterableUtil;
import net.automatalib.symbol.time.InputSymbol;
import net.automatalib.symbol.time.TimeStepSequence;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.symbol.time.TimedOutput;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The observation table used by the {@link ExtensibleLStarMMLT} learner.
 * <p>
 * Unlike an OT for standard Mealy learning, includes prefixes for the timeout transitions of one-shot timers. Intended
 * to be used with a symbol filter. The filter is queried before adding a new transition for a non-delaying input. If
 * the filter considers the transition to be a silent self-loop, the output of the transition is first verified. If it
 * is actually silent the learner considers the transition to be a silent self-loop. Consequently, it does not add a
 * transition for it. Transitions may be added later if an input was falsely ignored.
 * <p>
 * Assumes that all short prefixes lead to different locations (-> no need to make canonical)
 *
 * @param <I>
 *         input symbol type (of non-delaying inputs)
 * @param <O>
 *         output symbol type
 */
class MMLTObservationTable<I, O> implements ObservationTable<TimedInput<I>, Word<TimedOutput<O>>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MMLTObservationTable.class);
    private static final int NO_CONTENT = -1;

    private final RefutableSymbolFilter<TimedInput<I>, InputSymbol<I>> symbolFilter;

    private final Map<Word<TimedInput<I>>, LocationTimerInfo<I, O>> timerInfoMap; // prefix -> timer info

    private final Map<Word<TimedInput<I>>, RowImpl<TimedInput<I>>> shortPrefixRowMap; // label -> row info
    private final Map<Word<TimedInput<I>>, RowImpl<TimedInput<I>>> longPrefixRowMap; // label -> row info

    private final Map<Integer, List<Word<TimedOutput<O>>>> rowContentMap; // contentID -> row content

    private final List<Word<TimedInput<I>>> suffixes;
    private final Set<Word<TimedInput<I>>> suffixSet;

    private final Alphabet<TimedInput<I>> alphabet;
    private final long minTimerQueryWaitTime;
    private final TimedOutput<O> silentOutput; // used for symbol filtering

    MMLTObservationTable(Alphabet<TimedInput<I>> alphabet,
                         long minTimerQueryWaitTime,
                         RefutableSymbolFilter<TimedInput<I>, InputSymbol<I>> symbolFilter,
                         O silentOutput) {
        this.alphabet = alphabet;

        this.symbolFilter = symbolFilter;
        this.silentOutput = new TimedOutput<>(silentOutput);
        this.minTimerQueryWaitTime = minTimerQueryWaitTime;

        this.timerInfoMap = new HashMap<>();

        // use linked hashmaps for stable insertion-order
        this.shortPrefixRowMap = new LinkedHashMap<>();
        this.longPrefixRowMap = new LinkedHashMap<>();

        this.rowContentMap = new HashMap<>();
        this.suffixes = new ArrayList<>();
        this.suffixSet = new HashSet<>();
    }

    /**
     * Infers local timers for the provided location.
     *
     * @param location
     *         the source location
     */
    private void identifyLocalTimers(LocationTimerInfo<I, O> location, TimedQueryOracle<I, O> timeOracle) {
        TimerQueryResult<O> timerQueryResponse =
                timeOracle.queryTimers(location.getPrefix(), this.minTimerQueryWaitTime);
        List<TimerInfo<?, O>> timers = timerQueryResponse.timers();

        if (timerQueryResponse.aborted()) {
            int end = ExtensibleLStarMMLT.selectOneShotTimer(timers, Long.MAX_VALUE);
            timers.set(end, timers.get(end).asOneShot());
        }

        // Add timers up to one-shot:
        for (TimerInfo<?, O> timer : timerQueryResponse.timers()) {
            location.addTimer(timer);
            this.extendAlphabet(TimedInput.step(timer.initial()));
            if (!timer.periodic()) {
                break;
            }
        }
    }

    /**
     * Extends the global alphabet without adding new transitions.
     *
     * @param symbol
     *         the new alphabet symbol
     */
    private void extendAlphabet(TimeStepSequence<I> symbol) {
        if (!alphabet.containsSymbol(symbol)) {
            alphabet.asGrowingAlphabetOrThrowException().addSymbol(symbol);
        }

        for (RowImpl<TimedInput<I>> prefix : this.shortPrefixRowMap.values()) {
            prefix.ensureInputCapacity(alphabet.size());
        }
    }

    /**
     * Adds the initial location.
     *
     * @return the corresponding row in the observation table
     */
    private RowImpl<TimedInput<I>> addInitialLocation() {
        RowImpl<TimedInput<I>> newRow = new RowImpl<>(Word.epsilon(), 0, alphabet.size());
        newRow.makeShort(alphabet.size());
        this.shortPrefixRowMap.put(Word.epsilon(), newRow);

        return newRow;
    }

    /**
     * Adds a new location that belongs to the provided short-prefix row. Infers timers for this location and creates
     * outgoing transitions.
     *
     * @param newRow
     *         the newly-added short prefix row
     * @param timeOracle
     *         the time oracle
     */
    private void initLocation(RowImpl<TimedInput<I>> newRow, TimedQueryOracle<I, O> timeOracle) {
        LocationTimerInfo<I, O> timerInfo = new LocationTimerInfo<>(newRow.getLabel());
        this.identifyLocalTimers(timerInfo, timeOracle);

        if (timerInfo.getLastTimer() != null) { // location has timer
            this.timerInfoMap.put(newRow.getLabel(), timerInfo);
        }

        // Add outgoing transitions:
        List<RowImpl<TimedInput<I>>> transitions = this.createOutgoingTransitions(newRow, timeOracle);
        this.queryAllSuffixes(transitions, timeOracle);
    }

    /**
     * Creates transitions for the provided short-prefix row. Adds transitions for non-delaying inputs and a transition
     * for the one-shot timer of the location, if present.
     * <p>
     * If a symbol filter is provided, the filter is queried before adding a transition for a non-delaying input. If the
     * filter considers the input a silent self-loop, no transition is explicitly created for the input.
     *
     * @param spRow
     *         the short prefix row
     * @param timeOracle
     *         the time query oracle
     *
     * @return the new transitions
     */
    private List<RowImpl<TimedInput<I>>> createOutgoingTransitions(RowImpl<TimedInput<I>> spRow,
                                                                   TimedQueryOracle<I, O> timeOracle) {
        List<RowImpl<TimedInput<I>>> transitions = new ArrayList<>();

        Word<TimedInput<I>> sp = spRow.getLabel();

        // First, add transitions for non-delaying symbols:
        for (int i = 0; i < alphabet.size(); i++) {
            TimedInput<I> sym = alphabet.getSymbol(i);
            if (sym instanceof InputSymbol<I> in) {

                Word<TimedInput<I>> lp = sp.append(sym);
                assert !this.shortPrefixRowMap.containsKey(lp);

                RowImpl<TimedInput<I>> succRow = this.longPrefixRowMap.get(lp);
                if (succRow == null) {
                    // Query symbol filter before adding transition:
                    FilterResponse filterResponse = this.symbolFilter.query(sp, in);
                    if (filterResponse == FilterResponse.IGNORE) {
                        // Verify that output is silent:
                        Word<TimedOutput<O>> response = timeOracle.answerQuery(sp, Word.fromLetter(sym));
                        assert response.size() == 1;
                        if (!response.firstSymbol().equals(silentOutput)) {
                            // Not silent -> cannot be silent self-loop:
                            filterResponse = FilterResponse.ACCEPT;

                            // Update filter:
                            this.symbolFilter.accept(sp, in);
                        }
                    }

                    if (filterResponse == FilterResponse.ACCEPT) {
                        // Treat as usual:
                        succRow = this.createLpRow(lp);
                    }
                }

                if (succRow != null) {
                    spRow.setSuccessor(i, succRow);
                    transitions.add(succRow);
                }
            }

        }

        // Second, add one-shot timer transition (if any):
        LocationTimerInfo<I, O> locTimers = timerInfoMap.get(spRow.getLabel());
        if (locTimers != null) {
            TimerInfo<?, O> lastTimer = locTimers.getLastTimer();
            if (lastTimer != null && !lastTimer.periodic()) {
                TimedInput<I> waitSym = new TimeStepSequence<>(lastTimer.initial());
                Word<TimedInput<I>> lp = sp.append(waitSym);
                assert !this.shortPrefixRowMap.containsKey(lp);

                RowImpl<TimedInput<I>> succRow = this.longPrefixRowMap.get(lp);
                if (succRow == null) {
                    succRow = this.createLpRow(lp);
                }
                spRow.setSuccessor(this.alphabet.getSymbolIndex(waitSym), succRow);
                transitions.add(succRow);
            }
        }

        return transitions;
    }

    private RowImpl<TimedInput<I>> createLpRow(Word<TimedInput<I>> prefix) {
        RowImpl<TimedInput<I>> newRow = new RowImpl<>(prefix, 0);
        this.longPrefixRowMap.put(prefix, newRow);

        newRow.setLpIndex(0); // unused

        return newRow;
    }

    /**
     * Identify transitions that have not been closed, i.e., there is no state with the same suffix behavior. Also
     * removes unused content ids.
     *
     * @return the list of unclosed transition, in a deterministic order
     */
    List<List<Row<TimedInput<I>>>> findUnclosedTransitions() {
        // Identify contentIds for locations:
        Set<Integer> spContentIds = new HashSet<>(this.shortPrefixRowMap.size());

        for (RowImpl<TimedInput<I>> row : this.shortPrefixRowMap.values()) {
            spContentIds.add(row.getRowContentId());
        }

        // Identify ids that are not used by any SP and group them by their content id:
        Map<Integer, List<Row<TimedInput<I>>>> lpContentMap =
                new HashMap<>(HashUtil.capacity(this.longPrefixRowMap.size()));

        for (RowImpl<TimedInput<I>> row : this.longPrefixRowMap.values()) {
            int id = row.getRowContentId();

            if (!spContentIds.contains(id)) {
                lpContentMap.computeIfAbsent(id, k -> new ArrayList<>()).add(row);
            }
        }

        // Remove unused content ids:
        this.rowContentMap.keySet().removeIf(key -> !(spContentIds.contains(key) || lpContentMap.containsKey(key)));

        return new ArrayList<>(lpContentMap.values());
    }

    List<List<Row<TimedInput<I>>>> initialize(List<Word<TimedInput<I>>> initialShortPrefixes,
                                              List<Word<TimedInput<I>>> initialSuffixes,
                                              TimedQueryOracle<I, O> oracle) {

        assert this.shortPrefixRowMap.isEmpty() && this.longPrefixRowMap.isEmpty() && initialShortPrefixes.isEmpty();

        // Add initial suffixes:
        for (Word<TimedInput<I>> suffix : initialSuffixes) {
            if (suffixSet.add(suffix)) {
                suffixes.add(suffix);
            }
        }

        // 1. Create initial location:
        RowImpl<TimedInput<I>> newLoc = this.addInitialLocation();
        this.initLocation(newLoc, oracle);
        this.queryAllSuffixes(Collections.singleton(newLoc), oracle);

        // 2. Identify unclosed transitions:
        return this.findUnclosedTransitions();
    }

    private void queryAllSuffixes(Collection<RowImpl<TimedInput<I>>> rows, TimedQueryOracle<I, O> timedOracle) {

        int numSuffixes = this.suffixes.size();
        List<DefaultQuery<TimedInput<I>, Word<TimedOutput<O>>>> queries = new ArrayList<>(rows.size() * numSuffixes);

        for (RowImpl<TimedInput<I>> row : rows) {
            Word<TimedInput<I>> prefix = row.getLabel();

            for (Word<TimedInput<I>> suffix : this.suffixes) {
                queries.add(new DefaultQuery<>(prefix, suffix));
            }
        }

        timedOracle.processQueries(queries);
        Iterator<DefaultQuery<TimedInput<I>, Word<TimedOutput<O>>>> iter = queries.iterator();

        for (RowImpl<TimedInput<I>> row : rows) {
            List<Word<TimedOutput<O>>> outputs = new ArrayList<>(numSuffixes);
            fetchResults(iter, outputs, numSuffixes);

            this.processSuffixOutputs(row, outputs);
        }
    }

    private void processSuffixOutputs(RowImpl<TimedInput<I>> row, List<Word<TimedOutput<O>>> rowContents) {
        if (rowContents.isEmpty()) {
            row.setRowContentId(NO_CONTENT);
            return;
        }

        int contentId = rowContents.hashCode();
        this.rowContentMap.putIfAbsent(contentId, rowContents);
        row.setRowContentId(contentId);
    }

    private static <I, D> void fetchResults(Iterator<DefaultQuery<I, D>> queryIt, List<D> output, int numSuffixes) {
        for (int j = 0; j < numSuffixes; j++) {
            DefaultQuery<I, D> qry = queryIt.next();
            output.add(qry.getOutput());
        }
    }

    List<List<Row<TimedInput<I>>>> addSuffixes(Collection<? extends Word<TimedInput<I>>> newSuffixes,
                                               TimedQueryOracle<I, O> oracle) {
        // 1. Extend current suffixes + identify new suffixes:
        int numOld = this.suffixes.size();
        for (Word<TimedInput<I>> suffix : newSuffixes) {
            if (this.suffixSet.add(suffix)) {
                LOGGER.debug("Adding new suffix '{}'", suffix);
                this.suffixes.add(suffix);
            }
        }
        int numNew = this.suffixes.size();

        if (numOld == numNew) {
            return Collections.emptyList();
        }

        // 2. Update row content:
        int numNewSuffixes = numNew - numOld;
        List<DefaultQuery<TimedInput<I>, Word<TimedOutput<O>>>> queries =
                new ArrayList<>(numNewSuffixes * numberOfRows());
        Iterable<RowImpl<TimedInput<I>>> rows =
                IterableUtil.concat(shortPrefixRowMap.values(), longPrefixRowMap.values());
        List<Word<TimedInput<I>>> newSuffixList = this.suffixes.subList(numOld, numNew);

        for (RowImpl<TimedInput<I>> row : rows) {
            for (Word<TimedInput<I>> suffix : newSuffixList) {
                queries.add(new DefaultQuery<>(row.getLabel(), suffix));
            }
        }

        oracle.processQueries(queries);
        Iterator<DefaultQuery<TimedInput<I>, Word<TimedOutput<O>>>> iterator = queries.iterator();

        for (RowImpl<TimedInput<I>> row : rows) {
            List<Word<TimedOutput<O>>> updatedOutputs = new ArrayList<>(numNew);
            if (row.getRowContentId() != NO_CONTENT) {
                // Add existing suffix outputs:
                updatedOutputs.addAll(rowContents(row));
            }

            fetchResults(iterator, updatedOutputs, numNewSuffixes);
            this.processSuffixOutputs(row, updatedOutputs);
        }

        return this.findUnclosedTransitions();
    }

    List<List<Row<TimedInput<I>>>> toShortPrefixes(List<Row<TimedInput<I>>> lpRows, TimedQueryOracle<I, O> oracle) {
        for (Row<TimedInput<I>> row : lpRows) {
            LOGGER.debug("Adding new location with prefix '{}'", row.getLabel());

            final RowImpl<TimedInput<I>> lpRow = (RowImpl<TimedInput<I>>) row;

            // Delete from LP rows:
            this.longPrefixRowMap.remove(row.getLabel());

            // Add to SP rows:
            this.shortPrefixRowMap.put(row.getLabel(), lpRow);

            lpRow.makeShort(alphabet.size());

            this.initLocation(lpRow, oracle);
        }
        return this.findUnclosedTransitions();
    }

    @Override
    public Alphabet<TimedInput<I>> getInputAlphabet() {
        return this.alphabet;
    }

    @Override
    public Collection<Row<TimedInput<I>>> getShortPrefixRows() {
        return Collections.unmodifiableCollection(this.shortPrefixRowMap.values());
    }

    @Override
    public Collection<Row<TimedInput<I>>> getLongPrefixRows() {
        return Collections.unmodifiableCollection(this.longPrefixRowMap.values());
    }

    @Override
    public Row<TimedInput<I>> getRow(int idx) {
        throw new UnsupportedOperationException("Not supported. Use prefix to access rows instead.");
    }

    @Override
    public @Nullable Row<TimedInput<I>> getRow(Word<TimedInput<I>> prefix) {
        if (this.shortPrefixRowMap.containsKey(prefix)) {
            return this.shortPrefixRowMap.get(prefix);
        }
        if (this.longPrefixRowMap.containsKey(prefix)) {
            return this.longPrefixRowMap.get(prefix);
        }
        return null;
    }

    @Override
    public int numberOfDistinctRows() {
        return this.rowContentMap.size();
    }

    @Override
    public List<Word<TimedInput<I>>> getSuffixes() {
        return this.suffixes;
    }

    @Override
    public List<Word<TimedOutput<O>>> rowContents(Row<TimedInput<I>> row) {
        final List<Word<TimedOutput<O>>> content = this.rowContentMap.get(row.getRowContentId());
        if (content == null) {
            // OT may be empty if only single location with timers:
            assert this.suffixes.isEmpty();
            return Collections.emptyList();
        } else {
            return content;
        }
    }

    @Override
    public Word<TimedInput<I>> transformAccessSequence(Word<TimedInput<I>> word) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Nullable TimerInfo<?, O> getTimerInfo(Word<TimedInput<I>> prefix, long initial) {
        LocationTimerInfo<I, O> info = this.timerInfoMap.get(prefix);
        if (info != null) {
            return info.getTimerInfo(initial);
        }
        return null;
    }

    @Nullable LocationTimerInfo<I, O> getLocationTimerInfo(Row<TimedInput<I>> sp) {
        return this.timerInfoMap.get(sp.getLabel());
    }

    /**
     * Adds an outgoing transition for the given symbol to the given location and subsequently tests for unclosed
     * transitions.
     * <p>
     * Raises an error if this transition already exists.
     *
     * @param spRow
     *         the source location
     * @param symbol
     *         the input symbol
     * @param timeOracle
     *         the oracle
     *
     * @return List of unclosed rows. Empty, if none.
     */
    List<List<Row<TimedInput<I>>>> addOutgoingTransition(Row<TimedInput<I>> spRow,
                                                         TimedInput<I> symbol,
                                                         TimedQueryOracle<I, O> timeOracle) {
        if (!this.alphabet.containsSymbol(symbol)) {
            throw new IllegalArgumentException("Unknown symbol.");
        }

        Word<TimedInput<I>> transitionPrefix = spRow.getLabel().append(symbol);

        // Add long-prefix row:
        assert this.getRow(transitionPrefix) == null :
                "Location already has an outgoing transition for the provided symbol";

        RowImpl<TimedInput<I>> succRow = this.createLpRow(transitionPrefix);

        // Set as successor:
        int symIdx = this.alphabet.getSymbolIndex(symbol);
        ((RowImpl<TimedInput<I>>) spRow).setSuccessor(symIdx, succRow);

        // Update suffixes:
        this.queryAllSuffixes(Collections.singleton(succRow), timeOracle);

        return this.findUnclosedTransitions();
    }

    List<List<Row<TimedInput<I>>>> addTimerTransition(Row<TimedInput<I>> spRow,
                                                      TimerInfo<?, O> timeout,
                                                      TimedQueryOracle<I, O> timeOracle) {
        return this.addOutgoingTransition(spRow, new TimeStepSequence<>(timeout.initial()), timeOracle);
    }

    /**
     * Removes a long prefix row. Should only be used when removing a transition of a former one-shot timer. When
     * turning a long into a short prefix, use toShortPrefix instead,
     *
     * @param prefix
     *         the row prefix
     */
    void removeLpRow(Word<TimedInput<I>> prefix) {
        assert this.longPrefixRowMap.containsKey(prefix) : "Attempting to remove lp row that does not exist.";

        // Remove lp row:
        this.longPrefixRowMap.remove(prefix);

        // Unset as successor:
        int symIdx = this.alphabet.getSymbolIndex(prefix.lastSymbol());
        RowImpl<TimedInput<I>> spRow = this.shortPrefixRowMap.get(prefix.prefix(-1));
        assert spRow != null;

        spRow.setSuccessor(symIdx, null);
    }
}
