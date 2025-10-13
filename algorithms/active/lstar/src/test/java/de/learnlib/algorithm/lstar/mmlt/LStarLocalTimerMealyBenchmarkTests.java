package de.learnlib.algorithm.lstar.mmlt;


import de.learnlib.algorithm.LocalTimerMealyModelParams;
import de.learnlib.driver.simulator.LocalTimerMealySimulatorSUL;
import de.learnlib.filter.cache.mmlt.TimeoutReducerSUL;
import de.learnlib.filter.statistic.sul.LocalTimerMealyStatsSUL;
import de.learnlib.oracle.EquivalenceOracle;
import de.learnlib.oracle.equivalence.mmlt.LocalTimerMealyEQOracleChain;
import de.learnlib.oracle.equivalence.mmlt.LocalTimerMealyRandomWpOracle;
import de.learnlib.oracle.equivalence.mmlt.LocalTimerMealySimulatorOracle;
import de.learnlib.oracle.equivalence.mmlt.ResetSearchOracle;
import de.learnlib.oracle.membership.TimedQueryOracle;
import de.learnlib.oracle.symbol_filters.AcceptAllSymbolFilter;
import de.learnlib.oracle.symbol_filters.CachedSymbolFilter;
import de.learnlib.oracle.symbol_filters.IgnoreAllSymbolFilter;
import de.learnlib.oracle.symbol_filters.mmlt.*;
import de.learnlib.query.DefaultQuery;
import de.learnlib.statistic.container.StatsContainer;
import de.learnlib.sul.LocalTimerMealySUL;
import de.learnlib.symbol_filter.SymbolFilter;
import de.learnlib.testsupport.example.mmlt.LocalTimerMealyExamples;
import de.learnlib.util.statistic.container.MapStatsContainer;
import net.automatalib.alphabet.impl.GrowingMapAlphabet;
import net.automatalib.alphabet.time.mmlt.LocalTimerMealyOutputSymbol;
import net.automatalib.alphabet.time.mmlt.LocalTimerMealySemanticInputSymbol;
import net.automatalib.alphabet.time.mmlt.NonDelayingInput;
import net.automatalib.alphabet.time.mmlt.TimeoutSymbol;
import net.automatalib.automaton.time.mmlt.LocalTimerMealy;
import net.automatalib.word.Word;
import org.testng.annotations.Test;

import de.learnlib.filter.cache.mmlt.LocalTimerMealyTreeSULCache;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Integration tests for the MMLT learner that uses several EQ oracles, symbol filters
 * and a cache to learn different models.
 */
@Test
public class LStarLocalTimerMealyBenchmarkTests {

    private enum FilterMode {
        none, random, ignore_all, perfect
    }

    private static <I, O> void runExperiment(LStarLocalTimerMealy<I, O> learner, EquivalenceOracle.LocalTimerMealyEquivalenceOracle<I, O> tester, StatsContainer stats, int maxRounds,
                                             boolean printFinalResult) {
        stats.startOrResumeClock("learningRt", "Processing time");
        learner.startLearning();

        var hyp = learner.getHypothesisModel();
        DefaultQuery<LocalTimerMealySemanticInputSymbol<I>, Word<LocalTimerMealyOutputSymbol<O>>> cex = tester.findCounterExample(hyp, hyp.getSemantics().getInputAlphabet());
        stats.increaseCounter("roundCount", "CEX queries");

        int roundCount = 1;
        while (cex != null && roundCount < maxRounds) {
            learner.refineHypothesis(cex);
            hyp = learner.getHypothesisModel();
            cex = tester.findCounterExample(hyp, hyp.getSemantics().getInputAlphabet());
            stats.increaseCounter("roundCount", null);
            roundCount += 1;
        }
        stats.pauseClock("learningRt");

        final var finalHypothesis = learner.getHypothesisModel();

        // Add some more stats:
        stats.setCounter("result_locs", "Locations in result", finalHypothesis.getStates().size());

        // Print stats:
        stats.printStats();

        if (printFinalResult) {
            System.out.println("Final hypothesis:");
            LocalTimerMealyTestUtil.printModel(finalHypothesis);
            //new ObservationTableASCIIWriter<>().write(learner.getObservationTable(), System.out);
        }
    }

