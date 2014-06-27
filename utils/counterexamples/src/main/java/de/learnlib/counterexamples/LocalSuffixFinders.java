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

import java.util.Objects;

import net.automatalib.automata.concepts.SuffixOutput;
import net.automatalib.words.Word;
import de.learnlib.api.AccessSequenceTransformer;
import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;
import de.learnlib.oracles.MQUtil;

/**
 * A collection of suffix-based local counterexample analyzers.
 * 
 * @see LocalSuffixFinder
 * 
 * @author Malte Isberner 
 */
public abstract class LocalSuffixFinders {
	
	/**
	 * Searches for a distinguishing suffixes by checking for counterexample yielding
	 * access sequence transformations in linear ascending order.
	 * @see #findLinear(Query, AccessSequenceTransformer, SuffixOutput, MembershipOracle)
	 */
	public static final LocalSuffixFinder<Object,Object> FIND_LINEAR
		= new LocalSuffixFinder<Object,Object>() {
			@Override
			public <RI,RD>
			int findSuffixIndex(Query<RI, RD> ceQuery,
					AccessSequenceTransformer<RI> asTransformer,
					SuffixOutput<RI,RD> hypOutput,
					MembershipOracle<RI, RD> oracle) {
				return findLinear(ceQuery, asTransformer, hypOutput, oracle);
			}
			@Override
			public String toString() {
				return "FindLinear";
			}
	};
	
	/**
	 * Searches for a distinguishing suffixes by checking for counterexample yielding
	 * access sequence transformations in linear descending order.
	 * @see #findLinearReverse(Query, AccessSequenceTransformer, SuffixOutput, MembershipOracle)
	 */
	public static final LocalSuffixFinder<Object,Object> FIND_LINEAR_REVERSE
		= new LocalSuffixFinder<Object,Object>() {
			@Override
			public <RI,RD>
			int findSuffixIndex(Query<RI, RD> ceQuery,
					AccessSequenceTransformer<RI> asTransformer,
					SuffixOutput<RI,RD> hypOutput,
					MembershipOracle<RI, RD> oracle) {
				return findLinearReverse(ceQuery, asTransformer, hypOutput, oracle);
			}
			@Override
			public String toString() {
				return "FindLinear-Reverse";
			}
	};
	
	/**
	 * Searches for a distinguishing suffixes by checking for counterexample yielding
	 * access sequence transformations using a binary search, as proposed by Rivest &amp; Schapire.
	 * @see #findRivestSchapire(Query, AccessSequenceTransformer, SuffixOutput, MembershipOracle)
	 */
	public static final LocalSuffixFinder<Object,Object> RIVEST_SCHAPIRE
		= new LocalSuffixFinder<Object,Object>() {
			@Override
			public <RI,RD>
			int findSuffixIndex(Query<RI, RD> ceQuery,
					AccessSequenceTransformer<RI> asTransformer,
					SuffixOutput<RI,RD> hypOutput,
					MembershipOracle<RI, RD> oracle) {
				return findRivestSchapire(ceQuery, asTransformer, hypOutput, oracle);
			}
			@Override
			public String toString() {
				return "RivestSchapire";
			}
	};

	
	
	/**
	 * Searches for a distinguishing suffixes by checking for counterexample yielding
	 * access sequence transformations in linear ascending order.
	 * 
	 * @param ceQuery the initial counterexample query
	 * @param asTransformer the access sequence transformer
	 * @param hypOutput interface to the hypothesis output, for checking whether the oracle output
	 * contradicts the hypothesis
	 * @param oracle interface to the SUL
	 * @return the index of the respective suffix, or <tt>-1</tt> if no
	 * counterexample could be found
	 * @see LocalSuffixFinder
	 */
	public static <S,I,D> int findLinear(Query<I,D> ceQuery,
			AccessSequenceTransformer<I> asTransformer,
			SuffixOutput<I,D> hypOutput,
			MembershipOracle<I, D> oracle) {
		
		Word<I> queryWord = ceQuery.getInput();
		int queryLen = queryWord.length();
		
		Word<I> prefix = ceQuery.getPrefix();
		int prefixLen = prefix.length();
		
		// If the prefix is an access sequence (i.e., a short prefix),
		// then we can omit the first step, as transforming won't change
		int min = asTransformer.isAccessSequence(prefix) ? prefixLen+1 : prefixLen;
		
		for(int i = min; i <= queryLen; i++) {
			Word<I> nextPrefix = queryWord.prefix(i);
			Word<I> as = asTransformer.transformAccessSequence(nextPrefix);
			
			Word<I> nextSuffix = queryWord.subWord(i);
			
			D hypOut = hypOutput.computeSuffixOutput(as, nextSuffix);
			D mqOut = MQUtil.output(oracle, as, nextSuffix);
			
			if(Objects.equals(hypOut, mqOut))
				return i;
		}
		
		return -1;
	}
	
