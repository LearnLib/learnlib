/* Copyright (C) 2013-2014 TU Dortmund
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

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
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
@ParametersAreNonnullByDefault
public class Experiment<A> {
	
	public static class DFAExperiment<I> extends Experiment<DFA<?,I>> {
		public DFAExperiment(
				LearningAlgorithm<? extends DFA<?, I>, I, Boolean> learningAlgorithm,
				EquivalenceOracle<? super DFA<?, I>, I, Boolean> equivalenceAlgorithm,
				Alphabet<I> inputs) {
			super(learningAlgorithm, equivalenceAlgorithm, inputs);
		}
	}
	
	public static class MealyExperiment<I,O> extends Experiment<MealyMachine<?,I,?,O>> {
		public MealyExperiment(
				LearningAlgorithm<? extends MealyMachine<?, I, ?, O>, I, Word<O>> learningAlgorithm,
				EquivalenceOracle<? super MealyMachine<?, I, ?, O>, I, Word<O>> equivalenceAlgorithm,
				Alphabet<I> inputs) {
			super(learningAlgorithm, equivalenceAlgorithm, inputs);
		}
		
	}
	
	private final class ExperimentImpl<I,O> {
		private final LearningAlgorithm<? extends A, I, O> learningAlgorithm;
	    private final EquivalenceOracle<? super A, I, O> equivalenceAlgorithm;
	    private final Alphabet<I> inputs;
	    
	    public ExperimentImpl(LearningAlgorithm<? extends A, I, O> learningAlgorithm, EquivalenceOracle<? super A, I, O> equivalenceAlgorithm, Alphabet<I> inputs) {
	        this.learningAlgorithm = learningAlgorithm;
	        this.equivalenceAlgorithm = equivalenceAlgorithm;
	        this.inputs = inputs;
	    }
	    
	    public A run() {
	        rounds.increment();
	        logger.logPhase("Starting round " + rounds.getCount());
	        logger.logPhase("Learning");
	        profileStart("Learning");
	        learningAlgorithm.startLearning();
	        profileStop("Learning");

	        boolean done = false;
	        A hyp = null;
	        while (!done) {
	        	hyp = learningAlgorithm.getHypothesisModel();
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

	        return hyp;
	    }
	}

    private static LearnLogger logger = LearnLogger.getLogger(Experiment.class);
    
    private boolean logModels = false;
    private boolean profile = false;
    private Counter rounds = new Counter("rounds", "#");
    private A finalHypothesis = null;
    private final ExperimentImpl<?,?> impl;

    public <I,O> Experiment(LearningAlgorithm<? extends A, I, O> learningAlgorithm, EquivalenceOracle<? super A, I, O> equivalenceAlgorithm, Alphabet<I> inputs) {
        this.impl = new ExperimentImpl<>(learningAlgorithm, equivalenceAlgorithm, inputs);
    }
    

    
    /**
     * 
     */
    @Nonnull
    public A run() {
    	finalHypothesis = impl.run();
    	return finalHypothesis;
    }
    
    @Nonnull
    public A getFinalHypothesis() {
    	if(finalHypothesis == null) {
    		throw new IllegalStateException("Experiment has not yet been run");
    	}
    	return finalHypothesis;
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
    @Nonnull
    public Counter getRounds() {
        return rounds;
    }
}
