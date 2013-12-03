package de.learnlib.algorithms.discriminationtree.dfa;

import java.util.Collection;

import net.automatalib.automata.fsa.abstractimpl.AbstractDFA;
import de.learnlib.algorithms.discriminationtree.hypothesis.DTLearnerHypothesis;
import de.learnlib.algorithms.discriminationtree.hypothesis.HState;

final class HypothesisWrapperDFA<I> extends
		AbstractDFA<HState<I, Boolean, Boolean, Void>, I> {
	
	private final DTLearnerHypothesis<I, Boolean, Boolean, Void> dtHypothesis;
	
	public HypothesisWrapperDFA(DTLearnerHypothesis<I,Boolean,Boolean,Void> dtHypothesis) {
		this.dtHypothesis = dtHypothesis;
	}

	@Override
	public Collection<HState<I, Boolean, Boolean, Void>> getStates() {
		return dtHypothesis.getStates();
	}

	@Override
	public HState<I, Boolean, Boolean, Void> getInitialState() {
		return dtHypothesis.getInitialState();
	}

	@Override
	public HState<I, Boolean, Boolean, Void> getTransition(
			HState<I, Boolean, Boolean, Void> state, I input) {
		return dtHypothesis.getSuccessor(state, input);
	}

	@Override
	public boolean isAccepting(HState<I, Boolean, Boolean, Void> state) {
		return state.getProperty().booleanValue();
	}


}
