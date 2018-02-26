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
package de.learnlib.examples;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import de.learnlib.examples.LearningExample.DFALearningExample;
import de.learnlib.examples.LearningExample.MealyLearningExample;
import de.learnlib.examples.dfa.ExampleAngluin;
import de.learnlib.examples.dfa.ExampleKeylock;
import de.learnlib.examples.dfa.ExamplePaulAndMary;
import de.learnlib.examples.dfa.ExampleTinyDFA;
import de.learnlib.examples.mealy.ExampleCoffeeMachine;
import de.learnlib.examples.mealy.ExampleGrid;
import de.learnlib.examples.mealy.ExampleRandomMealy;
import de.learnlib.examples.mealy.ExampleShahbazGroz;
import de.learnlib.examples.mealy.ExampleStack;
import de.learnlib.examples.mealy.ExampleTinyMealy;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;

public final class LearningExamples {

    private static final Alphabet<Character> RANDOM_ALPHABET = Alphabets.characters('a', 'c');
    private static final int RANDOM_SIZE = 100;
    private static final int GRID_XSIZE = 5;
    private static final int GRID_YSIZE = 5;
    private static final String[] RANDOM_MEALY_OUTPUTS = {"o1", "o2", "o3"};
    private static final int KEYLOCK_SIZE = 100;
    private static final long RANDOM_SEED = 1337L;

    private LearningExamples() {
        throw new AssertionError("Constructor should not be invoked");
    }

    public static List<DFALearningExample<?>> createDFAExamples() {
        return Arrays.asList(ExampleAngluin.createExample(),
                             ExamplePaulAndMary.createExample(),
                             ExampleKeylock.createExample(KEYLOCK_SIZE, false),
                             ExampleKeylock.createExample(KEYLOCK_SIZE, true),
                             ExampleTinyDFA.createExample());
    }

    public static List<MealyLearningExample<?, ?>> createMealyExamples() {
        return Arrays.asList(ExampleCoffeeMachine.createExample(),
                             ExampleGrid.createExample(GRID_XSIZE, GRID_YSIZE),
                             ExampleShahbazGroz.createExample(),
                             ExampleStack.createExample(),
                             ExampleRandomMealy.createExample(new Random(RANDOM_SEED),
                                                              RANDOM_ALPHABET,
                                                              RANDOM_SIZE,
                                                              RANDOM_MEALY_OUTPUTS),
                             ExampleTinyMealy.createExample());
    }

}
