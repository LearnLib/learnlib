/* Copyright (C) 2013-2020 TU Dortmund
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
package de.learnlib.filter.cache.sul;

import java.util.concurrent.locks.ReadWriteLock;

import de.learnlib.api.SUL;
import de.learnlib.filter.cache.LearningCache.MealyLearningCache;
import de.learnlib.filter.cache.mealy.MealyCacheConsistencyTest;
import net.automatalib.SupportsGrowingAlphabet;
import net.automatalib.incremental.mealy.IncrementalMealyBuilder;
import net.automatalib.ts.output.MealyTransitionSystem;
import net.automatalib.words.WordBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;

abstract class AbstractSULCache<I, O> implements SUL<I, O>, MealyLearningCache<I, O>, SupportsGrowingAlphabet<I> {

    private final AbstractSULCacheImpl<?, I, ?, O> impl;

    protected <S, T> AbstractSULCache(AbstractSULCacheImpl<S, I, T, O> cacheImpl) {
        this.impl = cacheImpl;
    }

    @Override
    public void pre() {
        impl.pre();
    }

    @Override
    public void post() {
        impl.post();
    }

    @Override
    public O step(I in) {
        return impl.step(in);
    }

    @Override
    public boolean canFork() {
        return impl.canFork();
    }

    @Override
    public SUL<I, O> fork() {
        return impl.fork();
    }

    @Override
    public MealyCacheConsistencyTest<I, O> createCacheConsistencyTest() {
        return impl.createCacheConsistencyTest();
    }

    @Override
    public void addAlphabetSymbol(I symbol) {
        impl.addAlphabetSymbol(symbol);
    }

    public int size() {
        return impl.incMealy.asGraph().size();
    }

    /**
     * Implementation class; we need this to bind the {@code T} and {@code S} type parameters of the transition system
     * returned by {@link IncrementalMealyBuilder#asTransitionSystem()}.
     *
     * @param <S>
     *         transition system state type
     * @param <I>
     *         input symbol type
     * @param <T>
     *         transition system transition type
     * @param <O>
     *         output symbol type
     *
     * @author Malte Isberner
     */
    abstract static class AbstractSULCacheImpl<S, I, T, O>
            implements SUL<I, O>, MealyLearningCache<I, O>, SupportsGrowingAlphabet<I> {

        protected IncrementalMealyBuilder<I, O> incMealy;
        protected MealyTransitionSystem<S, I, T, O> mealyTs;
        protected final SUL<I, O> delegate;
        protected final ReadWriteLock incMealyLock;

        protected final WordBuilder<I> inputWord = new WordBuilder<>();
        private final WordBuilder<O> outputWord = new WordBuilder<>();

        private boolean delegatePreCalled;
        protected @Nullable S current;

        AbstractSULCacheImpl(IncrementalMealyBuilder<I, O> incMealy,
                             ReadWriteLock lock,
                             MealyTransitionSystem<S, I, T, O> mealyTs,
                             SUL<I, O> sul) {
            this.incMealy = incMealy;
            this.mealyTs = mealyTs;
            this.delegate = sul;
            this.incMealyLock = lock;
        }

        @Override
        public void pre() {
            incMealyLock.readLock().lock();
            this.current = mealyTs.getInitialState();
        }

        @Override
        public O step(I in) {
            O out = null;

            if (current != null) {
                T trans = mealyTs.getTransition(current, in);

                if (trans != null) {
                    out = mealyTs.getTransitionOutput(trans);
                    current = mealyTs.getSuccessor(trans);
                    assert current != null;
                } else {
                    incMealyLock.readLock().unlock();
                    current = null;
                    requiredInitializedDelegate();
                    for (I prevSym : inputWord) {
                        outputWord.append(delegate.step(prevSym));
                    }
                }
            }

            inputWord.append(in);

            if (current == null) {
                out = delegate.step(in);
                outputWord.add(out);
            }

            return out;
        }

        // TODO: The SUL interface might need a cleanup() method which, by contract,
        // is to be called regardless of whether preceding step()s threw unrecoverable
        // errors!
        @Override
        public void post() {
            if (outputWord.isEmpty()) {
                // if outputWord is empty we still hold the read-lock!
                incMealyLock.readLock().unlock();
            } else {
                // otherwise acquire write-lock to update cache!
                incMealyLock.writeLock().lock();
                try {
                    writeCache();
                } finally {
                    incMealyLock.writeLock().unlock();
                }
            }

            if (delegatePreCalled) {
                delegate.post();
                delegatePreCalled = false;
            }
            inputWord.clear();
            outputWord.clear();
            current = null;
        }

        @Override
        public boolean canFork() {
            return delegate.canFork();
        }

        @Override
        public MealyCacheConsistencyTest<I, O> createCacheConsistencyTest() {
            return new MealyCacheConsistencyTest<>(incMealy, incMealyLock);
        }

        @Override
        public void addAlphabetSymbol(I symbol) {
            incMealy.addAlphabetSymbol(symbol);
        }

        protected void requiredInitializedDelegate() {
            if (!delegatePreCalled) {
                delegate.pre();
            }
            delegatePreCalled = true;
        }

        protected void writeCache() {
            incMealy.insert(inputWord.toWord(), outputWord.toWord());
        }
    }
}
