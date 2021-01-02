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

import com.google.common.collect.Iterators;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.automata.transducers.SubsequentialTransducer;
import net.automatalib.automata.transducers.impl.compact.CompactSST;
import net.automatalib.commons.smartcollections.IntSeq;
import net.automatalib.commons.util.Pair;
import net.automatalib.commons.util.collections.CollectionsUtil;
import net.automatalib.util.automata.Automata;
import net.automatalib.util.automata.conformance.WMethodTestsIterator;
import net.automatalib.util.automata.random.RandomAutomata;
import net.automatalib.util.automata.transducers.SubsequentialTransducers;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class OSTIATest {

    private static final Alphabet<Character> INPUTS = Alphabets.characters('a', 'c');
    private static final Collection<String> OUTPUTS = Arrays.asList("o1", "o2", "o3");
    private static final long SEED = 1337L;

    @DataProvider(name = "sizes")
    public static Object[][] sizes() {
        return new Object[][] {{10}, {25}, {50}, {100}};
    }

    /**
     * Tests the example from Section 18.3.4 of Colin de la Higuera's book "Grammatical Inference".
     */
    @Test
    public void testStaticInvocation() {
        // a = 0, b = 1
        final List<Pair<IntSeq, IntSeq>> samples = Arrays.asList(Pair.of(IntSeq.of(0), IntSeq.of(1)),
                                                                 Pair.of(IntSeq.of(1), IntSeq.of(1)),
                                                                 Pair.of(IntSeq.of(0, 0), IntSeq.of(0, 1)),
                                                                 Pair.of(IntSeq.of(0, 0, 0), IntSeq.of(0, 0, 1)),
                                                                 Pair.of(IntSeq.of(0, 1, 0, 1), IntSeq.of(0, 1, 0, 1)));

        final State root = OSTIA.buildPtt(2, samples.iterator());
        OSTIA.ostia(root);

        Assert.assertEquals(OSTIA.run(root, IntSeq.of(1)), IntSeq.of(1));
        Assert.assertEquals(OSTIA.run(root, IntSeq.of(1, 0)), IntSeq.of(1, 1));
        Assert.assertEquals(OSTIA.run(root, IntSeq.of(1, 0, 0)), IntSeq.of(1, 0, 1));
        Assert.assertEquals(OSTIA.run(root, IntSeq.of(1, 0, 0, 1)), IntSeq.of(1, 0, 0, 1, 0, 1));
        Assert.assertEquals(OSTIA.run(root, IntSeq.of(1, 0, 0, 1, 0)), IntSeq.of(1, 0, 0, 1, 0, 1));
        Assert.assertEquals(OSTIA.run(root, IntSeq.of(1, 0, 0, 1, 0, 1)), IntSeq.of(1, 0, 0, 1, 0, 1));

        Assert.assertEquals(OSTIA.run(root, IntSeq.of(0, 1, 0, 1, 0)), IntSeq.of(0, 1, 0, 1, 1));
        Assert.assertEquals(OSTIA.run(root, IntSeq.of(0, 1, 0, 1, 1)), IntSeq.of(0, 1, 0, 1, 1));
    }

    @Test(enabled = false, dataProvider = "sizes")
    public void testMealySamples(int size) {

        final MealyMachine<?, Character, ?, String> automaton =
                RandomAutomata.randomMealy(new Random(SEED), size, INPUTS, OUTPUTS);

        final OSTIA<Character, String> learner = new OSTIA<>(INPUTS);

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

    @Test(dataProvider = "sizes")
    public void testEquivalence(int size) {

        final Random random = new Random(SEED);
        final CompactSST<Character, String> sst = new CompactSST<>(INPUTS);

        final List<Word<String>> words = new ArrayList<>();
        for (List<String> t : CollectionsUtil.allTuples(OUTPUTS, 0, 3)) {
            words.add(Word.fromList(t));
        }

        Collections.shuffle(words, random);
        final int midpoint = words.size() / 2;
        final Collection<Word<String>> stateProps = words.subList(0, midpoint);
        final Collection<Word<String>> transProps = words.subList(midpoint, words.size());

        RandomAutomata.randomDeterministic(random, size, INPUTS, stateProps, transProps, sst);
        final SubsequentialTransducer<?, Character, ?, String> osst =
                SubsequentialTransducers.toOnwardSST(sst, INPUTS, new CompactSST<>(INPUTS));
        Assert.assertTrue(SubsequentialTransducers.isOnwardSST(osst, INPUTS));

        final OSTIA<Character, String> learner = new OSTIA<>(INPUTS);

        final int lookAhead = 2;
        final Iterator<Word<Character>> testIterator = new WMethodTestsIterator<>(sst, INPUTS, lookAhead);

        learner.addSample(Word.epsilon(), sst.computeOutput(Word.epsilon()));
        while (testIterator.hasNext()) {
            final Word<Character> test = testIterator.next();
            learner.addSample(test, sst.computeOutput(test));
        }

        final SubsequentialTransducer<?, Character, ?, String> model = learner.computeModel();
        Assert.assertTrue(Automata.testEquivalence(osst, model, INPUTS));
    }
}
