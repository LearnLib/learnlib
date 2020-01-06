/* Copyright (C) 2013-2020 TU Dortmund
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
package de.learnlib.oracle.equivalence;

import java.util.HashSet;
import java.util.Set;

import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.examples.dfa.ExamplePaulAndMary;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.util.automata.Automata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Symbol;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author frohme
 */
public class RandomWpMethodEQOracleTest extends AbstractEQOracleTest<DFA<?, Symbol>, Symbol, Boolean> {

    private static final int MAX_TESTS;
    private static final int MIN_LENGTH;
    private static final int MAX_LENGTH;

    private int numberOfGeneratedQueries;
    private DFA<?, Symbol> dfa;
    private Set<Word<Symbol>> stateCover;
    private Set<Word<Symbol>> characterizingSet;

    static {
        MAX_TESTS = 1000;
        MIN_LENGTH = 0;
        MAX_LENGTH = 5;
    }

    @BeforeClass
    public void setUp() {
        this.numberOfGeneratedQueries = 0;
        this.dfa = ExamplePaulAndMary.constructMachine();

        this.stateCover = new HashSet<>(Automata.stateCover(this.dfa, getAlphabet()));
        this.characterizingSet = new HashSet<>(Automata.characterizingSet(this.dfa, getAlphabet()));
    }

    @Test(dependsOnMethods = "testGeneratedEQQueries")
    public void testNumberOfTotalQueries() {
        Assert.assertEquals(this.numberOfGeneratedQueries, MAX_TESTS);
    }

    @Override
    protected void checkGeneratedQuery(Word<Symbol> query) {
        numberOfGeneratedQueries++;

        stateCover.stream().filter(w -> w.isPrefixOf(query)).findAny().orElseThrow(AssertionError::new);
        characterizingSet.stream().filter(w -> w.isSuffixOf(query)).findAny().orElseThrow(AssertionError::new);
    }

    @Override
    protected EquivalenceOracle<DFA<?, Symbol>, Symbol, Boolean> getOracle(MembershipOracle<Symbol, Boolean> mOracle) {
        return new RandomWpMethodEQOracle<>(mOracle, MIN_LENGTH, MAX_LENGTH, MAX_TESTS);
    }

    @Override
    protected DFA<?, Symbol> getHypothesis() {
        return dfa;
    }

    @Override
    protected Alphabet<Symbol> getAlphabet() {
        return ExamplePaulAndMary.createInputAlphabet();
    }
}
