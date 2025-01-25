package ${package};

import java.io.IOException;

import de.learnlib.algorithm.lstar.dfa.ClassicLStarDFA;
import de.learnlib.algorithm.lstar.dfa.ClassicLStarDFABuilder;
import de.learnlib.datastructure.observationtable.writer.ObservationTableASCIIWriter;
import de.learnlib.filter.statistic.oracle.DFACounterOracle;
import de.learnlib.oracle.MembershipOracle.DFAMembershipOracle;
import de.learnlib.oracle.equivalence.DFAWMethodEQOracle;
import de.learnlib.oracle.membership.DFASimulatorOracle;
import de.learnlib.util.Experiment.DFAExperiment;
import de.learnlib.util.statistic.SimpleProfiler;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.fsa.impl.CompactDFA;
import net.automatalib.util.automaton.builder.AutomatonBuilders;
import net.automatalib.visualization.Visualization;

/**
 * This example shows the usage of a learning algorithm and an equivalence test as part of an experiment in order to
 * learn a simulated SUL (system under learning).
 */
public final class Example {

    private static final int EXPLORATION_DEPTH = 4;

    private Example() {
        // prevent instantiation
    }

    public static void main(String[] args) throws IOException {

        // load DFA and alphabet
        CompactDFA<Character> target = constructSUL();
        Alphabet<Character> inputs = target.getInputAlphabet();

        // construct a simulator membership query oracle
        // input  - Character (determined by example)
        DFAMembershipOracle<Character> sul = new DFASimulatorOracle<>(target);

        // oracle for counting queries wraps SUL
        DFACounterOracle<Character> mqOracle = new DFACounterOracle<>(sul);

        // construct L* instance
        ClassicLStarDFA<Character> lstar =
                new ClassicLStarDFABuilder<Character>().withAlphabet(inputs) // input alphabet
                                                       .withOracle(mqOracle) // membership oracle
                                                       .create();

        // construct a W-method conformance test
        // exploring the system up to depth 4 from
        // every state of a hypothesis
        DFAWMethodEQOracle<Character> wMethod = new DFAWMethodEQOracle<>(mqOracle, EXPLORATION_DEPTH);

        // construct a learning experiment from
        // the learning algorithm and the conformance test.
        // The experiment will execute the main loop of
        // active learning
        DFAExperiment<Character> experiment = new DFAExperiment<>(lstar, wMethod, inputs);

        // turn on time profiling
        experiment.setProfile(true);

        // enable logging of models
        experiment.setLogModels(true);

        // run experiment
        experiment.run();

        // get learned model
        DFA<?, Character> result = experiment.getFinalHypothesis();

        // report results
        System.out.println("-------------------------------------------------------");

        // profiling
        SimpleProfiler.logResults();

        // learning statistics
        System.out.println(experiment.getRounds().getSummary());
        System.out.println(mqOracle.getStatisticalData().getSummary());

        // model statistics
        System.out.println("States: " + result.size());
        System.out.println("Sigma: " + inputs.size());

        // show model
        Visualization.visualize(result, inputs);

        System.out.println("-------------------------------------------------------");

        System.out.println("Final observation table:");
        new ObservationTableASCIIWriter<>().write(lstar.getObservationTable(), System.out);
    }

    /**
     * creates example from Angluin's seminal paper.
     *
     * @return example dfa
     */
    private static CompactDFA<Character> constructSUL() {
        // input alphabet contains characters 'a'..'b'
        Alphabet<Character> sigma = Alphabets.characters('a', 'b');

        // create automaton
        return AutomatonBuilders.newDFA(sigma)
                .withInitial("q0")
                .from("q0")
                    .on('a').to("q1")
                    .on('b').to("q2")
                .from("q1")
                    .on('a').to("q0")
                    .on('b').to("q3")
                .from("q2")
                    .on('a').to("q3")
                    .on('b').to("q0")
                .from("q3")
                    .on('a').to("q2")
                    .on('b').to("q1")
                .withAccepting("q0")
                .create();
    }
}