    private static <S, I, O> void learnModel(String name, LocalTimerMealy<S, I, O> automaton, LocalTimerMealyModelParams<O> params,
                                             FilterMode symbolFilterMode, long seed, boolean printResults) {

        // Add some stats:
        var stats = new MapStatsContainer();
        stats.addTextInfo("LocalTimerMealyModel", null, name);
        stats.setCounter("original_locs", "Locations in original", automaton.getStates().size());
        stats.setCounter("original_inputs", "Untimed alphabet size in original", automaton.getUntimedAlphabet().size());

        // Set up a pipeline:
        GrowingMapAlphabet<LocalTimerMealySemanticInputSymbol<I>> alphabet = new GrowingMapAlphabet<>();
        alphabet.addAll(automaton.getUntimedAlphabet());

        // Query oracle -> TimeoutReducer -> Cache -> Query stats -> SUL
        LocalTimerMealySimulatorSUL<S, I, O> sul = new LocalTimerMealySimulatorSUL<>(automaton);
        LocalTimerMealyStatsSUL<I, O> statsAfterCache = new LocalTimerMealyStatsSUL<>(sul, stats);
        LocalTimerMealyTreeSULCache<I, O> cacheSUL = new LocalTimerMealyTreeSULCache<>(statsAfterCache, params);
        cacheSUL.setStatsContainer(stats);
        LocalTimerMealySUL<I, O> toReducerSul = new TimeoutReducerSUL<>(cacheSUL, params.maxTimeoutWaitingTime(), stats);

        TimedQueryOracle<I, O> timeOracle = new TimedQueryOracle<>(toReducerSul, params);

        // Prepare cex oracle chain:

        LocalTimerMealyEQOracleChain<I, O> chainOracle = new LocalTimerMealyEQOracleChain<>();
        chainOracle.addOracle(cacheSUL.createCacheConsistencyTest());
        chainOracle.addOracle(new ResetSearchOracle<>(timeOracle, seed, 1.0, 1.0));
        chainOracle.addOracle(new LocalTimerMealyRandomWpOracle<>(timeOracle, seed, 6, 12, 100));
        chainOracle.addOracle(new LocalTimerMealySimulatorOracle<>(automaton)); // ensure that we eventually find an accurate model
        chainOracle.setStatsContainer(stats);

        // Create learner:
        List<Word<LocalTimerMealySemanticInputSymbol<I>>> suffixes = new ArrayList<>();
        alphabet.forEach(s -> suffixes.add(Word.fromLetter(s)));
        suffixes.add(Word.fromLetter(new TimeoutSymbol<>()));

        // Configure symbol filter:
        SymbolFilter<LocalTimerMealySemanticInputSymbol<I>, NonDelayingInput<I>> filter = new AcceptAllSymbolFilter<>(); // pass-through
        switch (symbolFilterMode) {
            case perfect -> filter = new LocalTimerMealyPerfectSymbolFilter<>(automaton);
            case random -> filter = new LocalTimerMealyRandomSymbolFilter<>(automaton, 0.1, new Random(seed));
            case ignore_all -> filter = new IgnoreAllSymbolFilter<>();
        }

        filter = new LocalTimerMealyStatisticsSymbolFilter<>(automaton, filter, stats);
        filter = new CachedSymbolFilter<>(filter); // need to wrap to enable updates to responses

        var learner = new LStarLocalTimerMealy<>(alphabet, params, suffixes, timeOracle, filter);
        learner.setStatsContainer(stats);

        // Start learning:
        runExperiment(learner, chainOracle, stats, 100, printResults);
    }


    @Test
    public void learnExamplesNoFilter() {
        for (String modelFile : LocalTimerMealyTestUtil.listModelFiles()) {
            var model = LocalTimerMealyTestUtil.automatonFromFile(modelFile);
            learnModel(model.name(), model.automaton(), model.params(), FilterMode.none, 100, true);
        }
        LocalTimerMealyExamples.getAll().forEach(m ->
                learnModel(m.name(), m.automaton(), m.params(), FilterMode.none, 100, true));
    }

    @Test
    public void learnExamplesIgnoreAllFilter() {
        for (String modelFile : LocalTimerMealyTestUtil.listModelFiles()) {
            var model = LocalTimerMealyTestUtil.automatonFromFile(modelFile);
            learnModel(modelFile, model.automaton(), model.params(), FilterMode.ignore_all, 100, true);
        }
        LocalTimerMealyExamples.getAll().forEach(m ->
                learnModel(m.name(), m.automaton(), m.params(), FilterMode.ignore_all, 100, true));
    }

    @Test
    public void learnExamplesPerfectFilter() {
        for (String modelFile : LocalTimerMealyTestUtil.listModelFiles()) {
            var model = LocalTimerMealyTestUtil.automatonFromFile(modelFile);
            learnModel(modelFile, model.automaton(), model.params(), FilterMode.perfect, 100, true);
        }
        LocalTimerMealyExamples.getAll().forEach(m ->
                learnModel(m.name(), m.automaton(), m.params(), FilterMode.perfect, 100, true));
    }

    @Test
    public void learnExamplesRandomFilter() {
        for (String modelFile : LocalTimerMealyTestUtil.listModelFiles()) {
            var model = LocalTimerMealyTestUtil.automatonFromFile(modelFile);
            learnModel(modelFile, model.automaton(), model.params(), FilterMode.random, 100, true);
        }
        LocalTimerMealyExamples.getAll().forEach(m ->
                learnModel(m.name(), m.automaton(), m.params(), FilterMode.random, 100, true));
    }

}
