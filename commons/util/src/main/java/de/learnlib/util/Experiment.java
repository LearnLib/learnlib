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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import de.learnlib.algorithm.LearningAlgorithm;
import de.learnlib.algorithm.LearningAlgorithm.DFALearner;
import de.learnlib.algorithm.LearningAlgorithm.MealyLearner;
import de.learnlib.algorithm.LearningAlgorithm.MooreLearner;
import de.learnlib.logging.Category;
import de.learnlib.oracle.EquivalenceOracle;
import de.learnlib.oracle.EquivalenceOracle.DFAEquivalenceOracle;
import de.learnlib.oracle.EquivalenceOracle.MealyEquivalenceOracle;
import de.learnlib.oracle.EquivalenceOracle.MooreEquivalenceOracle;
import de.learnlib.query.DefaultQuery;
import de.learnlib.statistic.Statistics;
import de.learnlib.statistic.StatisticsKey;
import de.learnlib.statistic.StatisticsService;
import de.learnlib.tooling.annotation.refinement.GenerateRefinement;
import de.learnlib.tooling.annotation.refinement.Generic;
import de.learnlib.tooling.annotation.refinement.Mapping;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.concept.FiniteRepresentation;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.automaton.transducer.MooreMachine;
import net.automatalib.serialization.InputModelSerializer;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runs a learning experiment.
 *
 * @param <A>
 *         the automaton type
 * @param <I>
 *         input symbol type
 * @param <D>
 *         output domain type
 */
@GenerateRefinement(name = "DFAExperiment",
                    generics = @Generic(value = "I", desc = "input symbol type"),
                    parentGenerics = {@Generic(clazz = DFA.class, generics = {"?", "I"}),
                                      @Generic("I"),
                                      @Generic(clazz = Boolean.class)},
                    typeMappings = {@Mapping(from = LearningAlgorithm.class,
                                             to = DFALearner.class,
                                             generics = @Generic("I")),
                                    @Mapping(from = EquivalenceOracle.class,
                                             to = DFAEquivalenceOracle.class,
                                             generics = @Generic("I"))})
@GenerateRefinement(name = "MealyExperiment",
                    generics = {@Generic(value = "I", desc = "input symbol type"),
                                @Generic(value = "O", desc = "output symbol type")},
                    parentGenerics = {@Generic(clazz = MealyMachine.class, generics = {"?", "I", "?", "O"}),
                                      @Generic("I"),
                                      @Generic(clazz = Word.class, generics = "O")},
                    typeMappings = {@Mapping(from = LearningAlgorithm.class,
                                             to = MealyLearner.class,
                                             generics = {@Generic("I"), @Generic("O")}),
                                    @Mapping(from = EquivalenceOracle.class,
                                             to = MealyEquivalenceOracle.class,
                                             generics = {@Generic("I"), @Generic("O")})})
@GenerateRefinement(name = "MooreExperiment",
                    generics = {@Generic(value = "I", desc = "input symbol type"),
                                @Generic(value = "O", desc = "output symbol type")},
                    parentGenerics = {@Generic(clazz = MooreMachine.class, generics = {"?", "I", "?", "O"}),
                                      @Generic("I"),
                                      @Generic(clazz = Word.class, generics = "O")},
                    typeMappings = {@Mapping(from = LearningAlgorithm.class,
                                             to = MooreLearner.class,
                                             generics = {@Generic("I"), @Generic("O")}),
                                    @Mapping(from = EquivalenceOracle.class,
                                             to = MooreEquivalenceOracle.class,
                                             generics = {@Generic("I"), @Generic("O")})})
public class Experiment<A extends FiniteRepresentation, I, D> {

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

    protected static final Logger LOGGER = LoggerFactory.getLogger(Experiment.class);

    protected final LearningAlgorithm<? extends A, I, D> learningAlgorithm;
    protected final EquivalenceOracle<? super A, I, D> equivalenceAlgorithm;
    protected final Alphabet<I> inputs;
    protected final @Nullable InputModelSerializer<I, ? super A> serializer;
    protected final StatisticsService statistics;
    private int rounds;
    private @Nullable A finalHypothesis;

