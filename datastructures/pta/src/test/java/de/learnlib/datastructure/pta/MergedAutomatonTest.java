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
package de.learnlib.datastructure.pta;

import java.util.Arrays;
import java.util.List;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.UniversalDeterministicAutomaton;
import net.automatalib.word.Word;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test if the inferred automaton of a merge result behaves correctly.
 */
@Test
public class MergedAutomatonTest {

    /**
     * This test checks the merge step of Fig. 12.25 of the book "Grammatical Inference" by Colin de la Higuera.
     */
    @Test
    public void testMerge() {

        final Alphabet<Character> alphabet = Alphabets.fromArray('a', 'b');

        final Word<Character> p1 = Word.fromString("aaa");
        final Word<Character> p2 = Word.fromString("aaba");
        final Word<Character> p3 = Word.fromString("bba");
        final Word<Character> p4 = Word.fromString("bbaba");

        final Word<Character> n1 = Word.fromLetter('a');
        final Word<Character> n2 = Word.fromString("bb");
        final Word<Character> n3 = Word.fromString("aab");
        final Word<Character> n4 = Word.fromString("aba");

        final List<Word<Character>> positiveSamples = Arrays.asList(p1, p2, p3, p4);
        final List<Word<Character>> negativeSamples = Arrays.asList(n1, n2, n3, n4);

        final BlueFringePTA<Boolean, Void> pta = new BlueFringePTA<>(alphabet.size());

        for (Word<Character> w : positiveSamples) {
            pta.addSample(w.asIntSeq(alphabet), true);
        }
        for (Word<Character> w : negativeSamples) {
            pta.addSample(w.asIntSeq(alphabet), false);
        }

        // the PTA works on an Integer alphabet abstraction, hence a -> 0, b -> 1
        final BlueFringePTAState<Boolean, Void> q2 = pta.getState(Word.fromLetter(0));
        final BlueFringePTAState<Boolean, Void> q3 = pta.getState(Word.fromLetter(1));
        final BlueFringePTAState<Boolean, Void> q4 = pta.getState(Word.fromSymbols(0, 0));
        final BlueFringePTAState<Boolean, Void> q6 = pta.getState(Word.fromSymbols(0, 0, 0));

        // fast-forward algorithm
        pta.init((q) -> {});
        pta.promote(q2, (q) -> {});
        pta.promote(q3, (q) -> {});

        final RedBlueMerge<BlueFringePTAState<Boolean, Void>, Boolean, Void> merge = pta.tryMerge(q3, q4);
        Assert.assertNotNull(merge);

        final UniversalDeterministicAutomaton<BlueFringePTAState<Boolean, Void>, Integer, ?, Boolean, Void>
                mergedAutomaton = merge.toMergedAutomaton();

        // subtree of 3 states has been subsumed
        Assert.assertEquals(pta.size() - 3, mergedAutomaton.size());

        // the PTA works on an Integer alphabet abstraction, hence a -> 0, b -> 1
        Assert.assertEquals(mergedAutomaton.getState(Word.fromSymbols(0, 0)), q3);
        Assert.assertEquals(mergedAutomaton.getSuccessor(q2, 0), q3);

        Assert.assertEquals(mergedAutomaton.getState(Word.fromSymbols(1, 0)), q6);
        Assert.assertEquals(mergedAutomaton.getSuccessor(q3, 0), q6);
    }
}
