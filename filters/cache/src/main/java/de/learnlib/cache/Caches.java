/* Copyright (C) 2013 TU Dortmund
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

import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import de.learnlib.api.MembershipOracle;
import de.learnlib.api.SUL;
import de.learnlib.cache.dfa.DFACacheOracle;
import de.learnlib.cache.mealy.MealyCacheOracle;
import de.learnlib.cache.sul.SULCache;

public abstract class Caches {
	
	
	public static <I> DFACacheOracle<I> createDFACache(Alphabet<I> alphabet, MembershipOracle<I,Boolean> mqOracle) {
		return new DFACacheOracle<I>(alphabet, mqOracle);
	}
	
	public static <I,O> MealyCacheOracle<I, O> createMealyCache(Alphabet<I> alphabet, MembershipOracle<I,Word<O>> mqOracle) {
		return MealyCacheOracle.createDAGCacheOracle(alphabet, mqOracle);
	}
	
	public static <I,O> SULCache<I,O> createSULCache(Alphabet<I> alphabet, SUL<I,O> sul) {
		return new SULCache<>(alphabet, sul);
	}

	// prevent inheritance
	private Caches() {
	}

}
