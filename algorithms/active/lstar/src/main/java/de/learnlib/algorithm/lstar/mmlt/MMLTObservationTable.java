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
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.learnlib.datastructure.observationtable.ObservationTable;
import de.learnlib.datastructure.observationtable.Row;
import de.learnlib.datastructure.observationtable.RowImpl;
import de.learnlib.filter.FilterResponse;
import de.learnlib.filter.MutableSymbolFilter;
import de.learnlib.oracle.TimedQueryOracle;
import de.learnlib.oracle.TimedQueryOracle.TimerQueryResult;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.mmlt.TimerInfo;
import net.automatalib.symbol.time.InputSymbol;
import net.automatalib.symbol.time.TimeStepSequence;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.symbol.time.TimedOutput;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The observation table used by the MMLT learner.
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
public class MMLTObservationTable<I, O> implements ObservationTable<TimedInput<I>, Word<TimedOutput<O>>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MMLTObservationTable.class);
    private static final int NO_CONTENT = -1;

    private final MutableSymbolFilter<TimedInput<I>, InputSymbol<I>> symbolFilter;

    private final Map<Word<TimedInput<I>>, LocationTimerInfo<I, O>> timerInfoMap; // prefix -> timer info

    private final Map<Word<TimedInput<I>>, RowImpl<TimedInput<I>>> shortPrefixRowMap; // label -> row info
    private final Map<Word<TimedInput<I>>, RowImpl<TimedInput<I>>> longPrefixRowMap; // label -> row info

    private final List<RowImpl<TimedInput<I>>> sortedShortPrefixes;
    // values of shortPrefixRowMap sorted by label, for faster access.
    private final List<RowImpl<TimedInput<I>>> longPrefixList; // values of longPrefixRowMap as list, for faster access.

    private final Map<Integer, RowContent<O>> rowContentMap; // contentID -> row content

    private final List<Word<TimedInput<I>>> suffixes = new ArrayList<>();
    private final Set<Word<TimedInput<I>>> suffixSet = new HashSet<>();

    private final Alphabet<TimedInput<I>> alphabet;
    private final long minTimerQueryWaitTime;
    private final TimedOutput<O> silentOutput; // used for symbol filtering

    public MMLTObservationTable(Alphabet<TimedInput<I>> alphabet,
                                long minTimerQueryWaitTime,
                                MutableSymbolFilter<TimedInput<I>, InputSymbol<I>> symbolFilter,
                                O silentOutput) {
        this.alphabet = alphabet;

        this.symbolFilter = symbolFilter;
        this.silentOutput = new TimedOutput<>(silentOutput);
        this.minTimerQueryWaitTime = minTimerQueryWaitTime;

        this.timerInfoMap = new HashMap<>();

        this.shortPrefixRowMap = new HashMap<>();
        this.sortedShortPrefixes = new ArrayList<>();

        this.longPrefixRowMap = new HashMap<>();
        this.longPrefixList = new ArrayList<>();

        this.rowContentMap = new HashMap<>();
    }

    /**
     * Infers local timers for the provided location.
     *
     * @param location
     *         source location
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
     *         new alphabet symbol
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
     * @return corresponding row in the observation table
     */
    private RowImpl<TimedInput<I>> addInitialLocation() {
        RowImpl<TimedInput<I>> newRow = new RowImpl<>(Word.epsilon(), 0, alphabet.size());
        newRow.makeShort(alphabet.size());
        this.shortPrefixRowMap.put(Word.epsilon(), newRow);
        this.sortedShortPrefixes.add(newRow);
        this.sortedShortPrefixes.sort(Comparator.comparing(r -> r.getLabel().toString()));

        return newRow;
    }

    /**
     * Adds a new location that belongs to the provided short-prefix row. Infers timers for this location and creates
     * outgoing transitions.
     *
     * @param newRow
     *         newly-added short prefix row
     * @param timeOracle
     *         time oracle
     */
    private void initLocation(RowImpl<TimedInput<I>> newRow, TimedQueryOracle<I, O> timeOracle) {
        LocationTimerInfo<I, O> timerInfo = new LocationTimerInfo<>(newRow.getLabel());
        this.identifyLocalTimers(timerInfo, timeOracle);

        if (timerInfo.getLastTimer() != null) { // location has timer
            this.timerInfoMap.put(newRow.getLabel(), timerInfo);
        }

        // Add outgoing transitions:
        for (RowImpl<TimedInput<I>> t : this.createOutgoingTransitions(newRow, timeOracle)) {
            this.queryAllSuffixes(t, timeOracle);
        }
    }

    /**
     * Creates transitions for the provided short-prefix row. Adds transitions for non-delaying inputs and a transition
     * for the one-shot timer of the location, if present.
     * <p>
     * If a symbol filter is provided, the filter is queried before adding a transition for a non-delaying input. If the
     * filter considers the input a silent self-loop, no transition is explicitly created for the input.
     *
     * @param spRow
     *         short prefix row
     * @param timeOracle
     *         time query oracle
     *
     * @return new transitions
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

                spRow.setSuccessor(i, succRow);
                if (succRow != null) {
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
        this.longPrefixList.add(newRow);
        assert this.longPrefixList.size() == this.longPrefixRowMap.size();

        newRow.setLpIndex(0); // unused

        return newRow;
    }

    /**
     * Identify transitions that have not been closed, i.e., there is no state with the same suffix behavior. Also
     * removes unused content ids.
     *
     * @return the list of unclosed transition, in a deterministic order
     */
    public List<List<Row<TimedInput<I>>>> findUnclosedTransitions() {
        // Identify contentIds for locations:
        Set<Integer> spContentIds =
                this.shortPrefixRowMap.values().stream().map(RowImpl::getRowContentId).collect(Collectors.toSet());

        // Group lp rows by their content id:
        Map<Integer, List<Row<TimedInput<I>>>> lpContentMap = new HashMap<>();
        for (RowImpl<TimedInput<I>> lpRow : this.longPrefixRowMap.values()) {
            lpContentMap.putIfAbsent(lpRow.getRowContentId(), new ArrayList<>());
            lpContentMap.get(lpRow.getRowContentId()).add(lpRow);
        }

        // Identify ids that are not used by any SP:
        List<List<Row<TimedInput<I>>>> unclosedRows = new ArrayList<>();
        List<Integer> sortedLpIds = lpContentMap.keySet().stream().sorted().toList();
        for (Integer lpId : sortedLpIds) {
            if (spContentIds.contains(lpId)) {
                continue;
            }

            // Sort row s.t. list order deterministic:
            List<Row<TimedInput<I>>> unclosedWithId = lpContentMap.get(lpId);
            unclosedWithId.sort(Comparator.comparing(r -> r.getLabel().toString()));
            unclosedRows.add(unclosedWithId);
        }

        // Remove unused content ids:
        Set<Integer> usedContentIds =
                Stream.concat(this.shortPrefixRowMap.values().stream(), this.longPrefixRowMap.values().stream())
                      .map(RowImpl::getRowContentId)
                      .collect(Collectors.toSet());

        List<Integer> oldContentIds = this.rowContentMap.keySet().stream().toList();
        for (int oldId : oldContentIds) {
            if (!usedContentIds.contains(oldId)) {
                this.rowContentMap.remove(oldId);
            }
        }

        return unclosedRows;
    }

    public List<List<Row<TimedInput<I>>>> initialize(List<Word<TimedInput<I>>> initialShortPrefixes,
                                                     List<Word<TimedInput<I>>> initialSuffixes,
                                                     TimedQueryOracle<I, O> oracle) {

        if (isInitialized()) {
            throw new IllegalStateException("Called initialize, but there are already rows present");
        }
        if (!initialShortPrefixes.isEmpty()) {
            throw new IllegalArgumentException("Init with short prefixes is not supported.");
        }

        // Add initial suffixes:
        for (Word<TimedInput<I>> suffix : initialSuffixes) {
            if (suffixSet.add(suffix)) {
                suffixes.add(suffix);
            }
        }

        // 1. Create initial location:
        RowImpl<TimedInput<I>> newLoc = this.addInitialLocation();
        this.initLocation(newLoc, oracle);
        this.queryAllSuffixes(newLoc, oracle);

        // 2. Identify unclosed transitions:
        return this.findUnclosedTransitions();
    }

    private void queryAllSuffixes(RowImpl<TimedInput<I>> row, TimedQueryOracle<I, O> timedOracle) {
        Word<TimedInput<I>> prefix = row.getLabel();

        List<Word<TimedOutput<O>>> suffixOutputs = new ArrayList<>(this.suffixes.size());
        for (Word<TimedInput<I>> suffix : this.suffixes) {
            Word<TimedOutput<O>> output = timedOracle.answerQuery(prefix, suffix);
            suffixOutputs.add(output);
        }

        this.processSuffixOutputs(row, suffixOutputs);
    }

    private void processSuffixOutputs(RowImpl<TimedInput<I>> row, List<Word<TimedOutput<O>>> rowContents) {
        if (rowContents.isEmpty()) {
            row.setRowContentId(NO_CONTENT);
            return;
        }

        RowContent<O> content = new RowContent<>(rowContents);
        int contentId = content.hashCode();
        this.rowContentMap.putIfAbsent(contentId, content);
        row.setRowContentId(contentId);
    }

    private boolean isInitialized() {
        return !(shortPrefixRowMap.isEmpty() && longPrefixRowMap.isEmpty());
    }

    public List<List<Row<TimedInput<I>>>> addSuffixes(Collection<? extends Word<TimedInput<I>>> newSuffixes,
                                                      TimedQueryOracle<I, O> oracle) {
        // 1. Extend current suffixes + identify new suffixes:
        List<Word<TimedInput<I>>> newSuffixList = new ArrayList<>();
        for (Word<TimedInput<I>> suffix : newSuffixes) {
            if (this.suffixSet.add(suffix)) {
                LOGGER.debug("Adding new suffix '{}'", suffix);

                newSuffixList.add(suffix);
                this.suffixes.add(suffix);
            }
        }
        if (newSuffixList.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. Update row content:
        Stream.concat(shortPrefixRowMap.values().stream(), longPrefixRowMap.values().stream()).forEach(row -> {
            List<Word<TimedOutput<O>>> updatedOutputs = new ArrayList<>();
            if (row.getRowContentId() != NO_CONTENT) {
                // Add existing suffix outputs:
                updatedOutputs.addAll(this.rowContentMap.get(row.getRowContentId()).outputs());
            }

            for (Word<TimedInput<I>> suffix : newSuffixList) {
                Word<TimedOutput<O>> output = oracle.answerQuery(row.getLabel(), suffix);
                updatedOutputs.add(output);
            }

            this.processSuffixOutputs(row, updatedOutputs);
        });

        return this.findUnclosedTransitions();
    }

    public List<List<Row<TimedInput<I>>>> toShortPrefixes(List<Row<TimedInput<I>>> lpRows,
                                                          TimedQueryOracle<I, O> oracle) {
        for (Row<TimedInput<I>> row : lpRows) {
            LOGGER.debug("Adding new location with prefix '{}'", row.getLabel());

            final RowImpl<TimedInput<I>> lpRow = (RowImpl<TimedInput<I>>) row;

            // Delete from LP rows:
            RowImpl<TimedInput<I>> removed = this.longPrefixRowMap.remove(row.getLabel());
            this.longPrefixList.remove(removed);
            assert this.longPrefixList.size() == this.longPrefixRowMap.size();

            // Add to SP rows:
            this.shortPrefixRowMap.put(row.getLabel(), lpRow);
            this.sortedShortPrefixes.add(lpRow);
            this.sortedShortPrefixes.sort(Comparator.comparing(r -> r.getLabel().toString()));

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
        assert this.sortedShortPrefixes.size() == this.shortPrefixRowMap.size();
        return Collections.unmodifiableList(this.sortedShortPrefixes);
    }

    @Override
    public Collection<Row<TimedInput<I>>> getLongPrefixRows() {
        return Collections.unmodifiableList(this.longPrefixList);
    }

    @Override
    public Row<TimedInput<I>> getRow(int idx) {
        throw new IllegalStateException("Not supported. Use prefix to access rows instead.");
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
        if (this.rowContentMap.isEmpty()) {
            // OT may be empty if only single location with timers:
            if (!this.suffixes.isEmpty()) {
                throw new AssertionError();
            }
            return Collections.emptyList();
        }

        return this.rowContentMap.get(row.getRowContentId()).outputs();
    }

    @Override
    public Word<TimedInput<I>> transformAccessSequence(Word<TimedInput<I>> word) {
        throw new IllegalStateException("Not implemented.");
    }

    @Override
    public boolean isAccessSequence(Word<TimedInput<I>> word) {
        throw new IllegalStateException("Not implemented.");
    }

    public @Nullable TimerInfo<?, O> getTimerInfo(Word<TimedInput<I>> prefix, long initial) {
        LocationTimerInfo<I, O> info = this.timerInfoMap.get(prefix);
        if (info != null) {
            return info.getTimerInfo(initial);
        }
        return null;
    }

    public @Nullable LocationTimerInfo<I, O> getLocationTimerInfo(Row<TimedInput<I>> sp) {
        return this.timerInfoMap.getOrDefault(sp.getLabel(), null);
    }

    /**
     * Adds an outgoing transition for the given symbol to the given location and subsequently tests for unclosed
     * transitions.
     * <p>
     * Raises an error if this transition already exists.
     *
     * @param spRow
     *         Source location
     * @param symbol
     *         Input symbol
     * @param timeOracle
     *         Oracle
     *
     * @return List of unclosed rows. Empty, if none.
     */
    public List<List<Row<TimedInput<I>>>> addOutgoingTransition(Row<TimedInput<I>> spRow,
                                                                TimedInput<I> symbol,
                                                                TimedQueryOracle<I, O> timeOracle) {
        if (!this.alphabet.containsSymbol(symbol)) {
            throw new IllegalArgumentException("Unknown symbol.");
        }

        Word<TimedInput<I>> transitionPrefix = spRow.getLabel().append(symbol);

        // Add long-prefix row:
        if (this.getRow(transitionPrefix) != null) {
            throw new AssertionError("Location already has an outgoing transition for the provided symbol");
        }

        RowImpl<TimedInput<I>> succRow = this.createLpRow(transitionPrefix);

        // Set as successor:
        int symIdx = this.alphabet.getSymbolIndex(symbol);
        ((RowImpl<TimedInput<I>>) spRow).setSuccessor(symIdx, succRow);

        // Update suffixes:
        this.queryAllSuffixes(succRow, timeOracle);

        return this.findUnclosedTransitions();
    }

    public List<List<Row<TimedInput<I>>>> addTimerTransition(Row<TimedInput<I>> spRow,
                                                             TimerInfo<?, O> timeout,
                                                             TimedQueryOracle<I, O> timeOracle) {
        return this.addOutgoingTransition(spRow, new TimeStepSequence<>(timeout.initial()), timeOracle);
    }

    /**
     * Removes a long prefix row. Should only be used when removing a transition of a former one-shot timer. When
     * turning a long into a short prefix, use toShortPrefix instead,
     *
     * @param prefix
     *         Row prefix
     */
    public void removeLpRow(Word<TimedInput<I>> prefix) {
        if (!this.longPrefixRowMap.containsKey(prefix)) {
            throw new IllegalArgumentException("Attempting to remove lp row that does not exist.");
        }

        // Remove lp row:
        RowImpl<TimedInput<I>> removed = this.longPrefixRowMap.remove(prefix);
        this.longPrefixList.remove(removed);
        assert this.longPrefixList.size() == this.longPrefixRowMap.size();

        // Unset as successor:
        int symIdx = this.alphabet.getSymbolIndex(prefix.lastSymbol());
        RowImpl<TimedInput<I>> spRow = this.shortPrefixRowMap.get(prefix.prefix(-1));
        assert spRow != null;

        spRow.setSuccessor(symIdx, null);
    }

    // =============================

    private record RowContent<O>(List<Word<TimedOutput<O>>> outputs) {}

}
