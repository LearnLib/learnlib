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

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import de.learnlib.filter.cache.mealy.ThreadSafeMealyCacheConsistencyTest;
import de.learnlib.oracle.EquivalenceOracle.MealyEquivalenceOracle;
import de.learnlib.sul.StateLocalInputSUL;
import net.automatalib.incremental.mealy.IncrementalMealyBuilder;
import net.automatalib.ts.output.MealyTransitionSystem;
import net.automatalib.word.WordBuilder;

/**
 * A thread-safe variant of {@link StateLocalInputSULCache}.
 *
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbol type
 */
public class ThreadSafeStateLocalInputSULCache<I, O> extends StateLocalInputSULCache<I, O> {

    ThreadSafeStateLocalInputSULCache(IncrementalMealyBuilder<I, O> incMealy, StateLocalInputSUL<I, O> sul) {
        this(new ThreadSafeStateLocalInputSULCacheImpl<>(incMealy,
                                                         new ReentrantReadWriteLock(),
                                                         incMealy.asTransitionSystem(),
                                                         sul));
    }

    ThreadSafeStateLocalInputSULCache(ThreadSafeStateLocalInputSULCacheImpl<?, I, ?, O> cacheImpl) {
        super(cacheImpl);
    }

    private static final class ThreadSafeStateLocalInputSULCacheImpl<S, I, T, O>
            extends StateLocalInputSULCacheImpl<S, I, T, O> {

        private final ReadWriteLock lock;

        ThreadSafeStateLocalInputSULCacheImpl(IncrementalMealyBuilder<I, O> incMealy,
                                              ReadWriteLock lock,
                                              MealyTransitionSystem<S, I, T, O> mealyTs,
                                              StateLocalInputSUL<I, O> sul) {
            this(incMealy, lock, mealyTs, new ConcurrentHashMap<>(), sul);
        }

        ThreadSafeStateLocalInputSULCacheImpl(IncrementalMealyBuilder<I, O> incMealy,
                                              ReadWriteLock lock,
                                              MealyTransitionSystem<S, I, T, O> mealyTs,
                                              Map<S, Collection<I>> enabledInputCache,
                                              StateLocalInputSUL<I, O> sul) {
            super(incMealy, mealyTs, enabledInputCache, sul);
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
        public StateLocalInputSUL<I, O> fork() {
            return new ThreadSafeStateLocalInputSULCacheImpl<>(incMealy,
                                                               lock,
                                                               mealyTs,
                                                               enabledInputCache,
                                                               delegate.fork());
        }

    }

}
