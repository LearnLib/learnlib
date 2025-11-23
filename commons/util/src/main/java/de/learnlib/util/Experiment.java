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
package de.learnlib.util;

import de.learnlib.algorithm.LearningAlgorithm;
import de.learnlib.logging.Category;
import de.learnlib.oracle.EquivalenceOracle;
import de.learnlib.query.DefaultQuery;
import de.learnlib.statistic.Statistics;
import de.learnlib.statistic.StatisticsCollector;
import net.automatalib.alphabet.Alphabet;
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
public class Experiment<A extends Object> {

    public static final String LEARNING_PROFILE_KEY = "exp-expl-dur";
    public static final String COUNTEREXAMPLE_PROFILE_KEY = "exp-ce-dur";
    public static final String LEARNING_ROUNDS_KEY = "exp-rnd";

    private static final Logger LOGGER = LoggerFactory.getLogger(Experiment.class);
    private final ExperimentImpl<?, ?> impl;
    private final StatisticsCollector statisticsCollector;
    private @Nullable A finalHypothesis;

    public <I, D> Experiment(LearningAlgorithm<? extends A, I, D> learningAlgorithm,
                             EquivalenceOracle<? super A, I, D> equivalenceAlgorithm,
                             Alphabet<I> inputs) {
        this.impl = new ExperimentImpl<>(learningAlgorithm, equivalenceAlgorithm, inputs);
        this.statisticsCollector = Statistics.getCollector();
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
        private final Alphabet<I> inputs;
        private int rounds;

        ExperimentImpl(LearningAlgorithm<? extends A, I, D> learningAlgorithm,
                       EquivalenceOracle<? super A, I, D> equivalenceAlgorithm,
                       Alphabet<I> inputs) {
            this.learningAlgorithm = learningAlgorithm;
            this.equivalenceAlgorithm = equivalenceAlgorithm;
            this.inputs = inputs;
        }

        public A run() {
            rounds++;
            statisticsCollector.increaseCounter(LEARNING_ROUNDS_KEY, "Number of learning rounds");
            LOGGER.info(Category.PHASE, "Starting round {}", rounds);
            LOGGER.info(Category.PHASE, "Learning");

            statisticsCollector.startOrResumeClock(LEARNING_PROFILE_KEY, "Duration of exploration");
            learningAlgorithm.startLearning();
            statisticsCollector.pauseClock(LEARNING_PROFILE_KEY);

            while (true) {
                final A hyp = learningAlgorithm.getHypothesisModel();

                LOGGER.info(Category.PHASE, "Searching for counterexample");

                statisticsCollector.startOrResumeClock(COUNTEREXAMPLE_PROFILE_KEY, "Duration of counterexample search");
                DefaultQuery<I, D> ce = equivalenceAlgorithm.findCounterExample(hyp, inputs);
                statisticsCollector.pauseClock(COUNTEREXAMPLE_PROFILE_KEY);

                if (ce == null) {
                    return hyp;
                }

                LOGGER.info(Category.COUNTEREXAMPLE, ce.getInput().toString());

                // next round ...
                rounds++;
                statisticsCollector.increaseCounter(LEARNING_ROUNDS_KEY, "Number of learning rounds");
                LOGGER.info(Category.PHASE, "Starting round {}", rounds);
                LOGGER.info(Category.PHASE, "Learning");

                statisticsCollector.startOrResumeClock(LEARNING_PROFILE_KEY, "Duration of exploration");
                final boolean refined = learningAlgorithm.refineHypothesis(ce);
                statisticsCollector.pauseClock(LEARNING_PROFILE_KEY);

                assert refined;
            }
        }
    }

    public static class DFAExperiment<I> extends Experiment<DFA<?, I>> {

        public DFAExperiment(LearningAlgorithm<? extends DFA<?, I>, I, Boolean> learningAlgorithm,
                             EquivalenceOracle<? super DFA<?, I>, I, Boolean> equivalenceAlgorithm,
                             Alphabet<I> inputs) {
            super(learningAlgorithm, equivalenceAlgorithm, inputs);
        }

    }

    public static class MealyExperiment<I, O> extends Experiment<MealyMachine<?, I, ?, O>> {

        public MealyExperiment(LearningAlgorithm<? extends MealyMachine<?, I, ?, O>, I, Word<O>> learningAlgorithm,
                               EquivalenceOracle<? super MealyMachine<?, I, ?, O>, I, Word<O>> equivalenceAlgorithm,
                               Alphabet<I> inputs) {
            super(learningAlgorithm, equivalenceAlgorithm, inputs);
        }

    }

    public static class MooreExperiment<I, O> extends Experiment<MooreMachine<?, I, ?, O>> {

        public MooreExperiment(LearningAlgorithm<? extends MooreMachine<?, I, ?, O>, I, Word<O>> learningAlgorithm,
                               EquivalenceOracle<? super MooreMachine<?, I, ?, O>, I, Word<O>> equivalenceAlgorithm,
                               Alphabet<I> inputs) {
            super(learningAlgorithm, equivalenceAlgorithm, inputs);
        }

    }

}
