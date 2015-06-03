/* Copyright (C) 2014 TU Dortmund
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
