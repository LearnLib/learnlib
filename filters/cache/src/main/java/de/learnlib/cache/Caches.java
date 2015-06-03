/* Copyright (C) 2013-2014 TU Dortmund
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
