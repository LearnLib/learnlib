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
package de.learnlib.example.spmm;

import java.util.Random;

import de.learnlib.example.DefaultLearningExample.DefaultSPMMLearningExample;
import net.automatalib.util.automata.random.RandomAutomata;
import net.automatalib.words.ProceduralInputAlphabet;
import net.automatalib.words.ProceduralOutputAlphabet;

public class ExampleRandomSPMM<I, O> extends DefaultSPMMLearningExample<I, O> {

    public ExampleRandomSPMM(ProceduralInputAlphabet<I> inputAlphabet, ProceduralOutputAlphabet<O> outputAlphabet, int size) {
        this(new Random(), inputAlphabet, outputAlphabet, size);
    }

    public ExampleRandomSPMM(Random random, ProceduralInputAlphabet<I> inputAlphabet, ProceduralOutputAlphabet<O> outputAlphabet, int size) {
        super(RandomAutomata.randomSPMM(random, inputAlphabet, outputAlphabet, size));
    }

    public static <I, O> ExampleRandomSPMM<I, O> createExample(Random random, ProceduralInputAlphabet<I> inputAlphabet, ProceduralOutputAlphabet<O> outputAlphabet, int size) {
        return new ExampleRandomSPMM<>(random, inputAlphabet, outputAlphabet, size);
    }
}
