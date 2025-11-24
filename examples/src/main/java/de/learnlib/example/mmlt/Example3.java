package de.learnlib.example.mmlt;

import de.learnlib.algorithm.lstar.mmlt.ExtensibleLStarMMLT;
import de.learnlib.algorithm.lstar.mmlt.filter.MMLTRandomSymbolFilter;
import de.learnlib.algorithm.lstar.mmlt.filter.MMLTStatisticsSymbolFilter;
import de.learnlib.driver.simulator.MMLTSimulatorSUL;
import de.learnlib.filter.SymbolFilter;
import de.learnlib.filter.cache.mmlt.TimedSULTreeCache;
import de.learnlib.filter.cache.mmlt.TimeoutReducerSUL;
import de.learnlib.filter.statistic.oracle.CounterEQOracle;
import de.learnlib.filter.statistic.sul.CounterTimedSUL;
import de.learnlib.filter.symbol.CachedSymbolFilter;
import de.learnlib.oracle.equivalence.MMLTEQOracleChain;
import de.learnlib.oracle.equivalence.mmlt.RandomWpMethodEQOracle;
import de.learnlib.oracle.equivalence.mmlt.ResetSearchEQOracle;
import de.learnlib.oracle.equivalence.mmlt.SimulatorEQOracle;
import de.learnlib.oracle.membership.TimedSULOracle;
import de.learnlib.statistic.Statistics;
import de.learnlib.testsupport.example.mmlt.MMLTExamples;
import net.automatalib.symbol.time.InputSymbol;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.symbol.time.TimeoutSymbol;
import net.automatalib.word.Word;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static de.learnlib.example.mmlt.ExampleUtil.runExperiment;

/**
 * This example illustrates how to learn MMLTs with symbol filtering.
 * <p>
 * A symbol filter is a component that provides information about transitions that might silently self-loop.
 * The learner exploits this information to avoid redundant queries on the SUL.
 * The symbol filter might incorrectly classify a transition as silent self-loop.
 * The MMLT-learner detects and corrects such errors.
 * <p>
 * LearnLib includes four types of symbol filter:
 * <ul>
 * <li> AcceptAllSymbolFilter: no transition is considered as silent self-loop.
 * This is the default behavior if no filter is provided. </li>
 * <li> PerfectSymbolFilter: simulates perfect knowledge of silent self-loops.
 * Perfect knowledge is useful for benchmarking but rarely the case in practice. </li>
 * <li> IgnoreAllSymbolFilter: considers all transitions to be silent self-loops.
 * If no knowledge of the SUL is available, this filter still often yields strong performance improvements.</li>
 * <li>RandomSymbolFilter: simulates incorrect responses with a certain percentage.</li>
 * </ul>
 * <p>
 * When you apply MMLT-learning in practice, you usually want to implement your own symbol filter that exploits specific
 * domain knowledge.
 */
public class Example3 {

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

        // We use a chain of different equivalence oracles (see Example2):
        MMLTEQOracleChain<String, String> chainOracle = new MMLTEQOracleChain<>();
        chainOracle.addOracle(new CounterEQOracle<>(cacheSUL.createCacheConsistencyTest(), "cache"));
        chainOracle.addOracle(new CounterEQOracle<>(new ResetSearchEQOracle<>(timeOracle, 100, 1.0, 1.0), "reset"));
        chainOracle.addOracle(new CounterEQOracle<>(new RandomWpMethodEQOracle<>(timeOracle, 100, 16, 0, 100), "wp"));

        // Set up our L* learner:
        List<Word<TimedInput<String>>> suffixes = new ArrayList<>();
        model.getReferenceAutomaton().getInputAlphabet().forEach(s -> suffixes.add(Word.fromLetter(TimedInput.input(s))));
        suffixes.add(Word.fromLetter(new TimeoutSymbol<>()));

        // A symbol filter allows us to reduce queries by exploiting prior knowledge.
        // For this example, we use a AbstractRandomSymbolFilter. This filter correctly predicts
        // whether a transition silently self-loops with an accuracy of 90%:
        SymbolFilter<TimedInput<String>, InputSymbol<String>> filter =
                new MMLTRandomSymbolFilter<>(model.getReferenceAutomaton(), 0.1, new Random(100));

        // We wrap our filter with a StatisticsFilter to collect useful statistics about the filter:
        filter = new MMLTStatisticsSymbolFilter<>(model.getReferenceAutomaton(), filter, stats);

        // The learner may need to update incorrect responses of the filter.
        // To facilitate this, we wrap our filter with a CachedFilter:
        var cachedFilter = new CachedSymbolFilter<>(filter);

        var learner = new ExtensibleLStarMMLT<>(model.getReferenceAutomaton().getInputAlphabet(), model.getParams(), suffixes, timeOracle, cachedFilter);

        // Start learning:
        var finalModel = runExperiment(learner, chainOracle, stats, 100);

        // In this set-up, we actually know the reference automaton.
        // This allows us to check that we learned an accurate model:
        var simOracle = new SimulatorEQOracle<>(model.getReferenceAutomaton());
        if (simOracle.findCounterExample(finalModel, finalModel.getSemantics().getInputAlphabet()) != null) {
            throw new AssertionError("Incorrect model learned.");
        }
    }

}
