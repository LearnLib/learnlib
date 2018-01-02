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
package de.learnlib.examples.dfa;

import de.learnlib.examples.DefaultLearningExample.DefaultDFALearningExample;
import net.automatalib.automata.fsa.MutableDFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.util.automata.builders.AutomatonBuilders;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.FastAlphabet;
import net.automatalib.words.impl.Symbol;

/**
 * This class implements a sad love story - DFA style.
 *
 * @author Maik Merten
 */
public class ExamplePaulAndMary extends DefaultDFALearningExample<Symbol> {

    public static final Symbol IN_PAUL = new Symbol("Paul");
    public static final Symbol IN_LOVES = new Symbol("loves");
    public static final Symbol IN_MARY = new Symbol("Mary");

    public ExamplePaulAndMary() {
        super(constructMachine());
    }

    /**
     * Construct and return a machine representation of this example.
     *
     * @return machine instance of the example
     */
    public static CompactDFA<Symbol> constructMachine() {
        return constructMachine(new CompactDFA<>(createInputAlphabet()));
    }

    public static <A extends MutableDFA<S, ? super Symbol>, S> A constructMachine(A dfa) {

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

    public static Alphabet<Symbol> createInputAlphabet() {
        return new FastAlphabet<>(IN_PAUL, IN_LOVES, IN_MARY);
    }

    public static ExamplePaulAndMary createExample() {
        return new ExamplePaulAndMary();
    }

}
