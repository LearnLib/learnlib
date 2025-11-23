package de.learnlib.algorithm.lstar.mmlt;

import de.learnlib.datastructure.observationtable.MutableObservationTable;
import de.learnlib.datastructure.observationtable.Row;
import de.learnlib.datastructure.observationtable.RowImpl;
import de.learnlib.filter.MutableSymbolFilter;
import de.learnlib.oracle.TimedQueryOracle;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.filter.FilterResponse;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.mmlt.TimerInfo;
import net.automatalib.symbol.time.TimedOutput;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.symbol.time.InputSymbol;
import net.automatalib.symbol.time.TimeStepSequence;
import net.automatalib.symbol.time.TimeoutSymbol;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The observation table used by the MMLT learner.
 * <p>
 * Unlike an OT for standard Mealy learning, includes prefixes for the timeout transitions of one-shot timers.
 * Intended to be used with a symbol filter. The filter is queried before adding a new transition for a non-delaying input.
 * If the filter considers the transition to be a silent self-loop, the output of the transition is first verified.
 * If it is actually silent the learner considers the transition to be a silent self-loop. Consequently,
 * it does not add a transition for it. Transitions may be added later if an input was falsely ignored.
 * <p>
 * Assumes that all short prefixes lead to different locations (-> no need to make canonical)
 *
 * @param <I> Input type for non-delaying inputs
 * @param <O> Output symbol type
 */
public class MMLTObservationTable<I, O> implements MutableObservationTable<TimedInput<I>, Word<TimedOutput<O>>> {

    private static final Logger logger = LoggerFactory.getLogger(MMLTObservationTable.class);

    private final MutableSymbolFilter<TimedInput<I>, InputSymbol<I>> symbolFilter;

    private final Map<Word<TimedInput<I>>, LocationTimerInfo<I, O>> timerInfoMap; // prefix -> timer info

    private final Map<Word<TimedInput<I>>, RowImpl<TimedInput<I>>> shortPrefixRowMap; // label -> row info
    private final Map<Word<TimedInput<I>>, RowImpl<TimedInput<I>>> longPrefixRowMap; // label -> row info

    private final List<RowImpl<TimedInput<I>>> sortedShortPrefixes; // values of shortPrefixRowMap sorted by label, for faster access.
    private final List<RowImpl<TimedInput<I>>> longPrefixList; // values of longPrefixRowMap as list, for faster access.

    private final Map<Integer, RowContent<O>> rowContentMap; // contentID -> row content
    private static final int NO_CONTENT = -1;

    private final List<Word<TimedInput<I>>> suffixes = new ArrayList<>();
    private final Set<Word<TimedInput<I>>> suffixSet = new HashSet<>();

    private final Alphabet<TimedInput<I>> alphabet;
    private final long minTimerQueryWaitTime;
    private final TimedOutput<O> silentOutput; // used for symbol filtering

