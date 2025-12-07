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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.learnlib.acex.AcexAnalyzer;
import de.learnlib.acex.AcexAnalyzers;
import de.learnlib.algorithm.lstar.closing.ClosingStrategies;
import de.learnlib.algorithm.lstar.closing.ClosingStrategy;
import de.learnlib.algorithm.lstar.mmlt.cex.MMLTCounterexampleHandler;
import de.learnlib.algorithm.lstar.mmlt.cex.MMLTOutputInconsistency;
import de.learnlib.algorithm.lstar.mmlt.cex.results.CexAnalysisResult;
import de.learnlib.algorithm.lstar.mmlt.cex.results.FalseIgnoreResult;
import de.learnlib.algorithm.lstar.mmlt.cex.results.MissingDiscriminatorResult;
import de.learnlib.algorithm.lstar.mmlt.cex.results.MissingOneShotResult;
import de.learnlib.algorithm.lstar.mmlt.cex.results.MissingResetResult;
import de.learnlib.datastructure.observationtable.OTLearner;
import de.learnlib.datastructure.observationtable.ObservationTable;
import de.learnlib.datastructure.observationtable.Row;
import de.learnlib.filter.RefutableSymbolFilter;
import de.learnlib.filter.symbol.AcceptAllSymbolFilter;
import de.learnlib.oracle.TimedQueryOracle;
import de.learnlib.query.DefaultQuery;
import de.learnlib.query.Query;
import de.learnlib.statistic.Statistics;
import de.learnlib.statistic.StatisticsCollector;
import de.learnlib.time.MMLTModelParams;
import de.learnlib.tooling.annotation.builder.GenerateBuilder;
import de.learnlib.util.mealy.MealyUtil;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.GrowingAlphabet;
import net.automatalib.alphabet.impl.GrowingMapAlphabet;
import net.automatalib.automaton.DeterministicAutomaton.FullIntAbstraction;
import net.automatalib.automaton.mmlt.MMLT;
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
 * An L*-based leaner for inferring {@link MMLT}s.
 *
 * @param <I>
 *         input symbol type (of non-delaying inputs)
 * @param <O>
 *         output symbol type
 */
