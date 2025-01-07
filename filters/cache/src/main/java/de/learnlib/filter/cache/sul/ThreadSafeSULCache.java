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
package de.learnlib.filter.cache.sul;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import de.learnlib.filter.cache.mealy.ThreadSafeMealyCacheConsistencyTest;
import de.learnlib.oracle.EquivalenceOracle.MealyEquivalenceOracle;
import de.learnlib.sul.SUL;
import net.automatalib.incremental.mealy.IncrementalMealyBuilder;
import net.automatalib.ts.output.MealyTransitionSystem;
import net.automatalib.word.WordBuilder;

/**
 * A thread-safe variant of {@link SULCache}.
 *
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbol type
 */
public class ThreadSafeSULCache<I, O> extends SULCache<I, O> {

    ThreadSafeSULCache(IncrementalMealyBuilder<I, O> incMealy, SUL<I, O> sul) {
        this(new ThreadSafeSULCacheImpl<>(incMealy, new ReentrantReadWriteLock(), incMealy.asTransitionSystem(), sul));
    }

    private ThreadSafeSULCache(ThreadSafeSULCacheImpl<?, I, ?, O> cacheImpl) {
        super(cacheImpl);
    }

    private static final class ThreadSafeSULCacheImpl<S, I, T, O> extends SULCacheImpl<S, I, T, O> {

        private final ReadWriteLock lock;

        ThreadSafeSULCacheImpl(IncrementalMealyBuilder<I, O> incMealy,
                               ReadWriteLock lock,
                               MealyTransitionSystem<S, I, T, O> mealyTs,
                               SUL<I, O> sul) {
            super(incMealy, mealyTs, sul);
            this.lock = lock;
        }

        @Override
        public void pre() {
            lock.readLock().lock();
            super.pre();
        }

        @Override
        protected void requiredInitializedDelegate() {
            lock.readLock().unlock();
            super.requiredInitializedDelegate();
        }

        @Override
        protected void updateCache(WordBuilder<I> inputBuilder, WordBuilder<O> outputBuilder) {
            if (outputBuilder.isEmpty()) { // if outputBuilder is empty we still hold the read-lock!
                lock.readLock().unlock();
            } else { // otherwise acquire write-lock to update cache!
                lock.writeLock().lock();
                try {
                    super.updateCache(inputBuilder, outputBuilder);
                } finally {
                    lock.writeLock().unlock();
                }
            }
        }

        @Override
        public boolean canFork() {
            return delegate.canFork();
        }

        @Override
        public MealyEquivalenceOracle<I, O> createCacheConsistencyTest() {
            return new ThreadSafeMealyCacheConsistencyTest<>(super.createCacheConsistencyTest(), lock);
        }

        @Override
        public SUL<I, O> fork() {
            return new ThreadSafeSULCacheImpl<>(incMealy, lock, mealyTs, delegate.fork());
        }
    }

}
