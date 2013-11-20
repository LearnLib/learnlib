package de.learnlib.algorithms.dt;

import de.learnlib.api.MembershipOracle;

public class DiscriminationTree<I, O> {
	
	private final DTNode<I,O> root;
	private final MembershipOracle<I, O> oracle;

	public DiscriminationTree(MembershipOracle<I, O> oracle) {
		this.oracle = oracle;
	}

}
