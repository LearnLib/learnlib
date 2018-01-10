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
package de.learnlib.examples.mealy;

import de.learnlib.examples.DefaultLearningExample.DefaultMealyLearningExample;
import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.util.automata.builders.AutomatonBuilders;
import net.automatalib.words.impl.Alphabets;

/**
 * Tiny machine with the language (a1a2)^*.
 *
 * @author Jeroen Meijer
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
