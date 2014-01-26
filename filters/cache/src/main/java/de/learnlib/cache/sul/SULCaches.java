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
package de.learnlib.cache.sul;

import net.automatalib.words.Alphabet;
import de.learnlib.api.SUL;

public abstract class SULCaches {

	public static <I,O>
	SULCache<I, O> createTreeCache(Alphabet<I> alphabet, SUL<I,O> sul) {
		return SULCache.createTreeCache(alphabet, sul);
	}
	
	public static <I,O>
	SULCache<I,O> createDAGCache(Alphabet<I> alphabet, SUL<I,O> sul) {
		return SULCache.createDAGCache(alphabet, sul);
	}
	
	public static <I,O>
	SULCache<I,O> createCache(Alphabet<I> alphabet, SUL<I,O> sul) {
		return createDAGCache(alphabet, sul);
	}
	
	private SULCaches() {
		throw new IllegalStateException("Constructor should never be invoked");
	}
}
