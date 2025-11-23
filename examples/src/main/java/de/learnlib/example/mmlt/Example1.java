package de.learnlib.example.mmlt;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.learnlib.algorithm.lstar.mmlt.ExtensibleLStarMMLT;
import de.learnlib.datastructure.observationtable.writer.ObservationTableASCIIWriter;
import de.learnlib.driver.simulator.MMLTSimulatorSUL;
import de.learnlib.filter.SymbolFilter;
import de.learnlib.filter.cache.mmlt.TimedSULTreeCache;
import de.learnlib.filter.cache.mmlt.TimeoutReducerSUL;
import de.learnlib.filter.statistic.oracle.CounterEQOracle;
import de.learnlib.filter.statistic.sul.CounterTimedSUL;
import de.learnlib.oracle.EquivalenceOracle.MMLTEquivalenceOracle;
import de.learnlib.oracle.equivalence.MMLTEQOracleChain;
import de.learnlib.oracle.equivalence.mmlt.RandomWpEQOracle;
import de.learnlib.oracle.equivalence.mmlt.ResetSearchEQOracle;
import de.learnlib.oracle.equivalence.mmlt.SimulatorEQOracle;
import de.learnlib.oracle.membership.TimedSULOracle;
import de.learnlib.filter.symbol.CachedSymbolFilter;
import de.learnlib.algorithm.lstar.mmlt.filter.MMLTRandomSymbolFilter;
import de.learnlib.algorithm.lstar.mmlt.filter.MMLTStatisticsSymbolFilter;
import de.learnlib.query.DefaultQuery;
import de.learnlib.statistic.Statistics;
import de.learnlib.statistic.StatisticsCollector;
import de.learnlib.testsupport.example.mmlt.MMLTExamples;
import net.automatalib.automaton.visualization.MMLTVisualizationHelper;
import net.automatalib.symbol.time.InputSymbol;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.symbol.time.TimedOutput;
import net.automatalib.symbol.time.TimeoutSymbol;
import net.automatalib.visualization.Visualization;
import net.automatalib.word.Word;

/**
 * This example shows how to learn a Mealy machine with local timers,
 * an automaton model for real-time systems.
 */
public class Example1 {

    public static void main(String[] args) {
        var model = MMLTExamples.sensorCollector();

        // We first create a statistics container.
        // This container will store various statistical data during learning:
        var stats = Statistics.getCollector();
        stats.addText("LocalTimerMealyModel", null, model.toString());
        stats.setCounter("original_locs", "Locations in original", model.getReferenceAutomaton().getStates().size());
        stats.setCounter("original_inputs", "Untimed alphabet size in original", model.getReferenceAutomaton().getInputAlphabet().size());

        // ======================
        // Set up the pipeline:
        // We use a simulator SUL to simulate our automaton:
        var sul = new MMLTSimulatorSUL<>(model.getReferenceAutomaton().getSemantics());

        // We count all operations that are performed on the SUL with a stats-SUL:
        var statsAfterCache = new CounterTimedSUL<>(sul);

        // We use a cache to avoid redundant operations:
        var cacheSUL = new TimedSULTreeCache<>(statsAfterCache, model.getParams());
        var toReducerSul = new TimeoutReducerSUL<>(cacheSUL, model.getParams().maxTimeoutWaitingTime());

        // We use a query oracle to answer queries from the learner:
        var timeOracle = new TimedSULOracle<>(toReducerSul, model.getParams());

        // We use a chain of different equivalence oracles:
        MMLTEQOracleChain<String, String> chainOracle = new MMLTEQOracleChain<>();
        chainOracle.addOracle(new CounterEQOracle<>(cacheSUL.createCacheConsistencyTest(), "cache"));
        chainOracle.addOracle(new CounterEQOracle<>(new ResetSearchEQOracle<>(timeOracle, 100, 1.0, 1.0), "reset"));
        chainOracle.addOracle(new CounterEQOracle<>(new RandomWpEQOracle<>(timeOracle, 100, 16, 0, 100), "wp"));
        chainOracle.addOracle(new CounterEQOracle<>(new SimulatorEQOracle<>(model.getReferenceAutomaton()), "sim")); // ensure that we eventually find an accurate model

        // Set up our L* learner:
        List<Word<TimedInput<String>>> suffixes = new ArrayList<>();
        model.getReferenceAutomaton().getInputAlphabet().forEach(s -> suffixes.add(Word.fromLetter(TimedInput.input(s))));
        suffixes.add(Word.fromLetter(new TimeoutSymbol<>()));

        // A symbol filter allows us to reduce queries by exploiting prior knowledge.
        // For this example, we use a AbstractRandomSymbolFilter. This filter correctly predicts
        // whether a transition silently self-loops with an accuracy of 90%:
        SymbolFilter<TimedInput<String>, InputSymbol<String>> filter =
                new MMLTRandomSymbolFilter<>(model.getReferenceAutomaton(), 0.1, new Random(100));

        filter = new MMLTStatisticsSymbolFilter<>(model.getReferenceAutomaton(), filter, stats);
        var cachedFilter = new CachedSymbolFilter<>(filter); // need to wrap to enable updates to responses

        var learner = new ExtensibleLStarMMLT<>(model.getReferenceAutomaton().getInputAlphabet(), model.getParams(), suffixes, timeOracle, cachedFilter);

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

    private static void runExperiment(ExtensibleLStarMMLT<String, String> learner,
                                      MMLTEquivalenceOracle<String, String> tester,
                                      StatisticsCollector statisticsCollector, int maxRounds) {
        statisticsCollector.startOrResumeClock("learningRt", "Processing time");
        learner.startLearning();

        var hyp = learner.getHypothesisModel();
        DefaultQuery<TimedInput<String>, Word<TimedOutput<String>>> cex = tester.findCounterExample(hyp, hyp.getSemantics().getInputAlphabet());
        statisticsCollector.increaseCounter("roundCount", "CEX queries");

        int roundCount = 1;
        while (cex != null && roundCount < maxRounds) {
            learner.refineHypothesis(cex);
            hyp = learner.getHypothesisModel();
            cex = tester.findCounterExample(hyp, hyp.getSemantics().getInputAlphabet());
            statisticsCollector.increaseCounter("roundCount", null);
            roundCount += 1;
        }
        statisticsCollector.pauseClock("learningRt");

        final var finalHypothesis = learner.getHypothesisModel();

        // Add some more stats:
        statisticsCollector.setCounter("result_locs", "Locations in result", finalHypothesis.getStates().size());

        // Print final result + statistics:
        System.out.println(statisticsCollector.printStats());

        new ObservationTableASCIIWriter<>().write(learner.getObservationTable(), System.out);

        System.out.println("Final hypothesis:");
        Visualization.visualize(finalHypothesis.graphView(), new MMLTVisualizationHelper<>(finalHypothesis, true, true));
    }
}
