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
package de.learnlib.filter.cache.dfa;

import de.learnlib.filter.cache.AbstractParallelCacheTest;
import de.learnlib.filter.cache.CacheConfig;
import de.learnlib.filter.cache.CacheCreator.DFACacheCreator;
import de.learnlib.filter.cache.CacheTestUtils;
import de.learnlib.filter.statistic.oracle.DFACounterOracle;
import de.learnlib.oracle.ParallelOracle;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.fsa.DFA;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;

public class DFAParallelCacheTest extends AbstractParallelCacheTest<DFA<?, Character>, Character, Boolean> {

    private final DFACounterOracle<Character> sul;
    private final ThreadSafeDFACacheOracle<Character> cacheRepresentative;
    private final ParallelOracle<Character, Boolean> parallelOracle;

    @Factory(dataProvider = "caches")
    public DFAParallelCacheTest(DFACacheCreator<Character, ThreadSafeDFACacheOracle<Character>> creator) {
        this.sul = CacheTestUtils.getCounter(CacheTestUtils.DFA);

        final CacheConfig<Character, Boolean, ThreadSafeDFACacheOracle<Character>> config =
                creator.apply(CacheTestUtils.INPUT_ALPHABET, this.sul);

        this.cacheRepresentative = config.getRepresentative();
        this.parallelOracle = config.getParallelOracle();
    }

    @DataProvider(name = "caches")
    public static Object[][] cacheProvider() {
        return new DFACacheCreator<?, ?>[][] {{DFACacheCreator.forSupplier(ThreadSafeDFACaches::createCache)},
                                              {DFACacheCreator.forSupplier(ThreadSafeDFACaches::createDAGCache)},
                                              {DFACacheCreator.forSupplier(ThreadSafeDFACaches::createTreeCache)},
                                              {DFACacheCreator.forCollection(ThreadSafeDFACaches::createCache)},
                                              {DFACacheCreator.forCollection(ThreadSafeDFACaches::createDAGCache)},
                                              {DFACacheCreator.forCollection(ThreadSafeDFACaches::createTreeCache)}};
    }

    @Override
    protected Alphabet<Character> getAlphabet() {
        return CacheTestUtils.INPUT_ALPHABET;
    }

    @Override
    protected DFA<?, Character> getTargetModel() {
        return CacheTestUtils.DFA;
    }

    @Override
    protected ThreadSafeDFACacheOracle<Character> getCacheRepresentative() {
        return this.cacheRepresentative;
    }

    @Override
    protected ParallelOracle<Character, Boolean> getParallelOracle() {
        return this.parallelOracle;
    }

    @Override
    protected long getNumberOfQueries() {
        return this.sul.getQueryCounter().getCount();
    }
}