    /**
     * Constructor. Delegates to
     * {@link Experiment#Experiment(LearningAlgorithm, EquivalenceOracle, Alphabet, InputModelSerializer)} using
     * {@code null} for {@code serializer}.
     *
     * @param learningAlgorithm
     *         the learning algorithm to use in this experiment
     * @param equivalenceAlgorithm
     *         the strategy for finding counterexamples
     * @param inputs
     *         the inputs to consider for exploration
     *
     * @see Experiment#Experiment(LearningAlgorithm, EquivalenceOracle, Alphabet, InputModelSerializer)
     */
    public Experiment(LearningAlgorithm<? extends A, I, D> learningAlgorithm,
                      EquivalenceOracle<? super A, I, D> equivalenceAlgorithm,
                      Alphabet<I> inputs) {
        this(learningAlgorithm, equivalenceAlgorithm, inputs, null);
    }

    /**
     * Constructor. Creates a new experiment to run.
     *
     * @param learningAlgorithm
     *         the learning algorithm to use in this experiment
     * @param equivalenceAlgorithm
     *         the strategy for finding counterexamples
     * @param inputs
     *         the inputs to consider for exploration
     * @param serializer
     *         the serializer for logging intermediate hypotheses (may be {@code null} in case no such logging is
     *         wanted)
     */
    public Experiment(LearningAlgorithm<? extends A, I, D> learningAlgorithm,
                      EquivalenceOracle<? super A, I, D> equivalenceAlgorithm,
                      Alphabet<I> inputs,
                      @Nullable InputModelSerializer<I, ? super A> serializer) {
        this.learningAlgorithm = learningAlgorithm;
        this.equivalenceAlgorithm = equivalenceAlgorithm;
        this.inputs = inputs;
        this.serializer = serializer;
        this.statistics = Statistics.getService();
    }

    /**
     * Returns the final hypothesis model.
     *
     * @return the final hypothesis model
     *
     * @throws IllegalStateException
     *         if the experiment has not been run yet
     */
    public final A getFinalHypothesis() {
        if (finalHypothesis == null) {
            throw new IllegalStateException("Experiment has not yet been run");
        }

        return finalHypothesis;
    }

    /**
     * Run the experiment, once.
     *
     * @return the final hypothesis
     *
     * @throws IllegalStateException
     *         if invoked more than once
     */
    public final A run() {
        if (this.finalHypothesis != null) {
            throw new IllegalStateException("Experiment has already been run");
        }

        finalHypothesis = runInternal();
        return finalHypothesis;
    }

    private A runInternal() {
        rounds++;
        statistics.increaseCounter(KEY_ROUNDS, this);
        LOGGER.info(Category.PHASE, "Starting round {}", rounds);
        LOGGER.info(Category.PHASE, "Learning");

        initializeLearning();

        while (true) {
            final A hyp = learningAlgorithm.getHypothesisModel();

            if (serializer != null) {
                final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try {
                    serializer.writeModel(baos, hyp, inputs);
                    LOGGER.info(Category.MODEL, "Intermediate hypothesis:\n{}", baos.toString(StandardCharsets.UTF_8));
                } catch (IOException e) {
                    LOGGER.warn("Couldn't write intermediate hypothesis", e);
                }
            }

            LOGGER.info(Category.PHASE, "Searching for counterexample");

            statistics.startOrResumeClock(KEY_DUR_CEX, this);
            DefaultQuery<I, D> ce = equivalenceAlgorithm.findCounterExample(hyp, inputs);
            statistics.pauseClock(KEY_DUR_CEX, this);

            if (ce == null) {
                statistics.setCounter(KEY_FINAL_SIZE, hyp.size(), this);
                return hyp;
            }

            LOGGER.info(Category.COUNTEREXAMPLE, ce.getInput().toString());

            // next round ...
            rounds++;
            statistics.increaseCounter(KEY_ROUNDS, this);
            LOGGER.info(Category.PHASE, "Starting round {}", rounds);
            LOGGER.info(Category.PHASE, "Learning");

            statistics.startOrResumeClock(KEY_DUR_LEARN, this);
            final boolean refined = learningAlgorithm.refineHypothesis(ce);
            statistics.pauseClock(KEY_DUR_LEARN, this);

            assert refined;

            postRefinementHook();
        }
    }

    /**
     * Utility method to access to current learning round.
     *
     * @return the current learning round
     */
    protected final int getRound() {
        return rounds;
    }

    /**
     * Initializes the learning process. By default, this method calls {@link LearningAlgorithm#startLearning()}.
     */
    protected void initializeLearning() {
        statistics.startOrResumeClock(KEY_DUR_LEARN, this);
        learningAlgorithm.startLearning();
        statistics.pauseClock(KEY_DUR_LEARN, this);
    }

    /**
     * Called upon calling {@link LearningAlgorithm#refineHypothesis(DefaultQuery)}.
     */
    protected void postRefinementHook() {
        // do nothing by default
    }
}
