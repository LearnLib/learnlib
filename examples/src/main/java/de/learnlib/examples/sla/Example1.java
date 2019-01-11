package de.learnlib.examples.sla;

import de.learnlib.algorithms.lstar.ce.ObservationTableCEXHandlers;
import de.learnlib.algorithms.lstar.mealy.ExtensibleLStarMealy;
import de.learnlib.algorithms.lstar.mealy.ExtensibleLStarMealyBuilder;
import de.learnlib.algorithms.lstar.mealy.PartialLStarMealy;
import de.learnlib.algorithms.lstar.mealy.PartialLStarMealyBuilder;
import de.learnlib.api.SUL;
import de.learnlib.api.StateLocalInputSUL;
import de.learnlib.api.oracle.EquivalenceOracle.MealyEquivalenceOracle;
import de.learnlib.api.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.datastructure.observationtable.writer.ObservationTableASCIIWriter;
import de.learnlib.driver.util.MealySimulatorSUL;
import de.learnlib.driver.util.SLAMealySimulatorSUL;
import de.learnlib.oracle.equivalence.SimulatorEQOracle.MealySimulatorEQOracle;
import de.learnlib.oracle.equivalence.SimulatorEQOracle.SLIMealySimulatorEQOracle;
import de.learnlib.oracle.membership.SULOracle;
import de.learnlib.oracle.membership.StateLocalAlphabetSULOracle;
import de.learnlib.util.Experiment;
import de.learnlib.util.Experiment.MealyExperiment;
import net.automatalib.automata.transducers.MealyMachine;
import de.learnlib.api.oracle.OutputAndLocalInputs;
import net.automatalib.automata.transducers.StateLocalInputMealyMachine;
import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.util.automata.builders.AutomatonBuilders;
import de.learnlib.api.oracle.SLIMMUtil;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;

public class Example1 {

    private static final Alphabet<Integer> INPUTS;
    private static final CompactMealy<Integer, Character> PARTIAL_TARGET;
    private static final MealyMachine<?, Integer, ?, OutputAndLocalInputs<Integer, Character>> COMPLETED_TARGET;
    private static final SUL<Integer, OutputAndLocalInputs<Integer, Character>> SUL;
    private static final StateLocalInputSUL<Integer, Character> SLA_SUL;

    static {
        INPUTS = Alphabets.integers(0, 1);
        PARTIAL_TARGET = constructTarget();
        COMPLETED_TARGET = SLIMMUtil.partial2StateLocal(PARTIAL_TARGET);
        SUL = new MealySimulatorSUL<>(COMPLETED_TARGET);
        SLA_SUL = new SLAMealySimulatorSUL<>(PARTIAL_TARGET);
    }

    private Example1() {
        // prevent instantiation
    }

    public static void main(String[] args) {
        runPartialLearner();
        runAbstractedLearner();
    }

    public static void runPartialLearner() {

        // load DFA and alphabet
        StateLocalInputSUL<Integer, Character> target = SLA_SUL;

        // construct a simulator membership query oracle
        MealyMembershipOracle<Integer, OutputAndLocalInputs<Integer, Character>> mqOracle =
                new StateLocalAlphabetSULOracle<>(target);

        // construct L* instance
        PartialLStarMealy<Integer, Character> lstar =
                new PartialLStarMealyBuilder<Integer, Character>().withOracle(mqOracle).withCexHandler(
                        ObservationTableCEXHandlers.RIVEST_SCHAPIRE).create();

        // construct a W-method conformance test
        // exploring the system up to depth 4 from
        // every state of a hypothesis
        SLIMealySimulatorEQOracle<Integer, Character> eqOracle = new SLIMealySimulatorEQOracle<>(PARTIAL_TARGET);

        // note, we can't use the regular MealyExperiment because the outputs types of our hypothesis a different from our membership query type
        Experiment<StateLocalInputMealyMachine<?, Integer, ?, Character>> experiment = new Experiment<>(lstar, eqOracle, INPUTS);

        // run experiment
        experiment.run();

        // get learned model
        MealyMachine<?, Integer, ?, Character> result = experiment.getFinalHypothesis();

        // report results
        System.out.println("-------------------------------------------------------");

        System.out.println("States: " + result.size());
        System.out.println("Sigma: " + INPUTS.size());

        System.out.println("-------------------------------------------------------");

        System.out.println("Final observation table:");
        new ObservationTableASCIIWriter<>().write(lstar.getObservationTable(), System.out);
    }

    public static void runAbstractedLearner() {

        // load DFA and alphabet
        SUL<Integer, OutputAndLocalInputs<Integer, Character>> target = SUL;

        // construct a simulator membership query oracle
        MealyMembershipOracle<Integer, OutputAndLocalInputs<Integer, Character>> mqOracle = new SULOracle<>(target);

        // construct L* instance
        ExtensibleLStarMealy<Integer, OutputAndLocalInputs<Integer, Character>> lstar =
                new ExtensibleLStarMealyBuilder<Integer, OutputAndLocalInputs<Integer, Character>>().withAlphabet(
                        INPUTS).withOracle(mqOracle).create();

        // construct a W-method conformance test
        // exploring the system up to depth 4 from
        // every state of a hypothesis
        MealyEquivalenceOracle<Integer, OutputAndLocalInputs<Integer, Character>> eqOracle =
                new MealySimulatorEQOracle<>(COMPLETED_TARGET);

        // construct a learning experiment from
        // the learning algorithm and the conformance test.
        // The experiment will execute the main loop of
        // active learning
        MealyExperiment<Integer, OutputAndLocalInputs<Integer, Character>> experiment =
                new MealyExperiment<>(lstar, eqOracle, INPUTS);

        // run experiment
        experiment.run();

        // get learned model
        MealyMachine<?, Integer, ?, OutputAndLocalInputs<Integer, Character>> result =
                experiment.getFinalHypothesis();

        // report results
        System.out.println("-------------------------------------------------------");

        // model statistics
        System.out.println("States: " + result.size());
        System.out.println("Sigma: " + INPUTS.size());

        System.out.println("-------------------------------------------------------");

        System.out.println("Final observation table:");
        new ObservationTableASCIIWriter<>().write(lstar.getObservationTable(), System.out);
    }

    /**
     * creates example from Geske's thesis (fig. 2.1).
     *
     * @return example (partial) mealy
     */
    private static CompactMealy<Integer, Character> constructTarget() {
        // @formatter:off
        // create automaton
        return AutomatonBuilders.<Integer, Character>newMealy(INPUTS)
                .withInitial("s0")
                .from("s0")
                    .on(0).withOutput('a').to("s2")
                    .on(1).withOutput('b').to("s1")
                .from("s1")
                    .on(0).withOutput('b').loop()
                    .on(1).withOutput('a').loop()
                .from("s2")
                    .on(0).withOutput('b').loop()
                    .on(1).withOutput('a').to("s3")
                .from("s3")
                    .on(0).withOutput('a').to("s1")
                .create();
        // @formatter:on
    }
}
