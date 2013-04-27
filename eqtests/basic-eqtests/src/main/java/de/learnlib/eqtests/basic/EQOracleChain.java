package de.learnlib.eqtests.basic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import de.learnlib.api.EquivalenceOracle;
import de.learnlib.oracles.DefaultQuery;

public class EQOracleChain<A, I, O> implements EquivalenceOracle<A, I, O> {
	
	private final List<EquivalenceOracle<? super A, I, O>> oracles;

	public EQOracleChain(List<? extends EquivalenceOracle<? super A,I,O>> oracles) {
		this.oracles = new ArrayList<>(oracles);
	}
	
	@SafeVarargs
	public EQOracleChain(EquivalenceOracle<? super A,I,O> ...oracles) {
		this(Arrays.asList(oracles));
	}

	/*
	 * (non-Javadoc)
	 * @see de.learnlib.api.EquivalenceOracle#findCounterExample(java.lang.Object, java.util.Collection)
	 */
	@Override
	public DefaultQuery<I, O> findCounterExample(A hypothesis,
			Collection<? extends I> inputs) {
		for(EquivalenceOracle<? super A,I,O> eqOracle : oracles) {
			DefaultQuery<I,O> ceQry = eqOracle.findCounterExample(hypothesis, inputs);
			if(ceQry != null)
				return ceQry;
		}
		return null;
	}

}
