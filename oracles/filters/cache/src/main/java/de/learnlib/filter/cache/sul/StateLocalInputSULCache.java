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

import java.io.Serializable;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StateLocalInputSULCache<I, O> extends AbstractSULCache<I, O>
        implements StateLocalInputSUL<I, O>, Resumable<StateLocalInputSULCacheState<I, O>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(StateLocalInputSULCache.class);

    private final StateLocalInputSULCacheImpl<?, I, ?, O> impl;

    StateLocalInputSULCache(IncrementalMealyBuilder<I, O> incMealy, StateLocalInputSUL<I, O> sul) {
        this(new StateLocalInputSULCacheImpl<>(incMealy,
                                               new ReentrantReadWriteLock(),
                                               incMealy.asTransitionSystem(),
                                               new HashMap<>(),
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

    private static final class StateLocalInputSULCacheImpl<S, I, T, O> extends AbstractSULCacheImpl<S, I, T, O>
            implements StateLocalInputSUL<I, O>, Resumable<StateLocalInputSULCacheState<I, O>> {

        private final StateLocalInputSUL<I, O> delegate;
        private Map<S, Collection<I>> enabledInputCache;

        private final List<Collection<I>> currentlyEnabledInputs;
        private int cacheMissIdx;

        StateLocalInputSULCacheImpl(IncrementalMealyBuilder<I, O> incMealy,
                                    ReadWriteLock lock,
                                    MealyTransitionSystem<S, I, T, O> mealyTs,
                                    Map<S, Collection<I>> enabledInputCache,
                                    StateLocalInputSUL<I, O> sul) {
            super(incMealy, lock, mealyTs, sul);
            this.delegate = sul;
            this.enabledInputCache = enabledInputCache;
            this.currentlyEnabledInputs = new ArrayList<>();
            this.cacheMissIdx = -1;
        }

        @Override
        public void pre() {
            super.pre();

            // if this is our first initialization, query for the initially enabled inputs
            if (!this.enabledInputCache.containsKey(super.current)) {
                requiredInitializedDelegate();
                this.enabledInputCache.put(super.current, this.delegate.currentlyEnabledInputs());
            }
        }

        @Override
        public O step(I in) {
            final O result = super.step(in);

            // we had to delegate, so also query current available inputs
            if (super.current == null) {
                // only update pointer once
                if (this.cacheMissIdx < 0) {
                    this.cacheMissIdx = super.inputWord.size() - 1;
                }
                this.currentlyEnabledInputs.add(this.delegate.currentlyEnabledInputs());
            }

            return result;
        }

        @Override
        public void post() {
            super.post();
            this.currentlyEnabledInputs.clear();
            this.cacheMissIdx = -1;
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
        public Collection<I> currentlyEnabledInputs() {
            // we had to delegate so the currently enabled inputs are in our local cache
            if (super.current == null) {
                return this.currentlyEnabledInputs.get(this.currentlyEnabledInputs.size() - 1);
            }

            return this.enabledInputCache.get(super.current);
        }

        @Override
        public StateLocalInputSULCacheState<I, O> suspend() {
            return new StateLocalInputSULCacheState<>(incMealy, enabledInputCache);
        }

        @Override
        @SuppressWarnings("unchecked")
        public void resume(StateLocalInputSULCacheState<I, O> state) {
            final Class<?> thisClass = this.incMealy.getClass();
            final Class<?> stateClass = state.builder.getClass();

            if (!thisClass.equals(stateClass)) {
                LOGGER.warn(
                        "You currently plan to use a '{}', but the state contained a '{}'. This may yield unexpected behavior.",
                        thisClass,
                        stateClass);
            }

            this.incMealy = state.builder;
            this.mealyTs = (MealyTransitionSystem<S, I, T, O>) this.incMealy.asTransitionSystem();
            this.enabledInputCache = (Map<S, Collection<I>>) state.enabledInputCache;
        }

        @Override
        protected void writeCache() {
            // update TS
            super.writeCache();

            S iter = super.mealyTs.getInitialState();

            for (int i = 0; i < super.inputWord.size(); i++) {
                iter = mealyTs.getSuccessor(iter, super.inputWord.get(i));
                if (i >= this.cacheMissIdx) {
                    this.enabledInputCache.put(iter, this.currentlyEnabledInputs.get(i - this.cacheMissIdx));
                }
            }
        }
    }

    public static class StateLocalInputSULCacheState<I, O> implements Serializable {

        final IncrementalMealyBuilder<I, O> builder;
        final Map<?, Collection<I>> enabledInputCache;

        public StateLocalInputSULCacheState(IncrementalMealyBuilder<I, O> builder,
                                            Map<?, Collection<I>> enabledInputCache) {
            this.builder = builder;
            this.enabledInputCache = enabledInputCache;
        }
    }

}
