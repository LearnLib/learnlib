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
package de.learnlib.filter.cache;

import java.util.Random;

import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.util.automata.random.RandomAutomata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;

/**
 * @author frohme
 */
public final class CacheTestUtils {

    public static final Alphabet<Character> INPUT_ALPHABET;
    public static final Alphabet<Integer> OUTPUT_ALPHABET;

    public static final CompactDFA<Character> DFA;
    public static final CompactDFA<Character> DFA_INVALID;
    public static final CompactMealy<Character, Integer> MEALY;
    public static final CompactMealy<Character, Integer> MEALY_INVALID;

    static {
        INPUT_ALPHABET = Alphabets.characters('a', 'c');
        OUTPUT_ALPHABET = Alphabets.integers(1, 3);

        final Random random = new Random(42);
        final int size = 20;
        DFA = RandomAutomata.randomDFA(random, size, INPUT_ALPHABET);
        MEALY = RandomAutomata.randomMealy(random, size, INPUT_ALPHABET, OUTPUT_ALPHABET);

        DFA_INVALID = new CompactDFA<>(DFA);
        DFA_INVALID.flipAcceptance();

        // we rely on two generations not producing the same automaton
        MEALY_INVALID = RandomAutomata.randomMealy(random, size, INPUT_ALPHABET, OUTPUT_ALPHABET);
    }

    private CacheTestUtils() {}

}
