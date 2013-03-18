package de.learnlib.algorithms.angluin;

import de.learnlib.api.LearningAlgorithm;
import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;
import de.ls5.automata.Automaton;
import de.ls5.words.Alphabet;
import de.ls5.words.Word;
import de.ls5.words.impl.ArrayWord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Angluin<S> implements LearningAlgorithm {

	private final Alphabet<S> alphabet;
	private final List<Word<S>> alphabetAsWords;

	private final MembershipOracle<S, Boolean> oracle;

	private ObservationTable<S> observationTable;

	public Angluin(Alphabet<S> alphabet, MembershipOracle<S, Boolean> oracle) {
		this.alphabet = alphabet;
		this.alphabetAsWords = alphabetSymbolsAsWords();
		this.oracle = oracle;
		this.observationTable = new ObservationTable<S>();

		observationTable.getFutures().addAll(alphabetAsWords);
	}

	@Override
	public Automaton createHypothesis() {

		if (observationTable.getStates().isEmpty()) {
			final ArrayWord<S> emptyWord = new ArrayWord<S>();
			observationTable.getStates().add(emptyWord);
		}

		processMembershipQueriesForStates(observationTable.getStates(), observationTable.getSuffixes());
		processMembershipQueriesForStates(observationTable.getFutures(), observationTable.getSuffixes());

		while (!observationTable.isClosed() || !observationTable.isConsistentWithAlphabet(alphabetAsWords)) {
			if (!observationTable.isClosed()) {
				closeTable();
			}
			if (!observationTable.isConsistentWithAlphabet(alphabetAsWords)) {
				ensureConsistency();
			}
		}

		return null;
	}

	private void closeTable() {
		Word<S> future = observationTable.findUnclosedState();

		if (future == null) {
			return;
		}

		observationTable.getStates().add(future);
		observationTable.getFutures().remove(future);

		List<Word<S>> newFutures = new ArrayList<Word<S>>(observationTable.getSuffixes().size());
		for (Word<S> suffix : observationTable.getSuffixes()) {
			Word<S> newFuture = new ArrayWord<S>();
			newFuture.addAll(future);
			newFuture.addAll(suffix);
			newFutures.add(newFuture);
		}

		observationTable.getFutures().addAll(newFutures);

		processMembershipQueriesForStates(newFutures, observationTable.getSuffixes());
	}

	private void ensureConsistency() {
		InconsistencyDataHolder<S> dataHolder = observationTable.findInconsistentSymbol(alphabetAsWords);

		Word<S> witness = observationTable.determineWitnessForInconsistency(dataHolder);
		CombinedWord<S> newSuffix = new CombinedWord<S>(dataHolder.getDifferingSymbol(), witness);
		observationTable.getSuffixes().add(newSuffix.getWord());

		List<Word<S>> singleSuffixList = Collections.singletonList(newSuffix.getWord());

		processMembershipQueriesForStates(observationTable.getStates(), singleSuffixList);
		processMembershipQueriesForStates(observationTable.getFutures(), singleSuffixList);
	}

	private void processMembershipQueriesForStates(List<Word<S>> states, List<Word<S>> suffixes) {
		List<Query<S, Boolean>> queries = new ArrayList<Query<S, Boolean>>(states.size());
		for (Word<S> newFuture : states) {
			for (Word<S> suffix : suffixes) {
				CombinedWord<S> combinedWord = new CombinedWord<S>(newFuture, suffix);
				queries.add(new Query<S, Boolean>(combinedWord.getWord()));
			}
		}

		oracle.processQueries(queries);

		Map<Word, Boolean> results = new HashMap<Word, Boolean>((int) (1.5 * queries.size()));

		for (Query<S, Boolean> query : queries) {
			results.put(query.getInput(), query.getOutput());
		}

		for (Word<S> suffix : suffixes) {
			for (Word<S> newFuture : states) {
				CombinedWord<S> combinedWord = new CombinedWord<S>(newFuture, suffix);
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
	public Object refineHypothesis(Word counterexample, Object output) {
		return null;
	}

}
