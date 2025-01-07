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
package de.learnlib.filter.cache.mealy;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Supplier;

import de.learnlib.filter.cache.AbstractParallelCacheTest;
import de.learnlib.filter.cache.CacheConfig;
import de.learnlib.filter.cache.CacheCreator.MealyCacheCreator;
import de.learnlib.filter.cache.CacheTestUtils;
import de.learnlib.filter.statistic.oracle.MealyCounterOracle;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.oracle.ParallelOracle;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.word.Word;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;

public class MealyParallelCacheTest
        extends AbstractParallelCacheTest<MealyMachine<?, Character, ?, Integer>, Character, Word<Integer>> {

    private final MealyCounterOracle<Character, Integer> sul;
    private final ThreadSafeMealyCacheOracle<Character, Integer> cacheRepresentative;
    private final ParallelOracle<Character, Word<Integer>> parallelOracle;

    @Factory(dataProvider = "caches")
    public MealyParallelCacheTest(MealyCacheCreator<Character, Integer, ThreadSafeMealyCacheOracle<Character, Integer>> creator) {
        this.sul = CacheTestUtils.getCounter(CacheTestUtils.MEALY);

        final CacheConfig<Character, Word<Integer>, ThreadSafeMealyCacheOracle<Character, Integer>> config =
                creator.apply(CacheTestUtils.INPUT_ALPHABET, this.sul);

        this.cacheRepresentative = config.getRepresentative();
        this.parallelOracle = config.getParallelOracle();
    }

    @DataProvider(name = "caches")
    public static Object[][] cacheProvider() {
        return new MealyCacheCreator<?, ?, ?>[][] {{MealyCacheCreator.forSupplier(ThreadSafeMealyCaches::createDAGCache)},
                                                   {MealyCacheCreator.forSupplier(ThreadSafeMealyCaches::createTreeCache)},
                                                   {MealyCacheCreator.forSupplier((Function<Supplier<? extends MembershipOracle<Object, Word<Object>>>, Supplier<ThreadSafeMealyCacheOracle<Object, Object>>>) ThreadSafeMealyCaches::createDynamicTreeCache)},
                                                   {MealyCacheCreator.forCollection(ThreadSafeMealyCaches::createDAGCache)},
                                                   {MealyCacheCreator.forCollection(ThreadSafeMealyCaches::createTreeCache)},
                                                   {MealyCacheCreator.forCollection((Function<Collection<? extends MembershipOracle<Object, Word<Object>>>, Collection<ThreadSafeMealyCacheOracle<Object, Object>>>) ThreadSafeMealyCaches::createDynamicTreeCache)}};
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
    protected ThreadSafeMealyCacheOracle<Character, Integer> getCacheRepresentative() {
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
