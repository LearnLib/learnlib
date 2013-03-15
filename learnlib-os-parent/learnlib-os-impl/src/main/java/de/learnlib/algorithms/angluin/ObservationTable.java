package de.learnlib.algorithms.angluin;

import de.ls5.words.Word;
import de.ls5.words.impl.ArrayWord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObservationTable<S, O> {

	private List<Word<S>> states;   // S
	private List<Word<S>> futures;  // SA
	private List<Word<S>> suffixes; // E

	private Map<Word<S>, O> results;


	public ObservationTable() {
		states = new ArrayList<Word<S>>();
		futures = new ArrayList<Word<S>>();
		suffixes = new ArrayList<Word<S>>();
		results = new HashMap<Word<S>, O>();
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

	void addResult(Word<S> stateOrFuture, Word<S> suffix, O result) {
		if (!suffixes.contains(suffix)) {
			throw new IllegalStateException("Suffix " + suffix + " is not part of the suffixes set");
		}

		Word<S> word = new ArrayWord<S>();
		word.addAll(stateOrFuture);
		word.addAll(suffix);

		if (results.containsKey(word) && !results.get(word).equals(result)) {
			throw new IllegalStateException("New result " + results.get(word) + " differs from old result " + result);
		}
		else {
			results.put(word, result);
		}
	}

}