	/**
	 * Searches for a distinguishing suffixes by checking for counterexample yielding
	 * access sequence transformations in linear descending order.
	 * 
	 * @param ceQuery the initial counterexample query
	 * @param asTransformer the access sequence transformer
	 * @param hypOutput interface to the hypothesis output, for checking whether the oracle output
	 * contradicts the hypothesis
	 * @param oracle interface to the SUL
	 * @return the index of the respective suffix, or <tt>-1</tt> if no
	 * counterexample could be found
	 * @see LocalSuffixFinder
	 */
	public static <I,D> int findLinearReverse(Query<I,D> ceQuery,
			AccessSequenceTransformer<I> asTransformer,
			SuffixOutput<I,D> hypOutput,
			MembershipOracle<I, D> oracle) {
		
		Word<I> queryWord = ceQuery.getInput();
		int queryLen = queryWord.length();
		
		Word<I> prefix = ceQuery.getPrefix();
		int prefixLen = prefix.length();
		
		// If the prefix is no access sequence (i.e., a long prefix),
		// then we also need to consider that breakage only occurs
		// by transforming this long prefix into a short one
		int min = asTransformer.isAccessSequence(prefix) ? prefixLen : prefixLen-1;
		
		for(int i = queryLen - 1; i >= min; i--) {
			Word<I> nextPrefix = queryWord.prefix(i);
			Word<I> as = asTransformer.transformAccessSequence(nextPrefix);
			Word<I> nextSuffix = queryWord.subWord(i);
			
			D hypOut = hypOutput.computeSuffixOutput(as, nextSuffix);
			D mqOut = MQUtil.output(oracle, as, nextSuffix);
			
			if(!Objects.equals(hypOut, mqOut))
				return i+1;
		}
		
		return -1;
	}
	
	
	/**
	 * Searches for a distinguishing suffixes by checking for counterexample yielding
	 * access sequence transformations using a binary search, as proposed by Rivest &amp; Schapire.
	 * 
	 * @param ceQuery the initial counterexample query
	 * @param asTransformer the access sequence transformer
	 * @param hypOutput interface to the hypothesis output, for checking whether the oracle output
	 * contradicts the hypothesis
	 * @param oracle interface to the SUL
	 * @return the index of the respective suffix, or <tt>-1</tt> if no
	 * counterexample could be found
	 * @see LocalSuffixFinder
	 */
	public static <I,D> int findRivestSchapire(Query<I,D> ceQuery,
			AccessSequenceTransformer<I> asTransformer,
			SuffixOutput<I,D> hypOutput,
			MembershipOracle<I, D> oracle) {

		Word<I> queryWord = ceQuery.getInput();
		int queryLen = queryWord.length();
		
		Word<I> prefix = ceQuery.getPrefix();
		int prefixLen = prefix.length();
		
		
		int low = asTransformer.isAccessSequence(prefix) ? prefixLen : prefixLen-1;
		
		int high = queryLen;
		
		while((high - low) > 1) {
			int mid = low + (high - low + 1)/2;
			
			
			Word<I> nextPrefix = queryWord.prefix(mid);
			Word<I> as = asTransformer.transformAccessSequence(nextPrefix);
			
			Word<I> nextSuffix = queryWord.subWord(mid);
			
			D hypOut = hypOutput.computeSuffixOutput(as, nextSuffix);
			D ceOut = MQUtil.output(oracle, as, nextSuffix);
			
			if(!Objects.equals(hypOut, ceOut))
				low = mid;
			else
				high = mid;
		}
		
		// FIXME: No check if actually found CE
		return low+1;
	}
	
	@SuppressWarnings("unchecked")
	public static LocalSuffixFinder<Object, Object>[] values() {
		return new LocalSuffixFinder[]{
				FIND_LINEAR,
				FIND_LINEAR_REVERSE,
				RIVEST_SCHAPIRE
		};
	}
	
	// Prevent inheritance
	private LocalSuffixFinders() {}
}
