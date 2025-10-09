package de.learnlib.algorithm.lstar.mmlt;


import de.learnlib.acex.AcexAnalyzer;
import de.learnlib.acex.AcexAnalyzers;
import de.learnlib.algorithm.LocalTimerMealyModelParams;
import de.learnlib.algorithm.lstar.closing.ClosingStrategies;
import de.learnlib.algorithm.lstar.closing.ClosingStrategy;
import de.learnlib.algorithm.lstar.mmlt.cex.CexPreprocessor;
import de.learnlib.algorithm.lstar.mmlt.cex.LocalTimerMealyCounterexampleHandler;
import de.learnlib.algorithm.lstar.mmlt.cex.LocalTimerMealyOutputInconsistency;
import de.learnlib.algorithm.lstar.mmlt.cex.results.FalseIgnoreResult;
import de.learnlib.algorithm.lstar.mmlt.cex.results.MissingDiscriminatorResult;
import de.learnlib.algorithm.lstar.mmlt.cex.results.MissingOneShotResult;
import de.learnlib.algorithm.lstar.mmlt.cex.results.MissingResetResult;
import de.learnlib.algorithm.lstar.mmlt.hyp.LocalTimerMealyHypothesis;
import de.learnlib.datastructure.observationtable.OTLearner;
import de.learnlib.datastructure.observationtable.ObservationTable;
import de.learnlib.datastructure.observationtable.Row;
import de.learnlib.oracle.AbstractTimedQueryOracle;
import de.learnlib.query.DefaultQuery;
import de.learnlib.statistic.container.DummyStatsContainer;
import de.learnlib.statistic.container.LearnerStatsProvider;
import de.learnlib.statistic.container.StatsContainerX;
import de.learnlib.symbol_filter.SymbolFilter;
import de.learnlib.symbol_filter.SymbolFilterResponse;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.time.mmlt.LocalTimerMealySemanticInputSymbol;
import net.automatalib.alphabet.time.mmlt.LocalTimerMealyOutputSymbol;
import net.automatalib.alphabet.time.mmlt.TimeStepSequence;
import net.automatalib.automaton.time.mmlt.LocalTimerMealy;
import net.automatalib.automaton.time.mmlt.MealyTimerInfo;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * The MMLT learner.
 *
 * @param <I> Input type for non-delaying inputs
 * @param <O> Output symbol type
 */
public class LStarLocalTimerMealy<I, O> implements OTLearner<LocalTimerMealy<Integer, I, O>, LocalTimerMealySemanticInputSymbol<I>, Word<LocalTimerMealyOutputSymbol<O>>>, LearnerStatsProvider {

    private static final Logger logger = LoggerFactory.getLogger(LStarLocalTimerMealy.class);
    private StatsContainerX stats = new DummyStatsContainer();

    private final ClosingStrategy<? super LocalTimerMealySemanticInputSymbol<I>, ? super Word<LocalTimerMealyOutputSymbol<O>>> closingStrategy;

    private final AbstractTimedQueryOracle<I, O> timeOracle;
    private final SymbolFilter<I, O> symbolFilter;

    private final LStarLocalTimerMealyHypDataContainer<I, O> hypData;

    // ============================


    private final List<Word<LocalTimerMealySemanticInputSymbol<I>>> initialSuffixes;
    private final LocalTimerMealyCounterexampleHandler<Integer, I, O> cexAnalyzer;

    /**
     * Instantiates a new Rivest-Schapire learner for MMLTs.
     * <p>
     * Uses the close-shortest strategy for closing the observation table and
     * binary-backwards search for decomposing counterexamples.
     *
     * @param alphabet        Input alphabet for the semantic automaton
     * @param modelParams     Model parameters
     * @param initialSuffixes Initial set of suffixes. May be empty.
     * @param timeOracle      The output query oracle for MMLTs.
     * @param symbolFilter    The symbol filter. If no filter should be used, use the AcceptAll filter.
     */
    public LStarLocalTimerMealy(Alphabet<LocalTimerMealySemanticInputSymbol<I>> alphabet,
                                LocalTimerMealyModelParams<O> modelParams,
                                @NonNull
                                List<Word<LocalTimerMealySemanticInputSymbol<I>>> initialSuffixes,
                                AbstractTimedQueryOracle<I, O> timeOracle,
                                SymbolFilter<I, O> symbolFilter) {
        this(alphabet, modelParams, initialSuffixes, ClosingStrategies.CLOSE_SHORTEST, timeOracle, symbolFilter, AcexAnalyzers.BINARY_SEARCH_BWD);
    }

