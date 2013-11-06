package de.learnlib.eqtests.basic;

import java.util.Collection;

import de.learnlib.api.EquivalenceOracle;
import de.learnlib.oracles.DefaultQuery;

public abstract class DelegateEQOracle<A, I, O> implements
		EquivalenceOracle<A, I, O> {
	
	protected final EquivalenceOracle<? super A, I, O> delegate;

	public DelegateEQOracle(EquivalenceOracle<? super A, I, O> delegate) {
		this.delegate = delegate;
	}

	@Override
	public DefaultQuery<I, O> findCounterExample(A hypothesis,
			Collection<? extends I> inputs) {
		return delegate.findCounterExample(hypothesis, inputs);
	}

}
