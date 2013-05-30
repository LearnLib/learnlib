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
package de.learnlib.algorithms.baselinelstar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import de.learnlib.api.LearningAlgorithm;
import de.learnlib.api.MembershipOracle;
import de.learnlib.oracles.DefaultQuery;

/**
 * Implementation of the L* algorithm by Dana Angluin
 *
 * @param <I>
 * 		input symbol class.
 */
public class BaselineLStar<I> implements LearningAlgorithm<DFA<?, I>, I, Boolean> {

	private final Alphabet<I> alphabet;

	private final MembershipOracle<I, Boolean> oracle;

	private ObservationTable<I> observationTable;

	private boolean startLearningAlreadyCalled;

	/**
	 * Initializes a newly created baseline L* implementation. After this, the method
	 * {@link #startLearning()} may be called once.
	 *
	 * @param alphabet
	 * 		The {@link Alphabet} to learn.
	 * @param oracle
	 * 		The {@link MembershipOracle} which is used for membership queries.
	 */
	public BaselineLStar(Alphabet<I> alphabet, MembershipOracle<I, Boolean> oracle) {
		this.alphabet = alphabet;
		this.oracle = oracle;
		this.observationTable = new ObservationTable<>();

		LinkedHashSet<Word<I>> initialCandidates = observationTable.getCandidates();

		for (I alphabetSymbol : alphabet) {
			initialCandidates.add(Word.fromLetter(alphabetSymbol));
		}
	}

	@Override
	public void startLearning() {
		if (startLearningAlreadyCalled) {
			throw new IllegalStateException("startLearning may only be called once!");
		}

		final List<Word<I>> allSuffixes = observationTable.getSuffixes();

		processMembershipQueriesForStates(observationTable.getStates(), allSuffixes);
		processMembershipQueriesForStates(observationTable.getCandidates(), allSuffixes);

		makeTableClosedAndConsistent();

		startLearningAlreadyCalled = true;
	}

	@Override
	public boolean refineHypothesis(DefaultQuery<I, Boolean> ceQuery) {
		if (!startLearningAlreadyCalled) {
			throw new IllegalStateException("Unable to refine hypothesis before first learn iteration!");
		}

		LinkedHashSet<Word<I>> states = observationTable.getStates();
		LinkedHashSet<Word<I>> candidates = observationTable.getCandidates();

		LinkedHashSet<Word<I>> prefixes = prefixesOfWordNotInStates(ceQuery.getInput());

		states.addAll(prefixes);
		removeStatesFromCandidates();

		LinkedHashSet<Word<I>> newCandidates = getNewCandidatesFromPrefixes(prefixes);
		candidates.addAll(newCandidates);

		processMembershipQueriesForStates(prefixes, observationTable.getSuffixes());
		processMembershipQueriesForStates(newCandidates, observationTable.getSuffixes());

		makeTableClosedAndConsistent();

		return true;
	}

	private LinkedHashSet<Word<I>> prefixesOfWordNotInStates(Word<I> word) {
		LinkedHashSet<Word<I>> states = observationTable.getStates();

		LinkedHashSet<Word<I>> prefixes = new LinkedHashSet<>();
		for (Word<I> prefix : prefixesOfWord(word)) {
			if (!states.contains(prefix)) {
				prefixes.add(prefix);
			}
		}

		return prefixes;
	}

	private void removeStatesFromCandidates() {
		LinkedHashSet<Word<I>> states = observationTable.getStates();
		LinkedHashSet<Word<I>> candidates = observationTable.getCandidates();
		candidates.removeAll(states);
	}

	private LinkedHashSet<Word<I>> getNewCandidatesFromPrefixes(LinkedHashSet<Word<I>> prefixes) {
		LinkedHashSet<Word<I>> newCandidates = new LinkedHashSet<>();

		for (Word<I> prefix : prefixes) {
			Set<Word<I>> possibleCandidates = appendAlphabetSymbolsToWord(prefix);
			for (Word<I> possibleCandidate :possibleCandidates) {
				if (!observationTable.getStates().contains(possibleCandidate)) {
					newCandidates.add(possibleCandidate);
				}
			}
		}

		return newCandidates;
	}

