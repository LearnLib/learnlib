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
package de.learnlib.testsupport.example.spmm;

import java.util.HashMap;
import java.util.Map;

import de.learnlib.testsupport.example.DefaultLearningExample.DefaultSPMMLearningExample;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.ProceduralInputAlphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.alphabet.impl.DefaultProceduralInputAlphabet;
import net.automatalib.automaton.procedural.SPMM;
import net.automatalib.automaton.procedural.impl.StackSPMM;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.automaton.transducer.impl.CompactMealy;
import net.automatalib.automaton.transducer.impl.FastMealy;
import net.automatalib.util.automaton.builder.AutomatonBuilders;
import net.automatalib.util.automaton.transducer.MutableMealyMachines;

public class ExamplePalindrome extends DefaultSPMMLearningExample<Character, Character> {

    private static final char ERROR_OUTPUT = '-';
    private static final char SUCCESS_OUTPUT = '✓';

    public ExamplePalindrome() {
        super(createSPMM());
    }

    public static ExamplePalindrome createExample() {
        return new ExamplePalindrome();
    }

    private static SPMM<?, Character, ?, Character> createSPMM() {
        final Alphabet<Character> internalAlphabet = Alphabets.characters('a', 'c');
        final Alphabet<Character> callAlphabet = Alphabets.characters('S', 'T');
        final ProceduralInputAlphabet<Character> alphabet =
                new DefaultProceduralInputAlphabet<>(internalAlphabet, callAlphabet, 'R');

        final MealyMachine<?, Character, ?, Character> sProcedure = buildSProcedure(alphabet);
        final MealyMachine<?, Character, ?, Character> tProcedure = buildTProcedure(alphabet);

        final Map<Character, MealyMachine<?, Character, ?, Character>> subModels = new HashMap<>();
        subModels.put('S', sProcedure);
        subModels.put('T', tProcedure);

        return new StackSPMM<>(alphabet, 'S', SUCCESS_OUTPUT, ERROR_OUTPUT, subModels);
    }

    private static MealyMachine<?, Character, ?, Character> buildSProcedure(ProceduralInputAlphabet<Character> alphabet) {

        final CompactMealy<Character, Character> result = new CompactMealy<>(alphabet);

        // @formatter:off
        AutomatonBuilders.forMealy(result)
                         .withInitial("s0")
                         .from("s0").on('T').withOutput(SUCCESS_OUTPUT).to("s5")
                         .from("s0").on('a').withOutput('x').to("s1")
                         .from("s0").on('b').withOutput('y').to("s2")
                         .from("s0").on('R').withOutput(SUCCESS_OUTPUT).to("s6")
                         .from("s1").on('S').withOutput(SUCCESS_OUTPUT).to("s3")
                         .from("s1").on('R').withOutput(SUCCESS_OUTPUT).to("s6")
                         .from("s2").on('S').withOutput(SUCCESS_OUTPUT).to("s4")
                         .from("s2").on('R').withOutput(SUCCESS_OUTPUT).to("s6")
                         .from("s3").on('a').withOutput('x').to("s5")
                         .from("s4").on('b').withOutput('y').to("s5")
                         .from("s5").on('R').withOutput(SUCCESS_OUTPUT).to("s6")
                         .create();
        // @formatter:on

        MutableMealyMachines.complete(result, alphabet, ERROR_OUTPUT, true);
        return result;
    }

    private static MealyMachine<?, Character, ?, Character> buildTProcedure(ProceduralInputAlphabet<Character> alphabet) {

        final FastMealy<Character, Character> result = new FastMealy<>(alphabet);

        // @formatter:off
        AutomatonBuilders.forMealy(result)
                         .withInitial("t0")
                         .from("t0").on('S').withOutput(SUCCESS_OUTPUT).to("t3")
                         .from("t0").on('c').withOutput('z').to("t1")
                         .from("t1").on('T').withOutput(SUCCESS_OUTPUT).to("t2")
                         .from("t1").on('R').withOutput(SUCCESS_OUTPUT).to("t4")
                         .from("t2").on('c').withOutput('z').to("t3")
                         .from("t3").on('R').withOutput(SUCCESS_OUTPUT).to("t4")
                         .create();
        // @formatter:on

        MutableMealyMachines.complete(result, alphabet, ERROR_OUTPUT, true);
        return result;
    }
}