    public MMLTObservationTable(Alphabet<TimedInput<I>> alphabet, long minTimerQueryWaitTime,
                                @NonNull MutableSymbolFilter<TimedInput<I>, InputSymbol<I>> symbolFilter, O silentOutput) {
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
     * @param location Source location.
     */
    private void identifyLocalTimers(LocationTimerInfo<I, O> location, TimedQueryOracle<I, O> timeOracle) {
        var timerQueryResponse = timeOracle.queryTimers(location.getPrefix(), this.minTimerQueryWaitTime);
        var timers = timerQueryResponse.timers();

        if (timerQueryResponse.aborted()) {
            var end = ExtensibleLStarMMLT.selectOneShotTimer(timers, Long.MAX_VALUE);
            timers.set(end, timers.get(end).asOneShot());
        }

        // Add timers up to one-shot:
        for (var timer : timerQueryResponse.timers()) {
            location.addTimer(timer);
            this.extendAlphabet(new TimeStepSequence<>(timer.initial()));
            if (!timer.periodic()) {
                break;
            }
        }
    }

    /**
     * Extends the global alphabet without adding new transitions.
     *
     * @param symbol New alphabet symbol
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
     * @return Corresponding row in the OT
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
     * Adds a new location that belongs to the provided short-prefix row.
     * Infers timers for this location and creates outgoing transitions.
     *
     * @param newRow     Newly-added short prefix row
     * @param timeOracle Time oracle
     */
    private void initLocation(RowImpl<TimedInput<I>> newRow, TimedQueryOracle<I, O> timeOracle) {
        LocationTimerInfo<I, O> timerInfo = new LocationTimerInfo<>(newRow.getLabel());
        this.identifyLocalTimers(timerInfo, timeOracle);

        if (timerInfo.getLastTimer() != null) { // location has timer
            this.timerInfoMap.put(newRow.getLabel(), timerInfo);
        }

        // Add outgoing transitions:
        List<RowImpl<TimedInput<I>>> transitions = this.createOutgoingTransitions(newRow, timeOracle);
        transitions.forEach(t -> this.queryAllSuffixes(t, timeOracle));
    }

    /**
     * Creates transitions for the provided short-prefix row. Adds transitions for non-delaying inputs
     * and a transition for the one-shot timer of the location, if present.
     * <p>
     * If a symbol filter is provided, the filter is queried before adding a transition for a non-delaying input.
     * If the filter considers the input a silent self-loop, no transition is explicitly created for the input.
     *
     * @param spRow      Short prefix row
     * @param timeOracle Time query oracle
     * @return New transitions
     */
    private List<RowImpl<TimedInput<I>>> createOutgoingTransitions(RowImpl<TimedInput<I>> spRow, TimedQueryOracle<I, O> timeOracle) {
        List<RowImpl<TimedInput<I>>> transitions = new ArrayList<>();

        Word<TimedInput<I>> sp = spRow.getLabel();

        // First, add transitions for non-delaying symbols:
        for (int i = 0; i < alphabet.size(); i++) {
            TimedInput<I> sym = alphabet.getSymbol(i);
            if (sym instanceof TimeStepSequence<I> || sym instanceof TimeoutSymbol<I>) {
                continue;
            }

            Word<TimedInput<I>> lp = sp.append(sym);
            assert !this.shortPrefixRowMap.containsKey(lp);

            RowImpl<TimedInput<I>> succRow = this.longPrefixRowMap.get(lp);
            if (succRow == null) {
                // Query symbol filter before adding transition:
                var filterResponse = this.symbolFilter.query(sp, (InputSymbol<I>) sym);
                if (filterResponse == FilterResponse.IGNORE) {
                    // Verify that output is silent:
                    var response = timeOracle.answerQuery(sp, Word.fromLetter(sym));
                    assert response.size() == 1;
                    if (!response.firstSymbol().equals(silentOutput)) {
                        // Not silent -> cannot be silent self-loop:
                        filterResponse = FilterResponse.ACCEPT;

                        // Update filter:
                        this.symbolFilter.accept(sp, (InputSymbol<I>) sym);
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

        // Second, add one-shot timer transition (if any):
        var locTimers = timerInfoMap.getOrDefault(spRow.getLabel(), null);
        if (locTimers != null && !locTimers.getLastTimer().periodic()) {
            TimedInput<I> waitSym = new TimeStepSequence<>(locTimers.getLastTimer().initial());
            Word<TimedInput<I>> lp = sp.append(waitSym);
            assert !this.shortPrefixRowMap.containsKey(lp);

            RowImpl<TimedInput<I>> succRow = this.longPrefixRowMap.get(lp);
            if (succRow == null) {
                succRow = this.createLpRow(lp);
            }
            spRow.setSuccessor(this.alphabet.getSymbolIndex(waitSym), succRow);
            transitions.add(succRow);
        }

        return transitions;
    }

    private RowImpl<TimedInput<I>> createLpRow(Word<TimedInput<I>> prefix) {
        RowImpl<TimedInput<I>> newRow = new RowImpl<>(prefix, 0);
        this.longPrefixRowMap.put(prefix, newRow);
        this.longPrefixList.add(newRow);
        if (this.longPrefixList.size() != this.longPrefixRowMap.size()) throw new AssertionError();

        newRow.setLpIndex(0); // unused

        return newRow;
    }

    /**
     * Identify transitions that have not been closed.
     * I.e., there is no state with the same suffix behavior.
     * Also removes unused content ids.
     * <p>
     * Guarantees that returned transition list order is deterministic.
     */
    public List<List<Row<TimedInput<I>>>> findUnclosedTransitions() {
        // Identify contentIds for locations:
        Set<Integer> spContentIds = this.shortPrefixRowMap.values().stream()
                .map(RowImpl::getRowContentId)
                .collect(Collectors.toSet());

        // Group lp rows by their content id:
        Map<Integer, List<Row<TimedInput<I>>>> lpContentMap = new HashMap<>();
        for (var lpRow : this.longPrefixRowMap.values()) {
            lpContentMap.putIfAbsent(lpRow.getRowContentId(), new ArrayList<>());
            lpContentMap.get(lpRow.getRowContentId()).add(lpRow);
        }

        // Identify ids that are not used by any SP:
        List<List<Row<TimedInput<I>>>> unclosedRows = new ArrayList<>();
        List<Integer> sortedLpIds = lpContentMap.keySet().stream().sorted().toList();
        for (var lpId : sortedLpIds) {
            if (spContentIds.contains(lpId)) {
                continue;
            }

            // Sort row s.t. list order deterministic:
            List<Row<TimedInput<I>>> unclosedWithId = lpContentMap.get(lpId);
            unclosedWithId.sort(Comparator.comparing(r -> r.getLabel().toString()));
            unclosedRows.add(unclosedWithId);
        }

        // Remove unused content ids:
        Set<Integer> usedContentIds = Stream.concat(this.shortPrefixRowMap.values().stream(), this.longPrefixRowMap.values().stream())
                .map(RowImpl::getRowContentId).collect(Collectors.toSet());

        List<Integer> oldContentIds = this.rowContentMap.keySet().stream().toList();
        for (int oldId : oldContentIds) {
            if (!usedContentIds.contains(oldId)) {
                this.rowContentMap.remove(oldId);
            }
        }


        return unclosedRows;
    }

    @Override
    public List<List<Row<TimedInput<I>>>> initialize(List<Word<TimedInput<I>>> initialShortPrefixes,
                                                     List<Word<TimedInput<I>>> initialSuffixes,
                                                     MembershipOracle<TimedInput<I>, Word<TimedOutput<O>>> oracle) {

        if (isInitialized()) {
            throw new IllegalStateException("Called initialize, but there are already rows present");
        }
        if (!initialShortPrefixes.isEmpty()) {
            throw new IllegalArgumentException("Init with short prefixes is not supported.");
        }
        if (!(oracle instanceof TimedQueryOracle<I, O> timedOracle)) {
            throw new IllegalArgumentException("Must use timed oracle!");
        }

        // Add initial suffixes:
        for (Word<TimedInput<I>> suffix : initialSuffixes) {
            if (suffixSet.add(suffix)) {
                suffixes.add(suffix);
            }
        }

        // 1. Create initial location:
        var newLoc = this.addInitialLocation();
        this.initLocation(newLoc, timedOracle);
        this.queryAllSuffixes(newLoc, timedOracle);

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

    @Override
    public boolean isInitialized() {
        return !(shortPrefixRowMap.isEmpty() && longPrefixRowMap.isEmpty());
    }

    @Override
    public boolean isInitialConsistencyCheckRequired() {
        return false;
    }

    @Override
    public List<List<Row<TimedInput<I>>>> addSuffixes(Collection<? extends Word<TimedInput<I>>> newSuffixes, MembershipOracle<TimedInput<I>, Word<TimedOutput<O>>> oracle) {
        if (!(oracle instanceof TimedQueryOracle<I, O> timedOracle)) {
            throw new IllegalArgumentException();
        }

        // 1. Extend current suffixes + identify new suffixes:
        List<Word<TimedInput<I>>> newSuffixList = new ArrayList<>();
        for (Word<TimedInput<I>> suffix : newSuffixes) {
            if (this.suffixSet.add(suffix)) {
                logger.debug(String.format("Adding new suffix '%s'", suffix));

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
                Word<TimedOutput<O>> output = timedOracle.answerQuery(row.getLabel(), suffix);
                updatedOutputs.add(output);
            }

            this.processSuffixOutputs(row, updatedOutputs);
        });

        return this.findUnclosedTransitions();
    }

    @Override
    public List<List<Row<TimedInput<I>>>> addShortPrefixes(List<? extends Word<TimedInput<I>>> shortPrefixes, MembershipOracle<TimedInput<I>, Word<TimedOutput<O>>> oracle) {
        throw new IllegalStateException("Not supported.");
    }

    @Override
    public List<List<Row<TimedInput<I>>>> toShortPrefixes(List<Row<TimedInput<I>>> lpRows, MembershipOracle<TimedInput<I>, Word<TimedOutput<O>>> oracle) {
        if (!(oracle instanceof TimedQueryOracle<I, O> timedOracle)) {
            throw new IllegalArgumentException();
        }

        for (Row<TimedInput<I>> row : lpRows) {
            logger.debug(String.format("Adding new location with prefix '%s'", row.getLabel()));

            final RowImpl<TimedInput<I>> lpRow = (RowImpl<TimedInput<I>>) row;

            // Delete from LP rows:
            var removed = this.longPrefixRowMap.remove(row.getLabel());
            this.longPrefixList.remove(removed);
            if (this.longPrefixList.size() != this.longPrefixRowMap.size()) throw new AssertionError();

            // Add to SP rows:
            this.shortPrefixRowMap.put(row.getLabel(), lpRow);
            this.sortedShortPrefixes.add(lpRow);
            this.sortedShortPrefixes.sort(Comparator.comparing(r -> r.getLabel().toString()));

            lpRow.makeShort(alphabet.size());

            this.initLocation(lpRow, timedOracle);
        }
        return this.findUnclosedTransitions();
    }

    @Override
    public List<List<Row<TimedInput<I>>>> addAlphabetSymbol(TimedInput<I> symbol, MembershipOracle<TimedInput<I>, Word<TimedOutput<O>>> oracle) {
        throw new IllegalStateException("Not supported.");
    }

    @Override
    public Alphabet<TimedInput<I>> getInputAlphabet() {
        return this.alphabet;
    }

    @Override
    public Collection<Row<TimedInput<I>>> getShortPrefixRows() {
        if (this.sortedShortPrefixes.size() != this.shortPrefixRowMap.size()) throw new AssertionError();
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
    @Nullable
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
        var info = this.timerInfoMap.get(prefix);
        if (info != null) {
            return info.getTimerInfo(initial);
        }
        return null;
    }

    @Nullable
    public LocationTimerInfo<I, O> getLocationTimerInfo(Row<TimedInput<I>> sp) {
        return this.timerInfoMap.getOrDefault(sp.getLabel(), null);
    }

    /**
     * Adds an outgoing transition for the given symbol to the given location
     * and subsequently tests for unclosed transitions.
     * <p>
     * Raises an error if this transition already exists.
     *
     * @param spRow      Source location
     * @param symbol     Input symbol
     * @param timeOracle Oracle
     * @return List of unclosed rows. Empty, if none.
     */
    public List<List<Row<TimedInput<I>>>> addOutgoingTransition(Row<TimedInput<I>> spRow, TimedInput<I> symbol, TimedQueryOracle<I, O> timeOracle) {
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

    public List<List<Row<TimedInput<I>>>> addTimerTransition(Row<TimedInput<I>> spRow, TimerInfo<?, O> timeout, TimedQueryOracle<I, O> timeOracle) {
        return this.addOutgoingTransition(spRow, new TimeStepSequence<>(timeout.initial()), timeOracle);
    }

    /**
     * Removes a long prefix row. Should only be used when removing a transition of a former one-shot timer.
     * When turning a long into a short prefix, use toShortPrefix instead,
     *
     * @param prefix Row prefix
     */
    public void removeLpRow(Word<TimedInput<I>> prefix) {
        if (!this.longPrefixRowMap.containsKey(prefix)) {
            throw new IllegalArgumentException("Attempting to remove lp row that does not exist.");
        }

        // Remove lp row:
        var removed = this.longPrefixRowMap.remove(prefix);
        this.longPrefixList.remove(removed);
        if (this.longPrefixList.size() != this.longPrefixRowMap.size()) throw new AssertionError();

        // Unset as successor:
        int symIdx = this.alphabet.getSymbolIndex(prefix.lastSymbol());
        RowImpl<TimedInput<I>> spRow = this.shortPrefixRowMap.get(prefix.prefix(-1));
        assert spRow != null;

        spRow.setSuccessor(symIdx, null);
    }

    // =============================

    private record RowContent<O>(List<Word<TimedOutput<O>>> outputs) {

        @Override
        public int hashCode() {
            return outputs.toString().hashCode();
        }
    }


}
