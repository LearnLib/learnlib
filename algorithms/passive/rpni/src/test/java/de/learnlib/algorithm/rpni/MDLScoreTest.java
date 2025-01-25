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
package de.learnlib.algorithm.rpni;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import de.learnlib.datastructure.pta.BlueFringePTA;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.common.smartcollection.IntSeq;
import net.automatalib.word.Word;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Unit test for computing an MDL score. The example is based on chapter 14.4 of the book "Grammatical Inference" by
 * Colin de la Higuera.
 */
@Test
public class MDLScoreTest {

    private Alphabet<Character> alphabet;
    private Alphabet<Integer> alphabetAsInt;

    private List<Word<Character>> positiveSamples;
    private List<IntSeq> positiveSamplesAsIntSeq;

    @BeforeClass
    public void setUp() {
        alphabet = Alphabets.fromArray('a', 'b');
        alphabetAsInt = Alphabets.fromArray(0, 1);

        final Word<Character> p1 = Word.fromLetter('a');
        final Word<Character> p2 = Word.fromString("aa");
        final Word<Character> p3 = Word.fromString("bb");
        final Word<Character> p4 = Word.fromString("aaa");
        final Word<Character> p5 = Word.fromString("bba");
        final Word<Character> p6 = Word.fromString("aaaa");
        final Word<Character> p7 = Word.fromString("abba");
        final Word<Character> p8 = Word.fromString("bbbb");

        positiveSamples = Arrays.asList(p1, p2, p3, p4, p5, p6, p7, p8);
        positiveSamplesAsIntSeq = positiveSamples.stream().map(s -> s.asIntSeq(alphabet)).collect(Collectors.toList());
    }

    @Test
    public void testPTAValue() {
        final BlueFringePTA<Boolean, Void> pta = new BlueFringePTA<>(alphabet.size());

        for (IntSeq w : positiveSamplesAsIntSeq) {
            pta.addSample(w, true);
        }

        Assert.assertEquals(pta.size(), 13);

        final double encodingInformation = MDLUtil.score(pta, alphabet.size(), positiveSamplesAsIntSeq);

        Assert.assertTrue(51.67 < encodingInformation);
        Assert.assertTrue(encodingInformation < 51.68);
    }

    @Test
    public void testFinalHypothesis() {
        final BlueFringeMDLDFA<Integer> learner = new BlueFringeMDLDFA<>(alphabetAsInt);

        positiveSamples.stream()
                       .map(w -> w.transform(alphabetAsInt.translateFrom(alphabet)))
                       .forEach(learner::addPositiveSample);

        final DFA<?, Integer> model = learner.computeModel();

        Assert.assertEquals(model.size(), 2);
        Assert.assertTrue(model.accepts(Word.fromSymbols(0)));
        Assert.assertFalse(model.accepts(Word.fromSymbols(1)));
        Assert.assertTrue(model.accepts(Word.fromSymbols(1, 1)));

        final double finalEncodingInformation = MDLUtil.score(model, alphabet.size(), positiveSamplesAsIntSeq);

        // the official value of the book (43.68) is wrong. if computed by hand the value should be around 45.21
        Assert.assertTrue(45.2 < finalEncodingInformation);
        Assert.assertTrue(finalEncodingInformation < 45.21);
    }
}
