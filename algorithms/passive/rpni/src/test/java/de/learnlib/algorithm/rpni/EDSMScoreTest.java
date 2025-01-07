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
import java.util.stream.Stream;

import de.learnlib.datastructure.pta.BlueFringePTA;
import de.learnlib.datastructure.pta.BlueFringePTAState;
import de.learnlib.datastructure.pta.RedBlueMerge;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.fsa.impl.CompactDFA;
import net.automatalib.common.smartcollection.IntSeq;
import net.automatalib.util.automaton.Automata;
import net.automatalib.util.automaton.builder.AutomatonBuilders;
import net.automatalib.word.Word;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Unit test for computing an EDSM score. The example is based on chapter 14.5 of the book "Grammatical Inference" by
 * Colin de la Higuera.
 */
@Test
public class EDSMScoreTest {

    private final Alphabet<Character> alphabet = Alphabets.fromArray('a', 'b');

    private final Word<Character> p1 = Word.fromLetter('a');
    private final Word<Character> p2 = Word.fromString("aaa");
    private final Word<Character> p3 = Word.fromString("bba");
    private final Word<Character> p4 = Word.fromString("abab");

    private final Word<Character> p5 = Word.fromString("aaaba");
    private final Word<Character> p6 = Word.fromString("aaabab");

    private final Word<Character> n1 = Word.fromString("ab");
    private final Word<Character> n2 = Word.fromString("bb");

    private List<IntSeq> positiveSamples;
    private List<IntSeq> negativeSamples;

    @BeforeClass
    public void setUp() {
        positiveSamples = Stream.of(p1, p2, p3, p4).map(w -> w.asIntSeq(alphabet)).collect(Collectors.toList());
        negativeSamples = Stream.of(n1, n2).map(w -> w.asIntSeq(alphabet)).collect(Collectors.toList());
    }

    @Test
    public void testValue() {

        final BlueFringePTA<Boolean, Void> pta = initializePTA();

        // the PTA works on an Integer alphabet abstraction, hence a -> 0, b -> 1
        final BlueFringePTAState<Boolean, Void> qEpsilon = pta.getState(Word.epsilon());
        final BlueFringePTAState<Boolean, Void> qA = pta.getState(Word.fromLetter(0));
        final BlueFringePTAState<Boolean, Void> qB = pta.getState(Word.fromLetter(1));

        final RedBlueMerge<BlueFringePTAState<Boolean, Void>, Boolean, Void> merge1 = pta.tryMerge(qEpsilon, qB);
        Assert.assertNotNull(merge1);
        Assert.assertEquals(EDSMUtil.score(merge1.toMergedAutomaton(), positiveSamples, negativeSamples), 2L);

        final RedBlueMerge<BlueFringePTAState<Boolean, Void>, Boolean, Void> merge2 = pta.tryMerge(qA, qB);
        Assert.assertNotNull(merge2);
        // book is wrong, should be 2
        Assert.assertEquals(EDSMUtil.score(merge2.toMergedAutomaton(), positiveSamples, negativeSamples), 2L);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testFinalHypothesis() {

        final BlueFringeEDSMDFA<Character> learner = new BlueFringeEDSMDFA<>(alphabet);
        // add p5 to make the first merge deterministic
        learner.addPositiveSamples(p1, p2, p3, p4);
        learner.addNegativeSamples(n1, n2);

        // use reproducible runs
        learner.setParallel(false);
        learner.setDeterministic(true);

        final DFA<?, Character> model = learner.computeModel();

        // @formatter:off
        final CompactDFA<Character> expected = AutomatonBuilders.newDFA(alphabet)
                                                                .withInitial("s0")
                                                                .withAccepting("s0", "s2")
                                                                .from("s0").on('a').loop()
                                                                .from("s0").on('b').to("s1")
                                                                .from("s1").on('a').to("s2")
                                                                .from("s1").on('b').loop()
                                                                .from("s2").on('b').to("s0")
                                                                .create();
        // @formatter:on

        Assert.assertTrue(Automata.testEquivalence(model, expected, alphabet));
    }

    /*
     * Build PTA from Fig 14.12
     */
    private BlueFringePTA<Boolean, Void> initializePTA() {
        final BlueFringePTA<Boolean, Void> pta = new BlueFringePTA<>(alphabet.size());

        // We need use an alternate sample set to construct the back-edge,
        Arrays.asList(p1, p5, p6, p4).forEach(s -> pta.addSample(s.asIntSeq(alphabet), true));
        Arrays.asList(n1, n2).forEach(s -> pta.addSample(s.asIntSeq(alphabet), false));

        // the PTA works on an Integer alphabet abstraction, hence a -> 0, b -> 1
        final BlueFringePTAState<Boolean, Void> qEpsilon = pta.getState(Word.epsilon());
        final BlueFringePTAState<Boolean, Void> qA = pta.getState(Word.fromLetter(0));
        final BlueFringePTAState<Boolean, Void> qAA = pta.getState(Word.fromSymbols(0, 0));

        pta.init(q -> {});
        pta.promote(qA, q -> {});
        final RedBlueMerge<BlueFringePTAState<Boolean, Void>, Boolean, Void> merge = pta.tryMerge(qEpsilon, qAA);
        Assert.assertNotNull(merge);

        merge.apply(pta, q -> {});

        return pta;
    }
}
