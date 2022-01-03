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
package de.learnlib.oracle.equivalence.spa;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import com.google.common.collect.Streams;
import de.learnlib.oracle.membership.SimulatorOracle;
import net.automatalib.automata.spa.SPA;
import net.automatalib.util.automata.conformance.SPATestsIterator;
import net.automatalib.util.automata.conformance.WMethodTestsIterator;
import net.automatalib.util.automata.random.RandomAutomata;
import net.automatalib.words.SPAAlphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;
import net.automatalib.words.impl.DefaultSPAAlphabet;
import org.testng.Assert;
import org.testng.annotations.Test;

public class WMethodSPAEQOracleTest {

    @Test
    public void testOracle() {
        final Random random = new Random(42);
        final SPAAlphabet<Character> alphabet =
                new DefaultSPAAlphabet<>(Alphabets.characters('x', 'z'), Alphabets.characters('A', 'C'), 'R');
        final SPA<?, Character> spa = RandomAutomata.randomRedundancyFreeSPA(random, alphabet, 4);
        final int lookahead = 2;

        final WMethodSPAEQOracle<Character> oracle = new WMethodSPAEQOracle<>(new SimulatorOracle<>(spa), lookahead);

        final List<Word<Character>> eqWords = oracle.generateTestWords(spa, alphabet).collect(Collectors.toList());
        final List<Word<Character>> testWords = Streams.stream(new SPATestsIterator<>(spa,
                                                                                      (dfa, alph) -> new WMethodTestsIterator<>(
                                                                                              dfa,
                                                                                              alph,
                                                                                              lookahead)))
                                                       .collect(Collectors.toList());

        Assert.assertEquals(eqWords, testWords);
    }
}