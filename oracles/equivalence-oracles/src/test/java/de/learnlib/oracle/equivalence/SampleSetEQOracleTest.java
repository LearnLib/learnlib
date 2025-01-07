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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import de.learnlib.oracle.EquivalenceOracle;
import de.learnlib.oracle.MembershipOracle;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.concept.SuffixOutput;
import net.automatalib.common.util.random.RandomUtil;
import net.automatalib.word.Word;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class SampleSetEQOracleTest extends AbstractEQOracleTest<SuffixOutput<Character, Boolean>, Character, Boolean> {

    private static final Alphabet<Character> ALPHABET = Alphabets.characters('1', '6');
    private static final int TEST_WORD_LENGTH = 10;

    private Random random;
    private SampleSetEQOracle<Character, Boolean> oracle;
    private List<Word<Character>> expectedTestWords;
    private List<Word<Character>> testedWords;

    @BeforeClass
    public void setUp() {

        this.oracle = new SampleSetEQOracle<>(true);
        this.random = new Random(42);

        final List<Character> alphabet = new ArrayList<>(ALPHABET);

        // add initial queries
        this.oracle.add(generateTestWord(alphabet), Boolean.TRUE);
        this.oracle.add(generateTestWord(alphabet), Boolean.TRUE);
        this.oracle.add(generateTestWord(alphabet), Boolean.TRUE);
        // check if unsuccessful queries will be removed
        this.oracle.findCounterExample((prefix, suffix) -> Boolean.TRUE, ALPHABET);

        this.expectedTestWords =
                Arrays.asList(generateTestWord(alphabet), generateTestWord(alphabet), generateTestWord(alphabet));
        this.testedWords = new ArrayList<>(this.expectedTestWords.size());
    }

    private <I> Word<I> generateTestWord(List<I> alphabet) {
        return Word.fromList(RandomUtil.sample(random, alphabet, TEST_WORD_LENGTH));
    }

    @Test(dependsOnMethods = "testGeneratedEQQueries")
    public void testName() {
        Assert.assertEquals(this.expectedTestWords, this.testedWords);
    }

    @Override
    protected void checkGeneratedQuery(Word<Character> query) {
        this.testedWords.add(query);
    }

    @Override
    protected EquivalenceOracle<SuffixOutput<Character, Boolean>, Character, Boolean> getOracle(MembershipOracle<Character, Boolean> mOracle) {
        return this.oracle.addAll(mOracle, expectedTestWords);
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

