/* Copyright (C) 2013-2022 TU Dortmund
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
package de.learnlib.examples.vpda;

import java.util.Random;

import de.learnlib.examples.DefaultLearningExample.DefaultOneSEVPALearningExample;
import net.automatalib.util.automata.random.RandomAutomata;
import net.automatalib.words.VPDAlphabet;

public class ExampleRandomOneSEVPA<I> extends DefaultOneSEVPALearningExample<I> {

    public ExampleRandomOneSEVPA(VPDAlphabet<I> alphabet, int size, double acceptanceProb, double initialRetTransProb) {
        this(new Random(), alphabet, size, acceptanceProb, initialRetTransProb);
    }

    public ExampleRandomOneSEVPA(Random random,
                                 VPDAlphabet<I> alphabet,
                                 int size,
                                 double acceptanceProb,
                                 double initialRetTransProb) {
        super(alphabet,
              RandomAutomata.randomOneSEVPA(random, size, alphabet, acceptanceProb, initialRetTransProb, true));
    }

    public static <I> ExampleRandomOneSEVPA<I> createExample(Random random,
                                                             VPDAlphabet<I> alphabet,
                                                             int size,
                                                             double acceptanceProb,
                                                             double initialRetTransProb) {
        return new ExampleRandomOneSEVPA<>(random, alphabet, size, acceptanceProb, initialRetTransProb);
    }
}

