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
import net.automatalib.commons.util.comparison.CmpUtil;
import net.automatalib.words.Word;
import de.learnlib.api.AccessSequenceTransformer;
import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;
import de.learnlib.oracles.MQUtil;

/**
 * A collection of suffix-based counterexample analyzers.
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 * @param <I> input symbol class
 * @param <O> output class
 */
public abstract class SuffixFinders<I,O> {
	
	private static final SuffixFinder<?,?> LINEAR_INSTANCE
		= new SuffixFinder<Object,Object>() {
			@Override
			public int findSuffixIndex(Query<Object, Object> ceQuery,
					AccessSequenceTransformer<Object> asTransformer,
					SuffixOutput<Object,Object> hypOutput,
					MembershipOracle<Object, Object> oracle) {
				return findLinear(ceQuery, asTransformer, hypOutput, oracle);
			}

			@Override
			public boolean allSuffixes() {
				return false;
			}
	};
	
	private static final SuffixFinder<?,?> LINEAR_REVERSE_INSTANCE
		= new SuffixFinder<Object,Object>() {
			@Override
			public int findSuffixIndex(Query<Object, Object> ceQuery,
					AccessSequenceTransformer<Object> asTransformer,
					SuffixOutput<Object,Object> hypOutput,
					MembershipOracle<Object, Object> oracle) {
				return findLinearReverse(ceQuery, asTransformer, hypOutput, oracle);
			}

			@Override
			public boolean allSuffixes() {
				return false;
			}
	
	};
	
	private static final SuffixFinder<?,?> BINARY_SEARCH_INSTANCE
		= new SuffixFinder<Object,Object>() {
			@Override
			public int findSuffixIndex(Query<Object, Object> ceQuery,
					AccessSequenceTransformer<Object> asTransformer,
					SuffixOutput<Object,Object> hypOutput,
					MembershipOracle<Object, Object> oracle) {
				return findBinarySearch(ceQuery, asTransformer, hypOutput, oracle);
			}

			@Override
			public boolean allSuffixes() {
				return false;
			}
	};
	
	private static final SuffixFinder<?,?> SHAHBAZ_INSTANCE
		= new SuffixFinder<Object,Object>() {
			@Override
			public int findSuffixIndex(Query<Object, Object> ceQuery,
					AccessSequenceTransformer<Object> asTransformer,
					SuffixOutput<Object,Object> hypOutput,
					MembershipOracle<Object, Object> oracle) {
				return findShahbaz(ceQuery, asTransformer, hypOutput, oracle);
			}

			@Override
			public boolean allSuffixes() {
				return true;
			}
		};
	
	private static final SuffixFinder<?,?> MAHLER_INSTANCE
		= new SuffixFinder<Object,Object>() {

			@Override
			public int findSuffixIndex(Query<Object, Object> ceQuery,
					AccessSequenceTransformer<Object> asTransformer,
					SuffixOutput<Object,Object> hypOutput,
					MembershipOracle<Object, Object> oracle) {
				return 0;
			}

			@Override
			public boolean allSuffixes() {
				return true;
			}
		};
	
	@SuppressWarnings("unchecked")
	public static <I,O> SuffixFinder<I,O> getFindLinear() {
		return (SuffixFinder<I,O>)LINEAR_INSTANCE;
	}
	
	@SuppressWarnings("unchecked")
	public static <I,O> SuffixFinder<I,O> getFindLinearReverse() {
		return (SuffixFinder<I,O>)LINEAR_REVERSE_INSTANCE;
	}
	
	@SuppressWarnings("unchecked")
	public static <I,O> SuffixFinder<I,O> getFindBinarySearch() {
		return (SuffixFinder<I,O>)BINARY_SEARCH_INSTANCE;
	}
	
	@SuppressWarnings("unchecked")
	public static <I,O> SuffixFinder<I,O> getFindShahbazInstance() {
		return (SuffixFinder<I,O>)SHAHBAZ_INSTANCE;
	}
	
