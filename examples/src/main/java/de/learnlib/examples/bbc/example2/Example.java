/* Copyright (C) 2013-2018 TU Dortmund
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
package de.learnlib.examples.bbc.example2;

import java.util.function.Function;

import de.learnlib.acex.analyzers.AcexAnalyzers;
import de.learnlib.algorithms.ttt.mealy.TTTLearnerMealy;
import de.learnlib.api.algorithm.LearningAlgorithm.MealyLearner;
import de.learnlib.api.modelchecking.modelchecker.ModelChecker.MealyModelCheckerLasso;
import de.learnlib.api.oracle.BlackBoxOracle.MealyBlackBoxOracle;
import de.learnlib.api.oracle.BlackBoxOracle.MealyBlackBoxProperty;
import de.learnlib.api.oracle.EmptinessOracle.MealyLassoEmptinessOracle;
import de.learnlib.api.oracle.EquivalenceOracle.MealyEquivalenceOracle;
import de.learnlib.api.oracle.InclusionOracle.MealyInclusionOracle;
import de.learnlib.api.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.api.oracle.OmegaMembershipOracle.MealyOmegaMembershipOracle;
import de.learnlib.examples.LearningExample.MealyLearningExample;
import de.learnlib.examples.mealy.ExampleTinyMealy;
import de.learnlib.modelchecking.modelchecker.LTSminLTLIOBuilder;
import de.learnlib.oracle.blackbox.CExFirstBBOracle.CExFirstMealyBBOracle;
import de.learnlib.oracle.blackbox.ModelCheckingBBProperty.MealyBBPropertyMealyLasso;
import de.learnlib.oracle.emptiness.AbstractLassoAutomatonEmptinessOracle.MealyLassoMealyEmptinessOracle;
import de.learnlib.oracle.equivalence.WpMethodEQOracle.MealyWpMethodEQOracle;
import de.learnlib.oracle.inclusion.AbstractBreadthFirstInclusionOracle.MealyBreadthFirstInclusionOracle;
import de.learnlib.oracle.membership.SimulatorOmegaOracle.MealySimulatorOmegaOracle;
import de.learnlib.util.BBCExperiment.MealyBBCExperiment;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.util.automata.equivalence.DeterministicEquivalenceTest;
import net.automatalib.words.Alphabet;

/**
 * Run a black-box checking experiment with Mealy machines and straightforward edge semantics.
 *
 * The main difference with {@link de.learnlib.examples.bbc.example3.Example} is how the LTL formula is written.
 *
 * @see de.learnlib.examples.bbc.example3.Example
 *
 * @author Jeroen Meijer
 */
public final class Example {

    /**
     * A function that transforms edges in an FSM source to actual input, and output in the Mealy machine.
     */
    public static final Function<String, Character> EDGE_PARSER = s -> s.charAt(0);

    private Example() { }

    public static void main(String[] args) {

        MealyLearningExample<Character, Character> le = ExampleTinyMealy.createExample();

        // define the alphabet
        Alphabet<Character> sigma = le.getAlphabet();

        // define the Mealy machine to be verified/learned
        MealyMachine<?, Character, ?, Character> mealy = le.getReferenceAutomaton();

        // create an omega membership oracle
        MealyOmegaMembershipOracle<?, Character, Character> omqOracle = new MealySimulatorOmegaOracle<>(mealy);

        // create a regular membership oracle
        MealyMembershipOracle<Character, Character> mqOracle = omqOracle.getMealyMembershipOracle();

        // create an equivalence oracle
        MealyEquivalenceOracle<Character, Character> eqOracle = new MealyWpMethodEQOracle<>(mqOracle, 3);

        // create a learner
        MealyLearner<Character, Character> learner = new TTTLearnerMealy<>(sigma, mqOracle, AcexAnalyzers.LINEAR_FWD);

        // create a model checker
        MealyModelCheckerLasso<Character, Character, String> modelChecker =
                new LTSminLTLIOBuilder<Character, Character>().withString2Input(EDGE_PARSER).
                        withString2Output(EDGE_PARSER).create();

        // create an emptiness oracle, that is used to disprove properties
        MealyLassoEmptinessOracle<?, Character, Character> emptinessOracle = new MealyLassoMealyEmptinessOracle<>(omqOracle);

        // create an inclusion oracle, that is used to find counterexamples to hypotheses
        MealyInclusionOracle<Character, Character> inclusionOracle = new MealyBreadthFirstInclusionOracle<>(1, mqOracle);

        // create an ltl formula
        MealyBlackBoxProperty<String, Character, Character> ltl = new MealyBBPropertyMealyLasso<>(modelChecker,
                                                                                                  emptinessOracle,
                                                                                                  inclusionOracle,
                                                                                                  "X output==\"2\"");

        // create a black-box oracle
        MealyBlackBoxOracle<Character, Character> blackBoxOracle = new CExFirstMealyBBOracle<>(ltl);

        // create an experiment
        MealyBBCExperiment<Character, Character> experiment = new MealyBBCExperiment<>(learner,
                                                                                       eqOracle, sigma, blackBoxOracle);

        // run the experiment
        experiment.run();

        // get the final result
        MealyMachine<?, Character, ?, ?> result = experiment.getFinalHypothesis();

        // check we have the correct result
        assert DeterministicEquivalenceTest.findSeparatingWord(mealy, result, sigma) == null;
    }
}
