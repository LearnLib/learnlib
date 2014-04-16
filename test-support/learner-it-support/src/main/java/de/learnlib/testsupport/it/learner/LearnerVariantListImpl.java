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

import java.util.ArrayList;
import java.util.List;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.words.Word;

import de.learnlib.api.LearningAlgorithm;
import de.learnlib.mealy.MealyUtil;

class LearnerVariantListImpl<M,I,O> implements LearnerVariantList<M, I, O> {
	
	public static class DFALearnerVariantListImpl<I> extends LearnerVariantListImpl<DFA<?,I>,I,Boolean>
			implements DFALearnerVariantList<I> {}
	public static class MealyLearnerVariantListImpl<I,O> extends LearnerVariantListImpl<MealyMachine<?,I,?,O>, I, Word<O>>
			implements MealyLearnerVariantList<I, O> {}
	
	public static class MealySymLearnerVariantListImpl<I,O> implements MealySymLearnerVariantList<I, O> {
		
		private final MealyLearnerVariantListImpl<I, O> mealyLearnerVariants
			= new MealyLearnerVariantListImpl<>();
		
		public MealyLearnerVariantListImpl<I, O> getMealyLearnerVariants() {
			return mealyLearnerVariants;
		}

		@Override
		public void addLearnerVariant(
				String name,
				LearningAlgorithm<? extends MealyMachine<?, I, ?, O>, I, O> learner) {
			addLearnerVariant(name, learner, -1);
		}

		@Override
		public void addLearnerVariant(
				String name,
				LearningAlgorithm<? extends MealyMachine<?, I, ?, O>, I, O> learner,
				int maxRounds) {
			mealyLearnerVariants.addLearnerVariant(name,
					MealyUtil.wrapSymbolLearner(learner),
					maxRounds);
		}
		
	}
	
	private final List<LearnerVariant<M, I, O>> learnerVariants
		= new ArrayList<>();

	@Override
	public void addLearnerVariant(String name,
			LearningAlgorithm<? extends M, I, O> learner) {
		addLearnerVariant(name, learner, -1);
	}

	@Override
	public void addLearnerVariant(String name,
			LearningAlgorithm<? extends M, I, O> learner, int maxRounds) {
		LearnerVariant<M, I, O> variant
			= new LearnerVariant<>(name, learner, maxRounds);
		learnerVariants.add(variant);
	}
		
	
	public List<? extends LearnerVariant<M,I,O>> getLearnerVariants() {
		return learnerVariants;
	}
	
}
