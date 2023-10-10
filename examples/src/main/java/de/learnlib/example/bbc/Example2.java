/* Copyright (C) 2013-2023 TU Dortmund
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
package de.learnlib.example.bbc;

import java.util.function.Function;

import de.learnlib.acex.analyzers.AcexAnalyzers;
import de.learnlib.algorithm.ttt.mealy.TTTLearnerMealy;
import de.learnlib.api.algorithm.LearningAlgorithm.MealyLearner;
import de.learnlib.api.logging.LoggingPropertyOracle.MealyLoggingPropertyOracle;
import de.learnlib.api.oracle.EquivalenceOracle.MealyEquivalenceOracle;
import de.learnlib.api.oracle.InclusionOracle.MealyInclusionOracle;
import de.learnlib.api.oracle.LassoEmptinessOracle.MealyLassoEmptinessOracle;
import de.learnlib.api.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.api.oracle.OmegaMembershipOracle.MealyOmegaMembershipOracle;
import de.learnlib.api.oracle.PropertyOracle.MealyPropertyOracle;
import de.learnlib.example.LearningExample.MealyLearningExample;
import de.learnlib.example.mealy.ExampleTinyMealy;
import de.learnlib.oracle.emptiness.MealyLassoEmptinessOracleImpl;
import de.learnlib.oracle.equivalence.MealyBFInclusionOracle;
import de.learnlib.oracle.equivalence.MealyCExFirstOracle;
import de.learnlib.oracle.equivalence.MealyEQOracleChain;
import de.learnlib.oracle.equivalence.MealyWpMethodEQOracle;
import de.learnlib.oracle.membership.SimulatorOmegaOracle.MealySimulatorOmegaOracle;
import de.learnlib.oracle.property.MealyLassoPropertyOracle;
import de.learnlib.util.Experiment.MealyExperiment;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.modelchecker.ltsmin.ltl.LTSminLTLIOBuilder;
import net.automatalib.modelchecking.ModelCheckerLasso.MealyModelCheckerLasso;
import net.automatalib.util.automata.equivalence.DeterministicEquivalenceTest;
import net.automatalib.words.Alphabet;

/**
 * Run a black-box checking experiment with Mealy machines and straightforward edge semantics.
 * <p>
 * The main difference with {@link Example3} is how the LTL formula is written.
 *
 * @see Example3
 */
public final class Example2 {

    /**
     * A function that transforms edges in an FSM source to actual input, and output in the Mealy machine.
     */
    public static final Function<String, Character> EDGE_PARSER = s -> s.charAt(0);

    private Example2() { }

    public static void main(String[] args) {

        MealyLearningExample<Character, Character> le = ExampleTinyMealy.createExample();

        // define the alphabet
        Alphabet<Character> sigma = le.getAlphabet();

        // define the Mealy machine to be verified/learned
        MealyMachine<?, Character, ?, Character> mealy = le.getReferenceAutomaton();

        // create an omega membership oracle
        MealyOmegaMembershipOracle<?, Character, Character> omqOracle = new MealySimulatorOmegaOracle<>(mealy);

        // create a regular membership oracle
        MealyMembershipOracle<Character, Character> mqOracle = omqOracle.getMembershipOracle();

        // create a learner
        MealyLearner<Character, Character> learner = new TTTLearnerMealy<>(sigma, mqOracle, AcexAnalyzers.LINEAR_FWD);

        // create a model checker
        MealyModelCheckerLasso<Character, Character, String> modelChecker =
                new LTSminLTLIOBuilder<Character, Character>().withString2Input(EDGE_PARSER)
                                                              .withString2Output(EDGE_PARSER)
                                                              .create();

        // create an emptiness oracle, that is used to disprove properties
        MealyLassoEmptinessOracle<Character, Character> emptinessOracle =
                new MealyLassoEmptinessOracleImpl<>(omqOracle);

        // create an inclusion oracle, that is used to find counterexamples to hypotheses
        MealyInclusionOracle<Character, Character> inclusionOracle = new MealyBFInclusionOracle<>(mqOracle, 1.0);

        // create an LTL property oracle, that also logs stuff
        MealyPropertyOracle<Character, Character, String> ltl =
                new MealyLoggingPropertyOracle<>(new MealyLassoPropertyOracle<>("X output==\"2\"",
                                                                                inclusionOracle,
                                                                                emptinessOracle,
                                                                                modelChecker));

        // create an equivalence oracle, that first searches for a counter example using the ltl properties, and next
        // with the W-method.
        MealyEquivalenceOracle<Character, Character> eqOracle =
                new MealyEQOracleChain<>(new MealyCExFirstOracle<>(ltl), new MealyWpMethodEQOracle<>(mqOracle, 3));

        // create an experiment
        MealyExperiment<Character, Character> experiment = new MealyExperiment<>(learner, eqOracle, sigma);

        // run the experiment
        experiment.run();

        // get the final result
        MealyMachine<?, Character, ?, ?> result = experiment.getFinalHypothesis();

        // check we have the correct result
        assert DeterministicEquivalenceTest.findSeparatingWord(mealy, result, sigma) == null;
    }
}
