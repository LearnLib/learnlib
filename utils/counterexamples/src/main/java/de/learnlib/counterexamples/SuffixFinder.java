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
 * <http://www.gnu.de/documents/lgpl.en.html>.
 */
package de.learnlib.counterexamples;

import net.automatalib.automata.concepts.SuffixOutput;
import de.learnlib.api.AccessSequenceTransformer;
import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;

/**
 * Suffix-based counterexample analyzer.
 * 
 * Given a query (u, v) which is a counterexample (i.e., the suffix-output for (u,v) is distinct
 * from the target system's output for (u,v)), it calculates the index <tt>i</tt> of the suffix
 * such that <tt>v[i:]</tt> still allows to expose a behavioral difference for an adequate prefix.
 * 
 * This adequate prefix can be calculated as follows: Let <tt>w = uv</tt> be the concatenation
 * of <tt>u</tt> and <tt>v</tt>, and let <tt>p</tt> be the length of the prefix <tt>u</tt>
 * and <tt>i</tt> the index returned by the counterexample analyzer. It then holds that
 * It then holds that <tt>({w[:p+i]}, v[i:])</tt> is a counterexample query, where <tt>{.}</tt>
 * denotes the access sequence of the corresponding word.
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 * @param <I> input symbol class
 * @param <O> output class
 */
public interface SuffixFinder<I, O> {
	
	/**
	 * Finds an adequate index for the counterexample suffix. If no such suffix could be determined,
	 * -1 is returned. Otherwise, the suffix is relative to the suffix part of the query:
	 * if the result of this method is <tt>i</tt>, then the corresponding suffix can be retrieved
	 * as <tt>ceQuery.getSuffix().suffix(-i)</tt>.
	 * 
	 * @param ceQuery the original counterexample query
	 * @param asTransformer the access sequence transformer
	 * @param hyp the hypothesis automaton
	 * @param oracle the oracle
	 * @return the index of the splitting suffix, or <tt>-1</tt> if no such suffix could be
	 * determined.
	 */
	public int findSuffixIndex(Query<I,O> ceQuery,
			AccessSequenceTransformer<I> asTransformer,
			SuffixOutput<I,O> hypOutput,
			MembershipOracle<I, O> oracle);
	
	public boolean allSuffixes();
}