	/**
	 * Appends each symbol of the alphabet (with size m) to the given word (with size w),
	 * thus returning m words with a length of w+1.
	 *
	 * @param word
	 *      The {@link Word} to which the {@link Alphabet} is appended.
	 * @return
	 *      A set with the size of the alphabet, containing each time the word
	 *      appended with an alphabet symbol.
	 */
	private LinkedHashSet<Word<I>> appendAlphabetSymbolsToWord(Word<I> word) {
		LinkedHashSet<Word<I>> newCandidates = new LinkedHashSet<>(alphabet.size());
		for (I alphabetSymbol : alphabet) {
			Word<I> newCandidate = word.append(alphabetSymbol);
			newCandidates.add(newCandidate);
		}
		return newCandidates;
	}

	@Override
	public DFA<?, I> getHypothesisModel() {
		if (!startLearningAlreadyCalled) {
			throw new IllegalStateException("Unable to get hypothesis model before first learn iteration!");
		}

		return observationTable.toAutomaton(alphabet);
	}

	/**
	 * After calling this method the observation table is both closed and consistent.
	 */
	private void makeTableClosedAndConsistent() {
		boolean closedAndConsistent = false;

		while (!closedAndConsistent) {
			closedAndConsistent = true;

			if (!observationTable.isClosed()) {
				closedAndConsistent = false;
				closeTable();
			}

			if (!observationTable.isConsistentWithAlphabet(alphabet)) {
				closedAndConsistent = false;
				ensureConsistency();
			}
		}
	}

	/**
	 * After calling this method the observation table is closed.
	 */
	private void closeTable() {
		Word<I> candidate = observationTable.findUnclosedState();

		while (candidate != null) {
			observationTable.getStates().add(candidate);
			observationTable.getCandidates().remove(candidate);

			LinkedHashSet<Word<I>> newCandidates = appendAlphabetSymbolsToWord(candidate);

			observationTable.getCandidates().addAll(newCandidates);

			processMembershipQueriesForStates(newCandidates, observationTable.getSuffixes());

			candidate = observationTable.findUnclosedState();
		}
	}

	/**
	 * After calling this method the observation table is consistent.
	 */
	private void ensureConsistency() {
		InconsistencyDataHolder<I> dataHolder = observationTable.findInconsistentSymbol(alphabet);

		Word<I> witness = observationTable.determineWitnessForInconsistency(dataHolder);
		Word<I> newSuffix = Word.fromSymbols(dataHolder.getDifferingSymbol()).concat(witness);
		observationTable.getSuffixes().add(newSuffix);

		List<Word<I>> singleSuffixList = Collections.singletonList(newSuffix);

		processMembershipQueriesForStates(observationTable.getStates(), singleSuffixList);
		processMembershipQueriesForStates(observationTable.getCandidates(), singleSuffixList);
	}

	/**
	 * When new states are added to the observation table, this method fills the table values. For each
	 * given state it sends one membership query for each specified suffix symbol to the oracle of the
	 * form (state,symbol).
	 *
	 * @param states
	 * 		The new states which should be evaluated.
	 * @param suffixes
	 * 		The suffixes which are appended to the states before sending the resulting word to the oracle.
	 */
	private void processMembershipQueriesForStates(LinkedHashSet<Word<I>> states, Collection<Word<I>> suffixes) {
		List<DefaultQuery<I, Boolean>> queries = new ArrayList<>(states.size());
		for (Word<I> state : states) {
			for (Word<I> suffix : suffixes) {
				queries.add(new DefaultQuery<I, Boolean>(state, suffix));
			}
		}

		oracle.processQueries(queries);

		
		for(DefaultQuery<I,Boolean> query : queries) {
			Word<I> state = query.getPrefix();
			Word<I> suffix = query.getSuffix();
			observationTable.addResult(state, suffix, query.getOutput());
		}
	}

	/**
	 * A {@link Word} of the length n may have n prefixes of the length 1-n.
	 * This method returns all of them.
	 *
	 * @param word
	 * 		The word for which the prefixes should be returned.
	 * @return A list of all prefixes for the given word.
	 */
	// FIXME: This is superseded by Word.suffixes(boolean). Please adapt -misberner
	private static <I> List<Word<I>> prefixesOfWord(Word<I> word) {
		List<Word<I>> prefixes = new ArrayList<>(word.size());
		for (int i = 1; i <= word.size(); i++) {
			prefixes.add(word.prefix(i));
		}
		return prefixes;
	}

	public String getStringRepresentationOfObservationTable() {
		return ObservationTablePrinter.getPrintableStringRepresentation(observationTable);
	}

}
