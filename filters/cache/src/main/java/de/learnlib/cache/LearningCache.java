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
import de.learnlib.api.EquivalenceOracle;

/**
 * Interface for a cache used in automata learning.
 * <p>
 * The idea of a cache is to save (potentially expensive) queries to the real system under learning by storing the results of
 * previous queries. This is particularly useful as many learning algorithms pose redundant queries, i.e., pose the same query
 * twice or more times in different contexts.
 * <p>
 * A learning cache provides a {@link #createCacheConsistencyTest() cache consistency test}, which is an equivalence query
 * realization that only checks a given hypothesis against the contents of the cache.
 *    
 * @author Malte Isberner
 *
 * @param <A>
 * 		the (maximally generic) automaton model for which the caches stores information. For example,
 * 		for a {@link MealyMachine Mealy} cache this would be {@code MealyMachine<?,I,?,O>. This type
 * 		determines what the cache contents can be checked against by
 * 		a {@link #createCacheConsistencyTest() cache consistency test} 
 * @param <I>
 * 		input symbol type 
 * @param <O>
 * 		output symbol type
 */
public interface LearningCache<A,I,O> {
	
	/**
	 * Specialization of the {@link LearningCache} interface for DFA learning.
	 * 
	 * @author Malte Isberner
	 *
	 * @param <I> input symbol type
	 */
	public static interface DFALearningCache<I> extends LearningCache<DFA<?,I>,I,Boolean> {}
	
	/**
	 * Specialization of the {@link LearningCache} interface for Mealy machine learning.
	 * @author Malte Isberner
	 *
	 * @param <I> input symbol type
	 * @param <O> output symbol type
	 */
	public static interface MealyLearningCache<I,O> extends LearningCache<MealyMachine<?,I,?,O>,I,Word<O>> {}
	
	/**
	 * Creates a <i>cache consistency test</i>. A cache consistency test is an equivalence oracle which checks
	 * a given hypothesis against the current contents of the cache. Hence, no queries are posed to the underlying
	 * system.
	 * <p>
	 * The created cache consistency test is backed by the cache contents. This method does not need to be invoked
	 * repeatedly when the cache contents change.
	 *  
	 * @return a cache consistency test for the contents of this cache 
	 */
	public EquivalenceOracle<A, I, O> createCacheConsistencyTest();
}
