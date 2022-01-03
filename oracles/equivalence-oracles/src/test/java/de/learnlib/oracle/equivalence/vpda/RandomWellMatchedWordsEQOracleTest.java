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
package de.learnlib.oracle.equivalence.vpda;

import java.util.Random;

import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.oracle.equivalence.AbstractEQOracleTest;
import net.automatalib.automata.concepts.Output;
import net.automatalib.automata.concepts.SuffixOutput;
import net.automatalib.words.Alphabet;
import net.automatalib.words.VPDAlphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;
import net.automatalib.words.impl.DefaultVPDAlphabet;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;

/**
 * @author frohme
 */
public class RandomWellMatchedWordsEQOracleTest
        extends AbstractEQOracleTest<SuffixOutput<Character, Boolean>, Character, Boolean> {

    private static final int RANDOM_SEED;
    private static final VPDAlphabet<Character> ALPHABET;
    private static final double CALL_PROB;
    private static final int MAX_TESTS;
    private static final int MIN_LENGTH;
    private static final int MAX_LENGTH;

    private int numberOfGeneratedQueries;

    static {
        RANDOM_SEED = 42;
        ALPHABET = new DefaultVPDAlphabet<>(Alphabets.characters('1', '3'),
                                            Alphabets.characters('a', 'c'),
                                            Alphabets.characters('x', 'z'));
        CALL_PROB = 0.5;
        MAX_TESTS = 100;
        MIN_LENGTH = 25;
        MAX_LENGTH = 100;
    }

    @BeforeClass
    public void setUp() {
        this.numberOfGeneratedQueries = 0;
    }

    @Override
    protected void checkGeneratedQuery(Word<Character> query) {
        numberOfGeneratedQueries++;

        Assert.assertTrue(numberOfGeneratedQueries <= MAX_TESTS);
        Assert.assertTrue(query.length() <= MAX_LENGTH);
        Assert.assertTrue(query.length() >= MIN_LENGTH);
        Assert.assertTrue(isWellMatched(ALPHABET, query));
    }

    private static <I> boolean isWellMatched(VPDAlphabet<I> alphabet, Word<I> word) {

        int callBalance = 0;

        for (final I i : word) {
            switch (alphabet.getSymbolType(i)) {
                case CALL:
                    callBalance++;
                    break;
                case RETURN:
                    callBalance--;
                    break;
                default:
                    // do nothing
            }
        }

        return callBalance == 0;
    }

    @Override
    protected EquivalenceOracle<Output<Character, Boolean>, Character, Boolean> getOracle(MembershipOracle<Character, Boolean> mOracle) {
        return new RandomWellMatchedWordsEQOracle<>(new Random(RANDOM_SEED),
                                                    mOracle,
                                                    CALL_PROB,
                                                    MAX_TESTS,
                                                    MIN_LENGTH,
                                                    MAX_LENGTH);
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
