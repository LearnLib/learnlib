package de.learnlib.algorithms.angluin;

import de.ls5.automata.Automaton;
import de.ls5.automata.fsa.impl.FastDFA;
import de.ls5.automata.fsa.impl.FastDFAState;
import de.ls5.words.Alphabet;
import de.ls5.words.Word;
import de.ls5.words.impl.ArrayWord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObservationTable<S> {

	private List<Word<S>> states;   // S
	private List<Word<S>> futures;  // SA
	private List<Word<S>> suffixes; // E

	private Map<Word<S>, Boolean> results;

	public ObservationTable() {
		states = new ArrayList<Word<S>>();
		futures = new ArrayList<Word<S>>();
		suffixes = new ArrayList<Word<S>>();
		results = new HashMap<Word<S>, Boolean>();
	}

	List<Word<S>> getStates() {
		return states;
	}

	List<Word<S>> getFutures() {
		return futures;
	}

	List<Word<S>> getSuffixes() {
		return suffixes;
	}

	void addResult(CombinedWord<S> word, boolean result) {
		if (!suffixes.contains(word.getSuffix())) {
			throw new IllegalStateException("Suffix " + word.getSuffix() + " is not part of the suffixes set");
		}

		if (results.containsKey(word.getWord()) && results.get(word.getWord()) != result) {
			throw new IllegalStateException(
					"New result " + results.get(word.getWord()) + " differs from old result " + result);
		}
		else {
			results.put(word.getWord(), result);
		}
	}

	boolean isClosed() {
		return findUnclosedState() == null;
	}

	Word<S> findUnclosedState() {
		List<ObservationTableRow> stateRows = new ArrayList<ObservationTableRow>(states.size());

		for (Word<S> state : states) {
			stateRows.add(getRowForPrefix(state));
		}

		for (Word<S> future : futures) {
			boolean found = false;

			ObservationTableRow row = getRowForPrefix(future);
			for (ObservationTableRow stateRow : stateRows) {
				if (row.equals(stateRow)) {
					found = true;
					break;
				}
			}

			if (!found) {
				return future;
			}
		}

		return null;
	}

	boolean isConsistentWithAlphabet(List<Word<S>> alphabetSymbols) {
		return findInconsistentSymbol(alphabetSymbols) == null;
	}

	InconsistencyDataHolder<S> findInconsistentSymbol(List<Word<S>> alphabetSymbols) {
		for (Word<S> symbol : alphabetSymbols) {
			for (Word<S> firstState : states) {
				for (Word<S> secondState : states) {
					if (firstState.equals(secondState)) {
						continue;
					}

					if (checkInconsistency(firstState, secondState, symbol)) {
						return new InconsistencyDataHolder<S>(firstState, secondState, symbol);
					}
				}
			}
		}

		return null;
	}

	private boolean checkInconsistency(Word<S> firstState, Word<S> secondState, Word<S> alphabetSymbol) {
		ObservationTableRow rowForFirstState = getRowForPrefix(firstState);
		ObservationTableRow rowForSecondState = getRowForPrefix(secondState);
		boolean valuesEqualWithoutPrefix = rowForFirstState.equals(rowForSecondState);

		CombinedWord<S> extendedFirstState = new CombinedWord<S>(firstState, alphabetSymbol);
		CombinedWord<S> extendedSecondState = new CombinedWord<S>(secondState, alphabetSymbol);
		ObservationTableRow rowForExtendedFirstState = getRowForPrefix(extendedFirstState.getWord());
		ObservationTableRow rowForExtendedSecondState = getRowForPrefix(extendedSecondState.getWord());

		boolean valuesEqualWithPrefix = rowForExtendedFirstState.equals(rowForExtendedSecondState);

		return valuesEqualWithoutPrefix && !valuesEqualWithPrefix;
	}

	Word<S> determineWitnessForInconsistency(InconsistencyDataHolder<S> dataHolder) {
		if (dataHolder == null) {
			throw new IllegalArgumentException("Dataholder must not be null!");
		}

		CombinedWord<S> firstState = new CombinedWord<S>(dataHolder.getFirstState(), dataHolder.getDifferingSymbol());
		CombinedWord<S> secondState = new CombinedWord<S>(dataHolder.getSecondState(), dataHolder.getDifferingSymbol());

		ObservationTableRow firstRow = getRowForPrefix(firstState.getWord());
		ObservationTableRow secondRow = getRowForPrefix(secondState.getWord());

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
			Word<S> word = new ArrayWord<S>();
			word.addAll(state);
			word.addAll(suffix);
			row.addValue(results.get(word));
		}

		return row;
	}

	Automaton toAutomaton(Alphabet<S> alphabet) {
		FastDFA<S> automaton = new FastDFA<S>(alphabet);
		Map<ObservationTableRow, FastDFAState> dfaStates = new HashMap<ObservationTableRow, FastDFAState>(
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
				Word<S> word = new ArrayWord<S>();
				word.addAll(state);
				word.add(alphabetSymbol);

				final int index = alphabet.getSymbolIndex(alphabetSymbol);
				dfaState.setTransition(index, dfaStates.get(getRowForPrefix(word)));
			}
		}

		return automaton;
	}
}
