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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.learnlib.filter.cache.sul.StateLocalInputSULCache.StateLocalInputSULCacheState;
import de.learnlib.sul.StateLocalInputSUL;
import net.automatalib.incremental.mealy.IncrementalMealyBuilder;
import net.automatalib.ts.output.MealyTransitionSystem;
import net.automatalib.word.WordBuilder;

/**
 * A {@link SULCache} that additionally caches the {@link StateLocalInputSUL#currentlyEnabledInputs() currently enabled
 * inputs} of the given {@link StateLocalInputSUL}.
 *
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbol type
 */
public class StateLocalInputSULCache<I, O> extends AbstractSULCache<I, O, StateLocalInputSULCacheState<I, O>>
        implements StateLocalInputSUL<I, O> {

    private final StateLocalInputSULCacheImpl<?, I, ?, O> impl;

    StateLocalInputSULCache(IncrementalMealyBuilder<I, O> incMealy, StateLocalInputSUL<I, O> sul) {
        this(new StateLocalInputSULCacheImpl<>(incMealy, incMealy.asTransitionSystem(), sul));
    }

    StateLocalInputSULCache(StateLocalInputSULCacheImpl<?, I, ?, O> cacheImpl) {
        super(cacheImpl);
        this.impl = cacheImpl;
    }

    @Override
    public StateLocalInputSUL<I, O> fork() {
        return impl.fork();
    }

    @Override
    public Collection<I> currentlyEnabledInputs() {
        return impl.currentlyEnabledInputs();
    }

    static class StateLocalInputSULCacheImpl<S, I, T, O>
            extends AbstractSULCacheImpl<S, I, T, O, StateLocalInputSULCacheState<I, O>>
            implements StateLocalInputSUL<I, O> {

        protected final StateLocalInputSUL<I, O> delegate;

        private S initialState;
        protected Map<S, Collection<I>> enabledInputCache;
        private final List<Collection<I>> inputsTrace;

        StateLocalInputSULCacheImpl(IncrementalMealyBuilder<I, O> incMealy,
                                    MealyTransitionSystem<S, I, T, O> mealyTs,
                                    StateLocalInputSUL<I, O> sul) {
            this(incMealy, mealyTs, new HashMap<>(), sul);
        }

        StateLocalInputSULCacheImpl(IncrementalMealyBuilder<I, O> incMealy,
                                    MealyTransitionSystem<S, I, T, O> mealyTs,
                                    Map<S, Collection<I>> enabledInputCache,
                                    StateLocalInputSUL<I, O> sul) {
            super(incMealy, mealyTs, sul);
            this.delegate = sul;
            this.initialState = Objects.requireNonNull(mealyTs.getInitialState());
            this.enabledInputCache = enabledInputCache;
            this.inputsTrace = new ArrayList<>();
        }

        @Override
        protected void postNewStepHook() {
            inputsTrace.add(delegate.currentlyEnabledInputs());
        }

        @Override
        protected void updateCache(WordBuilder<I> input, WordBuilder<O> output) {
            super.updateCache(input, output);

            final int prefixLength = input.size() - this.inputsTrace.size();
            S iter = mealyTs.getSuccessor(initialState, input.subList(0, prefixLength));
            assert iter != null;

            for (int i = 0; i < this.inputsTrace.size(); i++) {
                iter = mealyTs.getSuccessor(iter, input.get(i + prefixLength));
                assert iter != null;
                this.enabledInputCache.put(iter, this.inputsTrace.get(i));
            }

            inputsTrace.clear();
        }

        @Override
        public Collection<I> currentlyEnabledInputs() {
            if (super.current == initialState) {
                Collection<I> initialInputs = this.enabledInputCache.get(initialState);
                if (initialInputs == null) {
                    super.requiredInitializedDelegate();
                    initialInputs = this.delegate.currentlyEnabledInputs();
                    this.enabledInputCache.put(initialState, initialInputs);
                }
                return initialInputs;
            }

            if (super.current != null) {
                final Collection<I> inputs = this.enabledInputCache.get(super.current);
                if (inputs != null) {
                    return inputs;
                }
            }
            return this.inputsTrace.get(this.inputsTrace.size() - 1);
        }

        @Override
        public StateLocalInputSULCacheState<I, O> suspend() {
            return new StateLocalInputSULCacheState<>(incMealy, enabledInputCache);
        }

        @Override
        @SuppressWarnings("unchecked")
        public void resume(StateLocalInputSULCacheState<I, O> state) {
            super.resume(state);
            S init = super.mealyTs.getInitialState();
            assert init != null;
            this.initialState = init;
            this.enabledInputCache = (Map<S, Collection<I>>) state.enabledInputCache;
        }
    }

    public static final class StateLocalInputSULCacheState<I, O> extends SULCacheState<I, O> {

        final Map<?, Collection<I>> enabledInputCache;

        StateLocalInputSULCacheState(IncrementalMealyBuilder<I, O> builder, Map<?, Collection<I>> enabledInputCache) {
            super(builder);
            this.enabledInputCache = enabledInputCache;
        }
    }

}
