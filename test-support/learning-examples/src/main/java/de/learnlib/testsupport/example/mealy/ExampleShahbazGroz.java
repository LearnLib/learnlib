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
package de.learnlib.testsupport.example.mealy;

import de.learnlib.testsupport.example.DefaultLearningExample.DefaultMealyLearningExample;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.transducer.MutableMealyMachine;
import net.automatalib.automaton.transducer.impl.CompactMealy;
import net.automatalib.util.automaton.builder.AutomatonBuilders;

/**
 * This class provides the example used in the paper ''Inferring Mealy Machines'' by Muzammil Shahbaz and Roland Groz
 * (see Figure 1).
 */
public class ExampleShahbazGroz extends DefaultMealyLearningExample<Character, String> {

    public ExampleShahbazGroz() {
        super(constructMachine());
    }

    public static CompactMealy<Character, String> constructMachine() {
        return constructMachine(new CompactMealy<>(createInputAlphabet()));
    }

    /**
     * Construct and return a machine representation of this example.
     *
     * @param fm
     *         the output object to write the contents to
     * @param <S>
     *         state type
     * @param <T>
     *         transition type
     * @param <A>
     *         automaton type
     *
     * @return machine instance of the example
     */
    public static <S, T, A extends MutableMealyMachine<S, ? super Character, T, ? super String>> A constructMachine(A fm) {

        // @formatter:off
        return AutomatonBuilders.forMealy(fm)
                .withInitial("q0")
                .from("q0")
                    .on('a').withOutput("x").to("q1")
                    .on('b').withOutput("x").to("q3")
                .from("q1")
                    .on('a').withOutput("y").loop()
                    .on('b').withOutput("x").to("q2")
                .from("q2")
                    .on('a', 'b').withOutput("x").to("q3")
                .from("q3")
                    .on('a', 'b').withOutput("x").to("q0")
                .create();
        // @formatter:on

        /*
         * In the paper the authors use the following counterexample
         * to refine the first conjecture from an angluin for mealy machines:
         * a b a b b a a
         */
    }

    public static Alphabet<Character> createInputAlphabet() {
        return Alphabets.characters('a', 'b');
    }

    public static ExampleShahbazGroz createExample() {
        return new ExampleShahbazGroz();
    }

}
