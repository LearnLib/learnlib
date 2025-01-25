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
package de.learnlib.oracle;

import java.util.Collection;
import java.util.Collections;

import de.learnlib.oracle.MembershipOracle.DFAMembershipOracle;
import de.learnlib.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.query.OmegaQuery;
import de.learnlib.sul.ObservableSUL;
import net.automatalib.common.util.Pair;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Answers {@link OmegaQuery}s, similar to a {@link MembershipOracle}. Additionally, one can ask whether two states
 * are equal to each other.
 *
 * @param <S> the state type
 * @param <I> the input type
 * @param <D> the output type
 */
public interface OmegaMembershipOracle<S, I, D> extends OmegaQueryAnswerer<S, I, D>, BatchProcessor<OmegaQuery<I, D>> {

    @Override
    default Pair<@Nullable D, Integer> answerQuery(Word<I> prefix, Word<I> loop, int repeat) {
        final OmegaQuery<I, D> query = new OmegaQuery<>(prefix, loop, repeat);
        processQuery(query);
        return Pair.of(query.getOutput(), query.getPeriodicity());
    }

    @Override
    default void processBatch(Collection<? extends OmegaQuery<I, D>> batch) {
        this.processQueries(batch);
    }

    default void processQuery(OmegaQuery<I, D> query) {
        processQueries(Collections.singleton(query));
    }

    void processQueries(Collection<? extends OmegaQuery<I, D>> queries);

    @Override
    default OmegaMembershipOracle<S, I, D> asOracle() {
        return this;
    }

    /**
     * Returns a regular membership oracle.
     *
     * @return a regular membership oracle.
     */
    MembershipOracle<I, D> getMembershipOracle();

    /**
     * Returns whether two states are equal, or if both access sequences {@code w1}, and {@code w2} end up in the
     * same state. If both access sequences end up in the same state then {@code s1.equals(s2)} must hold.
     *
     * @see ObservableSUL#getState()
     *
     * @param w1 the first access sequence.
     * @param s1 the first state.
     * @param w2 the second access sequence.
     * @param s2 the second state.
     *
     * @return whether both states, or states via the given access sequences are equal.
     */
    boolean isSameState(Word<I> w1, S s1, Word<I> w2, S s2);

    interface DFAOmegaMembershipOracle<S, I> extends OmegaMembershipOracle<S, I, Boolean> {

        @Override
        DFAMembershipOracle<I> getMembershipOracle();
    }

    interface MealyOmegaMembershipOracle<S, I, O> extends OmegaMembershipOracle<S, I, Word<O>> {

        @Override
        MealyMembershipOracle<I, O> getMembershipOracle();
    }
}
