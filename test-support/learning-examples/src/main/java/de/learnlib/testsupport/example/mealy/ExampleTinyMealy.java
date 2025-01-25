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
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.transducer.impl.CompactMealy;
import net.automatalib.util.automaton.builder.AutomatonBuilders;

/**
 * Tiny machine with the language (a1a2)^*.
 */
public class ExampleTinyMealy extends DefaultMealyLearningExample<Character, Character> {

    public ExampleTinyMealy() {
        super(constructMachine());
    }

    /**
     * Construct and return a machine representation of this example.
     *
     * @return machine instance of the example
     */
    public static CompactMealy<Character, Character> constructMachine() {
        // @formatter:off
        final CompactMealy<Character, Character> cm = new CompactMealy<>(Alphabets.characters('a', 'a'));
        return AutomatonBuilders.forMealy(cm).
                withInitial("q0").
                from("q0").
                on('a').withOutput('1').to("q1").
                from("q1").
                on('a').withOutput('2').to("q0").
                create();
        // @formatter:on
    }

    public static ExampleTinyMealy createExample() {
        return new ExampleTinyMealy();
    }

}
