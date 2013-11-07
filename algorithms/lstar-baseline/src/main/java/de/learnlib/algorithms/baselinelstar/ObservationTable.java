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
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.FastDFA;
import net.automatalib.automata.fsa.impl.FastDFAState;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

/**
 * The internal storage mechanism for {@link BaselineLStar}.
 *
 * @param <I>
 * 		input symbol class.
 */
public class ObservationTable<I> {

	private LinkedHashSet<Word<I>> states;     // S
	private LinkedHashSet<Word<I>> candidates; // SA
	private List<Word<I>> suffixes;            // E

	private Map<Word<I>, ObservationTableRow> rows;

	public ObservationTable() {
		Word<I> emptyWord = Word.epsilon();

		states = new LinkedHashSet<>();
		states.add(emptyWord);

		candidates = new LinkedHashSet<>();

		suffixes = new ArrayList<>();
		suffixes.add(emptyWord);

		rows = new HashMap<>();
	}

	/**
	 * The set of states in the observation table, often called "S".
	 *
	 * @return The set of states.
	 */
	LinkedHashSet<Word<I>> getStates() {
		return states;
	}

	/**
	 * The set of states in the observation table, often called "SA" or "S Sigma".
	 *
	 * @return The set of candidates.
	 */
	LinkedHashSet<Word<I>> getCandidates() {
		return candidates;
	}

	/**
	 * The set of suffixes in the observation table, often called "E".
	 *
	 * @return The set of candidates.
	 */
	List<Word<I>> getSuffixes() {
		return suffixes;
	}

	/**
	 * Adds the result of a membership query to this table.
	 *
	 * @param prefix
	 * 		The prefix of the {@link Word} asked with the membership query.
	 * @param suffix
	 * 		The prefix of the {@link Word} asked with the membership query.
	 * @param result
	 * 		The result of the query.
	 */
	void addResult(Word<I> prefix, Word<I> suffix, boolean result) {
		if (!suffixes.contains(suffix)) {
			throw new IllegalStateException("Suffix " + suffix + " is not part of the suffixes set");
		}

		final int suffixPosition = suffixes.indexOf(suffix);

		if (rows.containsKey(prefix)) {
			addResultToRow(result, suffixPosition, rows.get(prefix));
		}
		else {
			if (suffixPosition > 0) {
				throw new IllegalStateException("Unable to set position " + suffixPosition + " for an empty row.");
			}

			ObservationTableRow row = new ObservationTableRow();
			row.addValue(result);

			rows.put(prefix, row);
		}
	}

	private static void addResultToRow(boolean result, int suffixPosition, ObservationTableRow row) {
		final List<Boolean> values = row.getValues();
		if (values.size() > suffixPosition) {
			if (values.get(suffixPosition) != result) {
				throw new IllegalStateException(
						"New result " + values.get(suffixPosition) + " differs from old result " + result);
			}
		}
		else {
			row.addValue(result);
		}
	}

	/**
	 * @return if the table is currently closed.
	 */
	boolean isClosed() {
		return findUnclosedState() == null;
	}

	/**
	 * Determines the next state for which the observation table needs to be closed.
	 *
	 * @return The next state for which the observation table needs to be closed. If the
	 *         table is closed, this returns {@code null}.
	 */
	Word<I> findUnclosedState() {
		List<ObservationTableRow> stateRows = new ArrayList<>(states.size());

		for (Word<I> state : states) {
			stateRows.add(getRowForPrefix(state));
		}

		for (Word<I> candidate : candidates) {
			boolean found = false;

			ObservationTableRow row = getRowForPrefix(candidate);
			for (ObservationTableRow stateRow : stateRows) {
				if (row.equals(stateRow)) {
					found = true;
					break;
				}
			}

			if (!found) {
				return candidate;
			}
		}

		return null;
	}

	/**
	 * @param alphabet
	 * 		The {@link Alphabet} for which the consistency is checked
	 * @return if the observation table is consistent with the given alphabet.
	 */
	boolean isConsistentWithAlphabet(Alphabet<I> alphabet) {
		return findInconsistentSymbol(alphabet) == null;
	}

