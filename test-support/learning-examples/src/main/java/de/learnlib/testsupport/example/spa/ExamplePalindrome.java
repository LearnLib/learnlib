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
package de.learnlib.testsupport.example.spa;

import java.util.HashMap;
import java.util.Map;

import de.learnlib.testsupport.example.DefaultLearningExample.DefaultSPALearningExample;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.ProceduralInputAlphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.alphabet.impl.DefaultProceduralInputAlphabet;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.fsa.impl.CompactDFA;
import net.automatalib.automaton.fsa.impl.FastDFA;
import net.automatalib.automaton.procedural.SPA;
import net.automatalib.automaton.procedural.impl.StackSPA;
import net.automatalib.util.automaton.builder.AutomatonBuilders;
import net.automatalib.util.automaton.fsa.MutableDFAs;

public class ExamplePalindrome extends DefaultSPALearningExample<Character> {

    public ExamplePalindrome() {
        super(createSPA());
    }

    public static ExamplePalindrome createExample() {
        return new ExamplePalindrome();
    }

    private static SPA<?, Character> createSPA() {
        final Alphabet<Character> internalAlphabet = Alphabets.characters('a', 'c');
        final Alphabet<Character> callAlphabet = Alphabets.characters('S', 'T');
        final ProceduralInputAlphabet<Character> alphabet = new DefaultProceduralInputAlphabet<>(internalAlphabet, callAlphabet, 'R');

        final DFA<?, Character> sProcedure = buildSProcedure(alphabet);
        final DFA<?, Character> tProcedure = buildTProcedure(alphabet);

        final Map<Character, DFA<?, Character>> subModels = new HashMap<>();
        subModels.put('S', sProcedure);
        subModels.put('T', tProcedure);

        return new StackSPA<>(alphabet, 'S', subModels);
    }

    private static DFA<?, Character> buildSProcedure(ProceduralInputAlphabet<Character> alphabet) {

        final CompactDFA<Character> result = new CompactDFA<>(alphabet.getProceduralAlphabet());

        // @formatter:off
        AutomatonBuilders.forDFA(result)
                         .withInitial("s0")
                         .from("s0").on('T').to("s5")
                         .from("s0").on('a').to("s1")
                         .from("s0").on('b').to("s2")
                         .from("s1").on('S').to("s3")
                         .from("s2").on('S').to("s4")
                         .from("s3").on('a').to("s5")
                         .from("s4").on('b').to("s5")
                         .withAccepting("s0", "s1", "s2", "s5")
                         .create();
        // @formatter:on

        MutableDFAs.complete(result, alphabet.getProceduralAlphabet());
        return result;
    }

    private static DFA<?, Character> buildTProcedure(ProceduralInputAlphabet<Character> alphabet) {

        final FastDFA<Character> result = new FastDFA<>(alphabet.getProceduralAlphabet());

        // @formatter:off
        AutomatonBuilders.forDFA(result)
                         .withInitial("t0")
                         .from("t0").on('S').to("t3")
                         .from("t0").on('c').to("t1")
                         .from("t1").on('T').to("t2")
                         .from("t2").on('c').to("t3")
                         .withAccepting("t1", "t3")
                         .create();
        // @formatter:on

        MutableDFAs.complete(result, alphabet.getProceduralAlphabet());
        return result;
    }
}
