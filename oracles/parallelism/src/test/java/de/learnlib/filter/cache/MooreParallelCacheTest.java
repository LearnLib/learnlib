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
package de.learnlib.filter.cache;

import de.learnlib.filter.cache.configuration.CacheConfig;
import de.learnlib.filter.cache.configuration.CacheCreator.MooreCacheCreator;
import de.learnlib.filter.cache.configuration.Config;
import de.learnlib.filter.cache.moore.ThreadSafeMooreCacheOracle;
import de.learnlib.filter.cache.moore.ThreadSafeMooreCaches;
import de.learnlib.filter.statistic.oracle.MooreCounterOracle;
import de.learnlib.oracle.parallelism.ParallelOracle;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.transducer.MooreMachine;
import net.automatalib.word.Word;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;

public class MooreParallelCacheTest
        extends AbstractParallelCacheTest<MooreMachine<?, Character, ?, Character>, Character, Word<Character>> {

    private final MooreCounterOracle<Character, Character> sul;
    private final ThreadSafeMooreCacheOracle<Character, Character> cacheRepresentative;
    private final ParallelOracle<Character, Word<Character>> parallelOracle;

    @Factory(dataProvider = "caches")
    public MooreParallelCacheTest(MooreCacheCreator<Character, Character, ThreadSafeMooreCacheOracle<Character, Character>> creator) {
        this.sul = Config.getCounter(Config.TARGET_MODEL_MOORE);

        final CacheConfig<Character, Word<Character>, ThreadSafeMooreCacheOracle<Character, Character>> config =
                creator.apply(Config.ALPHABET, this.sul);

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
        return Config.ALPHABET;
    }

    @Override
    protected MooreMachine<?, Character, ?, Character> getTargetModel() {
        return Config.TARGET_MODEL_MOORE;
    }

    @Override
    protected ThreadSafeMooreCacheOracle<Character, Character> getCacheRepresentative() {
        return this.cacheRepresentative;
    }

    @Override
    protected ParallelOracle<Character, Word<Character>> getParallelOracle() {
        return this.parallelOracle;
    }

    @Override
    protected long getNumberOfQueries() {
        return this.sul.getQueryCounter().getCount();
    }
}
