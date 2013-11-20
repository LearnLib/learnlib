package de.learnlib.algorithms.dt;

import net.automatalib.automata.base.compact.AbstractCompactDeterministic;
import net.automatalib.words.Alphabet;
import de.learnlib.api.LearningAlgorithm;
import de.learnlib.api.MembershipOracle;
import de.learnlib.counterexamples.LocalSuffixFinder;
import de.learnlib.oracles.DefaultQuery;

public class DTLearner<M, A extends AbstractCompactDeterministic<I, ?, ?, ?>,I, O> implements LearningAlgorithm<M, I, O>{
	
	protected final Alphabet<I> alphabet;
	protected final MembershipOracle<I, O> oracle;
	protected final LocalSuffixFinder<I, O> suffixFinder;
	protected final A hypothesis;

	public DTLearner(Alphabet<I> alphabet, MembershipOracle<I, O> oracle, LocalSuffixFinder<I,O> suffixFinder) {
		this.alphabet = alphabet;
		this.oracle = oracle;
		this.suffixFinder = suffixFinder;
		hypothesis.se
	}

	@Override
	public void startLearning() {
		
	}

	@Override
	public boolean refineHypothesis(DefaultQuery<I, O> ceQuery) {
		suffixFinder.findSuffixIndex(ceQuery, this, hypOutput, oracle)
	}

	@Override
	public M getHypothesisModel() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
