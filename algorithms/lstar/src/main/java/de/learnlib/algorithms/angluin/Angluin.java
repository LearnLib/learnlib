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
package de.learnlib.algorithms.angluin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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
 * @param <S>
 * 		state class.
 */
public class Angluin<S> implements LearningAlgorithm<DFA<?,S>, S, Boolean> {

	private final Alphabet<S> alphabet;

	private final MembershipOracle<S, Boolean> oracle;

	private ObservationTable<S> observationTable;

	private boolean startLearningAlreadyCalled;

	/**
	 * Initializes a newly created Angluin implementation. After this, the method
	 * {@link #startLearning()} may be called once.
	 *
	 * @param alphabet
	 * 		The {@link Alphabet} to learn.
	 * @param oracle
	 * 		The {@link MembershipOracle} which is used for membership queries.
	 */
	public Angluin(Alphabet<S> alphabet, MembershipOracle<S, Boolean> oracle) {
		this.alphabet = alphabet;
		this.oracle = oracle;
		this.observationTable = new ObservationTable<>();

		LinkedHashSet<Word<S>> initialCandidates = observationTable.getCandidates();

		for (S alphabetSymbol : alphabet) {
			initialCandidates.add(Word.fromSymbols(alphabetSymbol));
		}
	}

	@Override
	public void startLearning() {
		if (startLearningAlreadyCalled) {
			throw new IllegalStateException("startLearning may only be called once!");
		}

		final List<Word<S>> allSuffixes = observationTable.getSuffixes();

		processMembershipQueriesForStates(observationTable.getStates(), allSuffixes);
		processMembershipQueriesForStates(observationTable.getCandidates(), allSuffixes);

		makeTableClosedAndConsistent();

		startLearningAlreadyCalled = true;
	}

	@Override
	public boolean refineHypothesis(DefaultQuery<S, Boolean> ceQuery) {
		if (!startLearningAlreadyCalled) {
			throw new IllegalStateException("Unable to refine hypothesis before first learn iteration!");
		}

		LinkedHashSet<Word<S>> states = observationTable.getStates();
		LinkedHashSet<Word<S>> candidates = observationTable.getCandidates();

		LinkedHashSet<Word<S>> prefixes = prefixesOfWordNotInStates(ceQuery.getInput());

		states.addAll(prefixes);
		removeStatesFromCandidates();

		LinkedHashSet<Word<S>> newCandidates = getNewCandidatesFromPrefixes(prefixes);
		candidates.addAll(newCandidates);

		processMembershipQueriesForStates(prefixes, observationTable.getSuffixes());
		processMembershipQueriesForStates(newCandidates, observationTable.getSuffixes());

		makeTableClosedAndConsistent();

		return true;
	}

	private LinkedHashSet<Word<S>> prefixesOfWordNotInStates(Word<S> word) {
		LinkedHashSet<Word<S>> states = observationTable.getStates();

		LinkedHashSet<Word<S>> prefixes = new LinkedHashSet<>();
		for (Word<S> prefix : prefixesOfWord(word)) {
			if (!states.contains(prefix)) {
				prefixes.add(prefix);
			}
		}

		return prefixes;
	}

	private void removeStatesFromCandidates() {
		LinkedHashSet<Word<S>> states = observationTable.getStates();
		LinkedHashSet<Word<S>> candidates = observationTable.getCandidates();
		candidates.removeAll(states);
	}

	private LinkedHashSet<Word<S>> getNewCandidatesFromPrefixes(LinkedHashSet<Word<S>> prefixes) {
		LinkedHashSet<Word<S>> newCandidates = new LinkedHashSet<>();

		for (Word<S> prefix : prefixes) {
			Set<Word<S>> possibleCandidates = appendAlphabetSymbolsToWord(prefix);
			for (Word<S> possibleCandidate :possibleCandidates) {
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
	private LinkedHashSet<Word<S>> appendAlphabetSymbolsToWord(Word<S> word) {
		LinkedHashSet<Word<S>> newCandidates = new LinkedHashSet<>(alphabet.size());
		for (S alphabetSymbol : alphabet) {
			Word<S> newCandidate = word.append(alphabetSymbol);
			newCandidates.add(newCandidate);
		}
		return newCandidates;
	}

	@Override
	public DFA<?,S> getHypothesisModel() {
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
		Word<S> candidate = observationTable.findUnclosedState();

		while (candidate != null) {
			observationTable.getStates().add(candidate);
			observationTable.getCandidates().remove(candidate);

			LinkedHashSet<Word<S>> newCandidates = appendAlphabetSymbolsToWord(candidate);

			observationTable.getCandidates().addAll(newCandidates);

			processMembershipQueriesForStates(newCandidates, observationTable.getSuffixes());

			candidate = observationTable.findUnclosedState();
		}
	}

	/**
	 * After calling this method the observation table is consistent.
	 */
	private void ensureConsistency() {
		InconsistencyDataHolder<S> dataHolder = observationTable.findInconsistentSymbol(alphabet);

		Word<S> witness = observationTable.determineWitnessForInconsistency(dataHolder);
		Word<S> newSuffix = Word.fromSymbols(dataHolder.getDifferingSymbol()).concat(witness);
		observationTable.getSuffixes().add(newSuffix);

		List<Word<S>> singleSuffixList = Collections.singletonList(newSuffix);

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
	private void processMembershipQueriesForStates(LinkedHashSet<Word<S>> states, Collection<Word<S>> suffixes) {
		List<DefaultQuery<S, Boolean>> queries = new ArrayList<>(states.size());
		for (Word<S> state : states) {
			for (Word<S> suffix : suffixes) {
				Word<S> combinedWord = state.concat(suffix);
				queries.add(new DefaultQuery<S, Boolean>(combinedWord));
			}
		}

		oracle.processQueries(queries);

		Map<Word<S>, Boolean> results = new HashMap<>((int) (1.5 * queries.size()));

		for (DefaultQuery<S, Boolean> query : queries) {
			results.put(query.getInput(), query.getOutput());
		}

		for (Word<S> suffix : suffixes) {
			for (Word<S> state : states) {
				Word<S> combinedWord = state.concat(suffix);
				observationTable.addResult(state, suffix, results.get(combinedWord));
			}
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
	private List<Word<S>> prefixesOfWord(Word<S> word) {
		List<Word<S>> prefixes = new ArrayList<>(word.size());
		for (int i = 1; i <= word.size(); i++) {
			prefixes.add(word.prefix(i));
		}
		return prefixes;
	}

	public String getStringRepresentationOfObservationTable() {
		return ObservationTablePrinter.getPrintableStringRepresentation(observationTable);
	}

}
