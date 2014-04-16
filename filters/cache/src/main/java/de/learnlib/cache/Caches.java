/* Copyright (C) 2013-2014 TU Dortmund
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
import de.learnlib.cache.dfa.DFACaches;
import de.learnlib.cache.mealy.MealyCacheOracle;
import de.learnlib.cache.mealy.MealyCaches;
import de.learnlib.cache.sul.SULCache;
import de.learnlib.cache.sul.SULCaches;

/**
 * Various methods to instantiate learning caches.
 * 
 * @author Malte Isberner
 * @deprecated since 2014-01-24. Use methods defined in {@link DFACaches}, {@link MealyCaches}, or {@link SULCaches}, respectively.
 */
@Deprecated
public abstract class Caches {
	
	/**
	 * @deprecated since 2014-01-24. Use {@link DFACaches#createCache(Alphabet, MembershipOracle)}
	 */
	@Deprecated
	public static <I> DFACacheOracle<I> createDFACache(Alphabet<I> alphabet, MembershipOracle<I,Boolean> mqOracle) {
        return DFACaches.createCache(alphabet, mqOracle);
	}

	/**
	 * @deprecated since 2014-01-24. Use {@link MealyCaches#createCache(Alphabet, MembershipOracle)}
	 */
	@Deprecated
	public static <I,O> MealyCacheOracle<I, O> createMealyCache(Alphabet<I> alphabet, MembershipOracle<I,Word<O>> mqOracle) {
        return MealyCaches.createCache(alphabet, mqOracle);
	}

	/**
	 * @deprecated since 2014-01-24. Use {@link SULCaches#createCache(Alphabet, SUL)}
	 */
	@Deprecated
	public static <I,O> SULCache<I,O> createSULCache(Alphabet<I> alphabet, SUL<I,O> sul) {
		return SULCaches.createCache(alphabet, sul);
	}

	// prevent inheritance
	private Caches() {
		throw new IllegalStateException("Constructor should never be invoked");
	}

}
