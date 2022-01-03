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

import de.learnlib.api.oracle.parallelism.ParallelOracle;
import de.learnlib.filter.cache.configuration.CacheConfig;
import de.learnlib.filter.cache.configuration.CacheCreator.SLISULCacheCreator;
import de.learnlib.filter.cache.configuration.Config;
import de.learnlib.filter.cache.sul.ThreadSafeSULCaches;
import de.learnlib.filter.cache.sul.ThreadSafeStateLocalInputSULCache;
import de.learnlib.filter.statistic.sul.ResetCounterStateLocalInputSUL;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;

/**
 * @author frohme
 */
public class SLISULParallelCacheTest
        extends AbstractParallelCacheTest<MealyMachine<?, Character, ?, Character>, Character, Word<Character>> {

    private final ResetCounterStateLocalInputSUL<Character, Character> sul;
    private final ThreadSafeStateLocalInputSULCache<Character, Character> cacheRepresentative;
    private final ParallelOracle<Character, Word<Character>> parallelOracle;

    @Factory(dataProvider = "caches")
    public SLISULParallelCacheTest(SLISULCacheCreator<Character, Character, ThreadSafeStateLocalInputSULCache<Character, Character>> creator) {
        this.sul = Config.getCounter(Config.TARGET_MODEL_SLI_SUL);

        final CacheConfig<Character, Word<Character>, ThreadSafeStateLocalInputSULCache<Character, Character>> config =
                creator.apply(Config.ALPHABET, sul);

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
        return Config.ALPHABET;
    }

    @Override
    protected MealyMachine<?, Character, ?, Character> getTargetModel() {
        return Config.TARGET_MODEL_MEALY;
    }

    @Override
    protected ThreadSafeStateLocalInputSULCache<Character, Character> getCacheRepresentative() {
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
