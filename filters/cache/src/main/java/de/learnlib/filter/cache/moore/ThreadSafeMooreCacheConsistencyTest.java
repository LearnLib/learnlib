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

import java.util.Collection;
import java.util.concurrent.locks.ReadWriteLock;

import de.learnlib.oracle.EquivalenceOracle.MooreEquivalenceOracle;
import de.learnlib.query.DefaultQuery;
import net.automatalib.automaton.transducer.MooreMachine;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A thread-safe variant of {@link MooreEquivalenceOracle}.
 *
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbol type
 */
@SuppressWarnings("PMD.TestClassWithoutTestCases") // not a traditional test class
public final class ThreadSafeMooreCacheConsistencyTest<I, O> implements MooreEquivalenceOracle<I, O> {

    private final MooreEquivalenceOracle<I, O> delegate;
    private final ReadWriteLock lock;

    public ThreadSafeMooreCacheConsistencyTest(MooreEquivalenceOracle<I, O> delegate, ReadWriteLock lock) {
        this.delegate = delegate;
        this.lock = lock;
    }

    @Override
    public @Nullable DefaultQuery<I, Word<O>> findCounterExample(MooreMachine<?, I, ?, O> hypothesis,
                                                                 Collection<? extends I> inputs) {
        lock.readLock().lock();
        try {
            return delegate.findCounterExample(hypothesis, inputs);
        } finally {
            lock.readLock().unlock();
        }
    }

}
