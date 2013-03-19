package de.learnlib.oracles.eq;

import net.automatalib.automata.concepts.InputAlphabetHolder;
import de.learnlib.api.EquivalenceOracle;
import de.learnlib.api.Query;

public class SimpleEQOracle<A extends InputAlphabetHolder<I>,I,O> {
	
	public static <A extends InputAlphabetHolder<I>,I,O>
	SimpleEQOracle<A,I,O> create(EquivalenceOracle<A,I,O> eqOracle) {
		return new SimpleEQOracle<A,I,O>(eqOracle);
	}
	
	private final EquivalenceOracle<A, I, O> eqOracle;
	
	public SimpleEQOracle(EquivalenceOracle<A,I,O> eqOracle) {
		this.eqOracle = eqOracle;
	}
	
	public Query<I,O> findCounterExample(A hypothesis) {
		return eqOracle.findCounterExample(hypothesis, hypothesis.getInputAlphabet());
	}
}
