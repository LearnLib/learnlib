/* Copyright (C) 2013 TU Dortmund
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
package de.learnlib.algorithms.baselinelstar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import com.github.misberner.buildergen.annotations.GenerateBuilder;
import com.google.common.collect.Maps;

import de.learnlib.algorithms.features.globalsuffixes.GlobalSuffixLearner.GlobalSuffixLearnerDFA;
import de.learnlib.algorithms.features.observationtable.OTLearner;
import de.learnlib.api.MembershipOracle;
import de.learnlib.oracles.DefaultQuery;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.FastDFA;
import net.automatalib.automata.fsa.impl.FastDFAState;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

/**
 * Implementation of the L* algorithm by Dana Angluin
 *
 * @param <I>
 * 		input symbol class.
 */
public class BaselineLStar<I> implements OTLearner<DFA<?, I>, I, Boolean>, GlobalSuffixLearnerDFA<I> {

	@Nonnull
	private final Alphabet<I> alphabet;

	@Nonnull
	private final MembershipOracle<I, Boolean> oracle;

	@Nonnull
	private final ObservationTable<I> observationTable;

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
	@GenerateBuilder
	public BaselineLStar(@Nonnull Alphabet<I> alphabet, @Nonnull MembershipOracle<I, Boolean> oracle) {
		this.alphabet = alphabet;
		this.oracle = oracle;
		this.observationTable = new ObservationTable<>();

		for (I alphabetSymbol : alphabet) {
			observationTable.addLongPrefix(Word.fromLetter(alphabetSymbol));
		}
	}

	@Override
	public void startLearning() {
		if (startLearningAlreadyCalled) {
			throw new IllegalStateException("startLearning may only be called once!");
		}

		final List<Word<I>> allSuffixes = observationTable.getSuffixes();

		processMembershipQueriesForStates(observationTable.getShortPrefixLabels(), allSuffixes);
		processMembershipQueriesForStates(observationTable.getLongPrefixLabels(), allSuffixes);

		makeTableClosedAndConsistent();

		startLearningAlreadyCalled = true;
	}

	@Override
	public boolean refineHypothesis(@Nonnull DefaultQuery<I, Boolean> ceQuery) {
		if (!startLearningAlreadyCalled) {
			throw new IllegalStateException("Unable to refine hypothesis before first learn iteration!");
		}

		LinkedHashSet<Word<I>> prefixes = prefixesOfWordNotInStates(ceQuery.getInput());

		for (Word<I> prefix : prefixes) {
			observationTable.addShortPrefix(prefix);
		}

		observationTable.removeShortPrefixesFromLongPrefixes();

		LinkedHashSet<Word<I>> newCandidates = getNewCandidatesFromPrefixes(prefixes);

		for (Word<I> candidate : newCandidates) {
			observationTable.addLongPrefix(candidate);
		}

		processMembershipQueriesForStates(prefixes, observationTable.getSuffixes());
		processMembershipQueriesForStates(newCandidates, observationTable.getSuffixes());

		makeTableClosedAndConsistent();

		return true;
	}

	@Nonnull
	private LinkedHashSet<Word<I>> prefixesOfWordNotInStates(@Nonnull Word<I> word) {
		List<Word<I>> states = observationTable.getShortPrefixLabels();

		LinkedHashSet<Word<I>> prefixes = new LinkedHashSet<>();
		for (Word<I> prefix : word.prefixes(false)) {
			if (!states.contains(prefix)) {
				prefixes.add(prefix);
			}
		}

		return prefixes;
	}

