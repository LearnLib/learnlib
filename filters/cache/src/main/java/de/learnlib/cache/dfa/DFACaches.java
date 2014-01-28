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
package de.learnlib.cache.dfa;

import javax.annotation.ParametersAreNonnullByDefault;

import net.automatalib.incremental.dfa.IncrementalDFABuilder;
import net.automatalib.words.Alphabet;
import de.learnlib.api.MembershipOracle;


@ParametersAreNonnullByDefault
public abstract class DFACaches {
	
	/**
	 * Creates a cache oracle for a DFA learning setup, using a DAG for internal cache organization. 
	 * 
	 * @param alphabet the input alphabet
	 * @param mqOracle the membership oracle
	 * @return a Mealy learning cache with a default implementation
	 */
	public static <I>
	DFACacheOracle<I> createDAGCache(Alphabet<I> alphabet, MembershipOracle<I,Boolean> mqOracle) {
		return DFACacheOracle.createDAGCacheOracle(alphabet, mqOracle);
	}
	
	public static <I>
	DFACacheOracle<I> createTreeCache(Alphabet<I> alphabet, MembershipOracle<I,Boolean> mqOracle) {
		return DFACacheOracle.createTreeCacheOracle(alphabet, mqOracle);
	}
	
	/**
	 * Creates a cache oracle for a DFA learning setup.
	 * <p>
	 * Note that this method does not specify the implementation to use for the cache. Currently, a DAG ({@link IncrementalDFABuilder})
	 * is used; however, this may change in the future.
	 * 
	 * @param alphabet the input alphabet
	 * @param mqOracle the membership oracle
	 * @return a Mealy learning cache with a default implementation
	 */
	public static <I>
	DFACacheOracle<I> createCache(Alphabet<I> alphabet, MembershipOracle<I,Boolean> mqOracle) {
		return createDAGCache(alphabet, mqOracle);
	}
	
	private DFACaches() {
		throw new IllegalStateException("Constructor should never be invoked");
	}
}
