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
package de.learnlib.example.bbc;

import java.util.function.Function;

import de.learnlib.acex.AcexAnalyzers;
import de.learnlib.algorithm.LearningAlgorithm.DFALearner;
import de.learnlib.algorithm.ttt.dfa.TTTLearnerDFA;
import de.learnlib.oracle.EmptinessOracle.DFAEmptinessOracle;
import de.learnlib.oracle.EquivalenceOracle.DFAEquivalenceOracle;
import de.learnlib.oracle.InclusionOracle.DFAInclusionOracle;
import de.learnlib.oracle.LassoEmptinessOracle.DFALassoEmptinessOracle;
import de.learnlib.oracle.MembershipOracle.DFAMembershipOracle;
import de.learnlib.oracle.OmegaMembershipOracle.DFAOmegaMembershipOracle;
import de.learnlib.oracle.PropertyOracle.DFAPropertyOracle;
import de.learnlib.oracle.emptiness.DFABFEmptinessOracle;
import de.learnlib.oracle.emptiness.DFALassoEmptinessOracleImpl;
import de.learnlib.oracle.equivalence.DFABFInclusionOracle;
import de.learnlib.oracle.equivalence.DFACExFirstOracle;
import de.learnlib.oracle.equivalence.DFAEQOracleChain;
import de.learnlib.oracle.equivalence.DFAWpMethodEQOracle;
import de.learnlib.oracle.membership.SimulatorOmegaOracle.DFASimulatorOmegaOracle;
import de.learnlib.oracle.property.DFAFinitePropertyOracle;
import de.learnlib.oracle.property.DFALassoPropertyOracle;
import de.learnlib.oracle.property.DFAPropertyOracleChain;
import de.learnlib.oracle.property.LoggingPropertyOracle.DFALoggingPropertyOracle;
import de.learnlib.testsupport.example.LearningExample.DFALearningExample;
import de.learnlib.testsupport.example.dfa.ExampleTinyDFA;
import de.learnlib.util.Experiment.DFAExperiment;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.modelchecker.ltsmin.LTSminUtil;
import net.automatalib.modelchecker.ltsmin.LTSminVersion;
import net.automatalib.modelchecker.ltsmin.ltl.LTSminLTLDFABuilder;
import net.automatalib.modelchecker.ltsmin.monitor.LTSminMonitorDFABuilder;
import net.automatalib.modelchecking.ModelChecker.DFAModelChecker;
import net.automatalib.modelchecking.ModelCheckerLasso.DFAModelCheckerLasso;
import net.automatalib.util.automaton.equivalence.DeterministicEquivalenceTest;

/**
 * Runs a black-box checking experiment for a DFA.
 * <p>
 * This example is similar to {@link Example1}, except that is also uses a monitor to disprove properties for the
 * learned DFA.
 */
public final class Example4 {

    /**
     * A function that transforms edges in an FSM source to actual input for a DFA.
     */
    public static final Function<String, Character> EDGE_PARSER = s -> s.charAt(0);

    private Example4() {}

    public static void main(String[] args) {

        // This code requires v3.1.0
        if (!LTSminUtil.supports(LTSminVersion.of(3, 1, 0))) {
            return;
        }

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

        // create a model checker that uses a Buchi automaton
        DFAModelCheckerLasso<Character, String> modelCheckerBuchi =
                new LTSminLTLDFABuilder<Character>().withString2Input(EDGE_PARSER).create();

        // create a lasso emptiness oracle, that is used to disprove properties
        DFALassoEmptinessOracle<Character> lassoEmptinessOracle = new DFALassoEmptinessOracleImpl<>(omqOracle);

        // create a model checker that uses monitors
        DFAModelChecker<Character, String, DFA<?, Character>> modelCheckerMonitor =
                new LTSminMonitorDFABuilder<Character>().withString2Input(EDGE_PARSER).create();

        // create an emptiness oracle, that is used to disprove properties.
        DFAEmptinessOracle<Character> emptinessOracle = new DFABFEmptinessOracle<>(mqOracle, 1.0);

        // create an inclusion oracle, that is used to find counterexamples to hypotheses
        DFAInclusionOracle<Character> inclusionOracle = new DFABFInclusionOracle<>(mqOracle, 1.0);

        // create an LTL property oracle, that also logs stuff
        // also it chains the property oracle that uses monitors and Buchi automata
        DFAPropertyOracle<Character, String> ltl = new DFALoggingPropertyOracle<>(
                new DFAPropertyOracleChain<>(
                        new DFAFinitePropertyOracle<>("letter==\"b\"", inclusionOracle, emptinessOracle, modelCheckerMonitor),
                        new DFALassoPropertyOracle<>("letter==\"b\"", inclusionOracle, lassoEmptinessOracle, modelCheckerBuchi)));

        // create an equivalence oracle, that first searches for a counter example using the ltl properties, and next
        // with the W-method.
        DFAEquivalenceOracle<Character> eqOracle =
                new DFAEQOracleChain<>(new DFACExFirstOracle<>(ltl), new DFAWpMethodEQOracle<>(mqOracle, 3));

        // create an experiment
        DFAExperiment<Character> experiment = new DFAExperiment<>(learner, eqOracle, sigma);

        // run the experiment
        experiment.run();

        // get the result
        final DFA<?, Character> result = experiment.getFinalHypothesis();

        // assert we have the correct result
        assert DeterministicEquivalenceTest.findSeparatingWord(dfa, result, sigma) == null;
    }
}
