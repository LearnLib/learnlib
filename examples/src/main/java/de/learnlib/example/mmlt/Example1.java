package de.learnlib.example.mmlt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.learnlib.algorithm.lstar.mmlt.LStarLocalTimerMealy;
import de.learnlib.datastructure.observationtable.writer.ObservationTableASCIIWriter;
import de.learnlib.driver.simulator.LocalTimerMealySimulatorSUL;
import de.learnlib.filter.cache.mmlt.LocalTimerMealyTreeSULCache;
import de.learnlib.filter.cache.mmlt.TimeoutReducerSUL;
import de.learnlib.filter.statistic.sul.LocalTimerMealyStatsSUL;
import de.learnlib.oracle.EquivalenceOracle;
import de.learnlib.oracle.equivalence.mmlt.LocalTimerMealyEQOracleChain;
import de.learnlib.oracle.equivalence.mmlt.LocalTimerMealyRandomWpOracle;
import de.learnlib.oracle.equivalence.mmlt.LocalTimerMealySimulatorOracle;
import de.learnlib.oracle.equivalence.mmlt.ResetSearchOracle;
import de.learnlib.oracle.membership.TimedQueryOracle;
import de.learnlib.oracle.symbol_filters.CachedSymbolFilter;
import de.learnlib.oracle.symbol_filters.mmlt.LocalTimerMealyRandomSymbolFilter;
import de.learnlib.oracle.symbol_filters.mmlt.LocalTimerMealyStatisticsSymbolFilter;
import de.learnlib.query.DefaultQuery;
import de.learnlib.statistic.container.StatsContainer;
import de.learnlib.symbol_filter.SymbolFilter;
import de.learnlib.testsupport.example.mmlt.LocalTimerMealyExamples;
import de.learnlib.util.statistic.container.MapStatsContainer;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.GrowingMapAlphabet;
import net.automatalib.automaton.visualization.MMLTVisualizationHelper;
import net.automatalib.serialization.dot.GraphDOT;
import net.automatalib.symbol.time.InputSymbol;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.symbol.time.TimedOutput;
import net.automatalib.symbol.time.TimeoutSymbol;
import net.automatalib.word.Word;

/**
 * This example shows how to learn a Mealy machine with local timers,
 * an automaton model for real-time systems.
 */
public class Example1 {

    public static void main(String[] args) {
        var model = LocalTimerMealyExamples.SensorCollector();

        // We first create a statistics container.
        // This container will store various statistical data during learning:
        var stats = new MapStatsContainer();
        stats.addTextInfo("LocalTimerMealyModel", null, model.name());
        stats.setCounter("original_locs", "Locations in original", model.automaton().getStates().size());
        stats.setCounter("original_inputs", "Untimed alphabet size in original", model.automaton().getInputAlphabet().size());

        // ======================
        // Set up the pipeline:
        // We use a simulator SUL to simulate our automaton:
        var sul = new LocalTimerMealySimulatorSUL<>(model.automaton().getSemantics());

        // We count all operations that are performed on the SUL with a stats-SUL:
        var statsAfterCache = new LocalTimerMealyStatsSUL<>(sul, stats);

        // We use a cache to avoid redundant operations:
        var cacheSUL = new LocalTimerMealyTreeSULCache<>(statsAfterCache, model.params());
        cacheSUL.setStatsContainer(stats);
        var toReducerSul = new TimeoutReducerSUL<>(cacheSUL, model.params().maxTimeoutWaitingTime(), stats);

        // We use a query oracle to answer queries from the learner:
        var timeOracle = new TimedQueryOracle<>(toReducerSul, model.params());

        // We use a chain of different equivalence oracles:
        LocalTimerMealyEQOracleChain<String, String> chainOracle = new LocalTimerMealyEQOracleChain<>();
        chainOracle.addOracle(cacheSUL.createCacheConsistencyTest());
        chainOracle.addOracle(new ResetSearchOracle<>(timeOracle, 100, 1.0, 1.0));
        chainOracle.addOracle(new LocalTimerMealyRandomWpOracle<>(timeOracle, 100, 16, 0, 100));
        chainOracle.addOracle(new LocalTimerMealySimulatorOracle<>(model.automaton())); // ensure that we eventually find an accurate model
        chainOracle.setStatsContainer(stats);

        // Set up our L* learner:
        List<Word<TimedInput<String>>> suffixes = new ArrayList<>();
        model.automaton().getInputAlphabet().forEach(s -> suffixes.add(Word.fromLetter(TimedInput.input(s))));
        suffixes.add(Word.fromLetter(new TimeoutSymbol<>()));

        // A symbol filter allows us to reduce queries by exploiting prior knowledge.
        // For this example, we use a RandomSymbolFilter. This filter correctly predicts
        // whether a transition silently self-loops with an accuracy of 90%:
        SymbolFilter<TimedInput<String>, InputSymbol<String>> filter =
                new LocalTimerMealyRandomSymbolFilter<>(model.automaton(), 0.1, new Random(100));

        filter = new LocalTimerMealyStatisticsSymbolFilter<>(model.automaton(), filter, stats);
        filter = new CachedSymbolFilter<>(filter); // need to wrap to enable updates to responses

        var learner = new LStarLocalTimerMealy<>(model.automaton().getInputAlphabet(), model.params(), suffixes, timeOracle, filter);
        learner.setStatsContainer(stats);

        // Start learning:
        runExperiment(learner, chainOracle, stats, 100);

        // Troubleshooting
        // If you attempt to learn a model of some application and the learner
        // throws assertion errors or illegal state exceptions,
        // your SUL likely has no MMLT semantics.
        // In this case, you can try to learn a partial model by excluding TimeStepSymbol
        // from the input alphabet for the counterexample search:
        // Replace tester.findCounterExample(hyp, hyp.getSemantics().getInputAlphabet());
        // with: tester.findCounterExample(hyp, hyp.getSemantics().getInputAlphabet().stream().filter(s -> !(s instanceof TimeStepSymbol<String>)).toList());
    }

    private static void runExperiment(LStarLocalTimerMealy<String, String> learner,
                                      EquivalenceOracle.LocalTimerMealyEquivalenceOracle<String, String> tester,
                                      StatsContainer stats, int maxRounds) {
        stats.startOrResumeClock("learningRt", "Processing time");
        learner.startLearning();

        var hyp = learner.getHypothesisModel();
        DefaultQuery<TimedInput<String>, Word<TimedOutput<String>>> cex = tester.findCounterExample(hyp, hyp.getSemantics().getInputAlphabet());
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

        // Print final result + statistics:
        stats.printStats();

        System.out.println("Final hypothesis:");
        try {
            GraphDOT.write(finalHypothesis.graphView(), System.out,
                    new MMLTVisualizationHelper<>(finalHypothesis, true, true));
        } catch (IOException ignored) {
        }
        new ObservationTableASCIIWriter<>().write(learner.getObservationTable(), System.out);

    }
}
