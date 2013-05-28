/* Copyright (C) 2013 TU Dortmund
 * This file is part of LearnLib, http://www.learnlib.de/.
 * 
 * LearnLib is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 3.0 as published by the Free Software Foundation.
 * 
 * LearnLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with LearnLib; if not, see
 * <http://www.gnu.de/documents/lgpl.en.html>.
 */
package de.learnlib.experiments;

import net.automatalib.words.Alphabet;
import de.learnlib.api.EquivalenceOracle;
import de.learnlib.api.LearningAlgorithm;
import de.learnlib.logging.LearnLogger;
import de.learnlib.oracles.DefaultQuery;
import de.learnlib.statistics.Counter;
import de.learnlib.statistics.SimpleProfiler;

/**
 * runs a learning experiment. 
 * 
 * @author falkhowar
 * @param <A>
 * @param <I>
 * @param <O> 
 */
public class Experiment<A, I, O> {

    private static LearnLogger logger = LearnLogger.getLogger(Experiment.class.getName());
    private LearningAlgorithm<? extends A, I, O> learningAlgorithm;
    private EquivalenceOracle<? super A, I, O> equivalenceAlgorithm;
    private Alphabet<I> inputs;
    private boolean logModels = false;
    private boolean profile = false;
    private Counter rounds = new Counter("rounds", "#");

    public Experiment(LearningAlgorithm<? extends A, I, O> learningAlgorithm, EquivalenceOracle<? super A, I, O> equivalenceAlgorithm, Alphabet<I> inputs) {
        this.learningAlgorithm = learningAlgorithm;
        this.equivalenceAlgorithm = equivalenceAlgorithm;
        this.inputs = inputs;
    }

    
    /**
     * 
     */
    public void run() {

        rounds.increment();
        logger.logPhase("Starting round " + rounds.getCount());
        logger.logPhase("Learning");
        profileStart("Learning");
        learningAlgorithm.startLearning();
        profileStop("Learning");

        boolean done = false;
        while (!done) {

            A hyp = learningAlgorithm.getHypothesisModel();
            if (logModels) {
                logger.logModel(hyp);
            }

            logger.logPhase("Searching for counterexample");
            profileStart("Searching for counterexample");
            DefaultQuery<I, O> ce = equivalenceAlgorithm.findCounterExample(hyp, inputs);
            if (ce == null) {
                done = true;
                continue;
            }
            profileStop("Searching for counterexample");
            
            logger.logCounterexample(ce.getInput().toString());

            // next round ...
            rounds.increment();
            logger.logPhase("Starting round " + rounds.getCount());
            logger.logPhase("Learning");
            profileStart("Learning");
            learningAlgorithm.refineHypothesis(ce);
            profileStop("Learning");
        }

    }

    
    
    private void profileStart(String taskname) {
        if (profile) {
            SimpleProfiler.start(taskname);
        }
    }

    private void profileStop(String taskname) {
        if (profile) {
            SimpleProfiler.stop(taskname);
        }
    }

    /**
     * @param logModels the logModels to set
     */
    public void setLogModels(boolean logModels) {
        this.logModels = logModels;
    }

    /**
     * @param profile the profile to set
     */
    public void setProfile(boolean profile) {
        this.profile = profile;
    }

    /**
     * @return the rounds
     */
    public Counter getRounds() {
        return rounds;
    }
}
