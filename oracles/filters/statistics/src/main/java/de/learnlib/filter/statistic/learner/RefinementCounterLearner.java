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
package de.learnlib.filter.statistic.learner;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import de.learnlib.api.algorithm.LearningAlgorithm;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.api.statistic.StatisticLearner;
import de.learnlib.filter.statistic.Counter;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.words.Word;

/**
 * Counts the number of hypothesis refinements.
 *
 * The value of the {@link Counter} returned by {@link #getStatisticalData()} returns the same value as
 * Experiment.getRounds().
 *
 * @param <M> the automaton type.
 * @param <I> the input type.
 * @param <D> the output type.
 */
@ParametersAreNonnullByDefault
public class RefinementCounterLearner<M, I, D> implements StatisticLearner<M, I, D> {

    private final LearningAlgorithm<M, I, D> learningAlgorithm;

    private final Counter counter;

    public RefinementCounterLearner(String name, LearningAlgorithm<M, I, D> learningAlgorithm) {
        counter = new Counter(name, "refinements");
        this.learningAlgorithm = learningAlgorithm;
    }

    @Override
    public void startLearning() {
        learningAlgorithm.startLearning();
    }

    @Override
    public boolean refineHypothesis(@Nonnull DefaultQuery<I, D> ceQuery) {
        final boolean refined = learningAlgorithm.refineHypothesis(ceQuery);
        if (refined) {
            counter.increment();
        }
        return refined;
    }

    @Nonnull
    @Override
    public M getHypothesisModel() {
        return learningAlgorithm.getHypothesisModel();
    }

    @Nonnull
    @Override
    public Counter getStatisticalData() {
        return counter;
    }

    public static class DFARefinementCounterLearner<I> extends RefinementCounterLearner<DFA<?, I>, I, Boolean>
            implements DFAStatisticLearner<I> {

        public DFARefinementCounterLearner(String name, LearningAlgorithm<DFA<?, I>, I, Boolean> learningAlgorithm) {
            super(name, learningAlgorithm);
        }
    }

    public static class MealyRefinementCounterLearner<I, O>
            extends RefinementCounterLearner<MealyMachine<?, I, ?, O>, I, Word<O>>
            implements MealyStatisticLearner<I, O> {

        public MealyRefinementCounterLearner(
                String name,
                LearningAlgorithm<MealyMachine<?, I, ?, O>, I, Word<O>> learningAlgorithm) {
            super(name, learningAlgorithm);
        }
    }
}
