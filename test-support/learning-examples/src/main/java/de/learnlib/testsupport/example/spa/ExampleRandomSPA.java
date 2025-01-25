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
package de.learnlib.testsupport.example.spa;

import java.util.Random;

import de.learnlib.testsupport.example.DefaultLearningExample.DefaultSPALearningExample;
import net.automatalib.alphabet.ProceduralInputAlphabet;
import net.automatalib.util.automaton.random.RandomAutomata;

public class ExampleRandomSPA<I> extends DefaultSPALearningExample<I> {

    public ExampleRandomSPA(Random random, ProceduralInputAlphabet<I> alphabet, int size) {
        super(RandomAutomata.randomSPA(random, alphabet, size));
    }

    public static <I> ExampleRandomSPA<I> createExample(Random random, ProceduralInputAlphabet<I> alphabet, int size) {
        return new ExampleRandomSPA<>(random, alphabet, size);
    }
}
