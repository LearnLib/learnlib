package de.learnlib.algorithms.ttt;

import net.automatalib.words.Word;
import de.learnlib.algorithms.ttt.hypothesis.HypothesisState;
import de.learnlib.oracles.AbstractQuery;

public abstract class StatePropertyQuery<I, O, SP> extends AbstractQuery<I, O> {
	
	protected final HypothesisState<I, O, SP, ?> state;

	public StatePropertyQuery(HypothesisState<I,O,SP,?> state, Word<I> prefix, Word<I> suffix) {
		super(prefix, suffix);
		this.state = state;
	}


	/*
	 * (non-Javadoc)
	 * @see de.learnlib.api.Query#answer(java.lang.Object)
	 */
	@Override
	public void answer(O output) {
		SP property = extractProperty(output);
		state.setProperty(property);
	}
	
	protected abstract SP extractProperty(O output);

}
