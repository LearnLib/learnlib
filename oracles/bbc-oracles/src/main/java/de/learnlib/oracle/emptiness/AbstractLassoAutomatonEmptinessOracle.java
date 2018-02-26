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
package de.learnlib.oracle.emptiness;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import de.learnlib.api.modelchecking.counterexample.Lasso;
import de.learnlib.api.modelchecking.counterexample.Lasso.DFALasso;
import de.learnlib.api.modelchecking.counterexample.Lasso.MealyLasso;
import de.learnlib.api.oracle.EmptinessOracle.LassoEmptinessOracle;
import de.learnlib.api.oracle.OmegaMembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.api.query.OmegaQuery;
import de.learnlib.oracle.AbstractBreadthFirstOracle;
import net.automatalib.automata.concepts.Output;
import net.automatalib.words.Word;

/**
 * A {@link LassoEmptinessOracle}, where the {@link Lasso} is given as an automaton.
 *
 * @author Jeroen Meijer
 *
 * @param <L> the Lasso type
 * @param <S> the state type
 * @param <I> the input type
 * @param <D> the output type
 */
public abstract class AbstractLassoAutomatonEmptinessOracle<L extends Lasso<?, ?, I, D>, S, I, D>
        extends AbstractBreadthFirstOracle<L, I, D, OmegaQuery<S, I, D>>
        implements LassoEmptinessOracle<L, S, I, D> {

    /**
     * The omega membership oracle used to answer {@link OmegaQuery}s.
     */
    private final OmegaMembershipOracle<S, I, D> omegaMembershipOracle;

    /**
     * Constructs a new {@link AbstractLassoAutomatonEmptinessOracle}.
     *
     * @param omegaMembershipOracle the {@link OmegaMembershipOracle} used to answer {@link OmegaQuery}s.
     */
    protected AbstractLassoAutomatonEmptinessOracle(OmegaMembershipOracle<S, I, D> omegaMembershipOracle) {
        super(1);
        this.omegaMembershipOracle = omegaMembershipOracle;
    }

    @Override
    public OmegaMembershipOracle<S, I, D> getOmegaMembershipOracle() {
        return omegaMembershipOracle;
    }

    /**
     * Checks whether the answered {@link OmegaQuery} is a counterexample for the given {@code lasso}.
     *
     * @see LassoEmptinessOracle#isCounterExample(Output, DefaultQuery)
     *
     * @return whether the two conditions hold:
     *  1. {@link LassoEmptinessOracle#isCounterExample(Output, DefaultQuery)}, and
     *  2. The {@code query} contains a closed-loop, i.e. there exists {@code 0 <= i < j < query.getStates().size()},
     *  such that {@code query.getStates().get(i).equals(query.getStates().get(j))}.
     */
    @Override
    public boolean isCounterExample(L lasso, OmegaQuery<S, I, D> query) {
        final boolean result;
        // check condition 1
        if (!LassoEmptinessOracle.super.isCounterExample(lasso, query)) {
            result = false;
        } else {
            // the states the SUL evolved through.
            final List<S> states = query.getStates();

            // get the indices in the Lasso where the loop begins
            final SortedSet<Integer> indices = lasso.getLoopBeginIndices();

            assert indices.size() > 1;

            final List<Integer> indexList = new ArrayList<>(indices);

            boolean loopClosed = false;

            // check whether the loop is closed
            for (int i = 0; i < indexList.size() && !loopClosed; i++) {
                final S s1 = states.get(i);
                final int i1 = indexList.get(i);

                // compute the first access sequence
                final Word<I> w1 = lasso.getWord().prefix(i1);

                for (int j = i + 1; j < indexList.size() && !loopClosed; j++) {
                    final S s2 = states.get(j);
                    final int i2 = indexList.get(j);

                    // compute the second access sequence
                    final Word<I> w2 = lasso.getWord().prefix(i2);

                    loopClosed = omegaMembershipOracle.isSameState(w1, s1, w2, s2);
                }
            }

            result = loopClosed;
        }

        return result;
    }

    public static class DFALassoDFAEmptinessOracle<S, I>
            extends AbstractLassoAutomatonEmptinessOracle<DFALasso<?, I>, S, I, Boolean>
            implements DFALassoEmptinessOracle<S, I> {

        public DFALassoDFAEmptinessOracle(OmegaMembershipOracle<S, I, Boolean> membershipOracle) {
            super(membershipOracle);
        }
    }

    public static class MealyLassoMealyEmptinessOracle<S, I, O>
            extends AbstractLassoAutomatonEmptinessOracle<MealyLasso<?, I, O>, S, I, Word<O>>
            implements MealyLassoEmptinessOracle<S, I, O> {

        public MealyLassoMealyEmptinessOracle(OmegaMembershipOracle<S, I, Word<O>> membershipOracle) {
            super(membershipOracle);
        }
    }
}
