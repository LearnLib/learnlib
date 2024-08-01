/* Copyright (C) 2013-2024 TU Dortmund University
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

import de.learnlib.driver.simulator.MealySimulatorSUL;
import de.learnlib.filter.cache.AbstractCacheTest;
import de.learnlib.filter.cache.CacheTestUtils;
import de.learnlib.filter.statistic.oracle.CounterAdaptiveQueryOracle;
import de.learnlib.filter.statistic.oracle.CounterSymbolQueryOracle;
import de.learnlib.oracle.membership.SULAdaptiveOracle;
import de.learnlib.oracle.membership.SULSymbolQueryOracle;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.GrowingMapAlphabet;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.word.Word;

public class AdaptiveQueryCacheTest
        extends AbstractCacheTest<AdaptiveQueryCache<Character, Integer>, MealyMachine<?, Character, ?, Integer>, Character, Word<Integer>> {

    private final CounterAdaptiveQueryOracle<Character, Integer> counter;

    public AdaptiveQueryCacheTest() {
        counter =
                new CounterAdaptiveQueryOracle<>(new SULAdaptiveOracle<>(new MealySimulatorSUL<>(CacheTestUtils.MEALY)));
    }

    @Override
    protected Alphabet<Character> getAlphabet() {
        return new GrowingMapAlphabet<>(CacheTestUtils.INPUT_ALPHABET);
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
    protected AdaptiveQueryCache<Character, Integer> getCachedOracle() {
        return MealyCaches.createAdaptiveQueryCache(getAlphabet(), counter);
    }

    @Override
    protected AdaptiveQueryCache<Character, Integer> getResumedOracle(AdaptiveQueryCache<Character, Integer> original) {
        final AdaptiveQueryCache<Character, Integer> fresh =
                MealyCaches.createAdaptiveQueryCache(getAlphabet(), counter);
        serializeResumable(original, fresh);
        return fresh;
    }

    @Override
    protected long getNumberOfPosedQueries() {
        return counter.getResetCount();
    }

    @Override
    protected boolean supportsPrefixes() {
        return true;
    }

    @Override
    protected Alphabet<Character> getExtensionAlphabet() {
        return CacheTestUtils.EXTENSION_ALPHABET;
    }

    @Override
    protected boolean supportsGrowing() {
        return true;
    }
}
