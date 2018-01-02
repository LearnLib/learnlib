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
package de.learnlib.datastructure.pta;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.learnlib.datastructure.pta.pta.BlueFringePTA;
import de.learnlib.datastructure.pta.pta.BlueFringePTAState;
import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test if the inferred automaton of a merge result behaves correctly.
 *
 * @author frohme
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

        final Word<Character> n1 = Word.fromString("a");
        final Word<Character> n2 = Word.fromString("bb");
        final Word<Character> n3 = Word.fromString("aab");
        final Word<Character> n4 = Word.fromString("aba");

        final List<int[]> positiveSamples =
                Stream.of(p1, p2, p3, p4).map(w -> w.toIntArray(alphabet)).collect(Collectors.toList());
        final List<int[]> negativeSamples =
                Stream.of(n1, n2, n3, n4).map(w -> w.toIntArray(alphabet)).collect(Collectors.toList());

        final BlueFringePTA<Boolean, Void> pta = new BlueFringePTA<>(alphabet.size());

        for (final int[] w : positiveSamples) {
            pta.addSample(w, true);
        }
        for (final int[] w : negativeSamples) {
            pta.addSample(w, false);
        }

        // the PTA works on an Integer alphabet abstraction, hence a -> 0, b -> 1
        final BlueFringePTAState<Boolean, Void> q2 = pta.getState(Word.fromSymbols(0));
        final BlueFringePTAState<Boolean, Void> q3 = pta.getState(Word.fromSymbols(1));
        final BlueFringePTAState<Boolean, Void> q4 = pta.getState(Word.fromSymbols(0, 0));
        final BlueFringePTAState<Boolean, Void> q6 = pta.getState(Word.fromSymbols(0, 0, 0));

        // fast forward algorithm
        pta.init((q) -> {});
        pta.promote(q2, (q) -> {});
        pta.promote(q3, (q) -> {});

        final UniversalDeterministicAutomaton<BlueFringePTAState<Boolean, Void>, Integer, ?, Boolean, Void>
                mergedAutomaton = pta.tryMerge(q3, q4).toMergedAutomaton();

        // subtree of 3 states has been subsumed
        Assert.assertEquals(pta.size() - 3, mergedAutomaton.size());

        Assert.assertEquals(mergedAutomaton.getState(Word.fromSymbols(0, 0)), q3);
        Assert.assertEquals(mergedAutomaton.getSuccessor(q2, 0), q3);

        Assert.assertEquals(mergedAutomaton.getState(Word.fromSymbols(1, 0)), q6);
        Assert.assertEquals(mergedAutomaton.getSuccessor(q3, 0), q6);
    }
}
