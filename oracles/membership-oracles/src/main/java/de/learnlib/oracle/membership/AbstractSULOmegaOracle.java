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
package de.learnlib.oracle.membership;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.learnlib.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.oracle.OmegaMembershipOracle;
import de.learnlib.oracle.OmegaMembershipOracle.MealyOmegaMembershipOracle;
import de.learnlib.query.OmegaQuery;
import de.learnlib.sul.ObservableSUL;
import de.learnlib.sul.SUL;
import net.automatalib.common.util.Pair;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * An omega membership oracle for an {@link ObservableSUL}.
 * <p>
 * The behavior is similar to a {@link SULOracle}, except that this class answers {@link OmegaQuery}s.
 * <p>
 * After some symbols (i.e. after {@link OmegaQuery#getPrefix()}, and after each {@link OmegaQuery#getLoop()}) the state
 * of the {@link ObservableSUL} is retrieved, and used to answer the query.
 * <p>
 * This class is <b>not</b> thread-safe.
 *
 * @param <S>
 *         the state type of the {@link ObservableSUL}
 * @param <I>
 *         the input type
 * @param <O>
 *         the output type
 * @param <Q>
 *         the state information type that is used to answer {@link OmegaQuery}s
 */
public abstract class AbstractSULOmegaOracle<S extends Object, I, O, Q> implements MealyOmegaMembershipOracle<Q, I, O> {

    private final ObservableSUL<S, I, O> sul;

    protected AbstractSULOmegaOracle(ObservableSUL<S, I, O> sul) {
        this.sul = sul;
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
    public void processQueries(Collection<? extends OmegaQuery<I, Word<O>>> queries) {
        for (OmegaQuery<I, Word<O>> q : queries) {
            final Pair<@Nullable Word<O>, Integer> output = answerQuery(q.getPrefix(), q.getLoop(), q.getRepeat());
            q.answer(output.getFirst(), output.getSecond());
        }
    }

    protected abstract Q getQueryState(ObservableSUL<S, I, O> sul);

    @Override
    public Pair<@Nullable Word<O>, Integer> answerQuery(Word<I> prefix, Word<I> loop, int repeat) {
        assert repeat > 0;
        sul.pre();
        try {
            final int traceLength = prefix.length() + loop.length() * repeat;
            final WordBuilder<I> inputBuilder = new WordBuilder<>(traceLength, prefix);
            final WordBuilder<O> outputBuilder = new WordBuilder<>(traceLength);
            final List<Q> states = new ArrayList<>(repeat + 1);

            for (int i = 0; i < prefix.length(); i++) {
                outputBuilder.append(sul.step(prefix.getSymbol(i)));
            }
            states.add(getQueryState(sul));

            for (int i = 0; i < repeat; i++) {
                inputBuilder.append(loop);
                for (int j = 0; j < loop.length(); j++) {
                    outputBuilder.append(sul.step(loop.getSymbol(j)));
                }
                final Q nextState = getQueryState(sul);

                int prefixLength = prefix.length();
                for (Q q : states) {
                    if (isSameState(inputBuilder.toWord(), nextState, inputBuilder.toWord(0, prefixLength), q)) {
                        return Pair.of(outputBuilder.toWord(), i + 1);
                    }
                    prefixLength += loop.length();
                }
                states.add(nextState);
            }

            return Pair.of(null, -1);
        } finally {
            sul.post();
        }
    }

    @Override
    public MealyMembershipOracle<I, O> getMembershipOracle() {
        return new SULOracle<>(sul);
    }

    /**
     * Creates a new {@link AbstractSULOmegaOracle}, while making sure the invariants of the {@link ObservableSUL} are
     * satisfied.
     *
     * @param sul
     *         the {@link ObservableSUL} to wrap around.
     * @param deepCopies
     *         whether to test for state equivalence directly on the retrieved state.
     * @param <S>
     *         the state type
     * @param <I>
     *         the input type
     * @param <O>
     *         the output type
     *
     * @return the {@link AbstractSULOmegaOracle}.
     */
    public static <S extends Object, I, O> AbstractSULOmegaOracle<S, I, O, ?> newOracle(ObservableSUL<S, I, O> sul,
                                                                                        boolean deepCopies) {
        final AbstractSULOmegaOracle<S, I, O, ?> abstractSulOmegaOracle;
        if (deepCopies) {
            if (sul.deepCopies()) {
                abstractSulOmegaOracle = new DeepCopySULOmegaOracle<>(sul);
            } else {
                throw new IllegalArgumentException("SUL can not make deep copies of states.");
            }
        } else {
            if (sul.canFork()) {
                abstractSulOmegaOracle = new ShallowCopySULOmegaOracle<>(sul);
            } else {
                throw new IllegalArgumentException("SUL must be forkable.");
            }
        }

        return abstractSulOmegaOracle;
    }

    /**
     * Creates a new {@link AbstractSULOmegaOracle} that assumes the {@link SUL} can not make deep copies.
     *
     * @param sul
     *         the sul to delegate queris to
     * @param <S>
     *         the state type
     * @param <I>
     *         the input type
     * @param <O>
     *         the output type
     *
     * @return the new oracle
     *
     * @see #newOracle(ObservableSUL, boolean)
     */
    public static <S extends Object, I, O> AbstractSULOmegaOracle<S, I, O, ?> newOracle(ObservableSUL<S, I, O> sul) {
        return newOracle(sul, !sul.canFork());
    }

    /**
     * A {@link AbstractSULOmegaOracle} that uses {@link Object#hashCode()}, and {@link Object#equals(Object)} to test
     * for state equivalence. When the hash codes of two states are equal this class will use two access sequences to
     * move two {@link ObservableSUL}s to those states and perform an equality check.
     * <p>
     * The state information used to answer {@link OmegaQuery}s is of type {@link Integer}. The values of those integers
     * are actually hash codes of states of the {@link ObservableSUL}.
     *
     * @param <S>
     *         the state type
     * @param <I>
     *         the input type
     * @param <O>
     *         the output type
     */
    private static final class ShallowCopySULOmegaOracle<S extends Object, I, O>
            extends AbstractSULOmegaOracle<S, I, O, Integer> {

        /**
         * A forked {@link SUL} is necessary when we need to step to two particular states at the same time.
         */
        private final ObservableSUL<S, I, O> forkedSUL;

        /**
         * Constructs a new {@link ShallowCopySULOmegaOracle}, use {@link #newOracle(ObservableSUL)} to create an
         * instance. This method makes sure the invariants of the {@link ObservableSUL} are satisfied (i.e., the
         * {@link ObservableSUL} must be forkable, i.e. ({@code {@link SUL#canFork()} == true}).
         *
         * @param sul
         *         the SUL
         */
        ShallowCopySULOmegaOracle(ObservableSUL<S, I, O> sul) {
            super(sul);
            assert sul.canFork();
            forkedSUL = sul.fork();
        }

        /**
         * Returns the state as a hash code.
         *
         * @param sul
         *         the {@link ObservableSUL} to retrieve the current state from.
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
         * @return whether the following conditions hold:
         * <ol>
         *     <li>the hash codes are the same, i.e. {@code s1.equals(s2)}, and</li>
         *     <li>the two access sequences lead to the same state.</li>
         * </ol>
         *
         * @see OmegaMembershipOracle#isSameState(Word, Object, Word, Object)
         */
        @Override
        public boolean isSameState(Word<I> input1, Integer s1, Word<I> input2, Integer s2) {
            if (s1.equals(s2)) {
                // in this case the hash codes are equal, now we must check if we accidentally had a hash-collision.
                final ObservableSUL<S, I, O> sul1 = getSul();
                final ObservableSUL<S, I, O> sul2 = forkedSUL;

                // assert sul1 is already in the correct state
                assert s1.equals(sul1.getState().hashCode());

                sul2.pre();
                try {
                    // step through the second SUL
                    for (I sym : input2) {
                        sul2.step(sym);
                    }

                    assert sul1.getState().hashCode() == sul2.getState().hashCode();
                    assert s2.equals(sul2.getState().hashCode());

                    // check for state equivalence
                    return sul1.getState().equals(sul2.getState());
                } finally {
                    sul2.post();
                }
            } else {
                return false;
            }
        }
    }

    /**
     * A {@link AbstractSULOmegaOracle} for states that are deep copies. When a state is a deep copy, this means we can
     * simply invoke {@link Object#equals(Object)} on both.
     * <p>
     * The state information used to answer {@link OmegaQuery}s is of type {@link S}.
     *
     * @param <S>
     *         the state type
     * @param <I>
     *         the input type
     * @param <O>
     *         the output type
     */
    private static final class DeepCopySULOmegaOracle<S extends Object, I, O>
            extends AbstractSULOmegaOracle<S, I, O, S> {

        /**
         * Constructs a {@link DeepCopySULOmegaOracle}, use {@link #newOracle(ObservableSUL, boolean)} to create an
         * actual instance. This method will make sure the invariants of the {@link ObservableSUL} are satisfied.
         *
         * @param sul
         *         the {@link ObservableSUL}.
         */
        DeepCopySULOmegaOracle(ObservableSUL<S, I, O> sul) {
            super(sul);
        }

        /**
         * Returns the current state of the {@link ObservableSUL}.
         *
         * @param sul
         *         the {@link ObservableSUL} to retrieve the current state from.
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
