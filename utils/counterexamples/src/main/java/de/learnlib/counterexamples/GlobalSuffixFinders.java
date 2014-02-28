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
 * <http://www.gnu.de/documents/lgpl.en.html>.
 */
package de.learnlib.counterexamples;

import java.util.Collections;
import java.util.List;

import net.automatalib.automata.concepts.SuffixOutput;
import net.automatalib.words.Word;
import de.learnlib.api.AccessSequenceTransformer;
import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;

/**
 * A collection of suffix-based global counterexample analyzers.
 * 
 * @see GlobalSuffixFinder
 * 
 * @author Malte Isberner
 */
public abstract class GlobalSuffixFinders {
	
	/**
	 * Adds all suffixes of the input word, as suggested by Mahler & Pnueli.
	 * @see #findMahlerPnueli(Query)
	 */
	public static final GlobalSuffixFinder<Object,Object> MAHLER_PNUELI
		= new GlobalSuffixFinder<Object,Object>() {
			@Override
			public <RI,RO>
			List<? extends Word<RI>> findSuffixes(
					Query<RI, RO> ceQuery,
					AccessSequenceTransformer<RI> asTransformer,
					SuffixOutput<RI, RO> hypOutput,
					MembershipOracle<RI, RO> oracle) {
				return findMahlerPnueli(ceQuery);
			}
			@Override
			public String toString() {
				return "MahlerPnueli";
			}
	};
	
	/**
	 * Adds all suffixes of the remainder of the input word, after stripping a maximal
	 * one-letter extension of an access sequence
	 * @see #findShahbaz(Query, AccessSequenceTransformer)
	 */
	public static final GlobalSuffixFinder<Object,Object> SHAHBAZ
		= new GlobalSuffixFinder<Object,Object>() {
			@Override
			public <RI,RO>
			List<? extends Word<RI>> findSuffixes(
					Query<RI, RO> ceQuery,
					AccessSequenceTransformer<RI> asTransformer,
					SuffixOutput<RI, RO> hypOutput,
					MembershipOracle<RI, RO> oracle) {
				return findShahbaz(ceQuery, asTransformer);
			}
			@Override
			public String toString() {
				return "Shahbaz";
			}
	};
	
	/**
	 * Adds the single suffix found by the access sequence transformation
	 * in ascending linear order.
	 * @see #findLinear(Query, AccessSequenceTransformer, SuffixOutput, MembershipOracle, boolean)
	 */
	public static final GlobalSuffixFinder<Object,Object> FIND_LINEAR
		= fromLocalFinder(LocalSuffixFinders.FIND_LINEAR, false);
	
	/**
	 * Adds the suffix found by the access sequence transformation
	 * in ascending linear order, and all of its suffixes.
	 * @see #findLinear(Query, AccessSequenceTransformer, SuffixOutput, MembershipOracle, boolean)
	 */
	public static final GlobalSuffixFinder<Object,Object> FIND_LINEAR_ALLSUFFIXES
		= fromLocalFinder(LocalSuffixFinders.FIND_LINEAR, true);
	
	/**
	 * Adds the single suffix found by the access sequence transformation
	 * in descending linear order.
	 * @see #findLinearReverse(Query, AccessSequenceTransformer, SuffixOutput, MembershipOracle, boolean)
	 */
	public static final GlobalSuffixFinder<Object,Object> FIND_LINEAR_REVERSE
		= fromLocalFinder(LocalSuffixFinders.FIND_LINEAR_REVERSE, false);
	
	/**
	 * Adds the suffix found by the access sequence transformation
	 * in descending linear order, and all of its suffixes.
	 * @see #findLinearReverse(Query, AccessSequenceTransformer, SuffixOutput, MembershipOracle, boolean)
	 */
	public static final GlobalSuffixFinder<Object,Object> FIND_LINEAR_REVERSE_ALLSUFFIXES
		= fromLocalFinder(LocalSuffixFinders.FIND_LINEAR_REVERSE, true);
	
	/**
	 * Adds the single suffix found by the access sequence transformation
	 * using binary search.
	 * @see #findRivestSchapire(Query, AccessSequenceTransformer, SuffixOutput, MembershipOracle, boolean) 
	 */
	public static final GlobalSuffixFinder<Object,Object> RIVEST_SCHAPIRE
		= fromLocalFinder(LocalSuffixFinders.RIVEST_SCHAPIRE, false);
	
