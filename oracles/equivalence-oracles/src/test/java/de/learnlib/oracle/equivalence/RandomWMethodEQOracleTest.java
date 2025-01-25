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

import java.util.HashSet;
import java.util.Set;

import de.learnlib.oracle.EquivalenceOracle;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.testsupport.example.dfa.ExamplePaulAndMary;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.util.automaton.Automata;
import net.automatalib.word.Word;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class RandomWMethodEQOracleTest extends AbstractEQOracleTest<DFA<?, String>, String, Boolean> {

    private static final int MAX_TESTS;
    private static final int MIN_LENGTH;
    private static final int MAX_LENGTH;

    private int numberOfGeneratedQueries;
    private DFA<?, String> dfa;
    private Set<Word<String>> transitionCover;
    private Set<Word<String>> characterizingSet;

    static {
        MAX_TESTS = 1000;
        MIN_LENGTH = 0;
        MAX_LENGTH = 5;
    }

    @BeforeClass
    public void setUp() {
        this.numberOfGeneratedQueries = 0;
        this.dfa = ExamplePaulAndMary.constructMachine();

        this.transitionCover = new HashSet<>(Automata.transitionCover(this.dfa, getAlphabet()));
        this.characterizingSet = new HashSet<>(Automata.characterizingSet(this.dfa, getAlphabet()));
    }

    @Test(dependsOnMethods = "testGeneratedEQQueries")
    public void testNumberOfTotalQueries() {
        Assert.assertEquals(this.numberOfGeneratedQueries, MAX_TESTS);
    }

    @Override
    protected void checkGeneratedQuery(Word<String> query) {
        numberOfGeneratedQueries++;

        transitionCover.stream().filter(w -> w.isPrefixOf(query)).findAny().orElseThrow(AssertionError::new);
        characterizingSet.stream().filter(w -> w.isSuffixOf(query)).findAny().orElseThrow(AssertionError::new);
    }

    @Override
    protected EquivalenceOracle<DFA<?, String>, String, Boolean> getOracle(MembershipOracle<String, Boolean> mOracle) {
        return new RandomWMethodEQOracle<>(mOracle, MIN_LENGTH, MAX_LENGTH, MAX_TESTS);
    }

    @Override
    protected DFA<?, String> getHypothesis() {
        return dfa;
    }

    @Override
    protected Alphabet<String> getAlphabet() {
        return ExamplePaulAndMary.createInputAlphabet();
    }
}

