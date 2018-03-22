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
import javax.annotation.ParametersAreNonnullByDefault;

import de.learnlib.api.algorithm.LearningAlgorithm;
import de.learnlib.api.logging.LearnLogger;
import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.filter.statistic.Counter;
import de.learnlib.util.statistics.SimpleProfiler;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

/**
 * runs a learning experiment.
 *
 * @param <A> the automaton type
 * @param <I> the input type
 * @param <D> the output type
 *
 * @author falkhowar
 * @author Jeroen Meijer
 */
@ParametersAreNonnullByDefault
public class Experiment<A, I, D> {

    public static final String LEARNING_PROFILE_KEY = "Learning";
    public static final String COUNTEREXAMPLE_PROFILE_KEY = "Searching for counterexample";

    private static final LearnLogger LOGGER = LearnLogger.getLogger(Experiment.class);

    private boolean logModels;

    private boolean profile;

    private final Counter rounds = new Counter("learning rounds", "#");

    private A finalHypothesis;

    private final LearningAlgorithm<? extends A, I, D> learningAlgorithm;

    private final EquivalenceOracle<? super A, I, D> equivalenceAlgorithm;

    private final Alphabet<I> inputs;

    public Experiment(LearningAlgorithm<? extends A, I, D> learningAlgorithm,
                      EquivalenceOracle<? super A, I, D> equivalenceAlgorithm,
                      Alphabet<I> inputs) {
        this.learningAlgorithm = learningAlgorithm;
        this.equivalenceAlgorithm = equivalenceAlgorithm;
        this.inputs = inputs;
    }

    public boolean isLogModels() {
        return logModels;
    }

    public Counter getRounds() {
        return rounds;
    }

    public LearningAlgorithm<? extends A, I, D> getLearningAlgorithm() {
        return learningAlgorithm;
    }

    public EquivalenceOracle<? super A, I, D> getEquivalenceAlgorithm() {
        return equivalenceAlgorithm;
    }

    public Alphabet<I> getInputs() {
        return inputs;
    }

    public void setLogModels(boolean logModels) {
        this.logModels = logModels;
    }

    public void setProfile(boolean profile) {
        this.profile = profile;
    }

    /**
     * Returns whether this experiment has already been run.
     *
     * @return {@code true} if this experiment has been run, {@code false} otherwise.
     */
    public boolean isRun() {
        return this.finalHypothesis != null;
    }

    protected void init() {
        rounds.increment();
        LOGGER.logPhase("Starting round " + rounds.getCount());
        LOGGER.logPhase("Learning");

        profileStart(LEARNING_PROFILE_KEY);
        learningAlgorithm.startLearning();
        profileStop(LEARNING_PROFILE_KEY);
    }

    protected boolean refineHypothesis() {
        A hyp = getLearningAlgorithm().getHypothesisModel();

        if (logModels) {
            LOGGER.logModel(hyp);
        }

        LOGGER.logPhase("Searching for counterexample");

        profileStart(COUNTEREXAMPLE_PROFILE_KEY);
        DefaultQuery<I, D> ce = equivalenceAlgorithm.findCounterExample(hyp, getInputs());
        profileStop(COUNTEREXAMPLE_PROFILE_KEY);

        if (ce != null) {
            LOGGER.logCounterexample(ce.toString());

            // next round ...
            rounds.increment();
            LOGGER.logPhase("Starting round " + getRounds().getCount());
            LOGGER.logPhase("Learning");

            profileStart(LEARNING_PROFILE_KEY);
            final boolean refined = learningAlgorithm.refineHypothesis(ce);
            profileStop(LEARNING_PROFILE_KEY);

            assert refined;
        }

        return ce != null;
    }

    @Nonnull
    public A run() {
        if (isRun()) {
            throw new IllegalStateException("Experiment has already been run");
        }

        init();

        while (refineHypothesis()) { }

        finalHypothesis = learningAlgorithm.getHypothesisModel();

        return finalHypothesis;
    }

    protected void setFinalHypothesis(A hyp) {
        finalHypothesis = hyp;
    }

    @Nonnull
    public A getFinalHypothesis() {
        if (!isRun()) {
            throw new IllegalStateException("Experiment has not yet been run");
        }
        return finalHypothesis;
    }

    protected void profileStart(String taskname) {
        if (profile) {
            SimpleProfiler.start(taskname);
        }
    }

    protected void profileStop(String taskname) {
        if (profile) {
            SimpleProfiler.stop(taskname);
        }
    }

    public static class DFAExperiment<I> extends Experiment<DFA<?, I>, I, Boolean> {

        public DFAExperiment(LearningAlgorithm<DFA<?, I>, I, Boolean> learningAlgorithm,
                             EquivalenceOracle<DFA<?, I>, I, Boolean> equivalenceAlgorithm,
                             Alphabet<I> inputs) {
            super(learningAlgorithm, equivalenceAlgorithm, inputs);
        }
    }

    public static class MealyExperiment<I, O> extends Experiment<MealyMachine<?, I, ?, O>, I, Word<O>> {

        public MealyExperiment(LearningAlgorithm<MealyMachine<?, I, ?, O>, I, Word<O>> learningAlgorithm,
                               EquivalenceOracle<MealyMachine<?, I, ?, O>, I, Word<O>> equivalenceAlgorithm,
                               Alphabet<I> inputs) {
            super(learningAlgorithm, equivalenceAlgorithm, inputs);
        }
    }
}

