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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import de.learnlib.api.Resumable;
import de.learnlib.api.StateLocalInputSUL;
import de.learnlib.filter.cache.sul.StateLocalInputSULCache.StateLocalInputSULCacheState;
import net.automatalib.incremental.mealy.IncrementalMealyBuilder;
import net.automatalib.incremental.mealy.tree.IncrementalMealyTreeBuilder;
import net.automatalib.ts.output.MealyTransitionSystem;
import net.automatalib.words.Alphabet;

public class StateLocalInputSULCache<I, O> extends AbstractSULCache<I, O>
        implements StateLocalInputSUL<I, O>, Resumable<StateLocalInputSULCacheState<I, O>> {

    private final StateLocalInputSULCacheImpl<?, I, ?, O> impl;

    StateLocalInputSULCache(IncrementalMealyBuilder<I, O> incMealy, StateLocalInputSUL<I, O> sul) {
        this(new StateLocalInputSULCacheImpl<>(incMealy,
                                               new ReentrantReadWriteLock(),
                                               incMealy.asTransitionSystem(),
                                               sul));
    }

    private <S, T> StateLocalInputSULCache(StateLocalInputSULCacheImpl<S, I, T, O> cacheImpl) {
        super(cacheImpl);
        this.impl = cacheImpl;
    }

    public static <I, O> StateLocalInputSULCache<I, O> createTreeCache(Alphabet<I> alphabet,
                                                                       StateLocalInputSUL<I, O> sul) {
        return new StateLocalInputSULCache<>(new IncrementalMealyTreeBuilder<>(alphabet), sul);
    }

    @Override
    public StateLocalInputSUL<I, O> fork() {
        return impl.fork();
    }

    @Override
    public Collection<I> currentlyEnabledInputs() {
        return impl.currentlyEnabledInputs();
    }

    @Override
    public StateLocalInputSULCacheState<I, O> suspend() {
        return impl.suspend();
    }

    @Override
    public void resume(StateLocalInputSULCacheState<I, O> state) {
        this.impl.resume(state);
    }

    private static final class StateLocalInputSULCacheImpl<S, I, T, O>
            extends AbstractSULCacheImpl<S, I, T, O, StateLocalInputSULCacheState<I, O>>
            implements StateLocalInputSUL<I, O> {

        private final StateLocalInputSUL<I, O> delegate;

        private S initialState;
        private Map<S, Collection<I>> enabledInputCache;
        private final List<Collection<I>> inputsTrace;

        StateLocalInputSULCacheImpl(IncrementalMealyBuilder<I, O> incMealy,
                                    ReadWriteLock lock,
                                    MealyTransitionSystem<S, I, T, O> mealyTs,
                                    StateLocalInputSUL<I, O> sul) {
            this(incMealy, lock, mealyTs, new HashMap<>(), sul);
        }

        private StateLocalInputSULCacheImpl(IncrementalMealyBuilder<I, O> incMealy,
                                            ReadWriteLock lock,
                                            MealyTransitionSystem<S, I, T, O> mealyTs,
                                            Map<S, Collection<I>> enabledInputCache,
                                            StateLocalInputSUL<I, O> sul) {
            super(incMealy, lock, mealyTs, sul);
            this.delegate = sul;
            S init = mealyTs.getInitialState();
            assert init != null;
            this.initialState = init;
            this.enabledInputCache = enabledInputCache;
            this.inputsTrace = new ArrayList<>();
        }

        @Override
        protected void postNewStepHook() {
            inputsTrace.add(delegate.currentlyEnabledInputs());
        }

        @Override
        protected void postCacheWriteHook(List<I> input) {
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

            final Collection<I> inputs = this.enabledInputCache.get(super.current);
            if (inputs != null) {
                return inputs;
            } else {
                return this.inputsTrace.get(this.inputsTrace.size() - 1);
            }
        }

        @Override
        public StateLocalInputSUL<I, O> fork() {
            return new StateLocalInputSULCacheImpl<>(incMealy,
                                                     incMealyLock,
                                                     mealyTs,
                                                     enabledInputCache,
                                                     delegate.fork());
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
