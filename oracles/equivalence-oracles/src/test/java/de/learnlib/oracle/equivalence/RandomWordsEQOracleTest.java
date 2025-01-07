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

import java.util.Random;

import de.learnlib.oracle.EquivalenceOracle;
import de.learnlib.oracle.MembershipOracle;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.concept.SuffixOutput;
import net.automatalib.word.Word;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class RandomWordsEQOracleTest
        extends AbstractEQOracleTest<SuffixOutput<Character, Boolean>, Character, Boolean> {

    private static final int RANDOM_SEED;
    private static final Alphabet<Character> ALPHABET;
    private static final int MAX_TESTS;
    private static final int MIN_LENGTH;
    private static final int MAX_LENGTH;

    private int numberOfGeneratedQueries;

    static {
        RANDOM_SEED = 42;
        ALPHABET = Alphabets.characters('1', '6');
        MAX_TESTS = 100;
        MIN_LENGTH = 25;
        MAX_LENGTH = 100;
    }

    @BeforeClass
    public void setUp() {
        this.numberOfGeneratedQueries = 0;
    }

    @Test(dependsOnMethods = "testGeneratedEQQueries")
    public void testNumberOfTotalQueries() {
        Assert.assertEquals(this.numberOfGeneratedQueries, MAX_TESTS);
    }

    @Override
    protected void checkGeneratedQuery(Word<Character> query) {
        numberOfGeneratedQueries++;

        Assert.assertTrue(query.length() <= MAX_LENGTH);
        Assert.assertTrue(query.length() >= MIN_LENGTH);
    }

    @Override
    protected EquivalenceOracle<SuffixOutput<Character, Boolean>, Character, Boolean> getOracle(MembershipOracle<Character, Boolean> mOracle) {
        return new RandomWordsEQOracle<>(mOracle, MIN_LENGTH, MAX_LENGTH, MAX_TESTS, new Random(RANDOM_SEED));
    }

    @Override
    protected SuffixOutput<Character, Boolean> getHypothesis() {
        return (prefix, suffix) -> Boolean.TRUE;
    }

    @Override
    protected Alphabet<Character> getAlphabet() {
        return ALPHABET;
    }
}
