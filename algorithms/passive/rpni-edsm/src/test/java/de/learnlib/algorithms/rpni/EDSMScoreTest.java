/* Copyright (C) 2013-2020 TU Dortmund
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
package de.learnlib.algorithms.rpni;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.learnlib.datastructure.pta.pta.BlueFringePTA;
import de.learnlib.datastructure.pta.pta.BlueFringePTAState;
import de.learnlib.datastructure.pta.pta.RedBlueMerge;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Unit test for computing an EDSM score. The example is based on chapter 14.5 of the book "Grammatical Inference" by
 * Colin de la Higuera.
 *
 * @author frohme
 */
@Test
public class EDSMScoreTest {

    private Alphabet<Character> alphabet;

    private List<int[]> positiveSamplesAsIntArray;
    private List<int[]> negativeSamplesAsIntArray;

    @BeforeClass
    public void setUp() {
        alphabet = Alphabets.fromArray('a', 'b');

        final Word<Character> p1 = Word.fromString("a");
        final Word<Character> p2 = Word.fromString("aaa");
        final Word<Character> p3 = Word.fromString("bba");
        final Word<Character> p4 = Word.fromString("abab");

        final Word<Character> n1 = Word.fromString("ab");
        final Word<Character> n2 = Word.fromString("bb");

        positiveSamplesAsIntArray =
                Stream.of(p1, p2, p3, p4).map(w -> w.toIntArray(alphabet)).collect(Collectors.toList());
        negativeSamplesAsIntArray = Stream.of(n1, n2).map(w -> w.toIntArray(alphabet)).collect(Collectors.toList());
    }

    @Test
    public void testValue() {

        final BlueFringePTA<Boolean, Void> pta = initializePTA();

        // the PTA works on an Integer alphabet abstraction, hence a -> 0, b -> 1
        final BlueFringePTAState<Boolean, Void> qEpsilon = pta.getState(Word.epsilon());
        final BlueFringePTAState<Boolean, Void> qA = pta.getState(Word.fromSymbols(0));
        final BlueFringePTAState<Boolean, Void> qB = pta.getState(Word.fromSymbols(1));

        final RedBlueMerge<Boolean, Void, BlueFringePTAState<Boolean, Void>> merge1 = pta.tryMerge(qEpsilon, qB);
        Assert.assertNotNull(merge1);
        Assert.assertEquals(2L,
                            EDSMUtil.score(merge1.toMergedAutomaton(),
                                           positiveSamplesAsIntArray,
                                           negativeSamplesAsIntArray));

        final RedBlueMerge<Boolean, Void, BlueFringePTAState<Boolean, Void>> merge2 = pta.tryMerge(qA, qB);
        Assert.assertNotNull(merge2);
        // book is wrong, should be 2
        Assert.assertEquals(2L,
                            EDSMUtil.score(merge2.toMergedAutomaton(),
                                           positiveSamplesAsIntArray,
                                           negativeSamplesAsIntArray));
    }

    /*
     * Build PTA from Fig 14.12
     */
    private BlueFringePTA<Boolean, Void> initializePTA() {
        // We need use an alternate sample set to construct the backloop,
        final Word<Character> p1 = Word.fromString("a");
        final Word<Character> p2 = Word.fromString("aaaba");
        final Word<Character> p3 = Word.fromString("aaabab");
        final Word<Character> p4 = Word.fromString("bba");

        final Word<Character> n1 = Word.fromString("ab");
        final Word<Character> n2 = Word.fromString("bb");

        final BlueFringePTA<Boolean, Void> pta = new BlueFringePTA<>(alphabet.size());

        Stream.of(p1, p2, p3, p4).map(w -> w.toIntArray(alphabet)).forEach(s -> pta.addSample(s, true));
        Stream.of(n1, n2).map(w -> w.toIntArray(alphabet)).forEach(s -> pta.addSample(s, false));

        // the PTA works on an Integer alphabet abstraction, hence a -> 0, b -> 1
        final BlueFringePTAState<Boolean, Void> qEpsilon = pta.getState(Word.epsilon());
        final BlueFringePTAState<Boolean, Void> qA = pta.getState(Word.fromSymbols(0));
        final BlueFringePTAState<Boolean, Void> qAA = pta.getState(Word.fromSymbols(0, 0));

        pta.init((q) -> {});
        pta.promote(qA, (q) -> {});
        final RedBlueMerge<Boolean, Void, BlueFringePTAState<Boolean, Void>> merge = pta.tryMerge(qEpsilon, qAA);
        Assert.assertNotNull(merge);

        merge.apply(pta, (q) -> {});

        return pta;
    }
}