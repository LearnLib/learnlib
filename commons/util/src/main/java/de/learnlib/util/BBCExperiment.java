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
package de.learnlib.util;

import javax.annotation.Nonnull;

import de.learnlib.api.algorithm.LearningAlgorithm;
import de.learnlib.api.logging.LearnLogger;
import de.learnlib.api.oracle.BlackBoxOracle;
import de.learnlib.api.oracle.BlackBoxOracle.BlackBoxProperty;
import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.filter.statistic.Counter;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

/**
 * A black-box checking experiment. The experiment can be started with {@link #run()}.
 *
 * @author Jeroen Meijer
 *
 * @param <A> the automaton type
 * @param <I> the input type
 * @param <D> the output type
 */
public class BBCExperiment<A, I, D> extends Experiment<A, I, D> {

    public static final String PROPERTY_VIOLATION_PROFILE_KEY = "Searching for property violation";

    private static final LearnLogger LOGGER = LearnLogger.getLogger(BBCExperiment.class);

    /**
     * @see #getRoundsBBC()
     */
    private final Counter roundsBBC = new Counter("BBC rounds", "#");

    /**
     * @see #getRoundsPropertyViolation()
     */
    private final Counter roundsPropertyViolation = new Counter("property violation detection rounds", "#");

    /**
     * @see #getBlackBoxOracle()
     */
    private final BlackBoxOracle<A, I, D, ? extends BlackBoxProperty<?, A, I, D>> blackBoxOracle;

    /**
     * Indicates whether the {@link BlackBoxOracle} should continue learning when all properties have been violated.
     */
    private final boolean keepLearning;

    /**
     * Constructs a new BBCExperiment.
     *
     * @param learningAlgorithm the learner.
     * @param equivalenceAlgorithm the equivalence oracle.
     * @param inputs the alphabet.
     * @param blackBoxOracle the black-box oracle.
     * @param keepLearning whether to keep learning the hypothesis when all properties have been violated.
     */
    protected BBCExperiment(
            LearningAlgorithm<? extends A, I, D> learningAlgorithm,
            EquivalenceOracle<? super A, I, D> equivalenceAlgorithm,
            Alphabet<I> inputs,
            BlackBoxOracle<A, I, D, ? extends BlackBoxProperty<?, A, I, D>> blackBoxOracle,
            boolean keepLearning) {
        super(learningAlgorithm, equivalenceAlgorithm, inputs);
        this.blackBoxOracle = blackBoxOracle;
        this.keepLearning = keepLearning;
    }

    /**
     * Returns a {@link Counter} that indicates how many main iterations where done.
     *
     * @return the Counter.
     */
    public Counter getRoundsBBC() {
        return roundsBBC;
    }

    /**
     * Returns a {@link Counter} that indicates how many nested iterations where done.
     *
     * @return the Counter.
     */
    public Counter getRoundsPropertyViolation() {
        return roundsPropertyViolation;
    }

    /**
     * Gets the {@link BlackBoxOracle} for this experiment.
     *
     * @return the {@link BlackBoxOracle}
     */
    public BlackBoxOracle<A, I, D, ? extends BlackBoxProperty<?, A, I, D>> getBlackBoxOracle() {
        return blackBoxOracle;
    }

    /**
     * Runs a black-box checking experiment.
     *
     * @return the final hypothesis.
     */
    @Nonnull
    @Override
    public A run() {
        if (isRun()) {
            throw new IllegalStateException("Experiment has already been run");
        }

        init();

        do {
            roundsBBC.increment();
            DefaultQuery<I, D> ce;
            do {
                roundsPropertyViolation.increment();
                LOGGER.logPhase("Searching for property violation");

                profileStart(PROPERTY_VIOLATION_PROFILE_KEY);
                ce = blackBoxOracle.findCounterExample(getLearningAlgorithm().getHypothesisModel(), getInputs());
                profileStop(PROPERTY_VIOLATION_PROFILE_KEY);
                assert blackBoxOracle.allPropertiesViolated() == (ce == null);
            } while (!blackBoxOracle.allPropertiesViolated() && ce != null && getLearningAlgorithm().refineHypothesis(ce));
        } while ((keepLearning || !blackBoxOracle.allPropertiesViolated()) && refineHypothesis());

        final A finalHyp = getLearningAlgorithm().getHypothesisModel();
        setFinalHypothesis(finalHyp);

        return finalHyp;
    }

    public static class DFABBCExperiment<I> extends BBCExperiment<DFA<?, I>, I, Boolean> {

        public DFABBCExperiment(LearningAlgorithm.DFALearner<I> learningAlgorithm,
                                EquivalenceOracle.DFAEquivalenceOracle<I> equivalenceAlgorithm,
                                Alphabet<I> inputs,
                                BlackBoxOracle.DFABlackBoxOracle<I> blackBoxOracle,
                                boolean keepLearning) {
            super(learningAlgorithm, equivalenceAlgorithm, inputs, blackBoxOracle, keepLearning);
        }

        public DFABBCExperiment(LearningAlgorithm.DFALearner<I> learningAlgorithm,
                                EquivalenceOracle.DFAEquivalenceOracle<I> equivalenceAlgorithm,
                                Alphabet<I> inputs,
                                BlackBoxOracle.DFABlackBoxOracle<I> blackBoxOracle) {
            this(learningAlgorithm, equivalenceAlgorithm, inputs, blackBoxOracle, false);
        }
    }

    public static class MealyBBCExperiment<I, O> extends BBCExperiment<MealyMachine<?, I, ?, O>, I, Word<O>> {

        public MealyBBCExperiment(LearningAlgorithm.MealyLearner<I, O> learningAlgorithm,
                                  EquivalenceOracle.MealyEquivalenceOracle<I, O> equivalenceAlgorithm,
                                  Alphabet<I> inputs,
                                  BlackBoxOracle.MealyBlackBoxOracle<I, O> blackBoxOracle,
                                  boolean keepLearning) {
            super(learningAlgorithm, equivalenceAlgorithm, inputs, blackBoxOracle, keepLearning);
        }

        public MealyBBCExperiment(LearningAlgorithm.MealyLearner<I, O> learningAlgorithm,
                               EquivalenceOracle.MealyEquivalenceOracle<I, O> equivalenceAlgorithm,
                               Alphabet<I> inputs,
                               BlackBoxOracle.MealyBlackBoxOracle<I, O> blackBoxOracle) {
            this(learningAlgorithm, equivalenceAlgorithm, inputs, blackBoxOracle, false);
        }
    }
}
