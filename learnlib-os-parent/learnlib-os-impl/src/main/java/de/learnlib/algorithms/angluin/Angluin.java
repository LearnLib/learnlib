package de.learnlib.algorithms.angluin;

import de.learnlib.api.LearningAlgorithm;
import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;
import de.ls5.automata.fsa.DFA;
import de.ls5.words.Alphabet;
import de.ls5.words.Word;
import de.ls5.words.impl.ArrayWord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Angluin<S> implements LearningAlgorithm<DFA, S, Boolean> {

	private final Alphabet<S> alphabet;
	private final List<Word<S>> alphabetAsWords;

	private final MembershipOracle<S, Boolean> oracle;

	private ObservationTable<S> observationTable;

	public Angluin(Alphabet<S> alphabet, MembershipOracle<S, Boolean> oracle) {
		this.alphabet = alphabet;
		this.alphabetAsWords = alphabetSymbolsAsWords();
		this.oracle = oracle;
		this.observationTable = new ObservationTable<S>();

		observationTable.getCandidates().addAll(alphabetAsWords);
	}

	@Override
	public DFA createHypothesis() {
		if (observationTable.getStates().isEmpty()) {
			final ArrayWord<S> emptyWord = new ArrayWord<S>();
			observationTable.getStates().add(emptyWord);
		}

		if (observationTable.getSuffixes().isEmpty()) {
			final ArrayWord<S> emptyWord = new ArrayWord<S>();
			observationTable.getSuffixes().add(emptyWord);
		}

		processMembershipQueriesForStates(observationTable.getStates(), observationTable.getSuffixes());
		processMembershipQueriesForStates(observationTable.getCandidates(), observationTable.getSuffixes());

		boolean closedAndConsistent = false;

		while (!closedAndConsistent) {
			closedAndConsistent = true;

			if (!observationTable.isClosed()) {
				closedAndConsistent = false;
				closeTable();
				continue;
			}

			if (!observationTable.isConsistentWithAlphabet(alphabetAsWords)) {
				closedAndConsistent = false;
				ensureConsistency();
			}
		}

		return observationTable.toAutomaton(alphabet);
	}

	private void closeTable() {
		Word<S> candidate = observationTable.findUnclosedState();

		if (candidate == null) {
			return;
		}

		observationTable.getStates().add(candidate);
		observationTable.getCandidates().remove(candidate);

		List<Word<S>> newCandidates = new ArrayList<Word<S>>(alphabetAsWords.size());
		for (Word<S> alphabetSymbol : alphabetAsWords) {
			Word<S> newCandidate = new ArrayWord<S>();
			newCandidate.addAll(candidate);
			newCandidate.addAll(alphabetSymbol);
			newCandidates.add(newCandidate);
		}

		observationTable.getCandidates().addAll(newCandidates);

		processMembershipQueriesForStates(newCandidates, observationTable.getSuffixes());
	}

	private void ensureConsistency() {
		InconsistencyDataHolder<S> dataHolder = observationTable.findInconsistentSymbol(alphabetAsWords);

		Word<S> witness = observationTable.determineWitnessForInconsistency(dataHolder);
		CombinedWord<S> newSuffix = new CombinedWord<S>(dataHolder.getDifferingSymbol(), witness);
		observationTable.getSuffixes().add(newSuffix.getWord());

		List<Word<S>> singleSuffixList = Collections.singletonList(newSuffix.getWord());

		processMembershipQueriesForStates(observationTable.getStates(), singleSuffixList);
		processMembershipQueriesForStates(observationTable.getCandidates(), singleSuffixList);
	}

	private void processMembershipQueriesForStates(List<Word<S>> states, List<Word<S>> suffixes) {
		List<Query<S, Boolean>> queries = new ArrayList<Query<S, Boolean>>(states.size());
		for (Word<S> state : states) {
			for (Word<S> suffix : suffixes) {
				CombinedWord<S> combinedWord = new CombinedWord<S>(state, suffix);
				queries.add(new Query<S, Boolean>(combinedWord.getWord()));
			}
		}

		oracle.processQueries(queries);

		Map<Word, Boolean> results = new HashMap<Word, Boolean>((int) (1.5 * queries.size()));

		for (Query<S, Boolean> query : queries) {
			results.put(query.getInput(), query.getOutput());
		}

		for (Word<S> suffix : suffixes) {
			for (Word<S> state : states) {
				CombinedWord<S> combinedWord = new CombinedWord<S>(state, suffix);
				observationTable.addResult(combinedWord, results.get(combinedWord.getWord()));
			}
		}
	}


	private List<Word<S>> alphabetSymbolsAsWords() {
		List<Word<S>> words = new ArrayList<Word<S>>(alphabet.size());
		for (S symbol : alphabet) {
			Word<S> word = new ArrayWord<S>();
			word.add(symbol);
			words.add(word);
		}
		return words;
	}

	@Override
	public DFA refineHypothesis(Word<S> counterexample, Boolean output) {
		return null;
	}

}