	/**
	 * Adds the suffix found by the access sequence transformation
	 * using binary search, and all of its suffixes.
	 * @see #findRivestSchapire(Query, AccessSequenceTransformer, SuffixOutput, MembershipOracle, boolean) 
	 */
	public static final GlobalSuffixFinder<Object,Object> RIVEST_SCHAPIRE_ALLSUFFIXES
		= fromLocalFinder(LocalSuffixFinders.RIVEST_SCHAPIRE, true);
	
	
	/**
	 * Transforms a {@link LocalSuffixFinder} into a global one. Since local suffix finders
	 * only return a single suffix, suffix-closedness of the set of distinguishing suffixes
	 * might not be preserved. Note that for correctly implemented local suffix finders, this
	 * does not impair correctness of the learning algorithm. However, without suffix closedness,
	 * intermediate hypothesis models might be non-canonical, if no additional precautions
	 * are taken. For that reasons, the <tt>allSuffixes</tt> parameter can be specified to control
	 * whether or not the list returned by
	 * {@link GlobalSuffixFinder#findSuffixes(Query, AccessSequenceTransformer, SuffixOutput, MembershipOracle)}
	 * of the returned global suffix finder should not only contain the single suffix, but also
	 * all of its suffixes, ensuring suffix-closedness.
	 *  
	 * @param localFinder the local suffix finder
	 * @param allSuffixes whether or not all suffixes of the found local suffix should be added
	 * @return a global suffix finder using the analysis method from the specified local suffix finder
	 */
	public static <I,O> GlobalSuffixFinder<I,O> fromLocalFinder(
			final LocalSuffixFinder<I,O> localFinder,
			final boolean allSuffixes) {
		
		return new GlobalSuffixFinder<I,O>() {
			@Override
			public <RI extends I,RO extends O>
			List<? extends Word<RI>> findSuffixes(Query<RI, RO> ceQuery,
					AccessSequenceTransformer<RI> asTransformer,
					SuffixOutput<RI, RO> hypOutput, MembershipOracle<RI, RO> oracle) {
				int idx = localFinder.findSuffixIndex(ceQuery, asTransformer, hypOutput, oracle);
				return suffixesForLocalOutput(ceQuery, idx, allSuffixes);
			}
			@Override
			public String toString() {
				return localFinder.toString() + (allSuffixes ? "-AllSuffixes" : "");
			}
		};
	}
	
	/**
	 * Transforms a {@link LocalSuffixFinder} into a global one. This is a convenience method,
	 * behaving like <tt>fromLocalFinder(localFinder, false)</tt>.
	 * @see #fromLocalFinder(LocalSuffixFinder, boolean)
	 */
	public static <I,O> GlobalSuffixFinder<I,O> fromLocalFinder(LocalSuffixFinder<I,O> localFinder) {
		return fromLocalFinder(localFinder, false);
	}
	
	
	/**
	 * Returns all suffixes of the counterexample word as distinguishing suffixes, as suggested
	 * by Mahler & Pnueli.
	 * @param ceQuery the counterexample query
	 * @return all suffixes of the counterexample input
	 */
	public static <I,O> List<? extends Word<I>> findMahlerPnueli(Query<I,O> ceQuery) {
		return ceQuery.getInput().suffixes(false);
	}
	
	/**
	 * Returns all suffixes of the counterexample word as distinguishing suffixes, after
	 * stripping a maximal one-letter extension of an access sequence, as suggested by
	 * Shahbaz. 
	 * @param ceQuery the counterexample query
	 * @param asTransformer the access sequence transformer
	 * @return all suffixes from the counterexample after stripping a maximal one-letter
	 * extension of an access sequence. 
	 */
	public static <I,O> List<? extends Word<I>> findShahbaz(Query<I,O> ceQuery,
			AccessSequenceTransformer<I> asTransformer) {
		Word<I> queryWord = ceQuery.getInput();
		int queryLen = queryWord.length();
		
		Word<I> prefix = ceQuery.getPrefix();
		int i = prefix.length();
		
		while(i <= queryLen) {
			Word<I> nextPrefix = queryWord.prefix(i);
			
			if(!asTransformer.isAccessSequence(nextPrefix))
				break;
			i++;
		}
		
		return queryWord.subWord(i).suffixes(false);
	}
	
	/**
	 * Returns the suffix (plus all of its suffixes, if <tt>allSuffixes</tt> is true) found by
	 * the access sequence transformation in ascending linear order.
	 * @param ceQuery the counterexample query
	 * @param asTransformer the access sequence transformer
	 * @param hypOutput interface to the hypothesis output
	 * @param oracle interface to the SUL output
	 * @param allSuffixes whether or not to include all suffixes of the found suffix
	 * @return the distinguishing suffixes
	 * @see LocalSuffixFinders#findLinear(Query, AccessSequenceTransformer, SuffixOutput, MembershipOracle)
	 */
	public static <I,O> List<? extends Word<I>> findLinear(Query<I,O> ceQuery,
			AccessSequenceTransformer<I> asTransformer,
			SuffixOutput<I,O> hypOutput,
			MembershipOracle<I, O> oracle,
			boolean allSuffixes) {
		int idx = LocalSuffixFinders.findLinear(ceQuery, asTransformer, hypOutput, oracle);
		return suffixesForLocalOutput(ceQuery, idx, allSuffixes);
	}
	
