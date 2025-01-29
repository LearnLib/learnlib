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
package de.learnlib.algorithm.procedural.sba;

import java.util.Arrays;

import de.learnlib.algorithm.procedural.adapter.dfa.TTTAdapterDFA;
import de.learnlib.algorithm.procedural.sba.manager.OptimizingATManager;
import de.learnlib.oracle.equivalence.EQOracleChain;
import de.learnlib.oracle.equivalence.SampleSetEQOracle;
import de.learnlib.oracle.equivalence.sba.SimulatorEQOracle;
import de.learnlib.oracle.membership.SimulatorOracle;
import de.learnlib.testsupport.example.sba.ExamplePalindrome;
import de.learnlib.util.Experiment;
import net.automatalib.alphabet.ProceduralInputAlphabet;
import net.automatalib.automaton.procedural.SBA;
import net.automatalib.util.automaton.procedural.SBAs;
import net.automatalib.word.Word;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * A test for checking the optimizations performed by the {@link OptimizingATManager}. This test provides the
 * {@link SBALearner} with a long, non-optimal counterexample first, so that following analysis steps should extract
 * optimized sequences.
 */
public class OptimizationsTest {

    @Test
    public void testOptimizations() {

        final ExamplePalindrome example = ExamplePalindrome.createExample();
        final SBA<?, Character> sba = example.getReferenceAutomaton();
        final ProceduralInputAlphabet<Character> alphabet = example.getAlphabet();

        final SimulatorOracle<Character, Boolean> mqo = new SimulatorOracle<>(sba);

        final SampleSetEQOracle<Character, Boolean> eqo1 = new SampleSetEQOracle<>();
        eqo1.addAll(mqo, Word.fromString("STcTSaSaRaRRcRR"));
        final SimulatorEQOracle<Character> eqo2 = new SimulatorEQOracle<>(sba);
        final EQOracleChain<SBA<?, Character>, Character, Boolean> eqo = new EQOracleChain<>(Arrays.asList(eqo1, eqo2));

        final SBALearner<Character, ?> learner = new SBALearner<>(alphabet, mqo, TTTAdapterDFA::new);

        final Experiment<SBA<?, Character>> experiment = new Experiment<>(learner, eqo, alphabet);

        experiment.run();

        Assert.assertTrue(SBAs.testEquivalence(sba, experiment.getFinalHypothesis(), alphabet));
    }
}
