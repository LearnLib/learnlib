package de.learnlib.algorithms.angluin;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.FastDFA;
import net.automatalib.automata.fsa.impl.FastDFAState;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The internal storage mechanism for {@link Angluin}.
 *
 * @param <S>
 * 		state class.
 */
public class ObservationTable<S> {

	private List<Word<S>> states;     // S
	private List<Word<S>> candidates; // SA
	private List<Word<S>> suffixes;   // E

	private Map<Word<S>, Boolean> results;

	public ObservationTable() {
		Word<S> emptyWord = Word.epsilon();

		states = new ArrayList<>();
		states.add(emptyWord);

		candidates = new ArrayList<>();

		suffixes = new ArrayList<>();
		suffixes.add(emptyWord);

		results = new HashMap<>();
	}

	/**
	 * The set of states in the observation table, often called "S".
	 *
	 * @return The set of states.
	 */
	List<Word<S>> getStates() {
		return states;
	}

	/**
	 * The set of states in the observation table, often called "SA" or "S Sigma".
	 *
	 * @return The set of candidates.
	 */
	List<Word<S>> getCandidates() {
		return candidates;
	}

	/**
	 * The set of suffixes in the observation table, often called "E".
	 *
	 * @return The set of candidates.
	 */
	List<Word<S>> getSuffixes() {
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
	void addResult(Word<S> prefix, Word<S> suffix, boolean result) {
		if (!suffixes.contains(suffix)) {
			throw new IllegalStateException("Suffix " + suffix + " is not part of the suffixes set");
		}

		Word<S> word = prefix.concat(suffix);
		if (results.containsKey(word) && results.get(word) != result) {
			throw new IllegalStateException(
					"New result " + results.get(word) + " differs from old result " + result);
		}
		else {
			results.put(word, result);
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
	Word<S> findUnclosedState() {
		List<ObservationTableRow> stateRows = new ArrayList<>(states.size());

		for (Word<S> state : states) {
			stateRows.add(getRowForPrefix(state));
		}

		for (Word<S> candidate : candidates) {
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
	 * @return if the observation table is consistent with the given alphabet.
	 */
	boolean isConsistentWithAlphabet(Alphabet<S> alphabet) {
		return findInconsistentSymbol(alphabet) == null;
	}

	InconsistencyDataHolder<S> findInconsistentSymbol(Alphabet<S> alphabet) {
		for (S symbol : alphabet) {
			for (Word<S> firstState : states) {
				for (Word<S> secondState : states) {
					if (firstState.equals(secondState)) {
						continue;
					}

					if (checkInconsistency(firstState, secondState, symbol)) {
						return new InconsistencyDataHolder<>(firstState, secondState, symbol);
					}
				}
			}
		}

		return null;
	}

	private boolean checkInconsistency(Word<S> firstState, Word<S> secondState, S alphabetSymbol) {
		ObservationTableRow rowForFirstState = getRowForPrefix(firstState);
		ObservationTableRow rowForSecondState = getRowForPrefix(secondState);
		boolean valuesEqualWithoutPrefix = rowForFirstState.equals(rowForSecondState);

		Word<S> extendedFirstState = firstState.append(alphabetSymbol);
		Word<S> extendedSecondState = secondState.append(alphabetSymbol);
		ObservationTableRow rowForExtendedFirstState = getRowForPrefix(extendedFirstState);
		ObservationTableRow rowForExtendedSecondState = getRowForPrefix(extendedSecondState);

		boolean valuesEqualWithPrefix = rowForExtendedFirstState.equals(rowForExtendedSecondState);

		return valuesEqualWithoutPrefix && !valuesEqualWithPrefix;
	}

	Word<S> determineWitnessForInconsistency(InconsistencyDataHolder<S> dataHolder) {
		if (dataHolder == null) {
			throw new IllegalArgumentException("Dataholder must not be null!");
		}

		Word<S> firstState = dataHolder.getFirstState().append(dataHolder.getDifferingSymbol());
		Word<S> secondState = dataHolder.getSecondState().append(dataHolder.getDifferingSymbol());

		ObservationTableRow firstRow = getRowForPrefix(firstState);
		ObservationTableRow secondRow = getRowForPrefix(secondState);

		for (int i = 0; i < firstRow.getValues().size(); i++) {
			if (firstRow.getValues().get(i) != secondRow.getValues().get(i)) {
				return suffixes.get(i);
			}
		}

		throw new IllegalStateException("Both rows are identical, unable to determine a witness!");
	}

	private ObservationTableRow getRowForPrefix(Word<S> state) {
		ObservationTableRow row = new ObservationTableRow();

		for (Word<S> suffix : suffixes) {
			row.addValue(results.get(state.concat(suffix)));
		}

		return row;
	}

	/**
	 * Creates a hypothesis automaton based on the current state of the observation table.
	 *
	 * @param alphabet
	 * 		The alphabet of the automaton.
	 * @return The current hypothesis automaton.
	 */
	DFA toAutomaton(Alphabet<S> alphabet) {
		FastDFA<S> automaton = new FastDFA<>(alphabet);
		Map<ObservationTableRow, FastDFAState> dfaStates = new HashMap<>(
				(int) (1.5 * states.size()));

		for (Word<S> state : states) {
			FastDFAState dfaState;

			if (state.isEmpty()) {
				dfaState = automaton.addInitialState();
			}
			else {
				dfaState = automaton.addState();
			}

			dfaState.setAccepting(results.get(state));
			dfaStates.put(getRowForPrefix(state), dfaState);
		}

		for (Word<S> state : states) {
			FastDFAState dfaState = dfaStates.get(getRowForPrefix(state));
			for (S alphabetSymbol : alphabet) {
				Word<S> word = state.append(alphabetSymbol);

				final int index = alphabet.getSymbolIndex(alphabetSymbol);
				dfaState.setTransition(index, dfaStates.get(getRowForPrefix(word)));
			}
		}

		return automaton;
	}
}
