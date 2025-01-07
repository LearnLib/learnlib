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
package de.learnlib.algorithm.ostia;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.transducer.SubsequentialTransducer;
import net.automatalib.automaton.transducer.impl.CompactSST;
import net.automatalib.common.smartcollection.IntSeq;
import net.automatalib.common.util.IOUtil;
import net.automatalib.common.util.Pair;
import net.automatalib.common.util.collection.CollectionUtil;
import net.automatalib.common.util.collection.IterableUtil;
import net.automatalib.serialization.dot.GraphDOT;
import net.automatalib.util.automaton.Automata;
import net.automatalib.util.automaton.conformance.WMethodTestsIterator;
import net.automatalib.util.automaton.random.RandomAutomata;
import net.automatalib.util.automaton.transducer.SubsequentialTransducers;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;
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
     * Returns the examples from Section 18.3.4 of Colin de la Higuera's book "Grammatical Inference" with a's encoded
     * as 0 and b's encoded as 1.
     *
     * @return the encoded traces
     */
    public List<Pair<IntSeq, IntSeq>> getExampleSamples() {
        return Arrays.asList(Pair.of(IntSeq.of(0), IntSeq.of(1)),
                             Pair.of(IntSeq.of(1), IntSeq.of(1)),
                             Pair.of(IntSeq.of(0, 0), IntSeq.of(0, 1)),
                             Pair.of(IntSeq.of(0, 0, 0), IntSeq.of(0, 0, 1)),
                             Pair.of(IntSeq.of(0, 1, 0, 1), IntSeq.of(0, 1, 0, 1)));
    }

    @Test
    public void testStaticInvocation() {
        final List<Pair<IntSeq, IntSeq>> samples = getExampleSamples();

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

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testInconsistentSamples() {
        final List<Pair<IntSeq, IntSeq>> samples = new ArrayList<>(getExampleSamples());

        samples.add(Pair.of(IntSeq.of(0, 1, 0, 1), IntSeq.of(0, 1, 0, 0)));

        OSTIA.buildPtt(2, samples.iterator());
    }

    @Test
    public void testVisualization() throws IOException {
        final Alphabet<Integer> alphabet = Alphabets.integers(0, 1);
        final OSTIA<Integer, Integer> learner = new OSTIA<>(alphabet);

        final WordBuilder<Integer> wb = new WordBuilder<>();
        for (Pair<IntSeq, IntSeq> p : getExampleSamples()) {
            CollectionUtil.add(wb, p.getFirst().iterator());
            final Word<Integer> input = wb.toWord();
            wb.clear();

            CollectionUtil.add(wb, p.getSecond().iterator());
            final Word<Integer> output = wb.toWord();
            wb.clear();

            learner.addSample(input, output);
        }

        final SubsequentialTransducer<?, Integer, ?, Integer> model = learner.computeModel();
        final String expectedHyp;

        try (InputStream is = getClass().getResourceAsStream("/hyp.dot")) {
            assert is != null;
            expectedHyp = IOUtil.toString(IOUtil.asBufferedUTF8Reader(is));
        }

        final StringWriter actualHyp = new StringWriter();
        GraphDOT.write(model, alphabet, actualHyp);

        Assert.assertEquals(actualHyp.toString(), expectedHyp);
    }

    @Test(dataProvider = "sizes")
    public void testEquivalence(int size) {

        final Random random = new Random(SEED);
        final CompactSST<Character, String> sst = new CompactSST<>(INPUTS);

        final List<Word<String>> words = new ArrayList<>();
        for (List<String> t : IterableUtil.allTuples(OUTPUTS, 0, 3)) {
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
