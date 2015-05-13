package de.learnlib.passive.rpni;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.util.automata.fsa.MutableDFAs;
import net.automatalib.words.Alphabet;
import de.learnlib.oracles.DefaultQuery;
import de.learnlib.passive.api.PassiveDFALearner;

public class BlueFringeRPNIDFA<I> extends AbstractBlueFringeRPNI<I, Boolean, Boolean, Void, DFA<?,I>>
		implements PassiveDFALearner<I> {
	
	private boolean complete;
	private List<int[]> positive = new ArrayList<>();
	private List<int[]> negative = new ArrayList<>();

	public BlueFringeRPNIDFA(Alphabet<I> alphabet) {
		super(alphabet);
	}
	
	public void setComplete(boolean complete) {
		this.complete = complete;
	}

	@Override
	public void addSamples(
			Collection<? extends DefaultQuery<I, Boolean>> samples) {
		for (DefaultQuery<I,Boolean> query : samples) {
			int[] arr = query.getInput().toIntArray(alphabet);
			if (query.getOutput()) {
				positive.add(arr);
			}
			else {
				negative.add(arr);
			}
		}
	}

	@Override
	protected void initializePTA(BlueFringePTA<Boolean, Void> pta) {
		for (int[] sample : positive) {
			pta.addSample(sample, true);
		}
		for (int[] sample : negative) {
			pta.addSample(sample, false);
		}
	}

	@Override
	protected CompactDFA<I> ptaToModel(BlueFringePTA<Boolean, Void> pta) {
		CompactDFA<I> dfa = new CompactDFA<>(alphabet, pta.getNumRedStates());
		pta.toAutomaton(dfa, alphabet, b -> b, x -> x);
		if (complete) {
			MutableDFAs.complete(dfa, alphabet, false);
		}
		return dfa;
	}

}