	@SuppressWarnings("unchecked")
	public static <I,O> SuffixFinder<I,O> getFindMahlerInstance() {
		return (SuffixFinder<I,O>)MAHLER_INSTANCE;
	}
	
	
	public static <S,I,O> int findLinear(Query<I,O> ceQuery,
			AccessSequenceTransformer<I> asTransformer,
			SuffixOutput<I,O> hypOutput,
			MembershipOracle<I, O> oracle) {
		
		Word<I> queryWord = ceQuery.getInput();
		
		Word<I> prefix = ceQuery.getPrefix();
		
		int prefixLen = prefix.length();
		Word<I> suffix = ceQuery.getSuffix();
		int suffixLen = suffix.length();
		
		// If the prefix is an access sequence (i.e., a short prefix),
		// then we can omit the first step, as transforming won't change
		int min = asTransformer.isAccessSequence(prefix) ? 1 : 0;
		
		for(int i = min; i <= suffixLen; i++) {
			Word<I> nextPrefix = queryWord.prefix(prefixLen + i);
			Word<I> as = asTransformer.transformAccessSequence(nextPrefix);
			Word<I> nextSuffix = suffix.subWord(i, suffixLen);
			
			O hypOut = hypOutput.computeSuffixOutput(as, nextSuffix);
			O mqOut = MQUtil.query(oracle, as, nextSuffix);
			
			if(CmpUtil.equals(hypOut, mqOut))
				return i;
		}
		
		return -1;
	}
	
	public static <I,O> int findLinearReverse(Query<I,O> ceQuery,
			AccessSequenceTransformer<I> asTransformer,
			SuffixOutput<I,O> hypOutput,
			MembershipOracle<I, O> oracle) {
		
		Word<I> queryWord = ceQuery.getInput();
		int queryLen = queryWord.length();
		Word<I> prefix = ceQuery.getPrefix();
		int prefixLen = prefix.length();
		
		Word<I> suffix = ceQuery.getSuffix();
		int suffixLen = suffix.length();
		
		// If the prefix is no access sequence (i.e., a long prefix),
		// then we also need to consider that breakage only occurs
		// by transforming this long prefix into a short one
		int min = asTransformer.isAccessSequence(prefix) ? 0 : -1;
		
		for(int i = suffixLen - 1; i >= min; i--) {
			Word<I> nextPrefix = queryWord.prefix(prefixLen + i);
			Word<I> as = asTransformer.transformAccessSequence(nextPrefix);
			Word<I> nextSuffix = queryWord.subWord(prefixLen + i, queryLen);
			
			O hypOut = hypOutput.computeSuffixOutput(as, nextSuffix);
			O mqOut = MQUtil.query(oracle, as, nextSuffix);
			
			if(!CmpUtil.equals(hypOut, mqOut))
				return i+1;
		}
		
		return -1;
	}
	
	public static <I,O> int findBinarySearch(Query<I,O> ceQuery,
			AccessSequenceTransformer<I> asTransformer,
			SuffixOutput<I,O> hypOutput,
			MembershipOracle<I, O> oracle) {
		
		Word<I> suffix = ceQuery.getSuffix();
		int suffixLen = suffix.length();
		
		Word<I> prefix = ceQuery.getPrefix();
		int prefixLen = prefix.length();
		Word<I> queryWord = ceQuery.getInput();
		int queryLen = queryWord.length();
		
		
		int low = asTransformer.isAccessSequence(prefix) ? 0 : -1;
		
		int high = suffixLen;
		
		while((high - low) > 1) {
			int mid = low + (high - low + 1)/2;
			
			
			Word<I> nextPrefix = queryWord.prefix(prefixLen + mid);
			Word<I> as = asTransformer.transformAccessSequence(nextPrefix);
			
			Word<I> nextSuffix = queryWord.subWord(prefixLen + mid, queryLen);
			
			O hypOut = hypOutput.computeSuffixOutput(as, nextSuffix);
			O ceOut = MQUtil.query(oracle, as, nextSuffix);
			
			if(!CmpUtil.equals(hypOut, ceOut))
				low = mid;
			else
				high = mid;
		}
		
		return low+1;
	}
	
	public static <I,O> int findShahbaz(Query<I,O> ceQuery,
			AccessSequenceTransformer<I> asTransformer,
			SuffixOutput<I,O> hypOutput,
			MembershipOracle<I, O> oracle) {
		
		Word<I> queryWord = ceQuery.getInput();
		Word<I> prefix = ceQuery.getPrefix();
		int prefixLen = prefix.length();
		
		int suffixLen = ceQuery.getSuffix().length();
		
		for(int i = 0; i < suffixLen; i++) {
			Word<I> nextPrefix = queryWord.prefix(prefixLen + i);
			
			
			if(!asTransformer.isAccessSequence(nextPrefix))
				return i;
		}
		
		return -1;
	}
	// Prevent inheritance
	private SuffixFinders() {}
}
