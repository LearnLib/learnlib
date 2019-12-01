/* Copyright (C) 2013-2019 TU Dortmund
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
package de.learnlib.examples.mealy;

import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import de.learnlib.examples.LearningExample.StateLocalInputMealyLearningExample;
import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.commons.util.random.RandomUtil;
import net.automatalib.util.automata.random.RandomAutomata;
import net.automatalib.words.Alphabet;

public class ExampleRandomStateLocalInputMealy<I, O> implements StateLocalInputMealyLearningExample<I, O> {

    private final CompactMealy<I, O> automaton;

    @SafeVarargs
    public ExampleRandomStateLocalInputMealy(Alphabet<I> alphabet, int size, O... outputs) {
        this(new Random(), alphabet, size, outputs);
    }

    @SafeVarargs
    public ExampleRandomStateLocalInputMealy(Random random, Alphabet<I> alphabet, int size, O... outputs) {
        this.automaton = RandomAutomata.randomDeterministic(random,
                                                            size,
                                                            alphabet,
                                                            Collections.emptyList(),
                                                            Arrays.asList(outputs),
                                                            new CompactMealy<>(alphabet));

        final int alphabetSize = alphabet.size();

        for (int i = 0; i < size; i++) {
            // randomly remove some transitions
            for (int idx : RandomUtil.distinctIntegers(random.nextInt(alphabetSize), alphabetSize, random)) {
                this.automaton.removeAllTransitions(i, alphabet.getSymbol(idx));
            }
        }
    }

    @SafeVarargs
    public static <I, O> ExampleRandomStateLocalInputMealy<I, O> createExample(Random random,
                                                                               Alphabet<I> alphabet,
                                                                               int size,
                                                                               O... outputs) {
        return new ExampleRandomStateLocalInputMealy<>(random, alphabet, size, outputs);
    }

    @Override
    public CompactMealy<I, O> getReferenceAutomaton() {
        return this.automaton;
    }

    @Override
    public Alphabet<I> getAlphabet() {
        return this.automaton.getInputAlphabet();
    }
}
