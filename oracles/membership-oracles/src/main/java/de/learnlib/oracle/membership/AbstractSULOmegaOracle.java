/* Copyright (C) 2013-2018 TU Dortmund
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
package de.learnlib.oracle.membership;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import de.learnlib.api.ObservableSUL;
import de.learnlib.api.SUL;
import de.learnlib.api.exception.SULException;
import de.learnlib.api.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.api.oracle.OmegaMembershipOracle;
import de.learnlib.api.oracle.OmegaMembershipOracle.MealyOmegaMembershipOracle;
import de.learnlib.api.query.OmegaQuery;
import net.automatalib.commons.util.Pair;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

/**
 * An omega membership oracle for an {@link ObservableSUL}.
 *
 * The behavior is similar to a {@link SULOracle}, except that this class answers {@link OmegaQuery}s.
 *
 * After some symbols (as specified in {@link OmegaQuery#getIndices()}) in an input word the state of the {@link
 * ObservableSUL} is retrieved, and used to answer the query.
 *
 * Like {@link SULOracle} this class is thread-safe.
 *
 * @author Jeroen Meijer
 *
 * @param <S> the state type of the {@link ObservableSUL}
 * @param <I> the input type
 * @param <O> the output type
 * @param <Q> the state information type that is used to answer {@link OmegaQuery}s
 */
public abstract class AbstractSULOmegaOracle<S, I, O, Q> implements MealyOmegaMembershipOracle<Q, I, O> {

    private final ObservableSUL<S, I, O> sul;
    private final ThreadLocal<ObservableSUL<S, I, O>> localSul;

    protected AbstractSULOmegaOracle(ObservableSUL<S, I, O> sul) {
        this.sul = sul;
        if (sul.canFork()) {
            this.localSul = ThreadLocal.withInitial(sul::fork);
        } else {
            this.localSul = null;
        }
    }

    /**
     * Gets the {@link ObservableSUL}.
     *
     * @return the {@link ObservableSUL}.
     */
    public ObservableSUL<S, I, O> getSul() {
        return sul;
    }

    @Override
    public void processQueries(Collection<? extends OmegaQuery<Q, I, Word<O>>> queries) {
        if (localSul != null) {
            processQueries(localSul.get(), queries);
        } else {
            synchronized (sul) {
                processQueries(sul, queries);
            }
        }
    }

    private void processQueries(ObservableSUL<S, I, O> sul, Collection<? extends OmegaQuery<Q, I, Word<O>>> queries) {
        for (OmegaQuery<Q, I, Word<O>> q : queries) {
            final Pair<Word<O>, List<Q>> output = answerQuery(sul, q.getPrefix(), q.getSuffix(), q.getIndices());
            q.answer(output.getFirst());
            q.setStates(output.getSecond());
        }
    }

    protected abstract Q getQueryState(ObservableSUL<S, I, O> sul);

    @Nonnull
    private Pair<Word<O>, List<Q>> answerQuery(ObservableSUL<S, I, O> sul,
                                               Word<I> prefix,
                                               Word<I> suffix,
                                               Set<Integer> indices) throws SULException {
        sul.pre();
        try {
            int index = 0;
            final List<Q> states = new ArrayList<>();

            // Prefix: Execute symbols, don't record output
            for (I sym : prefix) {
                sul.step(sym);
            }

            if (indices.contains(index++)) {
                states.add(getQueryState(sul));
            }

            // Suffix: Execute symbols, outputs constitute output word
            WordBuilder<O> wb = new WordBuilder<>(suffix.length());
            for (I sym : suffix) {
                wb.add(sul.step(sym));
                if (indices.contains(index++)) {
                    states.add(getQueryState(sul));
                }
            }

            return Pair.of(wb.toWord(), states);
        } finally {
            sul.post();
        }
    }

    @Override
    public MealyMembershipOracle<I, O> getMealyMembershipOracle() {
        return new SULOracle<>(sul);
    }

    /**
     * Creates a new {@link AbstractSULOmegaOracle}, while making sure the invariants of the {@link ObservableSUL} are
     * satisfied.
     *
     * @param sul the {@link ObservableSUL} to wrap around.
     * @param deepCopies whether to test for state equivalence directly on the retrieved state.
     *
     * @param <S> the state type
     * @param <I> the input type
     * @param <O> the output type
     *
     * @return the {@link AbstractSULOmegaOracle}.
     */
    public static <S, I, O> AbstractSULOmegaOracle<S, I, O, ?> newOracle(ObservableSUL<S, I, O> sul,
                                                                         boolean deepCopies) {
        final AbstractSULOmegaOracle<S, I, O, ?> abstractSulOmegaOracle;
        if (deepCopies) {
            if (!sul.deepCopies()) {
                throw new IllegalArgumentException("SUL can not make deep copies of states.");
            } else {
                abstractSulOmegaOracle = new DeepCopySULOmegaOracle<>(sul);
            }
        } else {
            if (!sul.canFork()) {
                throw new IllegalArgumentException("SUL must be forkable.");
            } else {
                abstractSulOmegaOracle = new ShallowCopySULOmegaOracle<>(sul);
            }
        }

        return abstractSulOmegaOracle;
    }