    /**
     * Instantiates a new Rivest-Schapire learner for MMLTs.
     *
     * @param alphabet        Input alphabet for the semantic automaton
     * @param modelParams     Model parameters
     * @param initialSuffixes Initial set of suffixes. May be empty.
     * @param closingStrategy Closing strategy for the observation table.
     * @param timeOracle      The output query oracle for MMLTs.
     * @param symbolFilter    The symbol filter. If no filter should be used, use the AcceptAll filter.
     * @param analyzer        The strategy for decomposing counterexamples.
     */
    public LStarLocalTimerMealy(Alphabet<LocalTimerMealySemanticInputSymbol<I>> alphabet,
                                LocalTimerMealyModelParams<O> modelParams,
                                @NonNull
                                List<Word<LocalTimerMealySemanticInputSymbol<I>>> initialSuffixes,
                                ClosingStrategy<? super LocalTimerMealySemanticInputSymbol<I>, ? super Word<LocalTimerMealyOutputSymbol<O>>> closingStrategy,
                                AbstractTimedQueryOracle<I, O> timeOracle,
                                @NonNull
                                SymbolFilter<I, O> symbolFilter,
                                AcexAnalyzer analyzer) {
        this.closingStrategy = closingStrategy;
        this.timeOracle = timeOracle;
        this.initialSuffixes = initialSuffixes;

        // Prepare hyp data:

        // Init hypothesis data:
        this.hypData = new LStarLocalTimerMealyHypDataContainer<>(alphabet, modelParams,
                new LocalTimerMealyObservationTable<>(alphabet, modelParams.maxTimerQueryWaitingTime(), symbolFilter, modelParams.silentOutput()));

        this.cexAnalyzer = new LocalTimerMealyCounterexampleHandler<>(timeOracle, analyzer, symbolFilter);
        this.symbolFilter = symbolFilter;
    }

    /**
     * Heuristically chooses a new one-shot timer from the provided timers:
     * takes the timer with the highest initial value that a) does not exceed maxInitialValue and b) has not timer
     * with a lower initial value that times out at the same time.
     *
     * @param sortedTimers    Timers, sorted ascendingly by their initial value
     * @param maxInitialValue Max. initial value to consider
     * @param <O>             Output type
     * @return New one-shot timer
     */
    public static <O> MealyTimerInfo<O> selectOneShotTimer(List<MealyTimerInfo<O>> sortedTimers, long maxInitialValue) {

        // Filter relevant timers:
        // Start at timer with the highest initial value.
        // Ignore all timers whose initial value exceeds the maximum value.
        // Also ignore timers whose timeout is the multiple of another timer's initial value.
        List<MealyTimerInfo<O>> relevantTimers = new ArrayList<>();
        for (int i = sortedTimers.size() - 1; i >= 0; i--) {
            MealyTimerInfo<O> timer = sortedTimers.get(i);

            if (timer.initial() > maxInitialValue) {
                continue; // could not have expired
            }

            // Ignore timers whose initial value is a multiple of another one.
            // When set to one-shot, these would expire at same time as periodic timer -> non-deterministic behavior!
            boolean multiple = false;
            for (int j = 0; j < i; j++) {
                MealyTimerInfo<O> otherTimer = sortedTimers.get(j);
                if (timer.initial() % otherTimer.initial() == 0) {
                    multiple = true;
                    break;
                }
            }
            if (multiple) {
                continue;
            }

            relevantTimers.add(timer); // not a multiple and within time
        }

        if (relevantTimers.isEmpty()) {
            throw new IllegalStateException("Max. initial value is too low; must include at least one timer.");
        }

        // Return the candidate with the highest initial value one-shot:
        return relevantTimers.get(0); // order is reversed -> first is last
    }


