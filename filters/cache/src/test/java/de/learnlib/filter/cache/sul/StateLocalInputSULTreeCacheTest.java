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
package de.learnlib.filter.cache.sul;

import de.learnlib.driver.simulator.StateLocalInputMealySimulatorSUL;
import de.learnlib.filter.cache.AbstractCacheTest;
import de.learnlib.filter.cache.CacheTestUtils;
import de.learnlib.filter.cache.SULLearningCacheOracle;
import de.learnlib.filter.statistic.sul.CounterStateLocalInputSUL;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.word.Word;
import org.testng.Assert;
import org.testng.annotations.Test;

public class StateLocalInputSULTreeCacheTest
        extends AbstractCacheTest<SULLearningCacheOracle<Character, Integer, StateLocalInputSULCache<Character, Integer>>, MealyMachine<?, Character, ?, Integer>, Character, Word<Integer>> {

    private final CounterStateLocalInputSUL<Character, Integer> counter;
    private final Integer undefined;

    public StateLocalInputSULTreeCacheTest() {
        undefined = -1;
        counter = new CounterStateLocalInputSUL<>(new StateLocalInputMealySimulatorSUL<>(CacheTestUtils.MEALY));
    }

    @Test
    @Override
    public void testNoQueriesReceived() {
        super.testNoQueriesReceived();
        Assert.assertEquals(counter.getInputCounter().getCount(), 0);
    }

    @Test(dependsOnMethods = "testNoQueriesReceived")
    @Override
    public void testFirstQuery() {
        super.testFirstQuery();
        Assert.assertEquals(counter.getInputCounter().getCount(), oracle.getCache().size());
    }

    @Test(dependsOnMethods = "testFirstQuery")
    @Override
    public void testFirstDuplicate() {
        super.testFirstDuplicate();
        Assert.assertEquals(counter.getInputCounter().getCount(), oracle.getCache().size());
    }

    @Test(dependsOnMethods = "testFirstDuplicate")
    @Override
    public void testTwoQueriesOneDuplicate() {
        super.testTwoQueriesOneDuplicate();
        Assert.assertEquals(counter.getInputCounter().getCount(), oracle.getCache().size());
    }

    @Test(dependsOnMethods = "testTwoQueriesOneDuplicate")
    @Override
    public void testOneNewQuery() {
        super.testOneNewQuery();
        Assert.assertEquals(counter.getInputCounter().getCount(), oracle.getCache().size());
    }

    @Test(dependsOnMethods = "testOneNewQuery")
    @Override
    public void testPrefix() {
        super.testPrefix();
        Assert.assertEquals(counter.getInputCounter().getCount(), oracle.getCache().size());
    }

    @Test(dependsOnMethods = "testPrefix")
    @Override
    public void testCacheConsistency() {
        super.testCacheConsistency();
    }

    @Test(dependsOnMethods = "testCacheConsistency")
    @Override
    public void testResuming() {
        super.testResuming();
    }

    @Test(dependsOnMethods = "testResuming")
    @Override
    public void testDuplicatesInBatch() {
        super.testDuplicatesInBatch();
    }

    @Test(dependsOnMethods = "testDuplicatesInBatch")
    public void testQueryWithNoContainedAlphabetSymbol() {
        final long oldCount = getNumberOfPosedQueries();

        final Word<Character> oldQuery = getQuery(0).getInput();
        final Word<Integer> answer = super.oracle.answerQuery(oldQuery.concat(Word.fromString("fcba")));

        Assert.assertEquals(getNumberOfPosedQueries(), oldCount);

        for (int i = 0; i < answer.length(); i++) {
            final Integer symbol = answer.getSymbol(i);

            if (i < oldQuery.size()) {
                Assert.assertNotEquals(symbol, undefined);
            } else {
                Assert.assertEquals(symbol, undefined);
            }
        }
    }

    @Override
    protected MealyMachine<?, Character, ?, Integer> getTargetModel() {
        return CacheTestUtils.MEALY;
    }

    @Override
    protected MealyMachine<?, Character, ?, Integer> getInvalidTargetModel() {
        return CacheTestUtils.MEALY_INVALID;
    }

    @Override
    protected SULLearningCacheOracle<Character, Integer, StateLocalInputSULCache<Character, Integer>> getCachedOracle() {
        return SULLearningCacheOracle.fromSLISULCache(getCache(), undefined);
    }

    @Override
    protected SULLearningCacheOracle<Character, Integer, StateLocalInputSULCache<Character, Integer>> getResumedOracle(
            SULLearningCacheOracle<Character, Integer, StateLocalInputSULCache<Character, Integer>> original) {
        final StateLocalInputSULCache<Character, Integer> fresh = getCache();
        serializeResumable(original.getCache(), fresh);
        return SULLearningCacheOracle.fromSLISULCache(fresh, undefined);
    }

    @Override
    protected long getNumberOfPosedQueries() {
        return counter.getResetCounter().getCount();
    }

    @Override
    protected boolean supportsPrefixes() {
        return true;
    }

    @Override
    protected Alphabet<Character> getAlphabet() {
        return CacheTestUtils.INPUT_ALPHABET;
    }

    @Override
    protected Alphabet<Character> getExtensionAlphabet() {
        return CacheTestUtils.EXTENSION_ALPHABET;
    }

    @Override
    protected boolean supportsGrowing() {
        return false;
    }

    private StateLocalInputSULCache<Character, Integer> getCache() {
        return SULCaches.createStateLocalInputCache(CacheTestUtils.INPUT_ALPHABET, counter);
    }
}

