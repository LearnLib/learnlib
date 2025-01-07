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
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;

import de.learnlib.oracle.EquivalenceOracle.MealyEquivalenceOracle;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.query.Query;
import net.automatalib.common.util.mapping.Mapping;
import net.automatalib.incremental.mealy.IncrementalMealyBuilder;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A thread-safe variant of {@link MealyCacheOracle}.
 *
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbol type
 */
public class ThreadSafeMealyCacheOracle<I, O> extends MealyCacheOracle<I, O> {

    private final ReadWriteLock lock;

    ThreadSafeMealyCacheOracle(IncrementalMealyBuilder<I, O> incMealy,
                               @Nullable Mapping<? super O, ? extends O> errorSyms,
                               MembershipOracle<I, Word<O>> delegate,
                               ReadWriteLock lock) {
        super(incMealy, errorSyms, delegate);
        this.lock = lock;
    }

    ThreadSafeMealyCacheOracle(IncrementalMealyBuilder<I, O> incMealy,
                               @Nullable Mapping<? super O, ? extends O> errorSyms,
                               MembershipOracle<I, Word<O>> delegate,
                               Comparator<I> comparator,
                               ReadWriteLock lock) {
        super(incMealy, errorSyms, delegate, comparator);
        this.lock = lock;
    }

    @Override
    List<MasterQuery<I, O>> queryCache(Collection<? extends Query<I, Word<O>>> queries) {
        this.lock.readLock().lock();
        try {
            return super.queryCache(queries);
        } finally {
            this.lock.readLock().unlock();
        }
    }

    @Override
    void updateCache(Collection<? extends MasterQuery<I, O>> masterQueries) {
        this.lock.writeLock().lock();
        try {
            super.updateCache(masterQueries);
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    @Override
    public MealyEquivalenceOracle<I, O> createCacheConsistencyTest() {
        return new ThreadSafeMealyCacheConsistencyTest<>(super.createCacheConsistencyTest(), lock);
    }
}