    /**
     * Creates a new {@link AbstractSULOmegaOracle} that assumes the {@link SUL} can not make deep copies.
     *
     * @see #newOracle(ObservableSUL, boolean)
     *
     * @param <S> the state type
     * @param <I> the input type
     * @param <O> the output type
     */
    public static <S, I, O> AbstractSULOmegaOracle<S, I, O, ?> newOracle(ObservableSUL<S, I, O> sul) {
        return newOracle(sul, !sul.canFork());
    }

    /**
     * A {@link AbstractSULOmegaOracle} that uses {@link Object#hashCode()}, and {@link Object#equals(Object)} to test
     * for state equivalence. When the hash codes of two states are equal this class will use two access sequences to
     * move two {@link ObservableSUL}s to those states and perform an equality check.
     *
     * The state information used to answer {@link OmegaQuery}s is of type {@link Integer}. The values of those integers
     * are actually hash codes of states of the {@link ObservableSUL}.
     *
     * @author Jeroen Meijer
     *
     * @param <S> the state type
     * @param <I> the input type
     * @param <O> the output type
     */
    private static final class ShallowCopySULOmegaOracle<S, I, O> extends AbstractSULOmegaOracle<S, I, O, Integer> {

        /**
         * A forked {@link SUL} is necessary when we need to step to two particular states at the same time.
         */
        private final ObservableSUL<S, I, O> forkedSUL;

        /**
         * Constructs a new {@link ShallowCopySULOmegaOracle}, use {@link #newOracle(ObservableSUL)} to create an
         * instance. This method makes sure the invariants of the {@link ObservableSUL} are satisfied (i.e. the {@link
         * ObservableSUL} must be forkable, i.e. ({@code {@link SUL#canFork()} == true}.
         *
         * @param sul the SUL
         */
        private ShallowCopySULOmegaOracle(ObservableSUL<S, I, O> sul) {
            super(sul);
            assert sul.canFork();
            forkedSUL = sul.fork();
        }

        /**
         * Returns the state as a hash code.
         *
         * @param sul the {@link ObservableSUL} to retrieve the current state from.
         *
         * @return the hash code of the state.
         */
        @Override
        protected Integer getQueryState(ObservableSUL<S, I, O> sul) {
            return sul.getState().hashCode();
        }

        /**
         * Test for state equivalence, by means of {@link Object#hashCode()}, and {@link Object#equals(Object)}.
         *
         * @see OmegaMembershipOracle#isSameState(Word, Object, Word, Object)
         *
         * @return whether the following conditions hold:
         *  1. the hash codes are the same, i.e. {@code s1.equals(s2)}, and
         *  2. the two access sequences lead to the same state.
         */
        @Override
        public boolean isSameState(Word<I> input1, Integer s1, Word<I> input2, Integer s2) {
            final boolean result;
            if (!s1.equals(s2)) {
                result = false;
            } else {
                // in this case the hash codes are equal, now we must check if we accidentally had a hash-collision.
                final ObservableSUL<S, I, O> sul1 = getSul();
                final ObservableSUL<S, I, O> sul2 = forkedSUL;
                sul1.pre();
                try {
                    // step through the first SUL
                    for (I sym : input1) {
                        sul1.step(sym);
                    }
                    sul2.pre();
                    try {
                        // step through the second SUL
                        for (I sym : input2) {
                            sul2.step(sym);
                        }

                        assert sul1.getState().hashCode() == sul2.getState().hashCode();
                        assert s1.equals(sul1.getState().hashCode());
                        assert s2.equals(sul2.getState().hashCode());

                        // check for state equivalence
                        result = sul1.getState().equals(sul2.getState());
                    } finally {
                        sul2.post();
                    }

                } finally {
                    sul1.post();
                }
            }

            return result;
        }
    }

    /**
     * A {@link AbstractSULOmegaOracle} for states that are deep copies. When a state is a deep copy, this means we can
     * simply invoke {@link Object#equals(Object)} on both.
     *
     * The state information used to answer {@link OmegaQuery}s is of type {@link S}.
     *
     * @author Jeroen Meijer
     *
     * @param <S> the state type
     * @param <I> the input type
     * @param <O> the output type
     */
    private static final class DeepCopySULOmegaOracle<S, I, O> extends AbstractSULOmegaOracle<S, I, O, S> {

        /**
         * Constructs a {@link DeepCopySULOmegaOracle}, use {@link #newOracle(ObservableSUL, boolean)} to create an
         * actual instance. This method will make sure the invariants of the {@link ObservableSUL} are satisfied.
         *
         * @param sul the {@link ObservableSUL}.
         */
        private DeepCopySULOmegaOracle(ObservableSUL<S, I, O> sul) {
            super(sul);
        }

        /**
         * Returns the current state of the {@link ObservableSUL}.
         *
         * @param sul the {@link ObservableSUL} to retrieve the current state from.
         *
         * @return the current state.
         */
        @Override
        protected S getQueryState(ObservableSUL<S, I, O> sul) {
            return sul.getState();
        }

        /**
         * Test for state equivalence using {@link Object#equals(Object)}.
         *
         * @see OmegaMembershipOracle#isSameState(Word, Object, Word, Object)
         */
        @Override
        public boolean isSameState(Word<I> input1, S s1, Word<I> input2, S s2) {
            return s1.equals(s2);
        }
    }
}
