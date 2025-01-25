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

import java.util.Collection;
import java.util.concurrent.locks.ReadWriteLock;

import de.learnlib.oracle.EquivalenceOracle.DFAEquivalenceOracle;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.query.Query;
import net.automatalib.common.util.Pair;
import net.automatalib.incremental.dfa.IncrementalDFABuilder;

/**
 * A thread-safe variant of {@link DFACacheOracle}.
 *
 * @param <I>
 *         input symbol type
 */
public class ThreadSafeDFACacheOracle<I> extends DFACacheOracle<I> {

    private final ReadWriteLock lock;

    ThreadSafeDFACacheOracle(IncrementalDFABuilder<I> incDfa,
                             MembershipOracle<I, Boolean> delegate,
                             ReadWriteLock lock) {
        super(incDfa, delegate);
        this.lock = lock;
    }

    @Override
    public DFAEquivalenceOracle<I> createCacheConsistencyTest() {
        return new ThreadSafeDFACacheConsistencyTest<>(super.createCacheConsistencyTest(), lock);
    }

    @Override
    Pair<Collection<ProxyQuery<I>>, Collection<Query<I, Boolean>>> queryCache(Collection<? extends Query<I, Boolean>> queries) {
        lock.readLock().lock();
        try {
            return super.queryCache(queries);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    void updateCache(Collection<? extends ProxyQuery<I>> proxyQueries) {
        lock.writeLock().lock();
        try {
            super.updateCache(proxyQueries);
        } finally {
            lock.writeLock().unlock();
        }
    }
}
