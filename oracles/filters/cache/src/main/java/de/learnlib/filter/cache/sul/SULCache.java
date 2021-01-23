/* Copyright (C) 2013-2021 TU Dortmund
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
import java.util.concurrent.locks.ReentrantReadWriteLock;

import de.learnlib.api.Resumable;
import de.learnlib.api.SUL;
import de.learnlib.filter.cache.sul.AbstractSULCache.SULCacheState;
import net.automatalib.incremental.mealy.IncrementalMealyBuilder;
import net.automatalib.incremental.mealy.dag.IncrementalMealyDAGBuilder;
import net.automatalib.incremental.mealy.tree.IncrementalMealyTreeBuilder;
import net.automatalib.ts.output.MealyTransitionSystem;
import net.automatalib.words.Alphabet;

/**
 * A cache to be used with a {@link SUL}.
 * <p>
 * Because on a {@link SUL}, a query is executed step-by-step, it is impossible to determine in advance whether the
 * cached information is sufficient to answer the complete query. However, in general it is undesired to execute any
 * actions on the underlying SUL as long as the requested information can be provided from the cache.
 * <p>
 * This class therefore defers any real execution to the point where the cached information is definitely insufficient;
 * if such a point is not reached before a call to {@link #post()} is made, the underlying SUL is not queried.
 *
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbol type
 *
 * @author Malte Isberner
 */
public class SULCache<I, O> extends AbstractSULCache<I, O> implements Resumable<SULCacheState<I, O>> {

    private final SULCacheImpl<?, I, ?, O> impl;

    SULCache(IncrementalMealyBuilder<I, O> incMealy, SUL<I, O> sul) {
        this(new SULCacheImpl<>(incMealy, new ReentrantReadWriteLock(), incMealy.asTransitionSystem(), sul));
    }

    private <S, T> SULCache(SULCacheImpl<S, I, T, O> cacheImpl) {
        super(cacheImpl);
        this.impl = cacheImpl;
    }

    public static <I, O> SULCache<I, O> createTreeCache(Alphabet<I> alphabet, SUL<I, O> sul) {
        return new SULCache<>(new IncrementalMealyTreeBuilder<>(alphabet), sul);
    }

    public static <I, O> SULCache<I, O> createDAGCache(Alphabet<I> alphabet, SUL<I, O> sul) {
        return new SULCache<>(new IncrementalMealyDAGBuilder<>(alphabet), sul);
    }

    @Override
    public SULCacheState<I, O> suspend() {
        return impl.suspend();
    }

    @Override
    public void resume(SULCacheState<I, O> state) {
        this.impl.resume(state);
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
    private static final class SULCacheImpl<S, I, T, O> extends AbstractSULCacheImpl<S, I, T, O, SULCacheState<I, O>> {

        SULCacheImpl(IncrementalMealyBuilder<I, O> incMealy,
                     ReadWriteLock lock,
                     MealyTransitionSystem<S, I, T, O> mealyTs,
                     SUL<I, O> sul) {
            super(incMealy, lock, mealyTs, sul);
        }

        @Override
        public SUL<I, O> fork() {
            return new SULCacheImpl<>(incMealy, incMealyLock, mealyTs, delegate.fork());
        }

        @Override
        public SULCacheState<I, O> suspend() {
            return new SULCacheState<>(incMealy);
        }
    }

}
