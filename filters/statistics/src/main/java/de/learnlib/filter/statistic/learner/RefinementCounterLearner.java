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
package de.learnlib.filter.statistic.learner;

import de.learnlib.algorithm.LearningAlgorithm;
import de.learnlib.algorithm.LearningAlgorithm.DFALearner;
import de.learnlib.algorithm.LearningAlgorithm.MealyLearner;
import de.learnlib.algorithm.LearningAlgorithm.MooreLearner;
import de.learnlib.filter.statistic.Counter;
import de.learnlib.query.DefaultQuery;
import de.learnlib.statistic.StatisticLearner;
import de.learnlib.statistic.StatisticLearner.DFAStatisticLearner;
import de.learnlib.statistic.StatisticLearner.MealyStatisticLearner;
import de.learnlib.statistic.StatisticLearner.MooreStatisticLearner;
import de.learnlib.tooling.annotation.refinement.GenerateRefinement;
import de.learnlib.tooling.annotation.refinement.Generic;
import de.learnlib.tooling.annotation.refinement.Interface;
import de.learnlib.tooling.annotation.refinement.Mapping;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.automaton.transducer.MooreMachine;
import net.automatalib.word.Word;

/**
 * Counts the number of hypothesis refinements.
 * <p>
 * The value of the {@link Counter} returned by {@link #getStatisticalData()} returns the same value as
 * Experiment.getRounds().
 *
 * @param <M>
 *         automaton type
 * @param <I>
 *         input symbol type
 * @param <D>
 *         output domain type
 */
@GenerateRefinement(name = "DFARefinementCounterLearner",
                    generics = @Generic(value = "I", desc = "input symbol type"),
                    parentGenerics = {@Generic(clazz = DFA.class, generics = {"?", "I"}),
                                      @Generic("I"),
                                      @Generic(clazz = Boolean.class)},
                    typeMappings = @Mapping(from = LearningAlgorithm.class,
                                            to = DFALearner.class,
                                            generics = @Generic("I")),
                    interfaces = @Interface(clazz = DFAStatisticLearner.class, generics = @Generic("I")))
@GenerateRefinement(name = "MealyRefinementCounterLearner",
                    generics = {@Generic(value = "I", desc = "input symbol type"),
                                @Generic(value = "O", desc = "output symbol type")},
                    parentGenerics = {@Generic(clazz = MealyMachine.class, generics = {"?", "I", "?", "O"}),
                                      @Generic("I"),
                                      @Generic(clazz = Word.class, generics = "O")},
                    typeMappings = @Mapping(from = LearningAlgorithm.class,
                                            to = MealyLearner.class,
                                            generics = {@Generic("I"), @Generic("O")}),
                    interfaces = @Interface(clazz = MealyStatisticLearner.class,
                                            generics = {@Generic("I"), @Generic("O")}))
@GenerateRefinement(name = "MooreRefinementCounterLearner",
                    generics = {@Generic(value = "I", desc = "input symbol type"),
                                @Generic(value = "O", desc = "output symbol type")},
                    parentGenerics = {@Generic(clazz = MooreMachine.class, generics = {"?", "I", "?", "O"}),
                                      @Generic("I"),
                                      @Generic(clazz = Word.class, generics = "O")},
                    typeMappings = @Mapping(from = LearningAlgorithm.class,
                                            to = MooreLearner.class,
                                            generics = {@Generic("I"), @Generic("O")}),
                    interfaces = @Interface(clazz = MooreStatisticLearner.class,
                                            generics = {@Generic("I"), @Generic("O")}))
public class RefinementCounterLearner<M, I, D> implements StatisticLearner<M, I, D> {

    private final LearningAlgorithm<M, I, D> learningAlgorithm;

    private final Counter counter;

    public RefinementCounterLearner(LearningAlgorithm<M, I, D> learningAlgorithm) {
        this.learningAlgorithm = learningAlgorithm;
        this.counter = new Counter("Refinements", "#");
    }

    @Override
    public void startLearning() {
        learningAlgorithm.startLearning();
    }

    @Override
    public boolean refineHypothesis(DefaultQuery<I, D> ceQuery) {
        final boolean refined = learningAlgorithm.refineHypothesis(ceQuery);
        if (refined) {
            counter.increment();
        }
        return refined;
    }

    @Override
    public M getHypothesisModel() {
        return learningAlgorithm.getHypothesisModel();
    }

    @Override
    public Counter getStatisticalData() {
        return counter;
    }
}