	/**
	 * Returns the suffix (plus all of its suffixes, if <tt>allSuffixes</tt> is true) found by
	 * the access sequence transformation in descending linear order.
	 * @param ceQuery the counterexample query
	 * @param asTransformer the access sequence transformer
	 * @param hypOutput interface to the hypothesis output
	 * @param oracle interface to the SUL output
	 * @param allSuffixes whether or not to include all suffixes of the found suffix
	 * @return the distinguishing suffixes
	 * @see LocalSuffixFinders#findLinearReverse(Query, AccessSequenceTransformer, SuffixOutput, MembershipOracle)
	 */
	public static <I,O> List<? extends Word<I>> findLinearReverse(Query<I,O> ceQuery,
			AccessSequenceTransformer<I> asTransformer,
			SuffixOutput<I,O> hypOutput,
			MembershipOracle<I, O> oracle,
			boolean allSuffixes) {
		int idx = LocalSuffixFinders.findLinearReverse(ceQuery, asTransformer, hypOutput, oracle);
		return suffixesForLocalOutput(ceQuery, idx, allSuffixes);
	}
	
	/**
	 * Returns the suffix (plus all of its suffixes, if <tt>allSuffixes</tt> is true) found by
	 * the binary search access sequence transformation.
	 * @param ceQuery the counterexample query
	 * @param asTransformer the access sequence transformer
	 * @param hypOutput interface to the hypothesis output
	 * @param oracle interface to the SUL output
	 * @param allSuffixes whether or not to include all suffixes of the found suffix
	 * @return the distinguishing suffixes
	 * @see LocalSuffixFinders#findRivestSchapire(Query, AccessSequenceTransformer, SuffixOutput, MembershipOracle)
	 */
	public static <I,O> List<? extends Word<I>> findRivestSchapire(Query<I,O> ceQuery,
			AccessSequenceTransformer<I> asTransformer,
			SuffixOutput<I,O> hypOutput,
			MembershipOracle<I, O> oracle,
			boolean allSuffixes) {
		int idx = LocalSuffixFinders.findRivestSchapire(ceQuery, asTransformer, hypOutput, oracle);
		return suffixesForLocalOutput(ceQuery, idx, allSuffixes);
	}
	
	
	/**
	 * Transforms a suffix index returned by a {@link LocalSuffixFinder} into a list containing
	 * the single distinguishing suffix.
	 */
	public static <I,O> List<? extends Word<I>> suffixesForLocalOutput(Query<I,O> ceQuery,
			int localSuffixIdx) {
		return suffixesForLocalOutput(ceQuery, localSuffixIdx, false);
	}
	
	/**
	 * Transforms a suffix index returned by a {@link LocalSuffixFinder} into a list of distinguishing
	 * suffixes. This list always contains the corresponding local suffix. Since local suffix finders
	 * only return a single suffix, suffix-closedness of the set of distinguishing suffixes
	 * might not be preserved. Note that for correctly implemented local suffix finders, this
	 * does not impair correctness of the learning algorithm. However, without suffix closedness,
	 * intermediate hypothesis models might be non-canonical, if no additional precautions
	 * are taken. For that reasons, the <tt>allSuffixes</tt> parameter can be specified to control
	 * whether or not the list returned by
	 * {@link GlobalSuffixFinder#findSuffixes(Query, AccessSequenceTransformer, SuffixOutput, MembershipOracle)}
	 * of the returned global suffix finder should not only contain the single suffix, but also
	 * all of its suffixes, ensuring suffix-closedness.
	 */
	public static <I,O> List<? extends Word<I>> suffixesForLocalOutput(Query<I,O> ceQuery,
			int localSuffixIdx, boolean allSuffixes) {
		
		if(localSuffixIdx == -1)
			return Collections.emptyList();
		
		Word<I> suffix = ceQuery.getInput().subWord(localSuffixIdx);
		
		if(!allSuffixes)
			return Collections.singletonList(suffix);
		
		return suffix.suffixes(false);
	}
	
	
	@SuppressWarnings("unchecked")
	public static GlobalSuffixFinder<Object,Object>[] values() {
		return new GlobalSuffixFinder[]{
			MAHLER_PNUELI,
			SHAHBAZ,
			FIND_LINEAR,
			FIND_LINEAR_ALLSUFFIXES,
			FIND_LINEAR_REVERSE,
			FIND_LINEAR_REVERSE_ALLSUFFIXES,
			RIVEST_SCHAPIRE,
			RIVEST_SCHAPIRE_ALLSUFFIXES
		};
	}

	// Prevent inheritance
	private GlobalSuffixFinders() {}
	
}
