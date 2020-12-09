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
import net.automatalib.automata.transducers.impl.compact.CompactOST;
import net.automatalib.automata.transducers.impl.compact.SequentialTransducer;
import net.automatalib.commons.util.collections.CollectionsUtil;
import net.automatalib.util.automata.Automata;
import net.automatalib.util.automata.conformance.WMethodTestsIterator;
import net.automatalib.util.automata.conformance.WpMethodTestsIterator;
import net.automatalib.util.automata.random.RandomAutomata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;
import org.testng.Assert;
import org.testng.annotations.Test;

public class OSTIATest {

    private static final Alphabet<Character> INPUTS = Alphabets.characters('a', 'c');
    private static final Collection<String> OUTPUTS = Arrays.asList("o1", "o2", "o3");
    private static final int SIZE = 100;
    private static final long SEED = 1337L;

    @Test
    public void testMealySamples() {

        final MealyMachine<?, Character, ?, String> automaton =
                RandomAutomata.randomMealy(new Random(SEED), SIZE, INPUTS, OUTPUTS);

        final OSTIA<Character, String> learner = new OSTIA<>(INPUTS, Alphabets.fromCollection(OUTPUTS));

        final List<Word<Character>> trainingWords = new ArrayList<>();
        final Iterator<Word<Character>> testIterator = new WMethodTestsIterator<>(automaton, INPUTS, 0);
        Iterators.addAll(trainingWords, testIterator);

        for (Word<Character> input : trainingWords) {
            learner.addSample(input, automaton.computeOutput(input));
        }

        final SequentialTransducer<?, Character, ?, String> model = learner.computeModel();

        for (Word<Character> input : trainingWords) {
            final Word<String> output = model.computeOutput(input);
            final Word<String> expectedOutput = automaton.computeOutput(input);

            Assert.assertEquals(output, expectedOutput);
        }
    }

    @Test(enabled = false)
    public void testEquivalence() {

        final Random random = new Random(SEED);
        final CompactOST<Character, String> automaton = new CompactOST<>(INPUTS);

        final List<Word<String>> words = new ArrayList<>();
        for (List<String> t : CollectionsUtil.allTuples(OUTPUTS, 1, 3)) {
            words.add(Word.fromList(t));
        }

        Collections.shuffle(words, random);
        final int midpoint = words.size() / 2;
        Collection<Word<String>> stateProps = words.subList(0, midpoint);
        Collection<Word<String>> transProps = words.subList(midpoint, words.size());

        RandomAutomata.randomDeterministic(random, SIZE, INPUTS, stateProps, transProps, automaton);

        final OSTIA<Character, String> learner = new OSTIA<>(INPUTS, Alphabets.fromCollection(OUTPUTS));

        final WpMethodTestsIterator<Character> wpIterator = new WpMethodTestsIterator<>(automaton, INPUTS, 0);

        while (wpIterator.hasNext()) {
            final Word<Character> input = wpIterator.next();
            learner.addSample(input, automaton.computeOutput(input));
        }

        final SequentialTransducer<?, Character, ?, String> model = learner.computeModel();

//        Word<Character> sepWord = Automata.findSeparatingWord(automaton, model, INPUTS);
//        Word<String> autOut = automaton.computeOutput(sepWord);
//        Word<String> modelOut = model.computeOutput(sepWord);
//
//        System.err.println("sepWord: " + sepWord);
//        System.err.println("automaton: " + autOut);
//        System.err.println("model: " + modelOut);

        Assert.assertTrue(Automata.testEquivalence(automaton, model, INPUTS));
    }

}
