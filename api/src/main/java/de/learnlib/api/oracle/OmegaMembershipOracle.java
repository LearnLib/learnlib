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
package de.learnlib.api.oracle;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.ParametersAreNonnullByDefault;

import de.learnlib.api.ObservableSUL;
import de.learnlib.api.oracle.MembershipOracle.DFAMembershipOracle;
import de.learnlib.api.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.api.query.OmegaQuery;
import net.automatalib.commons.util.Pair;
import net.automatalib.words.Word;

/**
 * Answers {@link OmegaQuery}s, similar to a {@link MembershipOracle}. Additionally, one can ask whether two states
 * are equal to each other.
 *
 * @author Jeroen Meijer
 *
 * @param <S> the state type
 * @param <I> the input type
 * @param <D> the output type
 */
@ParametersAreNonnullByDefault
public interface OmegaMembershipOracle<S, I, D> extends OmegaQueryAnswerer<S, I, D> {

    @Override
    default Pair<D, List<S>> answerQuery(Word<I> input, Set<Integer> indices) {
        return answerQuery(Word.epsilon(), input, indices);
    }

    @Override
    default Pair<D, List<S>> answerQuery(Word<I> prefix, Word<I> suffix, Set<Integer> indices) {
        final OmegaQuery<S, I, D> query = new OmegaQuery<>(prefix, suffix, indices);
        processQuery(query);
        return query.getOutputStates();
    }

    default void processQuery(OmegaQuery<S, I, D> query) {
        processQueries(Collections.singleton(query));
    }

    void processQueries(Collection<? extends OmegaQuery<S, I, D>> queries);

    @Override
    default OmegaMembershipOracle<S, I, D> asOracle() {
        return this;
    }

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

        DFAMembershipOracle<I> getDFAMembershipOracle();
    }

    interface MealyOmegaMembershipOracle<S, I, O> extends OmegaMembershipOracle<S, I, Word<O>> {

        MealyMembershipOracle<I, O> getMealyMembershipOracle();
    }
}
