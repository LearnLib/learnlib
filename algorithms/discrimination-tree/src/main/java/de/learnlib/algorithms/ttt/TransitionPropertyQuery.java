package de.learnlib.algorithms.ttt;

import net.automatalib.words.Word;
import de.learnlib.algorithms.ttt.hypothesis.HTransition;
import de.learnlib.oracles.AbstractQuery;

public abstract class TransitionPropertyQuery<I, O, TP> extends AbstractQuery<I,O> {
	
	protected final HTransition<I,O,?,TP> transition;
	
	public TransitionPropertyQuery(HTransition<I,O,?,TP> transition, Word<I> prefix, Word<I> suffix) {
		super(prefix, suffix);
		this.transition = transition;
	}
	
	@Override
	public void answer(O output) {
		TP property = extractProperty(output);
		transition.setProperty(property);
	}
	
	protected abstract TP extractProperty(O output);
	
}