public class ExtensibleLStarMMLT<I, O>
        implements OTLearner<MMLT<Integer, I, ?, O>, TimedInput<I>, Word<TimedOutput<O>>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExtensibleLStarMMLT.class);
    private final StatisticsCollector stats;

    private final ClosingStrategy<? super TimedInput<I>, ? super Word<TimedOutput<O>>> closingStrategy;

    private final TimedQueryOracle<I, O> timeOracle;
    private final RefutableSymbolFilter<TimedInput<I>, InputSymbol<I>> symbolFilter;

    private final MMLTHypDataContainer<I, O> hypData;

    // ============================

    private final List<Word<TimedInput<I>>> initialSuffixes;
    private final MMLTCounterexampleHandler<I, O> cexAnalyzer;

    /**
     * Instantiates a new learner.
     * <p>
     * This is a convenience constructor for
     * {@link #ExtensibleLStarMMLT(Alphabet, MMLTModelParams, TimedQueryOracle, List, ClosingStrategy,
     * RefutableSymbolFilter, AcexAnalyzer)} which uses
     * <ul>
     *     <li>{@link Collections#emptyList()} for {@code initialSuffixes},</li>
     *     <li>{@link ClosingStrategies#CLOSE_SHORTEST} for {@code closingStrategy},</li>
     *     <li>{@link AcceptAllSymbolFilter} for {@code symbolFilter}, and</li>
     *     <li>{@link AcexAnalyzers#BINARY_SEARCH_BWD} for {@code analyzer}.</li>
     * </ul>
     *
     * @param alphabet
     *         the alphabet (of non-delaying inputs)
     * @param modelParams
     *         the model parameters
     * @param timeOracle
     *         the query oracle for MMLTs
     */
    public ExtensibleLStarMMLT(Alphabet<I> alphabet,
                               MMLTModelParams<O> modelParams,
                               TimedQueryOracle<I, O> timeOracle) {
        this(alphabet,
             modelParams,
             timeOracle,
             Collections.emptyList(),
             ClosingStrategies.CLOSE_SHORTEST,
             new AcceptAllSymbolFilter<>(),
             AcexAnalyzers.BINARY_SEARCH_BWD);
    }

    /**
     * Instantiates a new learner.
     *
     * @param alphabet
     *         the alphabet (of non-delaying inputs)
     * @param modelParams
     *         the model parameters
     * @param timeOracle
     *         the query oracle for MMLTs
     * @param initialSuffixes
     *         the initial set of suffixes (may be empty)
     * @param closingStrategy
     *         the closing strategy for the observation table.
     * @param symbolFilter
     *         the symbol filter
     * @param analyzer
     *         the strategy for decomposing counterexamples.
     */
    @GenerateBuilder(defaults = BuilderDefaults.class)
    public ExtensibleLStarMMLT(Alphabet<I> alphabet,
                               MMLTModelParams<O> modelParams,
                               TimedQueryOracle<I, O> timeOracle,
                               List<Word<TimedInput<I>>> initialSuffixes,
                               ClosingStrategy<? super TimedInput<I>, ? super Word<TimedOutput<O>>> closingStrategy,
                               RefutableSymbolFilter<TimedInput<I>, InputSymbol<I>> symbolFilter,
                               AcexAnalyzer analyzer) {
        this.closingStrategy = closingStrategy;
        this.timeOracle = timeOracle;
        this.initialSuffixes = initialSuffixes;
        this.stats = Statistics.getCollector();

        // Prepare hyp data:

        // Internally, the learner also stores TimeStepSequences in its alphabet:
        GrowingAlphabet<TimedInput<I>> internalAlphabet = new GrowingMapAlphabet<>();
        alphabet.forEach(s -> internalAlphabet.add(TimedInput.input(s)));

        // Init hypothesis data:
        this.hypData = new MMLTHypDataContainer<>(internalAlphabet,
                                                  modelParams,
                                                  new MMLTObservationTable<>(internalAlphabet,
                                                                             modelParams.maxTimerQueryWaitingTime(),
                                                                             symbolFilter,
                                                                             modelParams.silentOutput()));

        this.cexAnalyzer = new MMLTCounterexampleHandler<>(timeOracle, analyzer, symbolFilter);
        this.symbolFilter = symbolFilter;
    }

    /**
     * Heuristically chooses a new one-shot timer from the provided timers. Takes the timer with the highest initial
     * value that
     * <ul>
     *     <li>does not exceed {@code maxInitialValue} and</li>
     *     <li>has not timer with a lower initial value that times out at the same time.</li>
     * </ul>
     *
     * @param sortedTimers
     *         timers, sorted ascendingly by their initial value
     * @param maxInitialValue
     *         the maximum initial value to consider
     * @param <O>
     *         output type
     *
     * @return the index (in {@code sortedTimers}) of the new one-shot candidate
     */
    public static <O> int selectOneShotTimer(List<? extends TimerInfo<?, O>> sortedTimers, long maxInitialValue) {

        // Filter relevant timers:
        // Start at timer with the highest initial value.
        // Ignore all timers whose initial value exceeds the maximum value.
        // Also ignore timers whose timeout is the multiple of another timer's initial value.
        timers:
        for (int i = sortedTimers.size() - 1; i >= 0; i--) {
            TimerInfo<?, O> timer = sortedTimers.get(i);

            // could not have expired
            if (timer.initial() <= maxInitialValue) {

                // Ignore timers whose initial value is a multiple of another one.
                // When set to one-shot, these would expire at same time as periodic timer -> non-deterministic behavior!
                for (int j = 0; j < i; j++) {
                    TimerInfo<?, O> otherTimer = sortedTimers.get(j);
                    if (timer.initial() % otherTimer.initial() == 0) {
                        continue timers;
                    }
                }

                return i; // not a multiple and within time
            }
        }

        throw new IllegalStateException("Maximum initial value is too low; must include at least one timer.");
    }

    @Override
    public MMLT<Integer, I, ?, O> getHypothesisModel() {
        return getInternalHypothesisModel();
    }

    /**
     * Like {@link #getHypothesisModel()}, but returns an {@link MMLTHypothesis} object instead. This objects provides
     * additional functions that are just intended for the learner but not the teacher.
     *
     * @return the internal hypothesis
     */
    private MMLTHypothesis<I, O> getInternalHypothesisModel() {
        this.updateOutputs();
        return constructHypothesis(this.hypData);
    }

    private List<Row<TimedInput<I>>> selectClosingRows(List<List<Row<TimedInput<I>>>> unclosed) {
        return closingStrategy.selectClosingRows(unclosed, hypData.getTable(), timeOracle);
    }

    private void updateOutputs() {
        // Query output of newly-added transitions:
        MMLTObservationTable<I, O> table = this.hypData.getTable();
        List<OutputQuery<I, O>> queries = new ArrayList<>();

        for (Row<TimedInput<I>> row : IterableUtil.concat(table.getShortPrefixRows(), table.getLongPrefixRows())) {
            Word<TimedInput<I>> label = row.getLabel();

            if (label.isEmpty()) {
                continue; // initial state
            }

            if (this.hypData.getTransitionOutputMap().containsKey(label)) {
                continue; // already queried
            }

            Word<TimedInput<I>> prefix = label.prefix(-1);
            TimedInput<I> inputSym = label.lastSymbol();

            TimedOutput<O> output;
            if (inputSym instanceof TimeStepSequence<I> ws) {
                // Query timer output from table:
                TimerInfo<?, O> timerInfo = this.hypData.getTable().getTimerInfo(prefix, ws.timeSteps());
                assert timerInfo != null;
                O combinedOutput = this.hypData.getModelParams().outputCombiner().combineSymbols(timerInfo.outputs());
                output = new TimedOutput<>(combinedOutput);
                this.hypData.getTransitionOutputMap().put(label, output);
            } else {
                queries.add(new OutputQuery<>(label, prefix));
            }
        }

        if (!queries.isEmpty()) {
            timeOracle.processQueries(queries);

            for (OutputQuery<I, O> q : queries) {
                q.process(this.hypData.getTransitionOutputMap());
            }
        }
    }

    // ==========================

    @Override
    public void startLearning() {
        List<List<Row<TimedInput<I>>>> initialUnclosed =
                this.hypData.getTable().initialize(Collections.emptyList(), this.initialSuffixes, timeOracle);

        // Ensure that closed:
        this.completeConsistentTable(initialUnclosed);
    }

    @Override
    public boolean refineHypothesis(DefaultQuery<TimedInput<I>, Word<TimedOutput<O>>> ceQuery) {
        if (!refineHypothesisSingle(ceQuery)) {
            return false; // no valid CEX
        }
        while (refineHypothesisSingle(ceQuery)) {
            // analyze exhaustively
        }
        return true;
    }

    /**
     * Transforms the provided counterexample to an inconsistency object: First, checks if still a counterexample. If
     * so, cuts the cex after the first output deviation.
     *
     * @param ceQuery
     *         Counterexample
     * @param hypothesis
     *         Current hypothesis
     *
     * @return The resulting inconsistency, or null, if the counterexample is not a counterexample.
     */
    private @Nullable MMLTOutputInconsistency<I, O> toOutputInconsistency(DefaultQuery<TimedInput<I>, Word<TimedOutput<O>>> ceQuery,
                                                                          MMLTHypothesis<I, O> hypothesis) {
        // 1. Cut example after first deviation:
        DefaultQuery<TimedInput<I>, Word<TimedOutput<O>>> shortQuery =
                MealyUtil.shortenCounterExample(hypothesis.getSemantics(), ceQuery);
        if (shortQuery == null) {
            return null;
        }

        // 2. Calculate shortened hypothesis output:
        Word<TimedOutput<O>> shortHypOutput =
                hypothesis.getSemantics().computeSuffixOutput(shortQuery.getPrefix(), shortQuery.getSuffix());

        assert !shortHypOutput.equals(shortQuery.getOutput()) : "Deviation lost after shortening.";

        return new MMLTOutputInconsistency<>(shortQuery.getPrefix(),
                                             shortQuery.getSuffix(),
                                             shortQuery.getOutput(),
                                             shortHypOutput);
    }

    private boolean refineHypothesisSingle(DefaultQuery<TimedInput<I>, Word<TimedOutput<O>>> ceQuery) {
        // 1. Update hypothesis (may have changed since last refinement):
        MMLTHypothesis<I, O> hypothesis = this.getInternalHypothesisModel();

        // 2. Transform to output inconsistency:
        MMLTOutputInconsistency<I, O> outputIncons = this.toOutputInconsistency(ceQuery, hypothesis);
        if (outputIncons == null) {
            return false;
        }

        LOGGER.debug("Refining with inconsistency {}", outputIncons);

        // 3. Identify source of deviation:
        stats.startOrResumeClock("clk_cex_analysis", "Total cex analysis time");
        stats.increaseCounter("cnt_cex_analysis", "Cex analyses");
        CexAnalysisResult<I, O> analysisResult = this.cexAnalyzer.analyzeInconsistency(outputIncons, hypothesis);
        stats.pauseClock("clk_cex_analysis");

        // 4. Refine:
        if (analysisResult instanceof MissingDiscriminatorResult<I, O> locSplit) {
            stats.increaseCounter("INACC_MISSING_DISC", "Inaccuracies: missing discriminators");

            // Add new discriminator as suffix:
            assert !hypData.getTable().getSuffixes().contains(locSplit.getDiscriminator());
            List<Word<TimedInput<I>>> suffixes = Collections.singletonList(locSplit.getDiscriminator());
            List<List<Row<TimedInput<I>>>> unclosed = hypData.getTable().addSuffixes(suffixes, timeOracle);

            // Close transitions:
            this.completeConsistentTable(unclosed); // no consistency check for RS
        } else if (analysisResult instanceof MissingResetResult<I, O> noReset) {
            stats.increaseCounter("INACC_MISSING_RESETS", "Inaccuracies: missing resets");

            // Add missing reset:
            Word<TimedInput<I>> resetTrans = hypothesis.getPrefix(noReset.getLocation()).append(noReset.getInput());
            this.hypData.getTransitionResetSet().add(resetTrans);
        } else if (analysisResult instanceof MissingOneShotResult<I, O> noAperiodic) {
            stats.increaseCounter("INACC_MISSING_OS", "Inaccuracies: missing one-shot timers");

            // Identify corresponding sp row:
            Word<TimedInput<I>> locPrefix = hypothesis.getPrefix(noAperiodic.getLocation());
            Row<TimedInput<I>> spRow = hypData.getTable().getRow(locPrefix);

            assert spRow != null && spRow.isShortPrefixRow();

            this.handleMissingTimeoutChange(spRow, noAperiodic.getTimeout());
        } else if (analysisResult instanceof FalseIgnoreResult<I, O> falseIgnore) {
            stats.increaseCounter("INACC_MISSING_FI", "Inaccuracies: false ignores");

            // Identify corresponding sp row:
            Word<TimedInput<I>> locPrefix = hypothesis.getPrefix(falseIgnore.getLocation());
            Row<TimedInput<I>> spRow = hypData.getTable().getRow(locPrefix);

            assert spRow != null && spRow.isShortPrefixRow();

            // Update filter:
            this.symbolFilter.accept(locPrefix, falseIgnore.getSymbol());

            // Legalize symbol + close table:
            List<List<Row<TimedInput<I>>>> unclosed =
                    hypData.getTable().addOutgoingTransition(spRow, falseIgnore.getSymbol(), this.timeOracle);
            stats.increaseCounter("Count_legalized", "Legalized symbols");

            this.completeConsistentTable(unclosed);
        } else {
            throw new IllegalStateException("Unknown inconsistency type.");
        }

        return true;
    }

    private void handleMissingTimeoutChange(Row<TimedInput<I>> spRow, TimerInfo<?, O> timeout) {
        MMLTObservationTable<I, O> table = hypData.getTable();
        LocationTimerInfo<I, O> locationTimerInfo = table.getLocationTimerInfo(spRow);
        assert locationTimerInfo != null : "Location with missing one-shot timer must have timers.";

        // Only timer with highest initial value can be one-shot.
        // If location already has a one-shot timer, prefix of its timeout-transition might be core or fringe prefix.
        // If it is a fringe prefix, we need to remove it:
        TimerInfo<?, O> lastTimer = locationTimerInfo.getLastTimer();
        assert lastTimer != null;
        if (!lastTimer.periodic()) {
            Word<TimedInput<I>> lastTimerTransPrefix = spRow.getLabel().append(TimedInput.step(lastTimer.initial()));
            Row<TimedInput<I>> row = table.getRow(lastTimerTransPrefix);
            assert row != null;
            if (!row.isShortPrefixRow()) {
                // Last timer is one-shot + has fringe prefix:
                table.removeLpRow(lastTimerTransPrefix);
            }
        }

        // Prefix for timeout-transition of new one-shot timer:
        assert table.getRow(spRow.getLabel().append(TimedInput.step(timeout.initial()))) == null :
                "Timer already appears to be one-shot.";

        // Remove all timers with greater timeout (are now redundant):
        for (TimerInfo<?, O> t : new ArrayList<>(locationTimerInfo.getSortedTimers())) {
            if (t.initial() > timeout.initial()) {
                locationTimerInfo.removeTimer(t.name());
            }
        }

        // Change from periodic to one-shot:
        locationTimerInfo.setOneShotTimer(timeout.name());

        // Update fringe prefixes + close table:
        List<List<Row<TimedInput<I>>>> unclosed = table.addTimerTransition(spRow, timeout, this.timeOracle);
        this.completeConsistentTable(unclosed);
    }

    @Override
    public ObservationTable<TimedInput<I>, Word<TimedOutput<O>>> getObservationTable() {
        return this.hypData.getTable();
    }

    /**
     * Iteratively checks for unclosedness and inconsistencies in the table, and fixes any occurrences thereof. This
     * process is repeated until the observation table is both closed and consistent.
     * <p>
     * Simplified version for RS learner: assumes that OT is always consistent.
     *
     * @param unclosed
     *         the unclosed rows (equivalence classes) to start with.
     */
    private void completeConsistentTable(List<List<Row<TimedInput<I>>>> unclosed) {
        List<List<Row<TimedInput<I>>>> unclosedIter = unclosed;
        while (!unclosedIter.isEmpty()) {
            List<Row<TimedInput<I>>> closingRows = this.selectClosingRows(unclosedIter);

            // Add new states:
            unclosedIter = hypData.getTable().toShortPrefixes(closingRows, timeOracle);
        }

    }

    /**
     * Constructs a hypothesis MMLT from an observation table, inferred local resets, and inferred local timers.
     */
    private static <I, O> MMLTHypothesis<I, O> constructHypothesis(MMLTHypDataContainer<I, O> hypData) {

        final MMLTObservationTable<I, O> table = hypData.getTable();
        final MMLTModelParams<O> params = hypData.getModelParams();

        // 1. Create map that stores link between contentID and short-prefix row:
        final Map<Integer, Row<TimedInput<I>>> locationContentIdMap = new HashMap<>(); // contentId -> sp location
        for (Row<TimedInput<I>> spRow : table.getShortPrefixRows()) {
            // Multiple sp rows may have same contentID. Thus, assign each id only one location:
            locationContentIdMap.putIfAbsent(spRow.getRowContentId(), spRow);
        }

        // 2. Create untimed alphabet:
        GrowingMapAlphabet<I> alphabet = new GrowingMapAlphabet<>();
        for (TimedInput<I> symbol : hypData.getAlphabet()) {
            if (symbol instanceof InputSymbol<I> ndi) {
                alphabet.add(ndi.symbol());
            }
        }

        // 3. Prepare objects for automaton, timers and resets:
        int numLocations = table.numberOfShortPrefixRows();
        final Map<Integer, Integer> stateMap =
                new HashMap<>(HashUtil.capacity(numLocations)); // row content id -> state id
        final Map<Integer, Word<TimedInput<I>>> prefixMap =
                new HashMap<>(HashUtil.capacity(numLocations)); // state id -> location prefix
        MMLTHypothesis<I, O> hypothesis = new MMLTHypothesis<>(alphabet,
                                                               numLocations,
                                                               params.silentOutput(),
                                                               params.outputCombiner(),
                                                               prefixMap); // we pass the prefix map as reference so that we can fill it later

        // 4. Create one state per location:
        for (Row<TimedInput<I>> row : table.getShortPrefixRows()) {
            int newStateId = hypothesis.addState();
            stateMap.putIfAbsent(row.getRowContentId(), newStateId);
            prefixMap.put(newStateId, row.getLabel());

            if (row.getLabel().equals(Word.epsilon())) {
                hypothesis.setInitialState(newStateId);
            }
        }
        // Ensure initial location:
        assert hypothesis.getInitialState() != null : "Automaton must have an initial location.";

        // 5. Create outgoing transitions for non-delaying inputs:
        for (Entry<Integer, Integer> e : stateMap.entrySet()) {
            Integer rowContentId = e.getKey();
            Row<TimedInput<I>> spLocation = locationContentIdMap.get(rowContentId);

            assert spLocation != null;

            for (I symbol : alphabet) {
                int symIdx = hypData.getAlphabet().getSymbolIndex(TimedInput.input(symbol));

                TimedOutput<O> transOutput = hypData.getTransitionOutput(spLocation, symIdx);
                O output = params.silentOutput(); // silent by default
                if (transOutput != null) {
                    output = transOutput.symbol();
                }

                int successorId;
                if (spLocation.getSuccessor(symIdx) == null) {
                    successorId = spLocation.getRowContentId(); // not in local alphabet -> self-loop
                } else {
                    successorId = spLocation.getSuccessor(symIdx).getRowContentId();
                }

                // Add transition to automaton:
                int sourceLocId = e.getValue();
                int successorLocId = stateMap.getOrDefault(successorId, FullIntAbstraction.INVALID_STATE);
                hypothesis.addTransition(sourceLocId, symbol, successorLocId, output);

                // Check for local reset:
                Word<TimedInput<I>> targetTransition = spLocation.getLabel().append(TimedInput.input(symbol));
                if (hypData.getTransitionResetSet().contains(targetTransition) && sourceLocId == successorLocId) {
                    hypothesis.addLocalReset(sourceLocId, symbol);
                }
            }
        }

        // 6. Add timeout transitions:
        for (Entry<Integer, Integer> e : stateMap.entrySet()) {
            Integer rowContentId = e.getKey();
            Row<TimedInput<I>> spLocation = locationContentIdMap.get(rowContentId);

            assert spLocation != null;

            LocationTimerInfo<I, O> timerInfo = table.getLocationTimerInfo(spLocation);

            if (timerInfo != null) {
                for (TimerInfo<?, O> timer : timerInfo.getLocalTimers().values()) {
                    if (timer.periodic()) {
                        hypothesis.addPeriodicTimer(e.getValue(), timer.name(), timer.initial(), timer.outputs());
                    } else {
                        // One-shot: use successor from table
                        TimedInput<I> symbol = new TimeStepSequence<>(timer.initial());

                        int symIdx = hypData.getAlphabet().getSymbolIndex(symbol);
                        int successorId = spLocation.getSuccessor(symIdx).getRowContentId();

                        hypothesis.addOneShotTimer(e.getValue(),
                                                   timer.name(),
                                                   timer.initial(),
                                                   timer.outputs(),
                                                   stateMap.getOrDefault(successorId,
                                                                         FullIntAbstraction.INVALID_STATE));
                    }
                }
            }
        }

        return hypothesis;
    }

    private static final class OutputQuery<I, O> extends Query<TimedInput<I>, Word<TimedOutput<O>>> {

        private final Word<TimedInput<I>> label;
        private final Word<TimedInput<I>> prefix;
        private TimedOutput<O> output;

        private OutputQuery(Word<TimedInput<I>> label, Word<TimedInput<I>> prefix) {
            this.label = label;
            this.prefix = prefix;
        }

        @Override
        public void answer(Word<TimedOutput<O>> output) {
            assert output.size() == 1;
            this.output = output.firstSymbol();
        }

        @Override
        public Word<TimedInput<I>> getPrefix() {
            return prefix;
        }

        @Override
        public Word<TimedInput<I>> getSuffix() {
            return Word.fromLetter(label.lastSymbol());
        }

        /**
         * Processes the query result by mapping the given label to the (single) response.
         *
         * @param outputs
         *         the output map to write the mapping to
         */
        void process(Map<Word<TimedInput<I>>, TimedOutput<O>> outputs) {
            outputs.put(label, output);
        }
    }

    static final class BuilderDefaults {

        private BuilderDefaults() {
            // prevent instantiation
        }

        static <I> List<Word<TimedInput<I>>> initialSuffixes() {
            return Collections.emptyList();
        }

        static <I, O> ClosingStrategy<? super TimedInput<I>, ? super Word<TimedOutput<O>>> closingStrategy() {
            return ClosingStrategies.CLOSE_SHORTEST;
        }

        static <I> RefutableSymbolFilter<TimedInput<I>, InputSymbol<I>> symbolFilter() {
            return new AcceptAllSymbolFilter<>();
        }

        static AcexAnalyzer analyzer() {
            return AcexAnalyzers.BINARY_SEARCH_BWD;
        }
    }

}
