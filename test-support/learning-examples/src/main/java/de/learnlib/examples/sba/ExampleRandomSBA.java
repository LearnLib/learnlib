/* Copyright (C) 2013-2023 TU Dortmund
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
package de.learnlib.examples.sba;

import java.util.Random;

import de.learnlib.examples.DefaultLearningExample.DefaultSBALearningExample;
import net.automatalib.util.automata.random.RandomAutomata;
import net.automatalib.words.SPAAlphabet;

public class ExampleRandomSBA<I> extends DefaultSBALearningExample<I> {

    public ExampleRandomSBA(SPAAlphabet<I> alphabet, int size) {
        this(new Random(), alphabet, size);
    }

    public ExampleRandomSBA(Random random, SPAAlphabet<I> alphabet, int size) {
        super(RandomAutomata.randomSBA(random, alphabet, size));
    }

    public static <I> ExampleRandomSBA<I> createExample(Random random, SPAAlphabet<I> alphabet, int size) {
        return new ExampleRandomSBA<>(random, alphabet, size);
    }
}
