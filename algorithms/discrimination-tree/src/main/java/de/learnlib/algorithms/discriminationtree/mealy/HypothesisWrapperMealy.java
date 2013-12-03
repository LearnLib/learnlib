package de.learnlib.algorithms.discriminationtree.mealy;

import java.util.Collection;
import java.util.List;

import net.automatalib.automata.abstractimpl.AbstractDeterministicAutomaton;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.automata.transout.abstractimpl.AbstractTransOutAutomaton;
import net.automatalib.words.Word;
import de.learnlib.algorithms.discriminationtree.hypothesis.DTLearnerHypothesis;
import de.learnlib.algorithms.discriminationtree.hypothesis.HState;
import de.learnlib.algorithms.discriminationtree.hypothesis.HTransition;

final class HypothesisWrapperMealy<I, O> extends AbstractDeterministicAutomaton<HState<I,Word<O>,Void,O>, I, HTransition<I,Word<O>,Void,O>>
		implements MealyMachine<HState<I,Word<O>,Void,O>, I, HTransition<I,Word<O>,Void,O>, O> {

	private final DTLearnerHypothesis<I, Word<O>, Void, O> dtHypothesis;
	
	public HypothesisWrapperMealy(DTLearnerHypothesis<I,Word<O>,Void,O> dtHypothesis) {
		this.dtHypothesis = dtHypothesis;
	}
	
	@Override
	public HState<I, Word<O>, Void, O> getSuccessor(
			HTransition<I, Word<O>, Void, O> trans) {
		return dtHypothesis.getSuccessor(trans);
	}

	@Override
	public Collection<HState<I, Word<O>, Void, O>> getStates() {
		return dtHypothesis.getStates();
	}

	@Override
	public HState<I, Word<O>, Void, O> getInitialState() {
		return dtHypothesis.getInitialState();
	}

	@Override
	public HTransition<I, Word<O>, Void, O> getTransition(
			HState<I, Word<O>, Void, O> state, I input) {
		return dtHypothesis.getTransition(state, input);
	}

	@Override
	public Void getStateProperty(HState<I, Word<O>, Void, O> state) {
		return dtHypothesis.getStateProperty(state);
	}

	@Override
	public O getTransitionProperty(HTransition<I, Word<O>, Void, O> trans) {
		return dtHypothesis.getTransitionProperty(trans);
	}

	@Override
	public O getOutput(HState<I, Word<O>, Void, O> state, I input) {
		return AbstractTransOutAutomaton.getOutput(this, state, input);
	}

	@Override
	public void trace(Iterable<I> input, List<O> output) {
		AbstractTransOutAutomaton.trace(this, input, output);
	}

	@Override
	public void trace(HState<I, Word<O>, Void, O> state, Iterable<I> input,
			List<O> output) {
		AbstractTransOutAutomaton.trace(this, state, input, output); 
	}

	@Override
	public Word<O> computeOutput(Iterable<I> input) {
		return AbstractTransOutAutomaton.computeOutput(this, input);
	}

	@Override
	public Word<O> computeSuffixOutput(Iterable<I> prefix, Iterable<I> suffix) {
		return AbstractTransOutAutomaton.computeSuffixOutput(this, prefix, suffix);
	}

	@Override
	public O getTransitionOutput(HTransition<I, Word<O>, Void, O> trans) {
		return trans.getProperty();
	}

}
