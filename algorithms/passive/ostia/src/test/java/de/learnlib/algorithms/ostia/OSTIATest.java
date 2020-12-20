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
package de.learnlib.algorithms.ostia;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import com.google.common.collect.Iterators;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.automata.transducers.SubsequentialTransducer;
import net.automatalib.automata.transducers.SubsequentialTransducers;
import net.automatalib.automata.transducers.impl.compact.CompactSST;
import net.automatalib.commons.util.Pair;
import net.automatalib.commons.util.collections.CollectionsUtil;
import net.automatalib.util.automata.Automata;
import net.automatalib.util.automata.conformance.WMethodTestsIterator;
import net.automatalib.util.automata.random.RandomAutomata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;
import org.testng.Assert;
import org.testng.annotations.Test;

public class OSTIATest {

    private static final Alphabet<Character> INPUTS = Alphabets.characters('a', 'c');
    private static final Collection<String> OUTPUTS = Arrays.asList("o1", "o2", "o3");
    private static final long SEED = 1337L;

    /**
     * Tests the example from Section 18.3.4 of Colin de la Higuera's book "Grammatical Inference".
     */
    @Test
    public void testStaticInvocation() {
        // a = 0, b = 1
        List<Pair<IntSeq, IntSeq>> samples = Arrays.asList(Pair.of(IntSeq.seq(0), IntSeq.seq(1)),
                                                           Pair.of(IntSeq.seq(1), IntSeq.seq(1)),
                                                           Pair.of(IntSeq.seq(0, 0), IntSeq.seq(0, 1)),
                                                           Pair.of(IntSeq.seq(0, 0, 0), IntSeq.seq(0, 0, 1)),
                                                           Pair.of(IntSeq.seq(0, 1, 0, 1), IntSeq.seq(0, 1, 0, 1)));

        State root = OSTIA.buildPtt(2, samples.iterator());
        OSTIA.ostia(root);

        Assert.assertEquals(OSTIA.run(root, IntStream.of(1).iterator()), Collections.singletonList(1));
        Assert.assertEquals(OSTIA.run(root, IntStream.of(1, 0).iterator()), Arrays.asList(1, 1));
        Assert.assertEquals(OSTIA.run(root, IntStream.of(1, 0, 0).iterator()), Arrays.asList(1, 0, 1));
        Assert.assertEquals(OSTIA.run(root, IntStream.of(1, 0, 0, 1).iterator()), Arrays.asList(1, 0, 0, 1, 0, 1));
        Assert.assertEquals(OSTIA.run(root, IntStream.of(1, 0, 0, 1, 0).iterator()), Arrays.asList(1, 0, 0, 1, 0, 1));
        Assert.assertEquals(OSTIA.run(root, IntStream.of(1, 0, 0, 1, 0, 1).iterator()), Arrays.asList(1, 0, 0, 1, 0, 1));

        Assert.assertNull(OSTIA.run(root, IntStream.of(0, 1, 1).iterator()));
        Assert.assertNull(OSTIA.run(root, IntStream.of(0, 1, 0, 0).iterator()));
        Assert.assertNull(OSTIA.run(root, IntStream.of(0, 1, 0, 1, 0).iterator()));
        Assert.assertNull(OSTIA.run(root, IntStream.of(0, 1, 0, 1, 1).iterator()));
    }

    @Test
    public void testMealySamples() {

        final int SIZE = 10;
        final MealyMachine<?, Character, ?, String> automaton =
                RandomAutomata.randomMealy(new Random(SEED), SIZE, INPUTS, OUTPUTS);

        final OSTIA<Character, String> learner = new OSTIA<>(INPUTS, Alphabets.fromCollection(OUTPUTS));

        final List<Word<Character>> trainingWords = new ArrayList<>();
        final Iterator<Word<Character>> testIterator = new WMethodTestsIterator<>(automaton, INPUTS, 0);
        Iterators.addAll(trainingWords, testIterator);

        for (Word<Character> input : trainingWords) {
            learner.addSample(input, automaton.computeOutput(input));
        }

        final SubsequentialTransducer<?, Character, ?, String> model = learner.computeModel();

        for (Word<Character> input : trainingWords) {
            final Word<String> output = model.computeOutput(input);
            final Word<String> expectedOutput = automaton.computeOutput(input);

            Assert.assertEquals(output, expectedOutput);
        }
    }