    /**
     * Constructs an MMLT hypothesis.
     * This updates all transition outputs, if required.
     *
     * @return MMLT hypothesis
     */
    public LocalTimerMealy<Integer, I, O> getHypothesisModel() {
        return getInternalLocalTimerMealyHypothesis();
    }

    /**
     * Like the construction above, but returns an LocalTimerMealyHypothesis object instead.
     * This objects provides additional functions that are just intended for the learner but not the teacher.
     *
     * @return MMLT hypothesis
     */
    private LocalTimerMealyHypothesis<Integer, I, O> getInternalLocalTimerMealyHypothesis() {
        this.updateOutputs();
        var hyp = LocalTimerMealyHypothesisBuilder.constructHypothesis(this.hypData);

        return new LocalTimerMealyHypothesis<>(hyp.automaton(), hyp.prefixMap());
    }

    protected List<Row<LocalTimerMealySemanticInputSymbol<I>>> selectClosingRows(List<List<Row<LocalTimerMealySemanticInputSymbol<I>>>> unclosed) {
        return closingStrategy.selectClosingRows(unclosed, hypData.getTable(), timeOracle);
    }


    protected void updateOutputs() {
        // Query output of newly-added transitions:
        Stream.concat(this.hypData.getTable().getShortPrefixRows().stream(), this.hypData.getTable().getLongPrefixRows().stream())
                .forEach(row -> {
                    if (row.getLabel().isEmpty()) {
                        return; // initial state
                    }

                    if (this.hypData.getTransitionOutputMap().containsKey(row.getLabel())) {
                        return; // already queried
                    }

                    Word<LocalTimerMealySemanticInputSymbol<I>> prefix = row.getLabel().prefix(-1);
                    LocalTimerMealySemanticInputSymbol<I> inputSym = row.getLabel().suffix(1).lastSymbol();

                    LocalTimerMealyOutputSymbol<O> output = null;
                    if (inputSym instanceof TimeStepSequence<I> ws) {
                        // Query timer output from table:
                        MealyTimerInfo<O> timerInfo = this.hypData.getTable().getTimerInfo(prefix, ws.getTimeSteps());
                        if (timerInfo == null) {
                            throw new AssertionError();
                        }
                        output = new LocalTimerMealyOutputSymbol<>(timerInfo.output());
                    } else {
                        output = this.timeOracle.querySuffixOutput(prefix, Word.fromLetter(inputSym)).lastSymbol();
                    }

                    if (output != null) {
                        this.hypData.getTransitionOutputMap().put(row.getLabel(), output);
                    }
                });
    }

    // ==========================

    @Override
    public void startLearning() {
        List<List<Row<LocalTimerMealySemanticInputSymbol<I>>>> initialUnclosed = this.hypData.getTable().initialize(Collections.emptyList(), this.initialSuffixes, timeOracle);

        // Ensure that closed:
        this.completeConsistentTable(initialUnclosed);
    }

    @Override
    public boolean refineHypothesis(DefaultQuery<LocalTimerMealySemanticInputSymbol<I>, Word<LocalTimerMealyOutputSymbol<O>>> ceQuery) {
        if (!refineHypothesisSingle(ceQuery)) {
            return false; // no valid CEX
        }
        while (refineHypothesisSingle(ceQuery)) {
        }
        return true;
    }


