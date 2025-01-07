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
package de.learnlib.filter.cache.moore;

import de.learnlib.filter.cache.AbstractParallelCacheTest;
import de.learnlib.filter.cache.CacheConfig;
import de.learnlib.filter.cache.CacheCreator.MooreCacheCreator;
import de.learnlib.filter.cache.CacheTestUtils;
import de.learnlib.filter.statistic.oracle.MooreCounterOracle;
import de.learnlib.oracle.ParallelOracle;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.transducer.MooreMachine;
import net.automatalib.word.Word;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;

public class MooreParallelCacheTest
        extends AbstractParallelCacheTest<MooreMachine<?, Character, ?, Integer>, Character, Word<Integer>> {

    private final MooreCounterOracle<Character, Integer> sul;
    private final ThreadSafeMooreCacheOracle<Character, Integer> cacheRepresentative;
    private final ParallelOracle<Character, Word<Integer>> parallelOracle;

    @Factory(dataProvider = "caches")
    public MooreParallelCacheTest(MooreCacheCreator<Character, Integer, ThreadSafeMooreCacheOracle<Character, Integer>> creator) {
        this.sul = CacheTestUtils.getCounter(CacheTestUtils.MOORE);

        final CacheConfig<Character, Word<Integer>, ThreadSafeMooreCacheOracle<Character, Integer>> config =
                creator.apply(CacheTestUtils.INPUT_ALPHABET, this.sul);

        this.cacheRepresentative = config.getRepresentative();
        this.parallelOracle = config.getParallelOracle();
    }

    @DataProvider(name = "caches")
    public static Object[][] cacheProvider() {
        return new MooreCacheCreator<?, ?, ?>[][] {{MooreCacheCreator.forSupplier(ThreadSafeMooreCaches::createDAGCache)},
                                                   {MooreCacheCreator.forSupplier(ThreadSafeMooreCaches::createTreeCache)},
                                                   {MooreCacheCreator.forCollection(ThreadSafeMooreCaches::createDAGCache)},
                                                   {MooreCacheCreator.forCollection(ThreadSafeMooreCaches::createTreeCache)}};
    }

    @Override
    protected Alphabet<Character> getAlphabet() {
        return CacheTestUtils.INPUT_ALPHABET;
    }

    @Override
    protected MooreMachine<?, Character, ?, Integer> getTargetModel() {
        return CacheTestUtils.MOORE;
    }

    @Override
    protected ThreadSafeMooreCacheOracle<Character, Integer> getCacheRepresentative() {
        return this.cacheRepresentative;
    }

    @Override
    protected ParallelOracle<Character, Word<Integer>> getParallelOracle() {
        return this.parallelOracle;
    }

    @Override
    protected long getNumberOfQueries() {
        return this.sul.getQueryCounter().getCount();
    }
}
