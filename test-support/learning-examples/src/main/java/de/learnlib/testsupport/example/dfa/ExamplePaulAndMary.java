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
 * This class implements a sad love story - DFA style.
 */
public class ExamplePaulAndMary extends DefaultDFALearningExample<String> {

    public static final String IN_PAUL = "Paul";
    public static final String IN_LOVES = "loves";
    public static final String IN_MARY = "Mary";

    public ExamplePaulAndMary() {
        super(constructMachine());
    }

    /**
     * Construct and return a machine representation of this example.
     *
     * @return machine instance of the example
     */
    public static CompactDFA<String> constructMachine() {
        return constructMachine(new CompactDFA<>(createInputAlphabet()));
    }

    public static <A extends MutableDFA<S, ? super String>, S> A constructMachine(A dfa) {

        // @formatter:off
        return AutomatonBuilders.forDFA(dfa)
                .withInitial("s0")
                .from("s0")
                    .on(IN_PAUL).to("s1")
                    .on(IN_LOVES, IN_MARY).to("s4")
                .from("s1")
                    .on(IN_LOVES).to("s2")
                    .on(IN_PAUL, IN_MARY).to("s4")
                .from("s2")
                    .on(IN_MARY).to("s3")
                    .on(IN_PAUL, IN_LOVES).to("s4")
                .from("s3")
                    .on(IN_PAUL, IN_LOVES, IN_MARY).to("s4")
                .from("s4")
                    .on(IN_PAUL, IN_LOVES, IN_MARY).loop()
                .withAccepting("s3")
                .create();
        // @formatter:on
    }

    public static Alphabet<String> createInputAlphabet() {
        return Alphabets.fromArray(IN_PAUL, IN_LOVES, IN_MARY);
    }

    public static ExamplePaulAndMary createExample() {
        return new ExamplePaulAndMary();
    }

}
