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
package de.learnlib.filter.cache.sul;

import de.learnlib.filter.cache.AbstractParallelCacheTest;
import de.learnlib.filter.cache.CacheConfig;
import de.learnlib.filter.cache.CacheCreator.SLISULCacheCreator;
import de.learnlib.filter.cache.CacheTestUtils;
import de.learnlib.filter.statistic.sul.ResetCounterStateLocalInputSUL;
import de.learnlib.oracle.ParallelOracle;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.word.Word;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;

public class SLISULParallelCacheTest
        extends AbstractParallelCacheTest<MealyMachine<?, Character, ?, Integer>, Character, Word<Integer>> {

    private final ResetCounterStateLocalInputSUL<Character, Integer> sul;
    private final ThreadSafeStateLocalInputSULCache<Character, Integer> cacheRepresentative;
    private final ParallelOracle<Character, Word<Integer>> parallelOracle;

    @Factory(dataProvider = "caches")
    public SLISULParallelCacheTest(SLISULCacheCreator<Character, Integer, ThreadSafeStateLocalInputSULCache<Character, Integer>> creator) {
        this.sul = CacheTestUtils.getCounter(CacheTestUtils.SLI_SUL);

        final CacheConfig<Character, Word<Integer>, ThreadSafeStateLocalInputSULCache<Character, Integer>> config =
                creator.apply(CacheTestUtils.INPUT_ALPHABET, sul);

        this.cacheRepresentative = config.getRepresentative();
        this.parallelOracle = config.getParallelOracle();
    }

    @DataProvider(name = "caches")
    public static Object[][] cacheProvider() {
        return new SLISULCacheCreator<?, ?, ?>[][] {{SLISULCacheCreator.forSupplier(ThreadSafeSULCaches::createStateLocalInputCache)},
                                                    {SLISULCacheCreator.forSupplier(ThreadSafeSULCaches::createStateLocalInputTreeCache)}};
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
    protected ThreadSafeStateLocalInputSULCache<Character, Integer> getCacheRepresentative() {
        return this.cacheRepresentative;
    }

    @Override
    protected ParallelOracle<Character, Word<Integer>> getParallelOracle() {
        return this.parallelOracle;
    }

    @Override
    protected long getNumberOfQueries() {
        return this.sul.getStatisticalData().getCount();
    }
}
