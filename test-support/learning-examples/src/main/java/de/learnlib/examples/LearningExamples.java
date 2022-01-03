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
package de.learnlib.examples;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import de.learnlib.examples.LearningExample.DFALearningExample;
import de.learnlib.examples.LearningExample.MealyLearningExample;
import de.learnlib.examples.LearningExample.OneSEVPALearningExample;
import de.learnlib.examples.LearningExample.SPALearningExample;
import de.learnlib.examples.LearningExample.SSTLearningExample;
import de.learnlib.examples.LearningExample.StateLocalInputMealyLearningExample;
import de.learnlib.examples.dfa.ExampleAngluin;
import de.learnlib.examples.dfa.ExampleKeylock;
import de.learnlib.examples.dfa.ExamplePaulAndMary;
import de.learnlib.examples.dfa.ExampleTinyDFA;
import de.learnlib.examples.mealy.ExampleCoffeeMachine;
import de.learnlib.examples.mealy.ExampleGrid;
import de.learnlib.examples.mealy.ExampleRandomMealy;
import de.learnlib.examples.mealy.ExampleRandomStateLocalInputMealy;
import de.learnlib.examples.mealy.ExampleShahbazGroz;
import de.learnlib.examples.mealy.ExampleStack;
import de.learnlib.examples.mealy.ExampleTinyMealy;
import de.learnlib.examples.spa.ExamplePalindrome;
import de.learnlib.examples.spa.ExampleRandomSPA;
import de.learnlib.examples.sst.ExampleRandomSST;
import de.learnlib.examples.vpda.ExampleRandomOneSEVPA;
import net.automatalib.words.Alphabet;
import net.automatalib.words.SPAAlphabet;
import net.automatalib.words.VPDAlphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;
import net.automatalib.words.impl.DefaultSPAAlphabet;
import net.automatalib.words.impl.DefaultVPDAlphabet;

public final class LearningExamples {

    private static final Alphabet<Character> RANDOM_ALPHABET = Alphabets.characters('a', 'c');
    private static final SPAAlphabet<Character> SPA_ALPHABET =
            new DefaultSPAAlphabet<>(Alphabets.characters('A', 'F'), Alphabets.characters('a', 'f'), 'R');
    private static final VPDAlphabet<Character> VPD_ALPHABET = new DefaultVPDAlphabet<>(Alphabets.characters('a', 'f'),
                                                                                        Alphabets.characters('1', '3'),
                                                                                        Alphabets.characters('7', '9'));
    private static final double ACCEPTANCE_PROB = 0.3;
    private static final double RETURN_PROB = 0.3;

    private static final int RANDOM_SIZE = 100;
    private static final int GRID_XSIZE = 5;
    private static final int GRID_YSIZE = 5;
    private static final String[] RANDOM_MEALY_OUTPUTS = {"o1", "o2", "o3"};
    private static final Collection<Word<Character>> RANDOM_SST_PROPS =
            Arrays.asList(Word.fromCharSequence("ab"), Word.fromCharSequence("bc"), Word.fromCharSequence("ca"));
    private static final String UNDEFINED_MEALY_OUTPUT = "undefined";
    private static final int KEYLOCK_SIZE = 100;
    private static final int PROCEDURE_SIZE = 10;
    private static final long RANDOM_SEED = 1337L;

    private LearningExamples() {
        // prevent instantiation
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

    public static List<StateLocalInputMealyLearningExample<?, ?>> createSLIMealyExamples() {
        return Collections.singletonList(ExampleRandomStateLocalInputMealy.createExample(new Random(RANDOM_SEED),
                                                                                         RANDOM_ALPHABET,
                                                                                         RANDOM_SIZE,
                                                                                         UNDEFINED_MEALY_OUTPUT,
                                                                                         RANDOM_MEALY_OUTPUTS));
    }

    public static List<SSTLearningExample<?, ?>> createSSTExamples() {
        return Collections.singletonList(ExampleRandomSST.createExample(new Random(RANDOM_SEED),
                                                                        RANDOM_ALPHABET,
                                                                        RANDOM_SIZE,
                                                                        RANDOM_SST_PROPS,
                                                                        RANDOM_SST_PROPS));
    }

    public static List<SPALearningExample<?>> createSPAExamples() {
        return Arrays.asList(ExamplePalindrome.createExample(),
                             ExampleRandomSPA.createExample(new Random(RANDOM_SEED), SPA_ALPHABET, PROCEDURE_SIZE));
    }

    public static List<OneSEVPALearningExample<?>> createOneSEVPAExamples() {
        return Collections.singletonList(ExampleRandomOneSEVPA.createExample(new Random(RANDOM_SEED),
                                                                             VPD_ALPHABET,
                                                                             RANDOM_SIZE,
                                                                             ACCEPTANCE_PROB,
                                                                             RETURN_PROB));
    }

}
