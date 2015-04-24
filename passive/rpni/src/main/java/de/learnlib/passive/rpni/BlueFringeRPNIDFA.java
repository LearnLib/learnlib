package de.learnlib.passive.rpni;

import java.util.Collection;

public class BlueFringeRPNIDFA<I> extends AbstractBlueFringeRPNI<I, Boolean, Boolean, Void, DFA<?,I>>
		implements PassiveDFALearner<I> {
	
	private boolean complete;

	public BlueFringeRPNIDFA(Alphabet<I> alphabet) {
		super(alphabet);
	}
	
	public void setComplete(boolean complete) {
		this.complete = complete;
	}

	@Override
	public void addSamples(
			Collection<? extends DefaultQuery<I, Boolean>> samples) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void initializePTA(BlueFringePTA<Boolean, Void> pta) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected DFA<?, I> ptaToModel(BlueFringePTA<Boolean, Void> pta) {
		CompactDFA<I> dfa = new CompactDFA<>(alphabet, pta.getNumRedStates());
		pta.toAutomaton(dfa, alphabet, b -> b, x -> x);
		if (complete) {
			MutableDFAs.complete(dfa, alphabet, false);
		}
		return dfa;
	}

}
