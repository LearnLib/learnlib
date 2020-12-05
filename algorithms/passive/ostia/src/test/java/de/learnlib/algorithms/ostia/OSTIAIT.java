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
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import de.learnlib.examples.mealy.ExampleRandomMealy;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.commons.util.Pair;
import net.automatalib.util.automata.Automata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;
import org.testng.Assert;
import org.testng.ITest;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

public class OSTIAIT {

    private static final Alphabet<Character> RANDOM_ALPHABET = Alphabets.characters('a', 'c');
    private static final int RANDOM_SIZE = 100;
    private static final String[] RANDOM_MEALY_OUTPUTS = {"o1", "o2", "o3"};
    private static final long RANDOM_SEED = 1337L;

    @Factory
    public Object[] createITCases() {

        final List<TestCase<?, ?>> result = new ArrayList<>();

        //        for (MealyLearningExample<?, ?> example : LearningExamples.createMealyExamples()) {
        //            result.add(new TestCase<Object, Object>(example.getReferenceAutomaton(), example.getAlphabet()));
        //        }

        final ExampleRandomMealy<Character, String> example = ExampleRandomMealy.createExample(new Random(RANDOM_SEED),
                                                                                               RANDOM_ALPHABET,
                                                                                               RANDOM_SIZE,
                                                                                               RANDOM_MEALY_OUTPUTS);

        result.add(new TestCase<>(example.getReferenceAutomaton(),
                                  RANDOM_ALPHABET,
                                  Alphabets.fromArray(RANDOM_MEALY_OUTPUTS)));

        return result.toArray();
    }

    static class TestCase<I, O> implements ITest {

        private final MealyMachine<?, I, ?, O> automaton;
        private final Alphabet<I> inputAlphabet;
        private final Alphabet<O> outputAlphabet;

        public TestCase(MealyMachine<?, I, ?, O> automaton, Alphabet<I> inputAlphabet, Alphabet<O> outputAlphabet) {
            this.automaton = automaton;
            this.inputAlphabet = inputAlphabet;
            this.outputAlphabet = outputAlphabet;
        }

        @Test
        public void test() {

            final List<Word<I>> characterizingSet = Automata.characterizingSet(automaton, inputAlphabet);

            final List<Pair<IntSeq, IntSeq>> informant = new ArrayList<>(characterizingSet.size());

            for (Word<I> input : characterizingSet) {
                final Word<O> output = automaton.computeOutput(input);

                final IntSeq inSeq = IntSeq.seq(input.stream().mapToInt(inputAlphabet).toArray());
                final IntSeq outSeq = IntSeq.seq(output.stream().mapToInt(outputAlphabet).toArray());
                informant.add(Pair.of(inSeq, outSeq));
            }

            State root = OSTIA.buildPtt(inputAlphabet.size(), informant.iterator());
            OSTIA.ostia(root);

            for (Word<I> input : characterizingSet) {
                final Iterator<Integer> inIter = input.stream().mapToInt(inputAlphabet).iterator();

                final ArrayList<Integer> output = OSTIA.run(root, inIter);
                final List<Integer> expectedOutput = automaton.computeOutput(input)
                                                              .stream()
                                                              .map(outputAlphabet::applyAsInt)
                                                              .collect(Collectors.toList());

                Assert.assertEquals(output, expectedOutput);
            }
        }

        @Override
        public String getTestName() {
            return "OSTIA";
        }
    }
}