    /**
     * Transforms the provided counterexample to an inconsistency object:
     * First, checks if still a counterexample. If so, cuts the cex after the first output deviation.
     *
     * @param ceQuery    Counterexample
     * @param hypothesis Current hypothesis
     * @return The resulting inconsistency, or null, if the counterexample is not a counterexample.
     */
    @Nullable
    private LocalTimerMealyOutputInconsistency<I, O> toOutputInconsistency(DefaultQuery<LocalTimerMealySemanticInputSymbol<I>, Word<LocalTimerMealyOutputSymbol<O>>> ceQuery, LocalTimerMealyHypothesis<Integer, I, O> hypothesis) {
        // 1. Cut example after first deviation:
        Word<LocalTimerMealyOutputSymbol<O>> hypOutput = hypothesis.getSemantics().computeSuffixOutput(ceQuery.getPrefix(), ceQuery.getSuffix());
        DefaultQuery<LocalTimerMealySemanticInputSymbol<I>, Word<LocalTimerMealyOutputSymbol<O>>> shortQuery = CexPreprocessor.truncateCEX(ceQuery, hypOutput);
        if (shortQuery == null) {
            return null;
        }

        // 2. Calculate shortened hypothesis output:
        var shortHypOutput = hypothesis.getSemantics().computeSuffixOutput(shortQuery.getPrefix(), shortQuery.getSuffix());
        if (shortHypOutput.equals(shortQuery.getOutput())) {
            throw new AssertionError("Deviation lost after shortening.");
        }

        return new LocalTimerMealyOutputInconsistency<>(shortQuery.getPrefix(),
                shortQuery.getSuffix(),
                shortQuery.getOutput(), shortHypOutput);
    }

    private boolean refineHypothesisSingle(DefaultQuery<LocalTimerMealySemanticInputSymbol<I>, Word<LocalTimerMealyOutputSymbol<O>>> ceQuery) {
        // 1. Update hypothesis (may have changed since last refinement):
        var hypothesis = this.getInternalLocalTimerMealyHypothesis();

        // 2. Transform to output inconsistency:
        var outputIncons = this.toOutputInconsistency(ceQuery, hypothesis);
        if (outputIncons == null) {
            return false;
        }

        logger.debug(String.format("Refining with inconsistency %s", outputIncons));

        // 3. Identify source of deviation:
        stats.startOrResumeClock("clk_cex_analysis", "Total cex analysis time");
        stats.increaseCounter("cnt_cex_analysis", "Cex analyses");
        var analysisResult = this.cexAnalyzer.analyzeInconsistency(outputIncons, hypothesis);
        stats.pauseClock("clk_cex_analysis");

        // 4. Refine:
        if (analysisResult instanceof MissingDiscriminatorResult<Integer, I, O> locSplit) {
            stats.increaseCounter("INACC_MISSING_DISC",
                    "Inaccuracies: missing discriminators");

            // Add new discriminator as suffix:
            if (hypData.getTable().getSuffixes().contains(locSplit.getDiscriminator())) throw new AssertionError();
            List<Word<LocalTimerMealySemanticInputSymbol<I>>> suffixes = Collections.singletonList(locSplit.getDiscriminator());
            var unclosed = hypData.getTable().addSuffixes(suffixes, timeOracle);

            // Close transitions:
            this.completeConsistentTable(unclosed); // no consistency check for RS
        } else if (analysisResult instanceof MissingResetResult<Integer, I, O> noReset) {
            stats.increaseCounter("INACC_MISSING_RESETS",
                    "Inaccuracies: missing resets");

            // Add missing reset:
            var resetTrans = hypothesis.getPrefix(noReset.getLocation()).append(noReset.getInput());
            this.hypData.getTransitionResetSet().add(resetTrans);
        } else if (analysisResult instanceof MissingOneShotResult<Integer, I, O> noAperiodic) {
            stats.increaseCounter("INACC_MISSING_OS",
                    "Inaccuracies: missing one-shot timers");

            // Identify corresponding sp row:
            Word<LocalTimerMealySemanticInputSymbol<I>> locPrefix = hypothesis.getPrefix(noAperiodic.getLocation());
            Row<LocalTimerMealySemanticInputSymbol<I>> spRow = hypData.getTable().getRow(locPrefix);
            if (spRow == null || !spRow.isShortPrefixRow()) {
                throw new AssertionError();
            }

            this.handleMissingTimeoutChange(spRow, noAperiodic.getTimeout());
        } else if (analysisResult instanceof FalseIgnoreResult<Integer, I, O> falseIgnore) {
            stats.increaseCounter("INACC_MISSING_FI",
                    "Inaccuracies: false ignores");

            if (this.symbolFilter == null) {
                throw new AssertionError("Cannot detect false ignores without symbol filter.");
            }

            // Identify corresponding sp row:
            Word<LocalTimerMealySemanticInputSymbol<I>> locPrefix = hypothesis.getPrefix(falseIgnore.getLocation());
            Row<LocalTimerMealySemanticInputSymbol<I>> spRow = hypData.getTable().getRow(locPrefix);
            if (spRow == null || !spRow.isShortPrefixRow()) {
                throw new AssertionError();
            }

            // Update filter:
            this.symbolFilter.update(locPrefix, falseIgnore.getSymbol(), SymbolFilterResponse.ACCEPT);

            // Legalize symbol + close table:
            var unclosed = hypData.getTable().addOutgoingTransition(spRow, falseIgnore.getSymbol(), this.timeOracle);
            stats.increaseCounter("Count_legalized", "Legalized symbols");

            this.completeConsistentTable(unclosed);
        } else {
            throw new IllegalStateException("Unknown inconsistency type.");
        }

        return true;
    }

