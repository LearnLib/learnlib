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
package de.learnlib.examples.bbc.example1;

import java.util.function.Function;

import de.learnlib.acex.analyzers.AcexAnalyzers;
import de.learnlib.algorithms.ttt.dfa.TTTLearnerDFA;
import de.learnlib.api.algorithm.LearningAlgorithm.DFALearner;
import de.learnlib.api.logging.LoggingPropertyOracle;
import de.learnlib.api.oracle.EquivalenceOracle.DFAEquivalenceOracle;
import de.learnlib.api.oracle.InclusionOracle.DFAInclusionOracle;
import de.learnlib.api.oracle.LassoEmptinessOracle;
import de.learnlib.api.oracle.MembershipOracle.DFAMembershipOracle;
import de.learnlib.api.oracle.OmegaMembershipOracle.DFAOmegaMembershipOracle;
import de.learnlib.api.oracle.PropertyOracle;
import de.learnlib.examples.LearningExample.DFALearningExample;
import de.learnlib.examples.dfa.ExampleTinyDFA;
import de.learnlib.oracle.emptiness.DFALassoEmptinessOracleImpl;
import de.learnlib.oracle.equivalence.CExFirstOracle;
import de.learnlib.oracle.equivalence.DFABFInclusionOracle;
import de.learnlib.oracle.equivalence.EQOracleChain;
import de.learnlib.oracle.equivalence.WpMethodEQOracle.DFAWpMethodEQOracle;
import de.learnlib.oracle.membership.SimulatorOmegaOracle.DFASimulatorOmegaOracle;
import de.learnlib.oracle.property.DFALassoPropertyOracle;
import de.learnlib.util.Experiment;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.modelcheckers.ltsmin.LTSminLTLDFABuilder;
import net.automatalib.modelchecking.ModelCheckerLasso.DFAModelCheckerLasso;
import net.automatalib.util.automata.equivalence.DeterministicEquivalenceTest;
import net.automatalib.words.Alphabet;

/**
 * Runs a black-box checking experiment for a DFA.
 *
 * @author Jeroen Meijer
 */
public final class Example {

    /**
     * A function that transforms edges in an FSM source to actual input for a DFA.
     */
    public static final Function<String, Character> EDGE_PARSER = s -> s.charAt(0);

    private Example() {}

    public static void main(String[] args) {

        DFALearningExample<Character> le = ExampleTinyDFA.createExample();

        // define the alphabet
        Alphabet<Character> sigma = le.getAlphabet();

        // create the DFA to be verified/learned
        DFA<?, Character> dfa = le.getReferenceAutomaton();

        // create an omega membership oracle
        DFAOmegaMembershipOracle<?, Character> omqOracle = new DFASimulatorOmegaOracle<>(dfa);

        // create a regular membership oracle
        DFAMembershipOracle<Character> mqOracle = omqOracle.getMembershipOracle();

        // create a learner
        DFALearner<Character> learner = new TTTLearnerDFA<>(sigma, mqOracle, AcexAnalyzers.LINEAR_FWD);

        // create a model checker
        DFAModelCheckerLasso<Character, String> modelChecker =
                new LTSminLTLDFABuilder<Character>().withString2Input(EDGE_PARSER).create();

        // create an emptiness oracle, that is used to disprove properties
        LassoEmptinessOracle.DFALassoEmptinessOracle<Character>
                emptinessOracle = new DFALassoEmptinessOracleImpl<>(omqOracle);

        // create an inclusion oracle, that is used to find counterexamples to hypotheses
        DFAInclusionOracle<Character> inclusionOracle = new DFABFInclusionOracle<>(mqOracle, 1.0);

        // create an LTL property oracle, that also logs stuff
        PropertyOracle.DFAPropertyOracle<Character, String> ltl = new LoggingPropertyOracle.DFALoggingPropertyOracle<>(
            new DFALassoPropertyOracle<>("letter==\"b\"", inclusionOracle, emptinessOracle, modelChecker));

        // create an equivalence oracle, that first searches for a counter example using the ltl properties, and next
        // with the W-method.
        DFAEquivalenceOracle<Character> eqOracle = new EQOracleChain.DFAEQOracleChain<>(
                new CExFirstOracle.DFACExFirstOracle<>(ltl),
                new DFAWpMethodEQOracle<>(mqOracle, 3));

        // create an experiment
        Experiment.DFAExperiment<Character> experiment = new Experiment.DFAExperiment<>(learner, eqOracle, sigma);

        // run the experiment
        experiment.run();

        // get the result
        final DFA<?, Character> result = experiment.getFinalHypothesis();

        // assert we have the correct result
        assert DeterministicEquivalenceTest.findSeparatingWord(dfa, result, sigma) == null;
    }
}
