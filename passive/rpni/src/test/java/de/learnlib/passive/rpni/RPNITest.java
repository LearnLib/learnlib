/* Copyright (C) 2013-2017 TU Dortmund
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
package de.learnlib.passive.rpni;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import de.learnlib.oracles.DefaultQuery;
import de.learnlib.passive.api.PassiveLearningAlgorithm;
import net.automatalib.automata.concepts.Output;
import net.automatalib.automata.concepts.SuffixOutput;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.util.automata.random.RandomAutomata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import net.automatalib.words.impl.Alphabets;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test
public class RPNITest {

    private List<List<DefaultQuery<Integer, Boolean>>> sampleSets = new ArrayList<>();

    private List<List<DefaultQuery<Integer, Word<String>>>> sampleSetsMealy = new ArrayList<>();

    @BeforeClass
    public void setUp() {
        Alphabet<Integer> alphabet = Alphabets.integers(0, 4);
        CompactDFA<Integer> dfa = RandomAutomata.randomICDFA(new Random(), 100, alphabet, true);
        sampleSets.add(createSample(new Random(2), dfa, alphabet, 100, 30));

        CompactMealy<Integer, String> mealy =
                RandomAutomata.randomMealy(new Random(), 100, alphabet, Arrays.asList("foo", "bar"));
        sampleSetsMealy.add(createSample(new Random(2), mealy, alphabet, 100, 30));
    }

    private <I, D> List<DefaultQuery<I, D>> createSample(Random r,
                                                         Output<I, D> out,
                                                         Alphabet<I> inputs,
                                                         int size,
                                                         int maxLength) {
        List<DefaultQuery<I, D>> result = new ArrayList<>(size);
        int alphabetSize = inputs.size();

        for (int i = 0; i < size; i++) {
            int len = r.nextInt(maxLength + 1);
            WordBuilder<I> wb = new WordBuilder<>();
            for (int j = 0; j < len; j++) {
                wb.add(inputs.getSymbol(r.nextInt(alphabetSize)));
            }
            Word<I> input = wb.toWord();
            D output = out.computeOutput(input);
            DefaultQuery<I, D> qry = new DefaultQuery<>(input, output);
            result.add(qry);
        }
        return result;
    }

    @Test
    public void testRPNIDFA() {
        for (Collection<? extends DefaultQuery<Integer, Boolean>> sampleSet : sampleSets) {
            BlueFringeRPNIDFA<Integer> learner = new BlueFringeRPNIDFA<>(Alphabets.integers(0, 4));
            testLearner(learner, sampleSet);
        }
    }

    private <I, D, M extends SuffixOutput<I, D>> void testLearner(PassiveLearningAlgorithm<M, I, D> learner,
                                                                  Collection<? extends DefaultQuery<I, D>> sample) {
        learner.addSamples(sample);
        M model = learner.computeModel();
        for (DefaultQuery<I, D> qry : sample) {
            Assert.assertEquals(model.computeSuffixOutput(qry.getPrefix(), qry.getSuffix()), qry.getOutput());
        }
    }

    @Test
    public void testRPNIMealy() {
        for (Collection<? extends DefaultQuery<Integer, Word<String>>> sampleSet : sampleSetsMealy) {
            BlueFringeRPNIMealy<Integer, String> learner = new BlueFringeRPNIMealy<>(Alphabets.integers(0, 4));
            testLearner(learner, sampleSet);
        }
    }

}
