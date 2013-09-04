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
 * http://www.gnu.de/documents/lgpl.en.html.
 */
package de.learnlib.mealy;

import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.words.Word;
import de.learnlib.api.LearningAlgorithm;
import de.learnlib.oracles.DefaultQuery;

final class MealyLearnerWrapper<M extends MealyMachine<?, I, ?, O>, I, O> implements LearningAlgorithm<M, I, Word<O>> {
	
	private final LearningAlgorithm<M, I, O> learner;
	
	private M hypothesis = null;
	
	public MealyLearnerWrapper(LearningAlgorithm<M, I, O> learner) {
		this.learner = learner;
	}

	@Override
	public void startLearning() {
		learner.startLearning();
	}

	@Override
	public boolean refineHypothesis(DefaultQuery<I, Word<O>> ceQuery) {
		if(hypothesis == null)
			hypothesis = learner.getHypothesisModel();
		
		DefaultQuery<I,O> reducedQry = MealyUtil.reduceCounterExample(hypothesis, ceQuery);
		
		if(reducedQry == null)
			return false;
		
		hypothesis = null;
		return learner.refineHypothesis(reducedQry);
	}

	@Override
	public M getHypothesisModel() {
		hypothesis = learner.getHypothesisModel();
		return hypothesis;
	}

}
