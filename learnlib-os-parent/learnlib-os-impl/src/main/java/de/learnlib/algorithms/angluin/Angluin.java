package de.learnlib.algorithms.angluin;

import de.learnlib.api.LearningAlgorithm;
import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Implementation of the L* algorithm by Dana Angluin
 *
 * @param <S>
 * 		state class.
 */
public class Angluin<S> implements LearningAlgorithm<DFA, S, Boolean> {

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

		for (S alphabetSymbol : alphabet) {
			observationTable.getCandidates().add(Word.fromSymbols(alphabetSymbol));
		}
	}

	@Override
	public void startLearning() {
		if (startLearningAlreadyCalled) {
			throw new IllegalStateException("startLearning may only be called once!");
		}

		processMembershipQueriesForStates(observationTable.getStates(), observationTable.getSuffixes());
		processMembershipQueriesForStates(observationTable.getCandidates(), observationTable.getSuffixes());

		makeTableClosedAndConsistent();

		startLearningAlreadyCalled = true;
	}

	@Override
	public boolean refineHypothesis(Query<S, Boolean> ceQuery) {
		if (!startLearningAlreadyCalled) {
			throw new IllegalStateException("Unable to refine hypothesis before first learn iteration!");
		}

		List<Word<S>> states = observationTable.getStates();
		List<Word<S>> candidates = observationTable.getCandidates();

		List<Word<S>> prefixes = new LinkedList<>();
		for (Word<S> prefix : prefixesOfWord(ceQuery.getInput())) {
			if (!states.contains(prefix)) {
				prefixes.add(prefix);
			}
		}

		states.addAll(prefixes);

		for (Word<S> state : states) {
			if (candidates.contains(state)) {
				candidates.remove(state);
			}
		}

		List<Word<S>> newCandidates = new LinkedList<>();

		for (Word<S> prefix : prefixes) {
			for (S alphabetSymbol : alphabet) {
				Word<S> word = prefix.append(alphabetSymbol);
				if (!states.contains(word)) {
					newCandidates.add(word);
				}
			}
		}

		processMembershipQueriesForStates(prefixes, observationTable.getSuffixes());
		processMembershipQueriesForStates(newCandidates, observationTable.getSuffixes());

		makeTableClosedAndConsistent();

		return true;
	}

	@Override
	public DFA getHypothesisModel() {
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

			List<Word<S>> newCandidates = new ArrayList<>(alphabet.size());
			for (S alphabetSymbol : alphabet) {
				Word<S> newCandidate = candidate.append(alphabetSymbol);
				newCandidates.add(newCandidate);
			}

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
		CombinedWord<S> newSuffix = new CombinedWord<>(Word.fromSymbols(dataHolder.getDifferingSymbol()), witness);
		observationTable.getSuffixes().add(newSuffix.getWord());

		List<Word<S>> singleSuffixList = Collections.singletonList(newSuffix.getWord());

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
	private void processMembershipQueriesForStates(List<Word<S>> states, List<Word<S>> suffixes) {
		List<Query<S, Boolean>> queries = new ArrayList<>(states.size());
		for (Word<S> state : states) {
			for (Word<S> suffix : suffixes) {
				CombinedWord<S> combinedWord = new CombinedWord<>(state, suffix);
				queries.add(new Query<S, Boolean>(combinedWord.getWord()));
			}
		}

		oracle.processQueries(queries);

		Map<Word, Boolean> results = new HashMap<>((int) (1.5 * queries.size()));

		for (Query<S, Boolean> query : queries) {
			results.put(query.getInput(), query.getOutput());
		}

		for (Word<S> suffix : suffixes) {
			for (Word<S> state : states) {
				CombinedWord<S> combinedWord = new CombinedWord<>(state, suffix);
				observationTable.addResult(combinedWord, results.get(combinedWord.getWord()));
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

}
