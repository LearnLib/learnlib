/* Copyright (C) 2013-2022 TU Dortmund
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
package de.learnlib.filter.cache;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Supplier;

import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.oracle.parallelism.ParallelOracle;
import de.learnlib.filter.cache.configuration.CacheConfig;
import de.learnlib.filter.cache.configuration.CacheCreator.MealyCacheCreator;
import de.learnlib.filter.cache.configuration.Config;
import de.learnlib.filter.cache.mealy.ThreadSafeMealyCacheOracle;
import de.learnlib.filter.cache.mealy.ThreadSafeMealyCaches;
import de.learnlib.filter.statistic.oracle.MealyCounterOracle;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;

/**
 * @author frohme
 */
public class MealyParallelCacheTest
        extends AbstractParallelCacheTest<MealyMachine<?, Character, ?, Character>, Character, Word<Character>> {

    private final MealyCounterOracle<Character, Character> sul;
    private final ThreadSafeMealyCacheOracle<Character, Character> cacheRepresentative;
    private final ParallelOracle<Character, Word<Character>> parallelOracle;

    @Factory(dataProvider = "caches")
    public MealyParallelCacheTest(MealyCacheCreator<Character, Character, ThreadSafeMealyCacheOracle<Character, Character>> creator) {
        this.sul = Config.getCounter(Config.TARGET_MODEL_MEALY);

        final CacheConfig<Character, Word<Character>, ThreadSafeMealyCacheOracle<Character, Character>> config =
                creator.apply(Config.ALPHABET, this.sul);

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
        return Config.ALPHABET;
    }

    @Override
    protected MealyMachine<?, Character, ?, Character> getTargetModel() {
        return Config.TARGET_MODEL_MEALY;
    }

    @Override
    protected ThreadSafeMealyCacheOracle<Character, Character> getCacheRepresentative() {
        return this.cacheRepresentative;
    }

    @Override
    protected ParallelOracle<Character, Word<Character>> getParallelOracle() {
        return this.parallelOracle;
    }

    @Override
    protected long getNumberOfQueries() {
        return this.sul.getStatisticalData().getCount();
    }
}
