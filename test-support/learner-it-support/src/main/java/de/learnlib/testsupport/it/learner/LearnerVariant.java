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

class LearnerVariant<M,I,D> {
	
	private final String name;
	private final LearningAlgorithm<? extends M, I, D> learner;
	private final int maxRounds;

	public LearnerVariant(String name, LearningAlgorithm<? extends M,I,D> learner, int maxRounds) {
		this.name = name;
		this.learner = learner;
		this.maxRounds = maxRounds;
	}
	
	public String getLearnerName() {
		String learnerName = learner.toString();
		int atPos = learnerName.lastIndexOf('@');
		if (atPos != -1) {
			int simpleNameStart = learnerName.lastIndexOf('.', atPos - 1) + 1;
			learnerName = learnerName.substring(simpleNameStart, atPos);
		}
		
		return learnerName;
	}

	public String getName() {
		return name;
	}

	public LearningAlgorithm<? extends M, I, D> getLearner() {
		return learner;
	}

	public int getMaxRounds() {
		return maxRounds;
	}
	
	

}
