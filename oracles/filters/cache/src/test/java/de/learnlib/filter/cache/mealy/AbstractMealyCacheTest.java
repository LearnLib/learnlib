/* Copyright (C) 2013-2018 TU Dortmund
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

import de.learnlib.api.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.filter.cache.AbstractCacheTest;
import de.learnlib.filter.cache.CacheTestUtils;
import de.learnlib.filter.cache.LearningCacheOracle;
import de.learnlib.filter.cache.LearningCacheOracle.MealyLearningCacheOracle;
import de.learnlib.filter.statistic.oracle.CounterOracle.MealyCounterOracle;
import de.learnlib.oracle.membership.SimulatorOracle.MealySimulatorOracle;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.commons.util.mappings.Mapping;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

/**
 * @author frohme
 */
public abstract class AbstractMealyCacheTest
        extends AbstractCacheTest<MealyMachine<?, Character, ?, Integer>, Character, Word<Integer>> {

    private final MealyCounterOracle<Character, Integer> counter;
    protected final Mapping<Integer, Integer> errorMapper;

    public AbstractMealyCacheTest() {
        counter = new MealyCounterOracle<>(new MealySimulatorOracle<>(CacheTestUtils.MEALY), "counterOracle");
        errorMapper = o -> {
            switch (o) {
                case 1: {
                    return 10;
                }
                default:
                    return null;
            }
        };
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
    protected LearningCacheOracle<MealyMachine<?, Character, ?, Integer>, Character, Word<Integer>> getCachedOracle() {
        return getCache(counter);
    }

    @Override
    protected long getNumberOfPosedQueries() {
        return counter.getCount();
    }

    @Override
    protected boolean supportsPrefixes() {
        return true;
    }

    @Override
    protected Alphabet<Character> getAlphabet() {
        return CacheTestUtils.INPUT_ALPHABET;
    }

    protected abstract MealyLearningCacheOracle<Character, Integer> getCache(MealyMembershipOracle<Character, Integer> delegate);
}