    private void handleMissingTimeoutChange(Row<LocalTimerMealySemanticInputSymbol<I>> spRow, MealyTimerInfo<O> timeout) {
        var locationTimerInfo = hypData.getTable().getLocationTimerInfo(spRow);
        if (locationTimerInfo == null) {
            throw new AssertionError("Location with missing one-shot timer must have timers.");
        }

        // Only timer with highest initial value can be one-shot.
        // If location already has a one-shot timer, prefix of its timeout-transition might be core or fringe prefix.
        // If it is a fringe prefix, we need to remove it:
        var lastTimerTransPrefix = spRow.getLabel().append(new TimeStepSequence<>(locationTimerInfo.getLastTimer().initial()));
        if (!locationTimerInfo.getLastTimer().periodic()) {
            if (!hypData.getTable().getRow(lastTimerTransPrefix).isShortPrefixRow()) {
                // Last timer is one-shot + has fringe prefix:
                this.hypData.getTable().removeLpRow(lastTimerTransPrefix);
            }
        }

        // Prefix for timeout-transition of new one-shot timer:
        Word<LocalTimerMealySemanticInputSymbol<I>> timerTransPrefix = spRow.getLabel().append(new TimeStepSequence<>(timeout.initial()));
        if (this.hypData.getTable().getRow(timerTransPrefix) != null) {
            throw new AssertionError("Timer already appears to be one-shot.");
        }

        // Remove all timers with greater timeout (are now redundant):
        var subsequentTimers = locationTimerInfo.getSortedTimers().stream()
                .filter(t -> t.initial() > timeout.initial())
                .map(MealyTimerInfo::name).toList();
        subsequentTimers.forEach(locationTimerInfo::removeTimer);

        // Change from periodic to one-shot:
        locationTimerInfo.setOneShotTimer(timeout.name());

        // Update fringe prefixes + close table:
        List<List<Row<LocalTimerMealySemanticInputSymbol<I>>>> unclosed = this.hypData.getTable().addTimerTransition(spRow, timeout, this.timeOracle);
        this.completeConsistentTable(unclosed);
    }


    @Override
    public ObservationTable<LocalTimerMealySemanticInputSymbol<I>, Word<LocalTimerMealyOutputSymbol<O>>> getObservationTable() {
        return this.hypData.getTable();
    }

    /**
     * Iteratively checks for unclosedness and inconsistencies in the table, and fixes any occurrences thereof. This
     * process is repeated until the observation table is both closed and consistent.
     * <p>
     * Simplified version for RS learner: assumes that OT is always consistent.
     *
     * @param unclosed the unclosed rows (equivalence classes) to start with.
     */
    protected void completeConsistentTable(List<List<Row<LocalTimerMealySemanticInputSymbol<I>>>> unclosed) {
        List<List<Row<LocalTimerMealySemanticInputSymbol<I>>>> unclosedIter = unclosed;
        while (!unclosedIter.isEmpty()) {
            List<Row<LocalTimerMealySemanticInputSymbol<I>>> closingRows = this.selectClosingRows(unclosedIter);

            // Add new states:
            unclosedIter = hypData.getTable().toShortPrefixes(closingRows, timeOracle);
        }

    }

    @Override
    public void setStatsContainer(StatsContainerX container) {
        this.stats = container;
        this.cexAnalyzer.setStatsContainer(container);
    }


}
