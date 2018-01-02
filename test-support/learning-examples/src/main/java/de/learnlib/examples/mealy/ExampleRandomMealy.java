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
package de.learnlib.examples.mealy;

import java.util.Arrays;
import java.util.Random;

import de.learnlib.examples.DefaultLearningExample.DefaultMealyLearningExample;
import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.util.automata.random.RandomAutomata;
import net.automatalib.words.Alphabet;

public class ExampleRandomMealy<I, O> extends DefaultMealyLearningExample<I, O> {

    @SafeVarargs
    public ExampleRandomMealy(Alphabet<I> alphabet, int size, O... outputs) {
        this(new Random(), alphabet, size, outputs);
    }

    @SafeVarargs
    public ExampleRandomMealy(Random random, Alphabet<I> alphabet, int size, O... outputs) {
        super(RandomAutomata.randomDeterministic(random,
                                                 size,
                                                 alphabet,
                                                 null,
                                                 Arrays.asList(outputs),
                                                 new CompactMealy<>(alphabet)));
    }

    @SafeVarargs
    public static <I, O> ExampleRandomMealy<I, O> createExample(Random random,
                                                                Alphabet<I> alphabet,
                                                                int size,
                                                                O... outputs) {
        return new ExampleRandomMealy<>(random, alphabet, size, outputs);
    }

}
