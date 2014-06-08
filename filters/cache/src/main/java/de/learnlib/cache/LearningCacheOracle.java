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
 * http://www.gnu.de/documents/lgpl.en.html.
 */
package de.learnlib.cache;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.words.Word;
import de.learnlib.api.MembershipOracle;

/**
 * A {@link LearningCache learning cache} that also serves as a {@link MembershipOracle membership oracle}.
 * 
 * @author Malte Isberner
 *
 * @param <A>
 * 		the (maximally generic) automaton model for which the cache stores information. See {@link LearningCache}
 * @param <I>
 * 		input symbol type
 * @param <D>
 * 		output domain type
 */
public interface LearningCacheOracle<A, I, D> extends LearningCache<A,I,D>, MembershipOracle<I, D> {
	
	/**
	 * Specialization of the {@link LearningCacheOracle} interface for DFA learning.
	 *  
	 * @author Malte Isberner
	 *
	 * @param <I> input symbol type
	 */
	public static interface DFALearningCacheOracle<I>
		extends LearningCacheOracle<DFA<?,I>,I,Boolean>, DFALearningCache<I>, DFAMembershipOracle<I> {}
	
	/**
	 * Specialization of the {@link LearningCacheOracle} interface for Mealy machine learning.
	 * 
	 * @author Malte Isberner
	 *
	 * @param <I> input symbol type
	 * @param <O> output symbol type
	 */
	public static interface MealyLearningCacheOracle<I,O>
		extends LearningCacheOracle<MealyMachine<?,I,?,O>,I,Word<O>>, MealyLearningCache<I,O>, MealyMembershipOracle<I,O> {}
}