	InconsistencyDataHolder<I> findInconsistentSymbol(Alphabet<I> alphabet) {
		List<Word<I>> allStates = new ArrayList<>(states);

		for (I symbol : alphabet) {
			for (int firstStateCounter = 0; firstStateCounter < states.size(); firstStateCounter++) {
				Word<I> firstState = allStates.get(firstStateCounter);

				for (int secondStateCounter = firstStateCounter + 1; secondStateCounter < states.size();
				     secondStateCounter++) {
					Word<I> secondState = allStates.get(secondStateCounter);

					if (checkInconsistency(firstState, secondState, symbol)) {
						return new InconsistencyDataHolder<>(firstState, secondState, symbol);
					}
				}
			}
		}

		return null;
	}

	private boolean checkInconsistency(Word<I> firstState, Word<I> secondState, I alphabetSymbol) {
		ObservationTableRow rowForFirstState = getRowForPrefix(firstState);
		ObservationTableRow rowForSecondState = getRowForPrefix(secondState);

		if (!rowForFirstState.equals(rowForSecondState)) {
			return false;
		}

		Word<I> extendedFirstState = firstState.append(alphabetSymbol);
		Word<I> extendedSecondState = secondState.append(alphabetSymbol);
		ObservationTableRow rowForExtendedFirstState = getRowForPrefix(extendedFirstState);
		ObservationTableRow rowForExtendedSecondState = getRowForPrefix(extendedSecondState);

		return !rowForExtendedFirstState.equals(rowForExtendedSecondState);
	}

	Word<I> determineWitnessForInconsistency(InconsistencyDataHolder<I> dataHolder) {
		if (dataHolder == null) {
			throw new IllegalArgumentException("Dataholder must not be null!");
		}

		Word<I> firstState = dataHolder.getFirstState().append(dataHolder.getDifferingSymbol());
		Word<I> secondState = dataHolder.getSecondState().append(dataHolder.getDifferingSymbol());

		ObservationTableRow firstRow = getRowForPrefix(firstState);
		ObservationTableRow secondRow = getRowForPrefix(secondState);

		for (int i = 0; i < firstRow.getValues().size(); i++) {
			if (firstRow.getValues().get(i) != secondRow.getValues().get(i)) {
				return suffixes.get(i);
			}
		}

		throw new IllegalStateException("Both rows are identical, unable to determine a witness!");
	}

	ObservationTableRow getRowForPrefix(Word<I> state) {
		return rows.get(state);
	}

	/**
	 * Creates a hypothesis automaton based on the current state of the observation table.
	 *
	 * @param alphabet
	 * 		The alphabet of the automaton.
	 * @return The current hypothesis automaton.
	 */
	DFA<?, I> toAutomaton(Alphabet<I> alphabet) {
		FastDFA<I> automaton = new FastDFA<>(alphabet);
		Map<ObservationTableRow, FastDFAState> dfaStates = new HashMap<>((int) (1.5 * states.size()));

		for (Word<I> state : states) {
			if (dfaStates.containsKey(getRowForPrefix(state))) {
				continue;
			}

			FastDFAState dfaState;

			if (state.isEmpty()) {
				dfaState = automaton.addInitialState();
			}
			else {
				dfaState = automaton.addState();
			}

			Word<I> emptyWord = Word.epsilon();
			int positionOfEmptyWord = suffixes.indexOf(emptyWord);
			dfaState.setAccepting(rows.get(state).getValues().get(positionOfEmptyWord));
			dfaStates.put(getRowForPrefix(state), dfaState);
		}

		for (Word<I> state : states) {
			FastDFAState dfaState = dfaStates.get(getRowForPrefix(state));
			for (I alphabetSymbol : alphabet) {
				Word<I> word = state.append(alphabetSymbol);

				final int index = alphabet.getSymbolIndex(alphabetSymbol);
				dfaState.setTransition(index, dfaStates.get(getRowForPrefix(word)));
			}
		}

		return automaton;
	}
}
