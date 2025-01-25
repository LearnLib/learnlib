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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.learnlib.driver.simulator.MealySimulatorSUL;
import de.learnlib.filter.cache.AbstractCacheTest;
import de.learnlib.filter.cache.CacheTestUtils;
import de.learnlib.filter.cache.LearningCacheOracle.MealyLearningCacheOracle;
import de.learnlib.filter.cache.mealy.AdaptiveQueryCacheTest.Wrapper;
import de.learnlib.filter.statistic.oracle.CounterAdaptiveQueryOracle;
import de.learnlib.oracle.EquivalenceOracle;
import de.learnlib.oracle.membership.SULAdaptiveOracle;
import de.learnlib.query.AdaptiveQuery;
import de.learnlib.query.Query;
import de.learnlib.util.mealy.PresetAdaptiveQuery;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.SupportsGrowingAlphabet;
import net.automatalib.alphabet.impl.GrowingMapAlphabet;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.word.Word;
import org.testng.annotations.Test;

public class AdaptiveQueryCacheTest
        extends AbstractCacheTest<Wrapper<Character, Integer>, MealyMachine<?, Character, ?, Integer>, Character, Word<Integer>> {

    private final CounterAdaptiveQueryOracle<Character, Integer> counter;

    public AdaptiveQueryCacheTest() {
        counter =
                new CounterAdaptiveQueryOracle<>(new SULAdaptiveOracle<>(new MealySimulatorSUL<>(CacheTestUtils.MEALY)));
    }

    @Test(enabled = false)
    @Override
    public void testDuplicatesInBatch() {
        // adaptive queries don't support duplicate detection in batches
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
    protected Wrapper<Character, Integer> getCachedOracle() {
        return new Wrapper<>(MealyCaches.createAdaptiveQueryCache(getAlphabet(), counter));
    }

    @Override
    protected Wrapper<Character, Integer> getResumedOracle(Wrapper<Character, Integer> original) {
        final AdaptiveQueryCache<Character, Integer> fresh =
                MealyCaches.createAdaptiveQueryCache(getAlphabet(), counter);
        serializeResumable(original.delegate, fresh);
        return new Wrapper<>(fresh);
    }

    @Override
    protected long getNumberOfPosedQueries() {
        return counter.getResetCounter().getCount();
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

    protected static final class Wrapper<I, O> implements MealyLearningCacheOracle<I, O>, SupportsGrowingAlphabet<I> {

        private final AdaptiveQueryCache<I, O> delegate;

        private Wrapper(AdaptiveQueryCache<I, O> delegate) {
            this.delegate = delegate;
        }

        @Override
        public EquivalenceOracle<MealyMachine<?, I, ?, O>, I, Word<O>> createCacheConsistencyTest() {
            return delegate.createCacheConsistencyTest();
        }

        @Override
        public void processQueries(Collection<? extends Query<I, Word<O>>> queries) {
            final List<AdaptiveQuery<I, O>> mapped = new ArrayList<>(queries.size());

            for (Query<I, Word<O>> q : queries) {
                mapped.add(new PresetAdaptiveQuery<>(q));
            }

            this.delegate.processQueries(mapped);
        }

        @Override
        public void addAlphabetSymbol(I i) {
            this.delegate.addAlphabetSymbol(i);
        }
    }
}
