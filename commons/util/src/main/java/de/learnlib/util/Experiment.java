/* Copyright (C) 2013-2026 TU Dortmund University
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
package de.learnlib.util;

import java.util.Collection;

import de.learnlib.algorithm.LearningAlgorithm;
import de.learnlib.logging.Category;
import de.learnlib.oracle.EquivalenceOracle;
import de.learnlib.query.DefaultQuery;
import de.learnlib.statistic.Statistics;
import de.learnlib.statistic.StatisticsKey;
import de.learnlib.statistic.StatisticsService;
import net.automatalib.automaton.concept.FiniteRepresentation;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.automaton.transducer.MooreMachine;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runs a learning experiment.
 *
 * @param <A>
 *         the automaton type
 */
public class Experiment<A extends FiniteRepresentation> {

    /**
     * The {@link StatisticsKey} this class uses for clocking the duration of the exploration phase of the learning
     * algorithm.
     */
    public static final StatisticsKey KEY_DUR_LEARN = new StatisticsKey("exp-expl-dur", "Duration of exploration");

    /**
     * The {@link StatisticsKey} this class uses for clocking the duration of the counterexample search of the
     * equivalence oracle.
     */
    public static final StatisticsKey KEY_DUR_CEX =
            new StatisticsKey("exp-ce-dur", "Duration of counterexample search");

    /**
     * The {@link StatisticsKey} this class uses for counting the number of learning rounds of this experiment.
     */
    public static final StatisticsKey KEY_ROUNDS = new StatisticsKey("exp-rnd", "Number of learning rounds");

    /**
     * The {@link StatisticsKey} this class uses for counting the size of the final hypothesis.
     */
    public static final StatisticsKey KEY_FINAL_SIZE = new StatisticsKey("exp-hyp-size", "Size of final hypothesis");

    private static final Logger LOGGER = LoggerFactory.getLogger(Experiment.class);
    private final ExperimentImpl<?, ?> impl;
    private @Nullable A finalHypothesis;

    public <I, D> Experiment(LearningAlgorithm<? extends A, I, D> learningAlgorithm,
                             EquivalenceOracle<? super A, I, D> equivalenceAlgorithm,
                             Collection<? extends I> inputs) {
        this.impl = new ExperimentImpl<>(learningAlgorithm, equivalenceAlgorithm, inputs);
    }

    /**
     * Run the experiment, once.
     *
     * @return the final hypothesis
     *
     * @throws IllegalStateException
     *         if invoked more than once
     */
    public A run() {
        if (this.finalHypothesis != null) {
            throw new IllegalStateException("Experiment has already been run");
        }

        finalHypothesis = impl.run();
        return finalHypothesis;
    }

    /**
     * Returns the final hypothesis model.
     *
     * @return the final hypothesis model
     *
     * @throws IllegalStateException
     *         if the experiment has not been run yet
     */
    public A getFinalHypothesis() {
        if (finalHypothesis == null) {
            throw new IllegalStateException("Experiment has not yet been run");
        }

        return finalHypothesis;
    }

    private final class ExperimentImpl<I, D> {

        private final LearningAlgorithm<? extends A, I, D> learningAlgorithm;
        private final EquivalenceOracle<? super A, I, D> equivalenceAlgorithm;
        private final Collection<? extends I> inputs;
        private final StatisticsService statistics;
        private int rounds;

        ExperimentImpl(LearningAlgorithm<? extends A, I, D> learningAlgorithm,
                       EquivalenceOracle<? super A, I, D> equivalenceAlgorithm,
                       Collection<? extends I> inputs) {
            this.learningAlgorithm = learningAlgorithm;
            this.equivalenceAlgorithm = equivalenceAlgorithm;
            this.inputs = inputs;
            this.statistics = Statistics.getService();
        }

        A run() {
            rounds++;
            statistics.increaseCounter(KEY_ROUNDS, Experiment.this);
            LOGGER.info(Category.PHASE, "Starting round {}", rounds);
            LOGGER.info(Category.PHASE, "Learning");

            statistics.startOrResumeClock(KEY_DUR_LEARN, Experiment.this);
            learningAlgorithm.startLearning();
            statistics.pauseClock(KEY_DUR_LEARN, Experiment.this);

            while (true) {
                final A hyp = learningAlgorithm.getHypothesisModel();

                LOGGER.info(Category.PHASE, "Searching for counterexample");

                statistics.startOrResumeClock(KEY_DUR_CEX, Experiment.this);
                DefaultQuery<I, D> ce = equivalenceAlgorithm.findCounterExample(hyp, inputs);
                statistics.pauseClock(KEY_DUR_CEX, Experiment.this);

                if (ce == null) {
                    statistics.setCounter(KEY_FINAL_SIZE, hyp.size(), Experiment.this);
                    return hyp;
                }

                LOGGER.info(Category.COUNTEREXAMPLE, ce.getInput().toString());

                // next round ...
                rounds++;
                statistics.increaseCounter(KEY_ROUNDS, Experiment.this);
                LOGGER.info(Category.PHASE, "Starting round {}", rounds);
                LOGGER.info(Category.PHASE, "Learning");

                statistics.startOrResumeClock(KEY_DUR_LEARN, Experiment.this);
                final boolean refined = learningAlgorithm.refineHypothesis(ce);
                statistics.pauseClock(KEY_DUR_LEARN, Experiment.this);

                assert refined;
            }
        }
    }

    public static class DFAExperiment<I> extends Experiment<DFA<?, I>> {

        public DFAExperiment(LearningAlgorithm<? extends DFA<?, I>, I, Boolean> learningAlgorithm,
                             EquivalenceOracle<? super DFA<?, I>, I, Boolean> equivalenceAlgorithm,
                             Collection<? extends I> inputs) {
            super(learningAlgorithm, equivalenceAlgorithm, inputs);
        }

    }

    public static class MealyExperiment<I, O> extends Experiment<MealyMachine<?, I, ?, O>> {

        public MealyExperiment(LearningAlgorithm<? extends MealyMachine<?, I, ?, O>, I, Word<O>> learningAlgorithm,
                               EquivalenceOracle<? super MealyMachine<?, I, ?, O>, I, Word<O>> equivalenceAlgorithm,
                               Collection<? extends I> inputs) {
            super(learningAlgorithm, equivalenceAlgorithm, inputs);
        }

    }

    public static class MooreExperiment<I, O> extends Experiment<MooreMachine<?, I, ?, O>> {

        public MooreExperiment(LearningAlgorithm<? extends MooreMachine<?, I, ?, O>, I, Word<O>> learningAlgorithm,
                               EquivalenceOracle<? super MooreMachine<?, I, ?, O>, I, Word<O>> equivalenceAlgorithm,
                               Collection<? extends I> inputs) {
            super(learningAlgorithm, equivalenceAlgorithm, inputs);
        }

    }

}
