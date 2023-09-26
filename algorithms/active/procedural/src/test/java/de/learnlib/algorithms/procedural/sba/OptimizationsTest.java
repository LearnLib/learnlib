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
package de.learnlib.algorithms.procedural.sba;

import java.util.Arrays;

import de.learnlib.algorithms.procedural.SymbolWrapper;
import de.learnlib.algorithms.procedural.adapter.dfa.TTTAdapterDFA;
import de.learnlib.algorithms.procedural.sba.manager.OptimizingATManager;
import de.learnlib.api.algorithm.LearnerConstructor;
import de.learnlib.examples.sba.ExamplePalindrome;
import de.learnlib.oracle.equivalence.EQOracleChain;
import de.learnlib.oracle.equivalence.SampleSetEQOracle;
import de.learnlib.oracle.equivalence.sba.SimulatorEQOracle;
import de.learnlib.oracle.membership.SimulatorOracle;
import de.learnlib.util.Experiment;
import net.automatalib.automata.procedural.SBA;
import net.automatalib.util.automata.Automata;
import net.automatalib.words.ProceduralInputAlphabet;
import net.automatalib.words.Word;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * A test for checking the optimizations performed by the {@link OptimizingATManager}. This test provides the
 * {@link SBALearner} with a long, non-optimal counterexample first, so that following analysis steps should extract
 * optimized sequences.
 *
 * @author frohme
 */
public class OptimizationsTest {

    @Test
    public void testOptimizations() {

        final ExamplePalindrome example = ExamplePalindrome.createExample();
        final SBA<?, Character> sba = example.getReferenceAutomaton();
        final ProceduralInputAlphabet<Character> alphabet = example.getAlphabet();

        final SimulatorOracle<Character, Boolean> mqo = new SimulatorOracle<>(sba);

        final SampleSetEQOracle<Character, Boolean> eqo1 = new SampleSetEQOracle<>(false);
        eqo1.addAll(mqo, Word.fromString("STcTSaSaRaRRcRR"));
        final SimulatorEQOracle<Character> eqo2 = new SimulatorEQOracle<>(sba);
        final EQOracleChain<SBA<?, Character>, Character, Boolean> eqo = new EQOracleChain<>(Arrays.asList(eqo1, eqo2));

        final LearnerConstructor<TTTAdapterDFA<SymbolWrapper<Character>>, SymbolWrapper<Character>, Boolean> cons =
                TTTAdapterDFA::new;
        final SBALearner<Character, ?> learner = new SBALearner<>(alphabet, mqo, cons);

        final Experiment<SBA<?, Character>> experiment = new Experiment<>(learner, eqo, alphabet);

        experiment.run();

        Assert.assertTrue(Automata.testEquivalence(sba, experiment.getFinalHypothesis(), alphabet));
    }
}
