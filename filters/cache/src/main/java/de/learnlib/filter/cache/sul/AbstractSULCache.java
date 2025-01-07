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

import de.learnlib.Resumable;
import de.learnlib.filter.cache.LearningCache.MealyLearningCache;
import de.learnlib.filter.cache.mealy.MealyCacheConsistencyTest;
import de.learnlib.filter.cache.sul.AbstractSULCache.SULCacheState;
import de.learnlib.logging.Category;
import de.learnlib.oracle.EquivalenceOracle.MealyEquivalenceOracle;
import de.learnlib.sul.SUL;
import net.automatalib.alphabet.SupportsGrowingAlphabet;
import net.automatalib.incremental.mealy.IncrementalMealyBuilder;
import net.automatalib.ts.output.MealyTransitionSystem;
import net.automatalib.word.WordBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractSULCache<I, O, C extends SULCacheState<I, O>>
        implements SUL<I, O>, MealyLearningCache<I, O>, SupportsGrowingAlphabet<I>, Resumable<C> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractSULCache.class);

    private final AbstractSULCacheImpl<?, I, ?, O, C> impl;

    AbstractSULCache(AbstractSULCacheImpl<?, I, ?, O, C> cacheImpl) {
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
    public MealyEquivalenceOracle<I, O> createCacheConsistencyTest() {
        return impl.createCacheConsistencyTest();
    }

    @Override
    public void addAlphabetSymbol(I symbol) {
        impl.addAlphabetSymbol(symbol);
    }

    @Override
    public C suspend() {
        return impl.suspend();
    }

    @Override
    public void resume(C state) {
        this.impl.resume(state);
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
     * @param <C>
     *         cache type
     */
    abstract static class AbstractSULCacheImpl<S, I, T, O, C extends SULCacheState<I, O>>
            implements SUL<I, O>, MealyLearningCache<I, O>, SupportsGrowingAlphabet<I>, Resumable<C> {

        protected IncrementalMealyBuilder<I, O> incMealy;
        protected MealyTransitionSystem<S, I, T, O> mealyTs;
        protected final SUL<I, O> delegate;

        private final WordBuilder<I> inputWord = new WordBuilder<>();
        private final WordBuilder<O> outputWord = new WordBuilder<>();

        private boolean delegatePreCalled;
        protected @Nullable S current;

        AbstractSULCacheImpl(IncrementalMealyBuilder<I, O> incMealy,
                             MealyTransitionSystem<S, I, T, O> mealyTs,
                             SUL<I, O> sul) {
            this.incMealy = incMealy;
            this.mealyTs = mealyTs;
            this.delegate = sul;
        }

        @Override
        public void pre() {
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
                    requiredInitializedDelegate();
                    current = null;
                    for (I prevSym : inputWord) {
                        outputWord.append(delegate.step(prevSym));
                    }
                }
            }

            inputWord.append(in);

            if (current == null) {
                out = delegate.step(in);
                postNewStepHook();
                outputWord.add(out);
            }

            return out;
        }

        // TODO: The SUL interface might need a cleanup() method which, by contract,
        // is to be called regardless of whether preceding step()s threw unrecoverable
        // errors!
        @Override
        public void post() {
            updateCache(inputWord, outputWord);

            if (delegatePreCalled) {
                delegate.post();
                delegatePreCalled = false;
            }
            inputWord.clear();
            outputWord.clear();
            current = null;
        }

        @Override
        public MealyEquivalenceOracle<I, O> createCacheConsistencyTest() {
            return new MealyCacheConsistencyTest<>(incMealy);
        }

        @Override
        public void addAlphabetSymbol(I symbol) {
            incMealy.addAlphabetSymbol(symbol);
        }

        @Override
        @SuppressWarnings("unchecked")
        public void resume(C state) {
            final Class<?> thisClass = this.incMealy.getClass();
            final Class<?> stateClass = state.builder.getClass();

            if (!thisClass.equals(stateClass)) {
                LOGGER.warn(Category.DATASTRUCTURE,
                            "You currently plan to use a '{}', but the state contained a '{}'. This may yield unexpected behavior.",
                            thisClass,
                            stateClass);
            }

            this.incMealy = state.builder;
            this.mealyTs = (MealyTransitionSystem<S, I, T, O>) this.incMealy.asTransitionSystem();
        }

        protected void requiredInitializedDelegate() {
            if (!delegatePreCalled) {
                delegate.pre();
            }
            delegatePreCalled = true;
        }

        protected void postNewStepHook() {}

        protected void updateCache(WordBuilder<I> inputBuilder, WordBuilder<O> outputBuilder) {
            if (!outputBuilder.isEmpty()) {
                incMealy.insert(inputBuilder.toWord(), outputBuilder.toWord());
            }
        }
    }

    public static class SULCacheState<I, O> {

        final IncrementalMealyBuilder<I, O> builder;

        SULCacheState(IncrementalMealyBuilder<I, O> builder) {
            this.builder = builder;
        }
    }
}
