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
package de.learnlib.cache.mealy;

import net.automatalib.commons.util.mappings.Mapping;
import net.automatalib.incremental.mealy.dag.IncrementalMealyDAGBuilder;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import de.learnlib.api.MembershipOracle;

public abstract class MealyCaches {

	/**
	 * Creates a cache oracle for a Mealy machine learning setup, using a DAG for internal cache organization.
	 * 
	 * @param alphabet the input alphabet
	 * @param mqOracle the membership oracle
	 * @return a Mealy learning cache with a DAG-based implementation
	 */
	public static <I,O> MealyCacheOracle<I,O> createDAGCache(Alphabet<I> alphabet, MembershipOracle<I,Word<O>> mqOracle) {
		return MealyCacheOracle.createDAGCacheOracle(alphabet, mqOracle);
	}

	/**
	 * Creates a cache oracle for a Mealy machine learning setup, using a DAG for internal cache organization.
	 *
	 * @param alphabet the input alphabet
	 * @param errorSyms a mapping for the prefix-closure filter
	 * @param mqOracle the membership oracle
	 * @return a Mealy learning cache with a DAG-based implementation
	 */
	public static <I,O> MealyCacheOracle<I,O> createDAGCache(Alphabet<I> alphabet,
			Mapping<? super O,? extends O> errorSyms, MembershipOracle<I,Word<O>> mqOracle) {
		return MealyCacheOracle.createDAGCacheOracle(alphabet, errorSyms, mqOracle);
	}

	/**
	 * Creates a cache oracle for a Mealy machine learning setup, using a tree for internal cache organization.
	 * 
	 * @param alphabet the input alphabet
	 * @param mqOracle the membership oracle
	 * @return a Mealy learning cache with a tree-based implementation
	 */
	public static <I,O> MealyCacheOracle<I,O> createTreeCache(Alphabet<I> alphabet, MembershipOracle<I,Word<O>> mqOracle) {
		return MealyCacheOracle.createTreeCacheOracle(alphabet, mqOracle);
	}

	/**
	 * Creates a cache oracle for a Mealy machine learning setup, using a tree for internal cache organization.
	 *
	 * @param alphabet the input alphabet
	 * @param errorSyms a mapping for the prefix-closure filter
	 * @param mqOracle the membership oracle
	 * @return a Mealy learning cache with a tree-based implementation
	 */
	public static <I,O> MealyCacheOracle<I,O> createTreeCache(Alphabet<I> alphabet,
			Mapping<? super O,? extends O> errorSyms, MembershipOracle<I,Word<O>> mqOracle) {
		return MealyCacheOracle.createTreeCacheOracle(alphabet, errorSyms, mqOracle);
	}

	/**
	 * Creates a cache oracle for a Mealy machine learning setup.
	 * <p>
	 * Note that this method does not specify the implementation to use for the cache. Currently, a DAG ({@link IncrementalMealyDAGBuilder})
	 * is used; however, this may change in the future.
	 * 
	 * @param alphabet the input alphabet
	 * @param mqOracle the membership oracle
	 * @return a Mealy learning cache with a default implementation
	 */
	public static <I,O> MealyCacheOracle<I, O> createCache(Alphabet<I> alphabet, MembershipOracle<I,Word<O>> mqOracle) {
		return MealyCacheOracle.createDAGCacheOracle(alphabet, mqOracle);
	}
	
	private MealyCaches() {
		throw new IllegalStateException();
	}
}