	@Nonnull
	private LinkedHashSet<Word<I>> getNewCandidatesFromPrefixes(@Nonnull LinkedHashSet<Word<I>> prefixes) {
		LinkedHashSet<Word<I>> newCandidates = new LinkedHashSet<>();

		for (Word<I> prefix : prefixes) {
			Set<Word<I>> possibleCandidates = appendAlphabetSymbolsToWord(prefix);
			for (Word<I> possibleCandidate :possibleCandidates) {
				if (!observationTable.getShortPrefixLabels().contains(possibleCandidate)) {
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
	@Nonnull
	private LinkedHashSet<Word<I>> appendAlphabetSymbolsToWord(@Nonnull Word<I> word) {
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

		FastDFA<I> automaton = new FastDFA<>(alphabet);
		Map<List<Boolean>, FastDFAState> dfaStates = Maps.newHashMapWithExpectedSize(
				observationTable.getShortPrefixRows().size());

		for (ObservationTableRow<I> stateRow : observationTable.getShortPrefixRows()) {
			if (dfaStates.containsKey(stateRow.getContents())) {
				continue;
			}

			FastDFAState dfaState;

			if (stateRow.getLabel().isEmpty()) {
				dfaState = automaton.addInitialState();
			}
			else {
				dfaState = automaton.addState();
			}

			Word<I> emptyWord = Word.epsilon();
			int positionOfEmptyWord = observationTable.getSuffixes().indexOf(emptyWord);
			dfaState.setAccepting(stateRow.getContents().get(positionOfEmptyWord));
			dfaStates.put(stateRow.getContents(), dfaState);
		}

		for (ObservationTableRow<I> stateRow : observationTable.getShortPrefixRows()) {
			FastDFAState dfaState = dfaStates.get(stateRow.getContents());
			for (I alphabetSymbol : alphabet) {
				Word<I> word = stateRow.getLabel().append(alphabetSymbol);

				final int index = alphabet.getSymbolIndex(alphabetSymbol);
				dfaState.setTransition(index, dfaStates.get(observationTable.getRowForPrefix(word).getContents()));
			}
		}

		return automaton;
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
			observationTable.moveLongPrefixToShortPrefixes(candidate);

			LinkedHashSet<Word<I>> newCandidates = appendAlphabetSymbolsToWord(candidate);
			for (Word<I> newCandidate : newCandidates) {
				observationTable.addLongPrefix(newCandidate);
			}

			processMembershipQueriesForStates(newCandidates, observationTable.getSuffixes());

			candidate = observationTable.findUnclosedState();
		}
	}

	/**
	 * After calling this method the observation table is consistent.
	 */
	private void ensureConsistency() {
		InconsistencyDataHolder<I> dataHolder = observationTable.findInconsistentSymbol(alphabet);

		if (dataHolder == null) {
			// It seems like this method has been called without checking if table is inconsistent first
			return;
		}

		Word<I> witness = observationTable.determineWitnessForInconsistency(dataHolder);
		Word<I> newSuffix = Word.fromSymbols(dataHolder.getDifferingSymbol()).concat(witness);
		observationTable.addSuffix(newSuffix);

		List<Word<I>> singleSuffixList = Collections.singletonList(newSuffix);

		processMembershipQueriesForStates(observationTable.getShortPrefixLabels(), singleSuffixList);
		processMembershipQueriesForStates(observationTable.getLongPrefixLabels(), singleSuffixList);
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
	private void processMembershipQueriesForStates(@Nonnull Collection<Word<I>> states,
			@Nonnull Collection<? extends Word<I>> suffixes) {
		List<DefaultQuery<I, Boolean>> queries = new ArrayList<>(states.size());
		for (Word<I> label : states) {
			for (Word<I> suffix : suffixes) {
				queries.add(new DefaultQuery<I, Boolean>(label, suffix));
			}
		}

		oracle.processQueries(queries);

		
		for(DefaultQuery<I,Boolean> query : queries) {
			Word<I> state = query.getPrefix();
			Word<I> suffix = query.getSuffix();
			observationTable.addResult(state, suffix, query.getOutput());
		}
	}

	@Nonnull
	public String getStringRepresentationOfObservationTable() {
		return ObservationTablePrinter.getPrintableStringRepresentation(observationTable);
	}

	@Override
	@Nonnull
	public Collection<? extends Word<I>> getGlobalSuffixes() {
		return Collections.unmodifiableCollection(observationTable.getSuffixes());
	}

	@Override
	public boolean addGlobalSuffixes(@Nonnull Collection<? extends Word<I>> newGlobalSuffixes) {
		observationTable.getSuffixes().addAll(newGlobalSuffixes);

		int numStatesOld = observationTable.getShortPrefixRows().size();

		processMembershipQueriesForStates(observationTable.getShortPrefixLabels(), newGlobalSuffixes);
		processMembershipQueriesForStates(observationTable.getLongPrefixLabels(), newGlobalSuffixes);

		closeTable();

		return (observationTable.getShortPrefixRows().size() != numStatesOld);
	}

	@Override
	@Nonnull
	public de.learnlib.algorithms.features.observationtable.ObservationTable<I, Boolean> getObservationTable() {
		return observationTable;
	}
}
