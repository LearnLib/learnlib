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

import de.learnlib.driver.simulator.MealySimulatorSUL;
import de.learnlib.filter.cache.AbstractCacheTest;
import de.learnlib.filter.cache.CacheTestUtils;
import de.learnlib.filter.cache.SULLearningCacheOracle;
import de.learnlib.filter.statistic.sul.CounterSUL;
import de.learnlib.sul.SUL;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.GrowingMapAlphabet;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.word.Word;

public abstract class AbstractSULCacheTest
        extends AbstractCacheTest<SULLearningCacheOracle<Character, Integer, SULCache<Character, Integer>>, MealyMachine<?, Character, ?, Integer>, Character, Word<Integer>> {

    private final CounterSUL<Character, Integer> counter;

    public AbstractSULCacheTest() {
        counter = new CounterSUL<>(new MealySimulatorSUL<>(CacheTestUtils.MEALY));
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
        return counter.getResetCounter().getCount();
    }

    @Override
    protected boolean supportsPrefixes() {
        return true;
    }

    @Override
    protected Alphabet<Character> getAlphabet() {
        return new GrowingMapAlphabet<>(CacheTestUtils.INPUT_ALPHABET);
    }

    @Override
    protected Alphabet<Character> getExtensionAlphabet() {
        return CacheTestUtils.EXTENSION_ALPHABET;
    }

    @Override
    protected boolean supportsGrowing() {
        return true;
    }

    protected abstract SULCache<Character, Integer> getCache(SUL<Character, Integer> delegate);
}
