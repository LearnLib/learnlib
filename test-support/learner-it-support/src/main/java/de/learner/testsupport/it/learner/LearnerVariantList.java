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
package de.learner.testsupport.it.learner;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.words.Word;
import de.learnlib.api.LearningAlgorithm;

/**
 * A write-only list to store multiple variants of a learning algorithm.
 * <p>
 * Usually, there should be one integration test class per learning algorithm. However,
 * in many cases a single learning algorithm can be configured in numerous ways, all (or many)
 * of which should be tested independently. Due to the large number of possible combinations,
 * it is undesirable to create a single integration test class for each configuration; instead,
 * these variants should be configured and created programmatically. The purpose of the variant
 * list is to offer a convenient interface for storing all these variants.
 * 
 * @author Malte Isberner
 *
 * @param <M> hypothesis model type (upper bound)
 * @param <I> input symbol type
 * @param <O> output type
 */
public interface LearnerVariantList<M, I, O> {
	
	public static interface DFALearnerVariantList<I> extends LearnerVariantList<DFA<?,I>,I,Boolean> {}
	public static interface MealyLearnerVariantList<I,O> extends LearnerVariantList<MealyMachine<?,I,?,O>,I,Word<O>> {}
	public static interface MealySymLearnerVariantList<I,O> extends LearnerVariantList<MealyMachine<?,I,?,O>,I,O> {}
	
	/**
	 * Adds a learner variant with the default maximum number of rounds (i.e., the size of the
	 * target automaton) to the list.
	 * <p>
	 * This is a convenience method, equivalent to invoking {@code addLearnerVariant(name, learner, -1)}.
	 * 
	 * @param name the name of the variant
	 * @param learner the algorithm instance for this variant
	 */
	public void addLearnerVariant(String name, LearningAlgorithm<? extends M,I,O> learner);
	
	/**
	 * Adds a learner variant with a given maximum number of rounds to the list.
	 * 
	 * @param name the name of the variant
	 * @param learner the algorithm instance for this variant
	 * @param maxRounds the maximum number of rounds for the specified target automaton. If a value
	 * less than or equal to zero is specified, the default maximum number of rounds (the size of the
	 * target automaton) is assumed.
	 */
	public void addLearnerVariant(String name, LearningAlgorithm<? extends M,I,O> learner, int maxRounds);

	
}
