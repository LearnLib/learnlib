/* Copyright (C) 2014 TU Dortmund
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
package de.learnlib.testsupport.it.learner;

import de.learnlib.api.LearningAlgorithm;

class LearnerVariant<M,I,O> {
	
	private final String name;
	private final LearningAlgorithm<? extends M, I, O> learner;
	private final int maxRounds;

	public LearnerVariant(String name, LearningAlgorithm<? extends M,I,O> learner, int maxRounds) {
		this.name = name;
		this.learner = learner;
		this.maxRounds = maxRounds;
	}

	public String getName() {
		return name;
	}

	public LearningAlgorithm<? extends M, I, O> getLearner() {
		return learner;
	}

	public int getMaxRounds() {
		return maxRounds;
	}
	
	

}
