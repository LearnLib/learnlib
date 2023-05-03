/* Copyright (C) 2013-2023 TU Dortmund
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
package de.learnlib.filter.cache.sul;

import de.learnlib.api.SUL;
import de.learnlib.driver.util.MealySimulatorSUL;
import de.learnlib.filter.cache.AbstractCacheTest;
import de.learnlib.filter.cache.CacheTestUtils;
import de.learnlib.filter.cache.SULLearningCacheOracle;
import de.learnlib.filter.statistic.sul.ResetCounterSUL;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

/**
 * @author frohme
 */
public abstract class AbstractSULCacheTest
        extends AbstractCacheTest<SULLearningCacheOracle<Character, Integer, SULCache<Character, Integer>>, MealyMachine<?, Character, ?, Integer>, Character, Word<Integer>> {

    private final ResetCounterSUL<Character, Integer> counter;

    public AbstractSULCacheTest() {
        counter = new ResetCounterSUL<>("counterOracle", new MealySimulatorSUL<>(CacheTestUtils.MEALY));
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
    protected SULLearningCacheOracle<Character, Integer, SULCache<Character, Integer>> getCachedOracle() {
        return SULLearningCacheOracle.fromSULCache(getCache(counter));
    }

    @Override
    protected SULLearningCacheOracle<Character, Integer, SULCache<Character, Integer>> getResumedOracle(
            SULLearningCacheOracle<Character, Integer, SULCache<Character, Integer>> original) {
        final SULCache<Character, Integer> fresh = getCache(counter);
        serializeResumable(original.getCache(), fresh);
        return SULLearningCacheOracle.fromSULCache(fresh);
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

    protected abstract SULCache<Character, Integer> getCache(SUL<Character, Integer> delegate);
}
