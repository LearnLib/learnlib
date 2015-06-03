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
package de.learnlib.cache.dfa;

import javax.annotation.ParametersAreNonnullByDefault;

import de.learnlib.api.MembershipOracle;
import de.learnlib.cache.LearningCacheOracle.DFALearningCacheOracle;

import net.automatalib.incremental.dfa.IncrementalDFABuilder;
import net.automatalib.words.Alphabet;


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
	
	public static <I>
	DFALearningCacheOracle<I> createHashCache(MembershipOracle<I, Boolean> mqOracle) {
		return new DFAHashCacheOracle<>(mqOracle);
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
