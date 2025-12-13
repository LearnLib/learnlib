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
import de.learnlib.query.DefaultQuery;
import de.learnlib.statistic.Statistics;
import de.learnlib.statistic.StatisticsKey;
import de.learnlib.statistic.StatisticsService;
import de.learnlib.tooling.annotation.refinement.GenerateRefinement;
import de.learnlib.tooling.annotation.refinement.Generic;
import de.learnlib.tooling.annotation.refinement.Mapping;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.automaton.transducer.MooreMachine;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Counts the number of hypothesis refinements.
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
                                            generics = @Generic("I")))
@GenerateRefinement(name = "MealyRefinementCounterLearner",
                    generics = {@Generic(value = "I", desc = "input symbol type"),
                                @Generic(value = "O", desc = "output symbol type")},
                    parentGenerics = {@Generic(clazz = MealyMachine.class, generics = {"?", "I", "?", "O"}),
                                      @Generic("I"),
                                      @Generic(clazz = Word.class, generics = "O")},
                    typeMappings = @Mapping(from = LearningAlgorithm.class,
                                            to = MealyLearner.class,
                                            generics = {@Generic("I"), @Generic("O")}))
@GenerateRefinement(name = "MooreRefinementCounterLearner",
                    generics = {@Generic(value = "I", desc = "input symbol type"),
                                @Generic(value = "O", desc = "output symbol type")},
                    parentGenerics = {@Generic(clazz = MooreMachine.class, generics = {"?", "I", "?", "O"}),
                                      @Generic("I"),
                                      @Generic(clazz = Word.class, generics = "O")},
                    typeMappings = @Mapping(from = LearningAlgorithm.class,
                                            to = MooreLearner.class,
                                            generics = {@Generic("I"), @Generic("O")}))
public class RefinementCounterLearner<M, I, D> implements LearningAlgorithm<M, I, D> {

    /**
     * The {@link StatisticsKey} this class uses for counting the number of
     * {@link LearningAlgorithm#refineHypothesis(DefaultQuery) refinements} executed on the learning algorithm.
     */
    public static final StatisticsKey KEY_REF = new StatisticsKey("ref-cnt", "Number of refinements");

    private final LearningAlgorithm<M, I, D> delegate;
    private final StatisticsService statistics;
    private final StatisticsKey keyRef;

    /**
     * Convenience constructor for {@link RefinementCounterLearner#RefinementCounterLearner(LearningAlgorithm, String)}
     * which uses {@code null} as {@code id}.
     *
     * @param delegate
     *         the learning algorithm to delegate calls to
     */
    public RefinementCounterLearner(LearningAlgorithm<M, I, D> delegate) {
        this(delegate, null);
    }

    /**
     * Constructs a new counter algorithm that writes statistical data to a {@link StatisticsService}. The provided
     * {@code id} is used to refine the supported {@link StatisticsKey}s and allows for using multiple instances of this
     * class for different purposes.
     *
     * @param delegate
     *         the learning algorithm to delegate calls to
     * @param id
     *         the id used for specialising the statistics keys
     */
    public RefinementCounterLearner(LearningAlgorithm<M, I, D> delegate, @Nullable String id) {
        this.delegate = delegate;
        this.statistics = Statistics.getService();
        this.keyRef = KEY_REF.withId(id);
    }

    @Override
    public void startLearning() {
        delegate.startLearning();
    }

    @Override
    public boolean refineHypothesis(DefaultQuery<I, D> ceQuery) {
        final boolean refined = delegate.refineHypothesis(ceQuery);
        if (refined) {
            statistics.increaseCounter(keyRef, this);
        }
        return refined;
    }

    @Override
    public M getHypothesisModel() {
        return delegate.getHypothesisModel();
    }
}
