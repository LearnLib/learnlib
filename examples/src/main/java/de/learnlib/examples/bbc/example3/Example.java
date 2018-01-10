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
package de.learnlib.examples.bbc.example3;

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
import de.learnlib.modelchecking.modelchecker.LTSminLTLAlternatingBuilder;
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
 * Run a black-box checking experiment with a Mealy machine and alternating edge semantics.
 *
 * The main difference with {@link de.learnlib.examples.bbc.example2.Example} is how the LTL formula is written.
 *
 * @see de.learnlib.examples.bbc.example2.Example
 *
 * @author Jeroen Meijer
 */
public final class Example {

    public static final Function<String, Character> EDGE_PARSER = s -> s.charAt(0);

    private Example() { }

    public static void main(String[] args) {

        MealyLearningExample<Character, Character> le = ExampleTinyMealy.createExample();

        // define the alphabet
        Alphabet<Character> sigma = le.getAlphabet();

        // define the Mealy machine to be verified/learned
        MealyMachine<?, Character, ?, Character> mealy = le.getReferenceAutomaton();

        MealyOmegaMembershipOracle<?, Character, Character> omqOracle = new MealySimulatorOmegaOracle<>(mealy);

        MealyMembershipOracle<Character, Character> mqOracle = omqOracle.getMealyMembershipOracle();

        MealyEquivalenceOracle<Character, Character> eqOracle = new MealyWpMethodEQOracle<>(mqOracle, 3);

        MealyLearner<Character, Character> learner = new TTTLearnerMealy<>(sigma, mqOracle, AcexAnalyzers.LINEAR_FWD);

        MealyModelCheckerLasso<Character, Character, String> modelChecker =
                new LTSminLTLAlternatingBuilder<Character, Character>().withString2Input(EDGE_PARSER).
                        withString2Output(EDGE_PARSER).create();

        MealyLassoEmptinessOracle<?, Character, Character> emptinessOracle = new MealyLassoMealyEmptinessOracle<>(omqOracle);

        MealyInclusionOracle<Character, Character> inclusionOracle = new MealyBreadthFirstInclusionOracle<>(1, mqOracle);

        MealyBlackBoxProperty<String, Character, Character> ltl =
                new MealyBBPropertyMealyLasso<>(modelChecker, emptinessOracle, inclusionOracle, "X X X letter==\"2\"");

        MealyBlackBoxOracle<Character, Character> blackBoxOracle = new CExFirstMealyBBOracle<>(ltl);

        MealyBBCExperiment<Character, Character> experiment = new MealyBBCExperiment<>(learner, eqOracle, sigma, blackBoxOracle);

        experiment.run();

        MealyMachine<?, Character, ?, Character> result = experiment.getFinalHypothesis();

        assert DeterministicEquivalenceTest.findSeparatingWord(mealy, result, sigma) == null;
    }
}
