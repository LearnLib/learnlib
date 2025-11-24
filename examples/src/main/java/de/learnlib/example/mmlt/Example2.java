package de.learnlib.example.mmlt;

import de.learnlib.algorithm.lstar.mmlt.ExtensibleLStarMMLT;
import de.learnlib.algorithm.lstar.mmlt.filter.MMLTRandomSymbolFilter;
import de.learnlib.algorithm.lstar.mmlt.filter.MMLTStatisticsSymbolFilter;
import de.learnlib.datastructure.observationtable.writer.ObservationTableASCIIWriter;
import de.learnlib.driver.simulator.MMLTSimulatorSUL;
import de.learnlib.filter.SymbolFilter;
import de.learnlib.filter.cache.mmlt.TimedSULTreeCache;
import de.learnlib.filter.cache.mmlt.TimeoutReducerSUL;
import de.learnlib.filter.statistic.oracle.CounterEQOracle;
import de.learnlib.filter.statistic.sul.CounterTimedSUL;
import de.learnlib.filter.symbol.CachedSymbolFilter;
import de.learnlib.oracle.EquivalenceOracle.MMLTEquivalenceOracle;
import de.learnlib.oracle.equivalence.MMLTEQOracleChain;
import de.learnlib.oracle.equivalence.mmlt.RandomWpMethodEQOracle;
import de.learnlib.oracle.equivalence.mmlt.ResetSearchEQOracle;
import de.learnlib.oracle.equivalence.mmlt.SimulatorEQOracle;
import de.learnlib.oracle.membership.TimedSULOracle;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static de.learnlib.example.mmlt.ExampleUtil.runExperiment;

/**
 * This example shows a basic set-up of the MMLT-learner for a black-box setting.
 * <p>
 * For this, we use a chain of different equivalence oracles
 * that can be applied if the reference automaton is not known.
 */
public class Example2 {

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

        // We use a chain of different equivalence oracles to find counterexamples more efficiently:
        MMLTEQOracleChain<String, String> chainOracle = new MMLTEQOracleChain<>();
        // A cache oracle tests if the current hypothesis and the reference automaton give the same outputs
        // for all words that have already been queried. As the words have already been queried, this
        // executes no additional queries on the SUL:
        chainOracle.addOracle(new CounterEQOracle<>(cacheSUL.createCacheConsistencyTest(), "cache"));

        // A ResetSearchOracle tests for missing local resets, which often require many and/or long test words
        // when using random-based testing. We configure the tester to consider all transitions that might cause a reset:
        chainOracle.addOracle(new CounterEQOracle<>(new ResetSearchEQOracle<>(timeOracle, 100, 1.0, 1.0), "reset"));

        // Finally, we add an MMLT-specific RandomWp oracle:
        chainOracle.addOracle(new CounterEQOracle<>(new RandomWpMethodEQOracle<>(timeOracle, 100, 16, 0, 100), "wp"));

        // Set up our L* learner:
        List<Word<TimedInput<String>>> suffixes = new ArrayList<>();
        model.getReferenceAutomaton().getInputAlphabet().forEach(s -> suffixes.add(Word.fromLetter(TimedInput.input(s))));
        suffixes.add(Word.fromLetter(new TimeoutSymbol<>()));

        var learner = new ExtensibleLStarMMLT<>(model.getReferenceAutomaton().getInputAlphabet(), model.getParams(), suffixes, timeOracle);

        // Start learning:
        var finalModel = runExperiment(learner, chainOracle, stats, 100);

        // In this set-up, we actually know the reference automaton.
        // This allows us to check that we learned an accurate model:
        var simOracle = new SimulatorEQOracle<>(model.getReferenceAutomaton());
        if (simOracle.findCounterExample(finalModel, finalModel.getSemantics().getInputAlphabet()) != null) {
            throw new AssertionError("Incorrect model learned.");
        }

        // Troubleshooting
        // If you attempt to learn a model of some application and the learner
        // throws assertion errors or illegal state exceptions,
        // your SUL likely has no MMLT semantics.
        // In this case, you can try to learn a partial model by excluding TimeStepSymbol
        // from the input alphabet for the counterexample search:
        // In your learn-loop (see ExampleUtil), replace
        // tester.findCounterExample(hyp, hyp.getSemantics().getInputAlphabet());
        // with: tester.findCounterExample(hyp, hyp.getSemantics().getInputAlphabet().stream().filter(s -> !(s instanceof TimeStepSymbol<String>)).toList());
    }

}
