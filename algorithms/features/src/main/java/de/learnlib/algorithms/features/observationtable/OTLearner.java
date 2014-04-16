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
package de.learnlib.algorithms.features.observationtable;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.words.Word;
import de.learnlib.api.LearningAlgorithm;


public interface OTLearner<M, I, O> extends
		LearningAlgorithm<M, I, O>, ObservationTableFeature<I,O> {
	
	public static interface OTLearnerDFA<I> extends DFALearner<I>, OTLearner<DFA<?,I>,I,Boolean> {
	}
	
	public static interface OTLearnerMealy<I,O> extends MealyLearner<I,O>, OTLearner<MealyMachine<?,I,?,O>,I,Word<O>> {
	}
}
