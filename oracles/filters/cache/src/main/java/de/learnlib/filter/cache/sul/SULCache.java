/* Copyright (C) 2013-2019 TU Dortmund
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
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import de.learnlib.api.Resumable;
import de.learnlib.api.SUL;
import de.learnlib.filter.cache.LearningCacheOracle.MealyLearningCacheOracle;
import de.learnlib.filter.cache.mealy.MealyCacheConsistencyTest;
import de.learnlib.filter.cache.sul.SULCache.SULCacheState;
import de.learnlib.oracle.membership.SULOracle;
import net.automatalib.SupportsGrowingAlphabet;
import net.automatalib.incremental.mealy.IncrementalMealyBuilder;
import net.automatalib.incremental.mealy.dag.IncrementalMealyDAGBuilder;
import net.automatalib.incremental.mealy.tree.IncrementalMealyTreeBuilder;
import net.automatalib.ts.output.MealyTransitionSystem;
import net.automatalib.words.Alphabet;
import net.automatalib.words.WordBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
@ParametersAreNonnullByDefault
public class SULCache<I, O> extends SULOracle<I, O> implements SUL<I, O>,
                                                               MealyLearningCacheOracle<I, O>,
                                                               SupportsGrowingAlphabet<I>,
                                                               Resumable<SULCacheState<I, O>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SULCache.class);

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

    @Nonnull
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
    @ParametersAreNonnullByDefault
    private static final class SULCacheImpl<S, I, T, O>
            implements SUL<I, O>, MealyLearningCache<I, O>, SupportsGrowingAlphabet<I>, Resumable<SULCacheState<I, O>> {

        private IncrementalMealyBuilder<I, O> incMealy;
        private MealyTransitionSystem<S, I, T, O> mealyTs;
        private final SUL<I, O> delegate;
        private final ReadWriteLock incMealyLock;

        private final WordBuilder<I> inputWord = new WordBuilder<>();
        private final WordBuilder<O> outputWord = new WordBuilder<>();

        private boolean delegatePreCalled;
        private S current;

        SULCacheImpl(IncrementalMealyBuilder<I, O> incMealy,
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

        @Nullable
        @Override
        public O step(@Nullable I in) {
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
                    delegate.pre();
                    delegatePreCalled = true;
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
                    incMealy.insert(inputWord.toWord(), outputWord.toWord());
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

        @Nonnull
        @Override
        public SUL<I, O> fork() {
            return new SULCacheImpl<>(incMealy, incMealyLock, mealyTs, delegate.fork());
        }

        @Nonnull
        @Override
        public MealyCacheConsistencyTest<I, O> createCacheConsistencyTest() {
            return new MealyCacheConsistencyTest<>(incMealy, incMealyLock);
        }

        @Override
        public void addAlphabetSymbol(I symbol) {
            incMealy.addAlphabetSymbol(symbol);
        }

        @Override
        public SULCacheState<I, O> suspend() {
            return new SULCacheState<>(incMealy);
        }

        @Override
        @SuppressWarnings("unchecked")
        public void resume(SULCacheState<I, O> state) {
            final Class<?> thisClass = this.incMealy.getClass();
            final Class<?> stateClass = state.getBuilder().getClass();

            if (!thisClass.equals(stateClass)) {
                LOGGER.warn(
                        "You currently plan to use a '{}', but the state contained a '{}'. This may yield unexpected behavior.",
                        thisClass,
                        stateClass);
            }

            this.incMealy = state.getBuilder();
            this.mealyTs = (MealyTransitionSystem<S, I, T, O>) this.incMealy.asTransitionSystem();
        }
    }

    public static class SULCacheState<I, O> implements Serializable {

        private final IncrementalMealyBuilder<I, O> builder;

        SULCacheState(IncrementalMealyBuilder<I, O> builder) {
            this.builder = builder;
        }

        IncrementalMealyBuilder<I, O> getBuilder() {
            return builder;
        }

    }

}
