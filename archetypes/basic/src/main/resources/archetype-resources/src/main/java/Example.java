package ${package};

import de.learnlib.algorithms.lstar.dfa.ClassicLStarDFA;
import de.learnlib.algorithms.lstar.dfa.ClassicLStarDFABuilder;
import de.learnlib.api.oracle.MembershipOracle.DFAMembershipOracle;
import de.learnlib.oracle.equivalence.WMethodEQOracle.DFAWMethodEQOracle;
import de.learnlib.oracle.membership.SimulatorOracle.DFASimulatorOracle;
import de.learnlib.util.Experiment.DFAExperiment;
import de.learnlib.util.statistics.SimpleProfiler;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.util.automata.builders.AutomatonBuilders;
import net.automatalib.visualization.Visualization;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;

/**
 * This example shows the usage of a learning algorithm and an equivalence test as part of an experiment in order to
 * learn a simulated SUL (system under learning).
 */
public final class Example {

    public static void main(String[] args) {

        // load DFA and alphabet
        CompactDFA<Character> target = constructSUL();
        Alphabet<Character> inputs = target.getInputAlphabet();

        // construct a simulator membership query oracle
        // input  - Character (determined by example)
        DFAMembershipOracle<Character> mqOracle = new DFASimulatorOracle<>(target);

        // construct L* instance
        ClassicLStarDFA<Character> lstar =
                new ClassicLStarDFABuilder<Character>().withAlphabet(inputs) // input alphabet
                                                       .withOracle(mqOracle) // membership oracle
                                                       .create();

        // construct a W-method conformance test
        // exploring the system up to depth 4 from
        // every state of a hypothesis
        DFAWMethodEQOracle<Character> wMethod = new DFAWMethodEQOracle<>(4, mqOracle);

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
        System.out.println(SimpleProfiler.getResults());

        // learning statistics
        System.out.println(experiment.getRounds().getSummary());

        // model statistics
        System.out.println("States: " + result.size());
        System.out.println("Sigma: " + inputs.size());

        // show model
        System.out.println();
        System.out.println("Model: ");

        Visualization.visualizeAutomaton(result, inputs, true);
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
