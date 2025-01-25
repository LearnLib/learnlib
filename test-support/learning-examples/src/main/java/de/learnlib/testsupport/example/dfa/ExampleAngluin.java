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
package de.learnlib.testsupport.example.dfa;

import de.learnlib.testsupport.example.DefaultLearningExample.DefaultDFALearningExample;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.fsa.MutableDFA;
import net.automatalib.automaton.fsa.impl.CompactDFA;
import net.automatalib.util.automaton.builder.AutomatonBuilders;

/**
 * This class provides the example used in the paper ''Learning Regular Sets from Queries and Counterexamples'' by Dana
 * Angluin that consists of an automaton that accepts ''all strings over {0,1} with an even number of 0's and an even
 * number of 1's''.
 */
public class ExampleAngluin extends DefaultDFALearningExample<Integer> {

    public ExampleAngluin() {
        super(constructMachine());
    }

    public static CompactDFA<Integer> constructMachine() {
        return constructMachine(new CompactDFA<>(createInputAlphabet()));
    }

    public static <A extends MutableDFA<S, ? super Integer>, S> A constructMachine(A machine) {

        // @formatter:off
        return AutomatonBuilders.forDFA(machine)
                .withInitial("q0")
                .from("q0")
                    .on(0).to("q1")
                    .on(1).to("q2")
                .from("q1")
                    .on(0).to("q0")
                    .on(1).to("q3")
                .from("q2")
                    .on(0).to("q3")
                    .on(1).to("q0")
                .from("q3")
                    .on(0).to("q2")
                    .on(1).to("q3")
                .withAccepting("q0")
                .create();
        // @formatter:on
    }

    public static Alphabet<Integer> createInputAlphabet() {
        return Alphabets.integers(0, 1);
    }

    public static ExampleAngluin createExample() {
        return new ExampleAngluin();
    }
}
