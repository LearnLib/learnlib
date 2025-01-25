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
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.fsa.impl.CompactDFA;
import net.automatalib.util.automaton.builder.AutomatonBuilders;

/**
 * Tiny DFA with language (ab)*.
 */
public class ExampleTinyDFA extends DefaultDFALearningExample<Character> {


    public ExampleTinyDFA() {
        super(constructMachine());
    }

    /**
     * Construct and return a machine representation of this example.
     *
     * @return machine instance of the example
     */
    public static CompactDFA<Character> constructMachine() {
        // @formatter:off
        return AutomatonBuilders.newDFA(Alphabets.characters('a', 'b')).
                withInitial("q0").withAccepting("q0").withAccepting("q1").
                from("q0").on('a').to("q1").
                from("q1").on('b').to("q0").

                from("q0").on('b').to("TRAP").
                from("q1").on('a').to("TRAP").
                from("TRAP").
                on('a').loop().
                on('b').loop().
                create();
        // @formatter:on
    }

    public static ExampleTinyDFA createExample() {
        return new ExampleTinyDFA();
    }
}
