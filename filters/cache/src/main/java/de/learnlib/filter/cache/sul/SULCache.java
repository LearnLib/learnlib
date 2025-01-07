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

import de.learnlib.filter.cache.sul.AbstractSULCache.SULCacheState;
import de.learnlib.sul.SUL;
import net.automatalib.incremental.mealy.IncrementalMealyBuilder;
import net.automatalib.ts.output.MealyTransitionSystem;

/**
 * A cache to be used with a {@link SUL}.
 * <p>
 * Because on a {@link SUL}, a query is executed step-by-step, it is impossible to determine in advance whether the
 * cached information is sufficient to answer the complete query. However, in general it is undesired to execute any
 * actions on the underlying SUL as long as the requested information can be provided from the cache.
 * <p>
 * This class therefore defers any real execution to the point where the cached information is definitely insufficient;
 * if such a point is not reached before a call to {@link #post()} is made, the underlying SUL is not queried.
 * <p>
 * <b>Note:</b> this implementation is <b>not</b> thread-safe. If you require a cache that is usable in a parallel
 * environment. use the {@code ThreadSafeSULCache} (or rather the {@code ThreadSafeSULCaches} factory) from the {@code
 * learnlib-parallelism} artifact.
 *
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbol type
 */
public class SULCache<I, O> extends AbstractSULCache<I, O, SULCacheState<I, O>> {

    SULCache(IncrementalMealyBuilder<I, O> incMealy, SUL<I, O> sul) {
        this(new SULCacheImpl<>(incMealy, incMealy.asTransitionSystem(), sul));
    }

    SULCache(SULCacheImpl<?, I, ?, O> cacheImpl) {
        super(cacheImpl);
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
     */
    static class SULCacheImpl<S, I, T, O> extends AbstractSULCacheImpl<S, I, T, O, SULCacheState<I, O>> {

        SULCacheImpl(IncrementalMealyBuilder<I, O> incMealy, MealyTransitionSystem<S, I, T, O> mealyTs, SUL<I, O> sul) {
            super(incMealy, mealyTs, sul);
        }

        @Override
        public SULCacheState<I, O> suspend() {
            return new SULCacheState<>(incMealy);
        }
    }

}
