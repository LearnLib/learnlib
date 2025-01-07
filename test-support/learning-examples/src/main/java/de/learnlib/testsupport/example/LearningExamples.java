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
package de.learnlib.testsupport.example;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import de.learnlib.testsupport.example.LearningExample.DFALearningExample;
import de.learnlib.testsupport.example.LearningExample.MealyLearningExample;
import de.learnlib.testsupport.example.LearningExample.MooreLearningExample;
import de.learnlib.testsupport.example.LearningExample.OneSEVPALearningExample;
import de.learnlib.testsupport.example.LearningExample.SBALearningExample;
import de.learnlib.testsupport.example.LearningExample.SPALearningExample;
import de.learnlib.testsupport.example.LearningExample.SPMMLearningExample;
import de.learnlib.testsupport.example.LearningExample.SSTLearningExample;
import de.learnlib.testsupport.example.LearningExample.StateLocalInputMealyLearningExample;
import de.learnlib.testsupport.example.dfa.ExampleAngluin;
import de.learnlib.testsupport.example.dfa.ExampleKeylock;
import de.learnlib.testsupport.example.dfa.ExamplePaulAndMary;
import de.learnlib.testsupport.example.dfa.ExampleTinyDFA;
import de.learnlib.testsupport.example.mealy.ExampleCoffeeMachine;
import de.learnlib.testsupport.example.mealy.ExampleGrid;
import de.learnlib.testsupport.example.mealy.ExampleRandomMealy;
import de.learnlib.testsupport.example.mealy.ExampleRandomStateLocalInputMealy;
import de.learnlib.testsupport.example.mealy.ExampleShahbazGroz;
import de.learnlib.testsupport.example.mealy.ExampleStack;
import de.learnlib.testsupport.example.mealy.ExampleTinyMealy;
import de.learnlib.testsupport.example.moore.ExampleRandomMoore;
import de.learnlib.testsupport.example.sba.ExampleRandomSBA;
import de.learnlib.testsupport.example.spa.ExamplePalindrome;
import de.learnlib.testsupport.example.spa.ExampleRandomSPA;
import de.learnlib.testsupport.example.spmm.ExampleRandomSPMM;
import de.learnlib.testsupport.example.sst.ExampleRandomSST;
import de.learnlib.testsupport.example.vpa.ExampleRandomOneSEVPA;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.ProceduralInputAlphabet;
import net.automatalib.alphabet.ProceduralOutputAlphabet;
import net.automatalib.alphabet.VPAlphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.alphabet.impl.DefaultProceduralInputAlphabet;
import net.automatalib.alphabet.impl.DefaultProceduralOutputAlphabet;
import net.automatalib.alphabet.impl.DefaultVPAlphabet;
import net.automatalib.word.Word;

public final class LearningExamples {

    private static final Alphabet<Character> RANDOM_ALPHABET = Alphabets.characters('a', 'c');
    private static final ProceduralInputAlphabet<Character> PROCEDURAL_INPUT_ALPHABET =
            new DefaultProceduralInputAlphabet<>(Alphabets.characters('A', 'F'), Alphabets.characters('a', 'f'), 'R');
    private static final ProceduralOutputAlphabet<Character> PROCEDURAL_OUTPUT_ALPHABET =
            new DefaultProceduralOutputAlphabet<>(Alphabets.characters('u', 'z'), '✗');
    private static final VPAlphabet<Character> VP_ALPHABET = new DefaultVPAlphabet<>(Alphabets.characters('a', 'f'),
                                                                                     Alphabets.characters('1', '3'),
                                                                                     Alphabets.characters('7', '9'));
    private static final double ACCEPTANCE_PROB = 0.3;
    private static final double RETURN_PROB = 0.3;

    private static final int RANDOM_SIZE = 100;
    private static final int GRID_XSIZE = 5;
    private static final int GRID_YSIZE = 5;
    private static final String[] RANDOM_MEALY_OUTPUTS = {"o1", "o2", "o3"};
    private static final Collection<Word<Character>> RANDOM_SST_PROPS =
            Arrays.asList(Word.fromString("ab"), Word.fromString("bc"), Word.fromString("ca"));
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

    public static List<MooreLearningExample<?, ?>> createMooreExamples() {
        return Collections.singletonList(ExampleRandomMoore.createExample(new Random(RANDOM_SEED),
                                                                          RANDOM_ALPHABET,
                                                                          RANDOM_SIZE,
                                                                          RANDOM_MEALY_OUTPUTS));
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
                             ExampleRandomSPA.createExample(new Random(RANDOM_SEED),
                                                            PROCEDURAL_INPUT_ALPHABET,
                                                            PROCEDURE_SIZE));
    }

    public static List<SBALearningExample<?>> createSBAExamples() {
        return Arrays.asList(de.learnlib.testsupport.example.sba.ExamplePalindrome.createExample(),
                             ExampleRandomSBA.createExample(new Random(RANDOM_SEED),
                                                            PROCEDURAL_INPUT_ALPHABET,
                                                            PROCEDURE_SIZE));
    }

    public static List<SPMMLearningExample<?, ?>> createSPMMExamples() {
        return Arrays.asList(de.learnlib.testsupport.example.spmm.ExamplePalindrome.createExample(),
                             ExampleRandomSPMM.createExample(new Random(RANDOM_SEED),
                                                             PROCEDURAL_INPUT_ALPHABET,
                                                             PROCEDURAL_OUTPUT_ALPHABET,
                                                             PROCEDURE_SIZE));
    }

    public static List<OneSEVPALearningExample<?>> createOneSEVPAExamples() {
        return Collections.singletonList(ExampleRandomOneSEVPA.createExample(new Random(RANDOM_SEED),
                                                                             VP_ALPHABET,
                                                                             RANDOM_SIZE,
                                                                             ACCEPTANCE_PROB,
                                                                             RETURN_PROB));
    }

}
