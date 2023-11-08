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
package de.learnlib.algorithm.procedural.spmm;

import java.util.Arrays;

import de.learnlib.algorithm.LearnerConstructor;
import de.learnlib.algorithm.procedural.SymbolWrapper;
import de.learnlib.algorithm.procedural.adapter.mealy.TTTAdapterMealy;
import de.learnlib.algorithm.procedural.spmm.manager.OptimizingATManager;
import de.learnlib.example.spmm.ExamplePalindrome;
import de.learnlib.oracle.equivalence.EQOracleChain;
import de.learnlib.oracle.equivalence.SampleSetEQOracle;
import de.learnlib.oracle.equivalence.spmm.SimulatorEQOracle;
import de.learnlib.oracle.membership.SimulatorOracle;
import de.learnlib.util.Experiment;
import net.automatalib.alphabet.ProceduralInputAlphabet;
import net.automatalib.automaton.procedural.SPMM;
import net.automatalib.util.automaton.procedural.SPMMs;
import net.automatalib.word.Word;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * A test for checking the optimizations performed by the {@link OptimizingATManager}. This test provides the
 * {@link SPMMLearner} with a long, non-optimal counterexample first, so that following analysis steps should extract
 * optimized sequences.
 */
public class OptimizationsTest {

    @Test
    public void testOptimizations() {

        final ExamplePalindrome example = ExamplePalindrome.createExample();
        final SPMM<?, Character, ?, Character> spmm = example.getReferenceAutomaton();
        final ProceduralInputAlphabet<Character> alphabet = example.getAlphabet();

        final SimulatorOracle<Character, Word<Character>> mqo = new SimulatorOracle<>(spmm);

        final SampleSetEQOracle<Character, Word<Character>> eqo1 = new SampleSetEQOracle<>(false);
        eqo1.addAll(mqo, Word.fromString("STcTSaSaRaRRcRR"));
        final SimulatorEQOracle<Character, Character> eqo2 = new SimulatorEQOracle<>(spmm);
        final EQOracleChain<SPMM<?, Character, ?, Character>, Character, Word<Character>> eqo =
                new EQOracleChain<>(Arrays.asList(eqo1, eqo2));

        final LearnerConstructor<TTTAdapterMealy<SymbolWrapper<Character>, Character>, SymbolWrapper<Character>, Word<Character>>
                cons = TTTAdapterMealy::new;
        final SPMMLearner<Character, Character, TTTAdapterMealy<SymbolWrapper<Character>, Character>> learner =
                new SPMMLearner<>(alphabet, spmm.getErrorOutput(), mqo, cons);

        final Experiment<SPMM<?, Character, ?, Character>> experiment = new Experiment<>(learner, eqo, alphabet);

        experiment.run();

        Assert.assertTrue(SPMMs.testEquivalence(spmm, experiment.getFinalHypothesis(), alphabet));
    }
}