    @Test
    public void testEquivalence() {

        final Random random = new Random(SEED);
        for (int size = 10; size < 20; size++) {
            System.out.println(size);
            final CompactSST<Character, String> sst = new CompactSST<>(INPUTS);

            final List<Word<String>> words = new ArrayList<>();
            for (List<String> t : CollectionsUtil.allTuples(OUTPUTS, 1, 3)) {
                words.add(Word.fromList(t));
            }

            Collections.shuffle(words, random);
            final int midpoint = words.size() / 2;
            final Collection<Word<String>> stateProps = words.subList(0, midpoint);
            final Collection<Word<String>> transProps = words.subList(midpoint, words.size());

            RandomAutomata.randomDeterministic(random, size, INPUTS, stateProps, transProps, sst);

            final OSTIA<Character, String> learner = new OSTIA<>(INPUTS, Alphabets.fromCollection(OUTPUTS));

            final Iterator<Word<Character>> testIterator = new WMethodTestsIterator<>(sst, INPUTS, 0);

            learner.addSample(Word.epsilon(), sst.computeOutput(Word.epsilon()));
            while (testIterator.hasNext()) {
                final Word<Character> input = testIterator.next();
                final Word<String> out = sst.computeOutput(input);
                System.out.println(input + "|" + out);
                learner.addSample(input, out);
                for (char lookahead1 : INPUTS) {
                    final Word<Character> inputLookahead1 = input.append(lookahead1);
                    final Word<String> outLookahead1 = sst.computeOutput(inputLookahead1);
                    learner.addSample(inputLookahead1, outLookahead1);
                    for (char lookahead2 : INPUTS) {
                        final Word<Character> inputLookahead2 = inputLookahead1.append(lookahead2);
                        final Word<String> outLookahead2 = sst.computeOutput(inputLookahead2);
                        learner.addSample(inputLookahead2, outLookahead2);
                    }
                }
            }

            final SubsequentialTransducer<?, Character, ?, String> model = learner.computeModel();
            final SubsequentialTransducer<?, Character, ?, String> osst =
                    SubsequentialTransducers.toOnwardSST(sst, INPUTS, new CompactSST<>(INPUTS));

//            System.err.println(osst.size());
//            printStateProperties(osst);
//            System.err.println("---");
//            System.err.println(model.size());
//            printStateProperties(model);

            final boolean areEquivalent = Automata.testEquivalence(osst, model, INPUTS);
            if (!areEquivalent) {
                System.err.println(osst.size());
                printStateProperties(osst);
                System.err.println("---");
                System.err.println(model.size());
                printStateProperties(model);

                Word<Character> sepWord = Automata.findSeparatingWord(osst, model, INPUTS);
                Word<String> autOut = osst.computeOutput(sepWord);
                Word<String> modelOut = model.computeOutput(sepWord);

                System.err.println("sepWord: " + sepWord);
                System.err.println("osst:  " + autOut);
                System.err.println("model: " + modelOut);
                // Visualization.visualize(sst);
                // Visualization.visualize(osst.transitionGraphView(INPUTS));
                // Visualization.visualize(model.transitionGraphView(INPUTS));
            }
            Assert.assertTrue(areEquivalent);
        }
    }

    private static <S, I, T, O> void printStateProperties(SubsequentialTransducer<S, I, T, O> sst) {
        for (S s : sst) {
            System.err.println(sst.getStateProperty(s));
        }
    }

}
