/* Copyright (C) 2013-2014 TU Dortmund
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
	
	private final class ExperimentImpl<I,D> {
		private final LearningAlgorithm<? extends A, I, D> learningAlgorithm;
	    private final EquivalenceOracle<? super A, I, D> equivalenceAlgorithm;
	    private final Alphabet<I> inputs;
	    
	    public ExperimentImpl(LearningAlgorithm<? extends A, I, D> learningAlgorithm, EquivalenceOracle<? super A, I, D> equivalenceAlgorithm, Alphabet<I> inputs) {
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
	            DefaultQuery<I, D> ce = equivalenceAlgorithm.findCounterExample(hyp, inputs);
	            profileStop("Searching for counterexample");
	            if (ce == null) {
	                done = true;
	                continue;
	            }
	            
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

    public <I,D> Experiment(LearningAlgorithm<? extends A, I, D> learningAlgorithm, EquivalenceOracle<? super A, I, D> equivalenceAlgorithm, Alphabet<I> inputs) {
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
