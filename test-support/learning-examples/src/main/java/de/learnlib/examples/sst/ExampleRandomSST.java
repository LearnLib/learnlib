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
package de.learnlib.examples.sst;

import java.util.Collection;
import java.util.Random;

import de.learnlib.examples.DefaultLearningExample.DefaultSSTLearningExample;
import net.automatalib.automata.transducers.impl.compact.CompactSST;
import net.automatalib.util.automata.random.RandomAutomata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

public class ExampleRandomSST<I, O> extends DefaultSSTLearningExample<I, O> {

    public ExampleRandomSST(Alphabet<I> alphabet,
                            int size,
                            Collection<Word<O>> stateProperties,
                            Collection<Word<O>> transitionProperties) {
        this(new Random(), alphabet, size, stateProperties, transitionProperties);
    }

    public ExampleRandomSST(Random random,
                            Alphabet<I> alphabet,
                            int size,
                            Collection<Word<O>> stateProperties,
                            Collection<Word<O>> transitionProperties) {
        super(alphabet,
              RandomAutomata.randomDeterministic(random,
                                                 size,
                                                 alphabet,
                                                 stateProperties,
                                                 transitionProperties,
                                                 new CompactSST<>(alphabet)));
    }

    public static <I, O> ExampleRandomSST<I, O> createExample(Random random,
                                                              Alphabet<I> alphabet,
                                                              int size,
                                                              Collection<Word<O>> stateProperties,
                                                              Collection<Word<O>> transitionProperties) {
        return new ExampleRandomSST<>(random, alphabet, size, stateProperties, transitionProperties);
    }
}
