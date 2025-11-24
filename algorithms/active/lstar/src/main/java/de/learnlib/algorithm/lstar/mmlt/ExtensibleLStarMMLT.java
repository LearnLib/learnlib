package de.learnlib.algorithm.lstar.mmlt;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import de.learnlib.acex.AcexAnalyzer;
import de.learnlib.acex.AcexAnalyzers;
import de.learnlib.filter.symbol.AcceptAllSymbolFilter;
import de.learnlib.filter.symbol.CachedSymbolFilter;
import de.learnlib.time.MMLTModelParams;
import de.learnlib.algorithm.lstar.closing.ClosingStrategies;
import de.learnlib.algorithm.lstar.closing.ClosingStrategy;
import de.learnlib.algorithm.lstar.mmlt.cex.MMLTCounterexampleHandler;
import de.learnlib.algorithm.lstar.mmlt.cex.MMLTOutputInconsistency;
import de.learnlib.algorithm.lstar.mmlt.cex.results.FalseIgnoreResult;
import de.learnlib.algorithm.lstar.mmlt.cex.results.MissingDiscriminatorResult;
import de.learnlib.algorithm.lstar.mmlt.cex.results.MissingOneShotResult;
import de.learnlib.algorithm.lstar.mmlt.cex.results.MissingResetResult;
import de.learnlib.datastructure.observationtable.OTLearner;
import de.learnlib.datastructure.observationtable.ObservationTable;
import de.learnlib.datastructure.observationtable.Row;
import de.learnlib.filter.MutableSymbolFilter;
import de.learnlib.oracle.TimedQueryOracle;
import de.learnlib.query.DefaultQuery;
import de.learnlib.statistic.Statistics;
import de.learnlib.statistic.StatisticsCollector;
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
 * @param <I> Input type for non-delaying inputs
 * @param <O> Output symbol type
 */
public class ExtensibleLStarMMLT<I, O> implements OTLearner<MMLT<Integer, I, ?, O>, TimedInput<I>, Word<TimedOutput<O>>> {

    private static final Logger logger = LoggerFactory.getLogger(ExtensibleLStarMMLT.class);
    private final StatisticsCollector stats;

    private final ClosingStrategy<? super TimedInput<I>, ? super Word<TimedOutput<O>>> closingStrategy;

    private final TimedQueryOracle<I, O> timeOracle;
    private final MutableSymbolFilter<TimedInput<I>, InputSymbol<I>> symbolFilter;

    private final MMLTHypDataContainer<I, O> hypData;

    // ============================


    private final List<Word<TimedInput<I>>> initialSuffixes;
    private final MMLTCounterexampleHandler<Integer, I, O> cexAnalyzer;

    /**
     * Instantiates a new Rivest-Schapire learner for MMLTs.
     * <p>
     * Uses the close-shortest strategy for closing the observation table,
     * binary-backwards search for decomposing counterexamples, and no symbol filter.
     *
     * @param alphabet        Alphabet of non-delaying inputs
     * @param modelParams     LocalTimerMealyModel parameters
     * @param initialSuffixes Initial set of suffixes. May be empty.
     * @param timeOracle      The output query oracle for MMLTs.
     */
    public ExtensibleLStarMMLT(Alphabet<I> alphabet,
                               MMLTModelParams<O> modelParams,
                               List<Word<TimedInput<I>>> initialSuffixes,
                               TimedQueryOracle<I, O> timeOracle) {
        this(alphabet, modelParams, initialSuffixes, ClosingStrategies.CLOSE_SHORTEST, timeOracle,
                new CachedSymbolFilter<>(new AcceptAllSymbolFilter<>()), AcexAnalyzers.BINARY_SEARCH_BWD);
    }

