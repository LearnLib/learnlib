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
package de.learnlib.oracle.equivalence;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.fsa.impl.CompactDFA;
import net.automatalib.common.util.collection.IteratorUtil;
import net.automatalib.util.automaton.conformance.KWayStateCoverTestsIterator;
import net.automatalib.util.automaton.random.RandomAutomata;
import net.automatalib.word.Word;
import org.testng.Assert;
import org.testng.annotations.Test;

public class KWayTransitionCoverEQOracleTest {

    private static final int SIZE = 10;

    @Test
    public void testOracle() {
        final int seed = 42;
        final Alphabet<Character> alphabet = Alphabets.characters('a', 'c');
        final CompactDFA<Character> dfa = RandomAutomata.randomDFA(new Random(seed), SIZE, alphabet);

        final KWayStateCoverEQOracle<DFA<Integer, Character>, Integer, Character, Integer, Boolean> oracle =
                new KWayStateCoverEQOracleBuilder<DFA<Integer, Character>, Integer, Character, Integer, Boolean>().withRandom(
                        new Random(seed)).create();

        List<Word<Character>> tests = oracle.generateTestWords(dfa, alphabet).collect(Collectors.toList());
        List<Word<Character>> iter =
                IteratorUtil.list(new KWayStateCoverTestsIterator<>(dfa, alphabet, new Random(seed)));

        Assert.assertEquals(tests, iter);
    }
}
