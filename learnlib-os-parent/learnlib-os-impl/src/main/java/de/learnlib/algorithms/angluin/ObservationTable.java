package de.learnlib.algorithms.angluin;

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
		return findUnclosedState() != null;
	}

	Word<S> findUnclosedState() {
		List<ObservationTableRow> stateRows = new ArrayList<ObservationTableRow>(states.size());

		for (Word<S> state : states) {
			stateRows.add(getRowForPrefix(state));
		}

		for (Word<S> future : futures) {
			ObservationTableRow row = getRowForPrefix(future);
			for (ObservationTableRow stateRow : stateRows) {
				if (!row.equals(stateRow)) {
					return future;
				}
			}
		}

		return null;
	}

	boolean isConsistent() {
		return false;
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
}
