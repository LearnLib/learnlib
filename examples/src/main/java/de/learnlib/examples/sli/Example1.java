/* Copyright (C) 2013-2019 TU Dortmund
 * This file is part of LearnLib, http://www.learnlib.de/.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.learnlib.examples.sli;

import de.learnlib.algorithms.lstar.ce.ObservationTableCEXHandlers;
import de.learnlib.algorithms.lstar.mealy.ExtensibleLStarMealy;
import de.learnlib.algorithms.lstar.mealy.ExtensibleLStarMealyBuilder;
import de.learnlib.algorithms.lstar.mealy.PartialLStarMealy;
import de.learnlib.algorithms.lstar.mealy.PartialLStarMealyBuilder;
import de.learnlib.api.StateLocalInputSUL;
import de.learnlib.api.oracle.EquivalenceOracle.MealyEquivalenceOracle;
import de.learnlib.api.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.api.oracle.StateLocalInputOracle.StateLocalInputMealyOracle;
import de.learnlib.datastructure.observationtable.writer.ObservationTableASCIIWriter;
import de.learnlib.driver.util.StateLocalInputMealySimulatorSUL;
import de.learnlib.oracle.equivalence.MealySimulatorEQOracle;
import de.learnlib.oracle.equivalence.mealy.StateLocalInputMealySimulatorEQOracle;
import de.learnlib.oracle.membership.StateLocalInputSULOracle;
import de.learnlib.util.Experiment;
import de.learnlib.util.Experiment.MealyExperiment;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.automata.transducers.OutputAndLocalInputs;
import net.automatalib.automata.transducers.StateLocalInputMealyMachine;
import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.util.automata.builders.AutomatonBuilders;
import net.automatalib.util.automata.transducers.StateLocalInputMealyUtil;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;

/**
 * An example showcasing the usage of learning a partial system using the {@link PartialLStarMealy} learning algorithm.
 *
 * @author frohme
 */
public final class Example1 {

    private static final Alphabet<Integer> INPUTS;
    private static final CompactMealy<Integer, Character> TARGET;
    private static final MealyMachine<?, Integer, ?, OutputAndLocalInputs<Integer, Character>> TRANSFORMED_TARGET;
    private static final StateLocalInputSUL<Integer, Character> SUL;

    static {
        INPUTS = Alphabets.integers(0, 1);
        TARGET = constructTarget();
        TRANSFORMED_TARGET = StateLocalInputMealyUtil.partialToObservableOutput(TARGET);
        SUL = new StateLocalInputMealySimulatorSUL<>(TARGET);
    }

    private Example1() {
        // prevent instantiation
    }

    public static void main(String[] args) {
        runPartialLearner();
        runTransformedLearner();
    }

    /**
     * Uses the raw {@link StateLocalInputSUL} to infer a (potentially) partial {@link MealyMachine}.
     */
    static void runPartialLearner() {

        // construct a (state local input) simulator membership query oracle
        StateLocalInputMealyOracle<Integer, OutputAndLocalInputs<Integer, Character>> mqOracle =
                new StateLocalInputSULOracle<>(SUL);

        // construct L* instance
        PartialLStarMealy<Integer, Character> lstar =
                new PartialLStarMealyBuilder<Integer, Character>().withOracle(mqOracle)
                                                                  .withCexHandler(ObservationTableCEXHandlers.RIVEST_SCHAPIRE)
                                                                  .create();

        // here, we simply use an equivalence check for the original automaton model
        StateLocalInputMealySimulatorEQOracle<Integer, Character> eqOracle =
                new StateLocalInputMealySimulatorEQOracle<>(TARGET);

        // construct the experiment
        // note, we can't use the regular MealyExperiment (or MooreExperiment) because the output type of our hypothesis
        // is different from our membership query type
        Experiment<StateLocalInputMealyMachine<?, Integer, ?, Character>> experiment =
                new Experiment<>(lstar, eqOracle, INPUTS);

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

    /**
     * Uses the raw {@link StateLocalInputSUL} to infer a complete partial {@link MealyMachine} as in classical automata
     * learning. Note: here the {@link MealyMembershipOracle} needs to take care of mapping potentially undefined inputs
     * of the system under learning to processable outputs for the learner.
     */
    static void runTransformedLearner() {

        // construct a (regular) simulator membership query oracle
        MealyMembershipOracle<Integer, OutputAndLocalInputs<Integer, Character>> mqOracle =
                new StateLocalInputSULOracle<>(SUL);

        // construct L* instance
        ExtensibleLStarMealy<Integer, OutputAndLocalInputs<Integer, Character>> lstar =
                new ExtensibleLStarMealyBuilder<Integer, OutputAndLocalInputs<Integer, Character>>().withAlphabet(INPUTS)
                                                                                                    .withOracle(mqOracle)
                                                                                                    .withCexHandler(
                                                                                                            ObservationTableCEXHandlers.RIVEST_SCHAPIRE)
                                                                                                    .create();

        // here, we simply use an equivalence check for the transformed automaton model
        MealyEquivalenceOracle<Integer, OutputAndLocalInputs<Integer, Character>> eqOracle =
                new MealySimulatorEQOracle<>(TRANSFORMED_TARGET);

        // construct the experiment
        MealyExperiment<Integer, OutputAndLocalInputs<Integer, Character>> experiment =
                new MealyExperiment<>(lstar, eqOracle, INPUTS);

        // run experiment
        experiment.run();

        // get learned model
        MealyMachine<?, Integer, ?, OutputAndLocalInputs<Integer, Character>> result = experiment.getFinalHypothesis();

        // report results
        System.out.println("-------------------------------------------------------");

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
