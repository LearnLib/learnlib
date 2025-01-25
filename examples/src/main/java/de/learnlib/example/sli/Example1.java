/* Copyright (C) 2013-2025 TU Dortmund University
 * This file is part of LearnLib <https://learnlib.de>.
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
package de.learnlib.example.sli;

import de.learnlib.algorithm.lstar.mealy.ExtensibleLStarMealyBuilder;
import de.learnlib.datastructure.observationtable.OTLearner.OTLearnerMealy;
import de.learnlib.datastructure.observationtable.writer.ObservationTableASCIIWriter;
import de.learnlib.driver.simulator.StateLocalInputMealySimulatorSUL;
import de.learnlib.oracle.EquivalenceOracle.MealyEquivalenceOracle;
import de.learnlib.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.oracle.equivalence.mealy.StateLocalInputMealySimulatorEQOracle;
import de.learnlib.oracle.membership.StateLocalInputSULOracle;
import de.learnlib.sul.StateLocalInputSUL;
import de.learnlib.util.Experiment.MealyExperiment;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.automaton.transducer.impl.CompactMealy;
import net.automatalib.util.automaton.builder.AutomatonBuilders;
import net.automatalib.util.automaton.transducer.MealyFilter;
import net.automatalib.visualization.Visualization;

/**
 * An example showcasing the usage of learning partial systems using the {@link StateLocalInputSUL} interface.
 */
@SuppressWarnings("PMD.SystemPrintln")
public final class Example1 {

    private static final Alphabet<Integer> INPUTS;
    private static final CompactMealy<Integer, Character> TARGET;
    private static final Character UNDEFINED;

    static {
        INPUTS = Alphabets.integers(0, 1);
        TARGET = constructTarget();
        UNDEFINED = '-';
    }

    private Example1() {
        // prevent instantiation
    }

    public static void main(String[] args) {

        // create SUL instance
        final StateLocalInputSUL<Integer, Character> sul = new StateLocalInputMealySimulatorSUL<>(TARGET);

        // construct a (state local input) SUL oracle which answers undefined transitions with the undefined symbol
        final MealyMembershipOracle<Integer, Character> mqOracle = new StateLocalInputSULOracle<>(sul, UNDEFINED);

        // construct learner instance
        final OTLearnerMealy<Integer, Character> lstar =
                new ExtensibleLStarMealyBuilder<Integer, Character>().withAlphabet(INPUTS)
                                                                     .withOracle(mqOracle)
                                                                     .create();

        // since we want to compare structural differences between our partial target and the total hypothesis, we use a
        // specific SLI equivalence oracle. Any EQ oracle based on output behavior (e.g. W(p) method) would work as
        // well.
        final MealyEquivalenceOracle<Integer, Character> eqOracle =
                new StateLocalInputMealySimulatorEQOracle<>(TARGET, INPUTS, UNDEFINED);

        // construct the experiment
        final MealyExperiment<Integer, Character> experiment = new MealyExperiment<>(lstar, eqOracle, INPUTS);

        // run experiment
        experiment.run();

        // get learned model
        final MealyMachine<?, Integer, ?, Character> result = experiment.getFinalHypothesis();

        // report results
        System.out.println("-------------------------------------------------------");

        System.out.println("States: " + result.size());
        System.out.println("Sigma: " + INPUTS.size());

        System.out.println("-------------------------------------------------------");

        System.out.println("Final observation table:");
        new ObservationTableASCIIWriter<>().write(lstar.getObservationTable(), System.out);

        // show the partial hypothesis by pruning our 'undefined' output symbol
        final CompactMealy<Integer, Character> filtered =
                MealyFilter.pruneTransitionsWithOutput(result, INPUTS, UNDEFINED);

        Visualization.visualize(filtered);
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
