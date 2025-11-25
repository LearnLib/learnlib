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

import java.util.Collection;
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
import de.learnlib.filter.MutableSymbolFilter;
import de.learnlib.filter.symbol.AcceptAllSymbolFilter;
import de.learnlib.oracle.TimedQueryOracle;
import de.learnlib.query.DefaultQuery;
import de.learnlib.statistic.Statistics;
import de.learnlib.statistic.StatisticsCollector;
import de.learnlib.time.MMLTModelParams;
import de.learnlib.tooling.annotation.builder.GenerateBuilder;
import de.learnlib.util.mealy.MealyUtil;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.GrowingAlphabet;
import net.automatalib.alphabet.impl.GrowingMapAlphabet;
import net.automatalib.automaton.mmlt.MMLT;
import net.automatalib.automaton.mmlt.TimerInfo;
import net.automatalib.common.util.HashUtil;
import net.automatalib.symbol.time.InputSymbol;
import net.automatalib.symbol.time.TimeStepSequence;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.symbol.time.TimedOutput;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The MMLT learner.
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
    private final MutableSymbolFilter<TimedInput<I>, InputSymbol<I>> symbolFilter;

    private final MMLTHypDataContainer<I, O> hypData;

    // ============================

    private final List<Word<TimedInput<I>>> initialSuffixes;
    private final MMLTCounterexampleHandler<I, O> cexAnalyzer;

    /**
     * Instantiates a new Rivest-Schapire learner for MMLTs.
     * <p>
     * Uses the close-shortest strategy for closing the observation table, binary-backwards search for decomposing
     * counterexamples, and no symbol filter.
     *
     * @param alphabet
     *         alphabet (of non-delaying inputs)
     * @param modelParams
     *         model parameters
     * @param initialSuffixes
     *         initial set of suffixes (may be empty)
     * @param timeOracle
     *         the query oracle for MMLTs
     */
    public ExtensibleLStarMMLT(Alphabet<I> alphabet,
                               MMLTModelParams<O> modelParams,
                               List<Word<TimedInput<I>>> initialSuffixes,
                               TimedQueryOracle<I, O> timeOracle) {
        this(alphabet,
             modelParams,
             initialSuffixes,
             ClosingStrategies.CLOSE_SHORTEST,
             timeOracle,
             new AcceptAllSymbolFilter<>(),
             AcexAnalyzers.BINARY_SEARCH_BWD);
    }

    /**
     * Instantiates a new Rivest-Schapire learner for MMLTs.
     * <p>
     * Uses the close-shortest strategy for closing the observation table and binary-backwards search for decomposing
     * counterexamples.
     *
     * @param alphabet
     *         alphabet (of non-delaying inputs)
     * @param modelParams
     *         model parameters
     * @param initialSuffixes
     *         initial set of suffixes (may be empty)
     * @param timeOracle
     *         the query oracle for MMLTs
     * @param symbolFilter
     *         the symbol filter
     */
    public ExtensibleLStarMMLT(Alphabet<I> alphabet,
                               MMLTModelParams<O> modelParams,
                               List<Word<TimedInput<I>>> initialSuffixes,
                               TimedQueryOracle<I, O> timeOracle,
                               MutableSymbolFilter<TimedInput<I>, InputSymbol<I>> symbolFilter) {
        this(alphabet,
             modelParams,
             initialSuffixes,
             ClosingStrategies.CLOSE_SHORTEST,
             timeOracle,
             symbolFilter,
             AcexAnalyzers.BINARY_SEARCH_BWD);
    }

    /**
     * Instantiates a new Rivest-Schapire learner for MMLTs.
     *
     * @param alphabet
     *         alphabet (of non-delaying inputs)
     * @param modelParams
     *         model parameters
     * @param initialSuffixes
     *         initial set of suffixes (may be empty)
     * @param closingStrategy
     *         closing strategy for the observation table.
     * @param timeOracle
     *         the query oracle for MMLTs
     * @param symbolFilter
     *         the symbol filter
     * @param analyzer
     *         The strategy for decomposing counterexamples.
     */
    @GenerateBuilder(defaults = BuilderDefaults.class)
    public ExtensibleLStarMMLT(Alphabet<I> alphabet,
                               MMLTModelParams<O> modelParams,
                               List<Word<TimedInput<I>>> initialSuffixes,
                               ClosingStrategy<? super TimedInput<I>, ? super Word<TimedOutput<O>>> closingStrategy,
                               TimedQueryOracle<I, O> timeOracle,
                               MutableSymbolFilter<TimedInput<I>, InputSymbol<I>> symbolFilter,
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
     * Heuristically chooses a new one-shot timer from the provided timers: takes the timer with the highest initial
     * value that a) does not exceed maxInitialValue and b) has not timer with a lower initial value that times out at
     * the same time.
     *
     * @param sortedTimers
     *         Timers, sorted ascendingly by their initial value
     * @param maxInitialValue
     *         Max. initial value to consider
     * @param <O>
     *         Output type
     *
     * @return New one-shot timer
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

        throw new IllegalStateException("Max. initial value is too low; must include at least one timer.");
    }

    /**
     * Constructs an MMLT hypothesis. This updates all transition outputs, if required.
     *
     * @return MMLT hypothesis
     */
    @Override
    public MMLT<Integer, I, ?, O> getHypothesisModel() {
        return getInternalLocalTimerMealyHypothesis();
    }

    /**
     * Like the construction above, but returns an LocalTimerMealyHypothesis object instead. This objects provides
     * additional functions that are just intended for the learner but not the teacher.
     *
     * @return MMLT hypothesis
     */
    private MMLTHypothesis<I, O> getInternalLocalTimerMealyHypothesis() {
        this.updateOutputs();
        return constructHypothesis(this.hypData);
    }

    protected List<Row<TimedInput<I>>> selectClosingRows(List<List<Row<TimedInput<I>>>> unclosed) {
        return closingStrategy.selectClosingRows(unclosed, hypData.getTable(), timeOracle);
    }

    private void updateOutputs() {
        // Query output of newly-added transitions:
        updateOutputs(this.hypData.getTable().getShortPrefixRows());
        updateOutputs(this.hypData.getTable().getLongPrefixRows());
    }

    private void updateOutputs(Collection<Row<TimedInput<I>>> rows) {
        for (Row<TimedInput<I>> row : rows) {
            if (row.getLabel().isEmpty()) {
                continue; // initial state
            }

            if (this.hypData.getTransitionOutputMap().containsKey(row.getLabel())) {
                continue; // already queried
            }

            Word<TimedInput<I>> prefix = row.getLabel().prefix(-1);
            TimedInput<I> inputSym = row.getLabel().suffix(1).lastSymbol();

            TimedOutput<O> output;
            if (inputSym instanceof TimeStepSequence<I> ws) {
                // Query timer output from table:
                TimerInfo<?, O> timerInfo = this.hypData.getTable().getTimerInfo(prefix, ws.timeSteps());
                assert timerInfo != null;
                O combinedOutput = this.hypData.getModelParams().outputCombiner().combineSymbols(timerInfo.outputs());
                output = new TimedOutput<>(combinedOutput);
            } else {
                output = this.timeOracle.answerQuery(prefix, Word.fromLetter(inputSym)).lastSymbol();
            }

            if (output != null) {
                this.hypData.getTransitionOutputMap().put(row.getLabel(), output);
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
        MMLTHypothesis<I, O> hypothesis = this.getInternalLocalTimerMealyHypothesis();

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
        LocationTimerInfo<I, O> locationTimerInfo = hypData.getTable().getLocationTimerInfo(spRow);
        assert locationTimerInfo != null : "Location with missing one-shot timer must have timers.";

        // Only timer with highest initial value can be one-shot.
        // If location already has a one-shot timer, prefix of its timeout-transition might be core or fringe prefix.
        // If it is a fringe prefix, we need to remove it:
        TimerInfo<?, O> lastTimer = locationTimerInfo.getLastTimer();
        assert lastTimer != null;
        Word<TimedInput<I>> lastTimerTransPrefix = spRow.getLabel().append(TimedInput.step(lastTimer.initial()));
        if (!lastTimer.periodic()) {
            Row<TimedInput<I>> row = hypData.getTable().getRow(lastTimerTransPrefix);
            assert row != null;
            if (!row.isShortPrefixRow()) {
                // Last timer is one-shot + has fringe prefix:
                this.hypData.getTable().removeLpRow(lastTimerTransPrefix);
            }
        }

        // Prefix for timeout-transition of new one-shot timer:
        Word<TimedInput<I>> timerTransPrefix = spRow.getLabel().append(TimedInput.step(timeout.initial()));
        assert this.hypData.getTable().getRow(timerTransPrefix) == null : "Timer already appears to be one-shot.";

        // Remove all timers with greater timeout (are now redundant):
        List<String> subsequentTimers = locationTimerInfo.getSortedTimers()
                                                         .stream()
                                                         .filter(t -> t.initial() > timeout.initial())
                                                         .map(TimerInfo::name)
                                                         .toList();
        subsequentTimers.forEach(locationTimerInfo::removeTimer);

        // Change from periodic to one-shot:
        locationTimerInfo.setOneShotTimer(timeout.name());

        // Update fringe prefixes + close table:
        List<List<Row<TimedInput<I>>>> unclosed =
                this.hypData.getTable().addTimerTransition(spRow, timeout, this.timeOracle);
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
    protected void completeConsistentTable(List<List<Row<TimedInput<I>>>> unclosed) {
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

        // 1. Create map that stores link between contentID and short-prefix row:
        final Map<Integer, Row<TimedInput<I>>> locationContentIdMap = new HashMap<>(); // contentId -> sp location
        for (Row<TimedInput<I>> spRow : hypData.getTable().getShortPrefixRows()) {
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
        int numLocations = hypData.getTable().numberOfShortPrefixRows();
        final Map<Integer, Integer> stateMap =
                new HashMap<>(HashUtil.capacity(numLocations)); // row content id -> state id
        final Map<Integer, Word<TimedInput<I>>> prefixMap =
                new HashMap<>(HashUtil.capacity(numLocations)); // state id -> location prefix
        MMLTHypothesis<I, O> hypothesis = new MMLTHypothesis<>(alphabet,
                                                               numLocations,
                                                               hypData.getModelParams().silentOutput(),
                                                               hypData.getModelParams().outputCombiner(),
                                                               prefixMap); // we pass the prefix map as reference so that we can fill it later

        // 4. Create one state per location:
        for (Row<TimedInput<I>> row : hypData.getTable().getShortPrefixRows()) {
            int newStateId = hypothesis.addState();
            stateMap.putIfAbsent(row.getRowContentId(), newStateId);
            prefixMap.put(newStateId, row.getLabel());

            if (row.getLabel().equals(Word.epsilon())) {
                hypothesis.setInitialState(newStateId);
            }
        }
        // Ensure initial location:
        if (hypothesis.getInitialState() == null) {
            throw new IllegalArgumentException("Automaton must have an initial location.");
        }

        // 5. Create outgoing transitions for non-delaying inputs:
        for (Entry<Integer, Integer> e : stateMap.entrySet()) {
            Integer rowContentId = e.getKey();
            Row<TimedInput<I>> spLocation = locationContentIdMap.get(rowContentId);

            for (I symbol : alphabet) {
                int symIdx = hypData.getAlphabet().getSymbolIndex(TimedInput.input(symbol));

                TimedOutput<O> transOutput = hypData.getTransitionOutput(spLocation, symIdx);
                O output = hypData.getModelParams().silentOutput(); // silent by default
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
                int successorLocId = stateMap.get(successorId);
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

            LocationTimerInfo<I, O> timerInfo = hypData.getTable().getLocationTimerInfo(spLocation);

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
                                                   stateMap.get(successorId));
                    }
                }
            }
        }

        return hypothesis;
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

        static <I> MutableSymbolFilter<TimedInput<I>, InputSymbol<I>> symbolFilter() {
            return new AcceptAllSymbolFilter<>();
        }

        static AcexAnalyzer analyzer() {
            return AcexAnalyzers.LINEAR_BWD;
        }
    }

}
