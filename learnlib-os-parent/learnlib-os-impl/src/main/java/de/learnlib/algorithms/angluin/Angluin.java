package de.learnlib.algorithms.angluin;

import de.learnlib.api.LearningAlgorithm;
import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;
import de.ls5.automata.Automaton;
import de.ls5.words.Alphabet;
import de.ls5.words.Word;
import de.ls5.words.impl.ArrayWord;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Angluin<S> implements LearningAlgorithm {

	private final Alphabet<S> alphabet;

	private final MembershipOracle<S, Object> oracle;

	private ObservationTable<S, Object> observationTable;

	public Angluin(Alphabet<S> alphabet, MembershipOracle<S, Object> oracle) {
		this.alphabet = alphabet;
		this.oracle = oracle;
		this.observationTable = new ObservationTable<S, Object>();

		for (S alphabetSymbol : this.alphabet) {
			Word<S> word = new ArrayWord<S>();
			word.add(alphabetSymbol);
			observationTable.getFutures().add(word);
		}
	}

	@Override
	public Automaton createHypothesis() {

		if (observationTable.getStates().isEmpty()) {
			observationTable.getStates().add(new ArrayWord<S>());
		}

		List<Query<S, Object>> queries = new LinkedList<Query<S, Object>>();

		for (Word<S> suffix : observationTable.getSuffixes()) {
			for (Word<S> word : observationTable.getStates()) {
				queries.add(new Query<S, Object>(word, suffix));
			}

			for (Word<S> word : observationTable.getFutures()) {
				queries.add(new Query<S, Object>(word, suffix));
			}
		}

		oracle.processQueries(queries);

		Map<Word, Object> results = new HashMap<Word, Object>((int)(1.5 * queries.size()));

		for (Query<S, Object> query : queries) {
			results.put(query.getInput(), query.getOutput());
		}

		for (Word<S> suffix : observationTable.getSuffixes()) {
			for (Word<S> word : observationTable.getStates()) {
				observationTable.addResult(word, suffix, results.get(word));
			}

			for (Word<S> word : observationTable.getFutures()) {
				observationTable.addResult(word, suffix, results.get(word));
			}
		}

		return null;
	}

	@Override
	public Object refineHypothesis(Word counterexample, Object output) {
		return null;
	}

}
