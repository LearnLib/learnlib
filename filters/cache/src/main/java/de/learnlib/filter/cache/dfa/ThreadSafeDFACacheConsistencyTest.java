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
import de.learnlib.query.DefaultQuery;
import net.automatalib.automaton.fsa.DFA;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A thread-safe variant of {@link DFAEquivalenceOracle}.
 *
 * @param <I>
 *         input symbol type
 */
@SuppressWarnings("PMD.TestClassWithoutTestCases") // not a traditional test class
final class ThreadSafeDFACacheConsistencyTest<I> implements DFAEquivalenceOracle<I> {

    private final DFAEquivalenceOracle<I> delegate;
    private final ReadWriteLock lock;

    ThreadSafeDFACacheConsistencyTest(DFAEquivalenceOracle<I> delegate, ReadWriteLock lock) {
        this.delegate = delegate;
        this.lock = lock;
    }

    @Override
    public @Nullable DefaultQuery<I, Boolean> findCounterExample(DFA<?, I> hypothesis, Collection<? extends I> inputs) {
        lock.readLock().lock();
        try {
            return delegate.findCounterExample(hypothesis, inputs);
        } finally {
            lock.readLock().unlock();
        }
    }

}