    /**
     * Instantiates a new Rivest-Schapire learner for MMLTs.
     * <p>
     * Uses the close-shortest strategy for closing the observation table and
     * binary-backwards search for decomposing counterexamples.
     *
     * @param alphabet        Alphabet of non-delaying inputs
     * @param modelParams     LocalTimerMealyModel parameters
     * @param initialSuffixes Initial set of suffixes. May be empty.
     * @param timeOracle      The output query oracle for MMLTs.
     * @param symbolFilter    The symbol filter. If no filter should be used, use the AcceptAll filter.
     */
    public ExtensibleLStarMMLT(Alphabet<I> alphabet,
                               MMLTModelParams<O> modelParams,
                               List<Word<TimedInput<I>>> initialSuffixes,
                               TimedQueryOracle<I, O> timeOracle,
                               MutableSymbolFilter<TimedInput<I>, InputSymbol<I>> symbolFilter) {
        this(alphabet, modelParams, initialSuffixes, ClosingStrategies.CLOSE_SHORTEST, timeOracle, symbolFilter, AcexAnalyzers.BINARY_SEARCH_BWD);
    }

    /**
     * Instantiates a new Rivest-Schapire learner for MMLTs.
     *
     * @param alphabet        Alphabet of non-delaying inputs
     * @param modelParams     LocalTimerMealyModel parameters
     * @param initialSuffixes Initial set of suffixes. May be empty.
     * @param closingStrategy Closing strategy for the observation table.
     * @param timeOracle      The output query oracle for MMLTs.
     * @param symbolFilter    The symbol filter. If no filter should be used, use the AcceptAll filter.
     * @param analyzer        The strategy for decomposing counterexamples.
     */
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
        this.hypData = new MMLTHypDataContainer<>(internalAlphabet, modelParams,
                new MMLTObservationTable<>(internalAlphabet,
                        modelParams.maxTimerQueryWaitingTime(), symbolFilter, modelParams.silentOutput()));

        this.cexAnalyzer = new MMLTCounterexampleHandler<>(timeOracle, analyzer, symbolFilter);
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
    public static <O> int selectOneShotTimer(List<? extends TimerInfo<?, O>> sortedTimers, long maxInitialValue) {

        // Filter relevant timers:
        // Start at timer with the highest initial value.
        // Ignore all timers whose initial value exceeds the maximum value.
        // Also ignore timers whose timeout is the multiple of another timer's initial value.
        for (int i = sortedTimers.size() - 1; i >= 0; i--) {
            TimerInfo<?, O> timer = sortedTimers.get(i);

            if (timer.initial() > maxInitialValue) {
                continue; // could not have expired
            }

            // Ignore timers whose initial value is a multiple of another one.
            // When set to one-shot, these would expire at same time as periodic timer -> non-deterministic behavior!
            boolean multiple = false;
            for (int j = 0; j < i; j++) {
                TimerInfo<?, O> otherTimer = sortedTimers.get(j);
                if (timer.initial() % otherTimer.initial() == 0) {
                    multiple = true;
                    break;
                }
            }
            if (multiple) {
                continue;
            }

            return i; // not a multiple and within time
        }

        throw new IllegalStateException("Max. initial value is too low; must include at least one timer.");
    }


    /**
     * Constructs an MMLT hypothesis.
     * This updates all transition outputs, if required.
     *
     * @return MMLT hypothesis
     */
    public MMLT<Integer, I, ?, O> getHypothesisModel() {
        return getInternalLocalTimerMealyHypothesis();
    }

