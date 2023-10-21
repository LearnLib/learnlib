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
package de.learnlib.filter.cache.mealy;

import de.learnlib.driver.simulator.MealySimulatorSUL;
import de.learnlib.filter.cache.AbstractCacheTest;
import de.learnlib.filter.cache.CacheTestUtils;
import de.learnlib.filter.statistic.oracle.CounterSymbolQueryOracle;
import de.learnlib.oracle.membership.SULSymbolQueryOracle;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.word.Word;

public class SymbolQueryCacheTest
        extends AbstractCacheTest<SymbolQueryCache<Character, Integer>, MealyMachine<?, Character, ?, Integer>, Character, Word<Integer>> {

    private final CounterSymbolQueryOracle<Character, Integer> counter;

    public SymbolQueryCacheTest() {
        counter =
                new CounterSymbolQueryOracle<>(new SULSymbolQueryOracle<>(new MealySimulatorSUL<>(CacheTestUtils.MEALY)));
    }

    @Override
    protected Alphabet<Character> getAlphabet() {
        return CacheTestUtils.INPUT_ALPHABET;
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
    protected SymbolQueryCache<Character, Integer> getCachedOracle() {
        return MealyCaches.createSymbolQueryCache(getAlphabet(), counter);
    }

    @Override
    protected SymbolQueryCache<Character, Integer> getResumedOracle(SymbolQueryCache<Character, Integer> original) {
        final SymbolQueryCache<Character, Integer> fresh = MealyCaches.createSymbolQueryCache(getAlphabet(), counter);
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
}
