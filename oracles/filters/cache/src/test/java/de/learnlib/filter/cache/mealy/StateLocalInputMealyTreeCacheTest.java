/* Copyright (C) 2013-2019 TU Dortmund
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
package de.learnlib.filter.cache.mealy;

import java.util.Collection;

import de.learnlib.api.oracle.StateLocalInputOracle.StateLocalInputMealyOracle;
import de.learnlib.driver.util.StateLocalInputMealySimulatorSUL;
import de.learnlib.filter.cache.AbstractCacheTest;
import de.learnlib.filter.cache.CacheTestUtils;
import de.learnlib.filter.statistic.sul.ResetCounterStateLocalInputSUL;
import de.learnlib.oracle.membership.StateLocalInputSULOracle;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.automata.transducers.OutputAndLocalInputs;
import net.automatalib.util.automata.transducers.StateLocalInputMealyUtil;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author frohme
 */
public class StateLocalInputMealyTreeCacheTest
        extends AbstractCacheTest<StateLocalInputMealyCacheOracle<Character, Integer>, MealyMachine<?, Character, ?, OutputAndLocalInputs<Character, Integer>>, Character, Word<OutputAndLocalInputs<Character, Integer>>> {

    private final ResetCounterStateLocalInputSUL<Character, Integer> counter;
    private final StateLocalInputMealyOracle<Character, OutputAndLocalInputs<Character, Integer>> oracle;

    private final Collection<Character> initialInputs;

    public StateLocalInputMealyTreeCacheTest() {
        final StateLocalInputMealySimulatorSUL<Character, Integer> sul =
                new StateLocalInputMealySimulatorSUL<>(CacheTestUtils.MEALY);
        counter = new ResetCounterStateLocalInputSUL<>("counterOracle", sul);
        oracle = new StateLocalInputSULOracle<>(counter);

        sul.pre();
        initialInputs = sul.currentlyEnabledInputs();
        sul.post();
    }

    @Override
    protected MealyMachine<?, Character, ?, OutputAndLocalInputs<Character, Integer>> getTargetModel() {
        return StateLocalInputMealyUtil.partialToObservableOutput(CacheTestUtils.MEALY);
    }

    @Override
    protected MealyMachine<?, Character, ?, OutputAndLocalInputs<Character, Integer>> getInvalidTargetModel() {
        return StateLocalInputMealyUtil.partialToObservableOutput(CacheTestUtils.MEALY_INVALID);
    }

    @Override
    protected StateLocalInputMealyCacheOracle<Character, Integer> getCachedOracle() {
        return MealyCaches.createStateLocalInputTreeCache(initialInputs, oracle);
    }

    @Override
    protected StateLocalInputMealyCacheOracle<Character, Integer> getResumedOracle(StateLocalInputMealyCacheOracle<Character, Integer> original) {
        final StateLocalInputMealyCacheOracle<Character, Integer> fresh = getCachedOracle();
        serializeResumable(original, fresh);
        return fresh;
    }

    @Override
    protected long getNumberOfPosedQueries() {
        return counter.getStatisticalData().getCount();
    }

    @Override
    protected boolean supportsPrefixes() {
        return true;
    }

    @Override
    protected Alphabet<Character> getAlphabet() {
        return CacheTestUtils.INPUT_ALPHABET;
    }

    @Test(dependsOnMethods = "testResuming")
    public void testQueryWithNoContainedAlphabetSymbol() {
        final long oldCount = getNumberOfPosedQueries();

        final Word<Character> oldQuery = getQuery(0).getInput();
        final Word<OutputAndLocalInputs<Character, Integer>> answer =
                super.oracle.answerQuery(oldQuery.concat(Word.fromCharSequence("dcba")));

        Assert.assertEquals(getNumberOfPosedQueries(), oldCount);

        for (int i = 0; i < answer.length(); i++) {
            final OutputAndLocalInputs<Character, Integer> symbol = answer.getSymbol(i);

            if (i < oldQuery.size()) {
                Assert.assertNotNull(symbol.getOutput());
                Assert.assertFalse(symbol.getLocalInputs().isEmpty());
            } else {
                Assert.assertNull(symbol.getOutput());
                Assert.assertTrue(symbol.getLocalInputs().isEmpty());

            }
        }
    }
}

