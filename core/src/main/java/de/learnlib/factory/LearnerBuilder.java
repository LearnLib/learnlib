package de.learnlib.factory;

import net.automatalib.words.Alphabet;
import de.learnlib.api.LearningAlgorithm;
import de.learnlib.api.MembershipOracle;

public abstract class LearnerBuilder<M, I, O, B extends LearnerBuilder<M,I,O,B>> {
	
	
	protected Alphabet<I> alphabet;
	protected MembershipOracle<I,O> oracle;
	
	protected abstract B _this();
	
	public void setAlphabet(Alphabet<I> alphabet) {
		this.alphabet = alphabet;
	}
	
	public Alphabet<I> getAlphabet() {
		return alphabet;
	}
	
	public B withAlphabet(Alphabet<I> alphabet) {
		setAlphabet(alphabet);
		return _this();
	}
	
	public void setOracle(MembershipOracle<I,O> oracle) {
		this.oracle = oracle;
	}
	
	public MembershipOracle<I,O> getOracle() {
		return oracle;
	}
	
	public B withOracle(MembershipOracle<I, O> oracle) {
		setOracle(oracle);
		return _this();
	}
	
	
	public abstract LearningAlgorithm<M,I,O> create();
	
	
}
