package de.learnlib.example.mmlt;

import java.util.ArrayList;
import java.util.List;

import de.learnlib.algorithm.lstar.mmlt.ExtensibleLStarMMLT;
import de.learnlib.driver.simulator.MMLTSimulatorSUL;
import de.learnlib.filter.cache.mmlt.TimedSULTreeCache;
import de.learnlib.filter.cache.mmlt.TimeoutReducerSUL;
import de.learnlib.filter.statistic.sul.CounterTimedSUL;
import de.learnlib.oracle.equivalence.mmlt.SimulatorEQOracle;
import de.learnlib.oracle.membership.TimedSULOracle;
import de.learnlib.statistic.Statistics;
import de.learnlib.testsupport.example.mmlt.MMLTExamples;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.symbol.time.TimeoutSymbol;
import net.automatalib.word.Word;

/**
 * This example shows how to learn a Mealy machine with local timers (MMLT),
 * an automaton model for real-time systems.
 * <p>
 * MMLTs extend Mealy machines with multiple timers. More information about MMLTs can be found
 * in the included README file.
 * <p>
 * This example uses a very basic learner set-up.
 */
public class Example1 {

    public static void main(String[] args) {
        // We use the included sensor collector model as reference automaton:
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

        // In the basic set-up, we use a simulator oracle to answer equivalence queries.
        // This oracle has perfect knowledge of the reference automaton.
        var eqOracle = new SimulatorEQOracle<>(model.getReferenceAutomaton());

        // Set up our L* learner:

        // We provide the learner with an initial set of suffixes.
        // We include all untimed inputs and the symbolic timeout symbol, which causes the learner to wait
        // until the next timeout (but no longer than model.getParams().maxTimeoutWaitingTime()).
        List<Word<TimedInput<String>>> suffixes = new ArrayList<>();
        model.getReferenceAutomaton().getInputAlphabet().forEach(s -> suffixes.add(Word.fromLetter(TimedInput.input(s))));
        suffixes.add(Word.fromLetter(new TimeoutSymbol<>()));

        var learner = new ExtensibleLStarMMLT<>(model.getReferenceAutomaton().getInputAlphabet(), model.getParams(), suffixes, timeOracle);

        // Start learning:
        ExampleUtil.runExperiment(learner, eqOracle, stats, 100);

    }

}