    /**
     * Like the construction above, but returns an LocalTimerMealyHypothesis object instead.
     * This objects provides additional functions that are just intended for the learner but not the teacher.
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

                    Word<TimedInput<I>> prefix = row.getLabel().prefix(-1);
                    TimedInput<I> inputSym = row.getLabel().suffix(1).lastSymbol();

                    TimedOutput<O> output = null;
                    if (inputSym instanceof TimeStepSequence<I> ws) {
                        // Query timer output from table:
                        TimerInfo<?, O> timerInfo = this.hypData.getTable().getTimerInfo(prefix, ws.timeSteps());
                        if (timerInfo == null) {
                            throw new AssertionError();
                        }
                        output = new TimedOutput<>(timerInfo.output());
                    } else {
                        output = this.timeOracle.answerQuery(prefix, Word.fromLetter(inputSym)).lastSymbol();
                    }

                    if (output != null) {
                        this.hypData.getTransitionOutputMap().put(row.getLabel(), output);
                    }
                });
    }

    // ==========================

    @Override
    public void startLearning() {
        List<List<Row<TimedInput<I>>>> initialUnclosed = this.hypData.getTable().initialize(Collections.emptyList(), this.initialSuffixes, timeOracle);

        // Ensure that closed:
        this.completeConsistentTable(initialUnclosed);
    }

    @Override
    public boolean refineHypothesis(DefaultQuery<TimedInput<I>, Word<TimedOutput<O>>> ceQuery) {
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
    private MMLTOutputInconsistency<I, O> toOutputInconsistency(DefaultQuery<TimedInput<I>, Word<TimedOutput<O>>> ceQuery, MMLTHypothesis<I, O> hypothesis) {
        // 1. Cut example after first deviation:
        DefaultQuery<TimedInput<I>, Word<TimedOutput<O>>> shortQuery = MealyUtil.shortenCounterExample(hypothesis.getSemantics(), ceQuery);
        if (shortQuery == null) {
            return null;
        }

        // 2. Calculate shortened hypothesis output:
        var shortHypOutput = hypothesis.getSemantics().computeSuffixOutput(shortQuery.getPrefix(), shortQuery.getSuffix());
        if (shortHypOutput.equals(shortQuery.getOutput())) {
            throw new AssertionError("Deviation lost after shortening.");
        }

        return new MMLTOutputInconsistency<>(shortQuery.getPrefix(),
                shortQuery.getSuffix(),
                shortQuery.getOutput(),
                shortHypOutput);
    }

    private boolean refineHypothesisSingle(DefaultQuery<TimedInput<I>, Word<TimedOutput<O>>> ceQuery) {
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
        if (analysisResult instanceof MissingDiscriminatorResult<I, O> locSplit) {
            stats.increaseCounter("INACC_MISSING_DISC",
                    "Inaccuracies: missing discriminators");

            // Add new discriminator as suffix:
            if (hypData.getTable().getSuffixes().contains(locSplit.getDiscriminator())) throw new AssertionError();
            List<Word<TimedInput<I>>> suffixes = Collections.singletonList(locSplit.getDiscriminator());
            var unclosed = hypData.getTable().addSuffixes(suffixes, timeOracle);

            // Close transitions:
            this.completeConsistentTable(unclosed); // no consistency check for RS
        } else if (analysisResult instanceof MissingResetResult<I, O> noReset) {
            stats.increaseCounter("INACC_MISSING_RESETS",
                    "Inaccuracies: missing resets");

            // Add missing reset:
            var resetTrans = hypothesis.getPrefix(noReset.getLocation()).append(noReset.getInput());
            this.hypData.getTransitionResetSet().add(resetTrans);
        } else if (analysisResult instanceof MissingOneShotResult<I, O> noAperiodic) {
            stats.increaseCounter("INACC_MISSING_OS",
                    "Inaccuracies: missing one-shot timers");

            // Identify corresponding sp row:
            Word<TimedInput<I>> locPrefix = hypothesis.getPrefix(noAperiodic.getLocation());
            Row<TimedInput<I>> spRow = hypData.getTable().getRow(locPrefix);
            if (spRow == null || !spRow.isShortPrefixRow()) {
                throw new AssertionError();
            }

            this.handleMissingTimeoutChange(spRow, noAperiodic.getTimeout());
        } else if (analysisResult instanceof FalseIgnoreResult<I, O> falseIgnore) {
            stats.increaseCounter("INACC_MISSING_FI",
                    "Inaccuracies: false ignores");

            if (this.symbolFilter == null) {
                throw new AssertionError("Cannot detect false ignores without symbol filter.");
            }

            // Identify corresponding sp row:
            Word<TimedInput<I>> locPrefix = hypothesis.getPrefix(falseIgnore.getLocation());
            Row<TimedInput<I>> spRow = hypData.getTable().getRow(locPrefix);
            if (spRow == null || !spRow.isShortPrefixRow()) {
                throw new AssertionError();
            }

            // Update filter:
            this.symbolFilter.accept(locPrefix, falseIgnore.getSymbol());

            // Legalize symbol + close table:
            var unclosed = hypData.getTable().addOutgoingTransition(spRow, falseIgnore.getSymbol(), this.timeOracle);
            stats.increaseCounter("Count_legalized", "Legalized symbols");

            this.completeConsistentTable(unclosed);
        } else {
            throw new IllegalStateException("Unknown inconsistency type.");
        }

        return true;
    }

    private void handleMissingTimeoutChange(Row<TimedInput<I>> spRow, TimerInfo<?, O> timeout) {
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
        Word<TimedInput<I>> timerTransPrefix = spRow.getLabel().append(new TimeStepSequence<>(timeout.initial()));
        if (this.hypData.getTable().getRow(timerTransPrefix) != null) {
            throw new AssertionError("Timer already appears to be one-shot.");
        }

        // Remove all timers with greater timeout (are now redundant):
        var subsequentTimers = locationTimerInfo.getSortedTimers().stream()
                .filter(t -> t.initial() > timeout.initial())
                .map(TimerInfo::name).toList();
        subsequentTimers.forEach(locationTimerInfo::removeTimer);

        // Change from periodic to one-shot:
        locationTimerInfo.setOneShotTimer(timeout.name());

        // Update fringe prefixes + close table:
        List<List<Row<TimedInput<I>>>> unclosed = this.hypData.getTable().addTimerTransition(spRow, timeout, this.timeOracle);
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
     * @param unclosed the unclosed rows (equivalence classes) to start with.
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
        for (var spRow : hypData.getTable().getShortPrefixRows()) {
            if (locationContentIdMap.containsKey(spRow.getRowContentId())) {
                // Multiple sp rows may have same contentID. Thus, assign each id one location:
                continue;
            }
            locationContentIdMap.put(spRow.getRowContentId(), spRow);
        }

        // 2. Create untimed alphabet:
        GrowingMapAlphabet<I> alphabet = new GrowingMapAlphabet<>();
        for (var symbol : hypData.getAlphabet()) {
            if (symbol instanceof InputSymbol<I> ndi) {
                alphabet.add(ndi.symbol());
            }
        }

        // 3. Prepare objects for automaton, timers and resets:
        int numLocations = hypData.getTable().numberOfShortPrefixRows();
        final Map<Integer, Integer> stateMap = new HashMap<>(HashUtil.capacity(numLocations)); // row content id -> state id
        final Map<Integer, Word<TimedInput<I>>> prefixMap = new HashMap<>(HashUtil.capacity(numLocations)); // state id -> location prefix
        var hypothesis = new MMLTHypothesis<>(alphabet, numLocations, hypData.getModelParams().silentOutput(), hypData.getModelParams().outputCombiner(), prefixMap); // we pass the prefix map as reference so that we can fill it later

        // 4. Create one state per location:
        for (var row : hypData.getTable().getShortPrefixRows()) {
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
        for (var rowContentId : stateMap.keySet()) {
            Row<TimedInput<I>> spLocation = locationContentIdMap.get(rowContentId);

            for (var symbol : alphabet) {
                int symIdx = hypData.getAlphabet().getSymbolIndex(TimedInput.input(symbol));

                var transOutput = hypData.getTransitionOutput(spLocation, symIdx);
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
                int sourceLocId = stateMap.get(rowContentId);
                int successorLocId = stateMap.get(successorId);
                hypothesis.addTransition(sourceLocId, symbol, successorLocId, output);

                // Check for local reset:
                var targetTransition = spLocation.getLabel().append(TimedInput.input(symbol));
                if (hypData.getTransitionResetSet().contains(targetTransition) && sourceLocId == successorLocId) {
                    hypothesis.addLocalReset(sourceLocId, symbol);
                }
            }

        }

        // 6. Add timeout transitions:
        for (var rowContentId : stateMap.keySet()) {
            Row<TimedInput<I>> spLocation = locationContentIdMap.get(rowContentId);

            var timerInfo = hypData.getTable().getLocationTimerInfo(spLocation);
            if (timerInfo == null) {
                continue; // no timers
            }

            for (var timer : timerInfo.getLocalTimers().values()) {
                if (timer.periodic()) {
                    hypothesis.addPeriodicTimer(stateMap.get(rowContentId), timer.name(), timer.initial(), timer.output());
                } else {
                    // One-shot: use successor from table
                    TimedInput<I> symbol = new TimeStepSequence<>(timer.initial());

                    int symIdx = hypData.getAlphabet().getSymbolIndex(symbol);
                    int successorId = spLocation.getSuccessor(symIdx).getRowContentId();

                    hypothesis.addOneShotTimer(stateMap.get(rowContentId), timer.name(), timer.initial(), timer.output(), stateMap.get(successorId));
                }
            }
        }

        return hypothesis;
    }

}
