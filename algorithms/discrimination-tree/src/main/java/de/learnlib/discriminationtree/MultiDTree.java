package de.learnlib.discriminationtree;

import de.learnlib.api.MembershipOracle;

public class MultiDTree<I, O, D> extends DiscriminationTree<I, O, D> {

	
	public MultiDTree(MembershipOracle<I, O> oracle) {
		this(null, oracle);
	}
	
	public MultiDTree(D rootData, MembershipOracle<I, O> oracle) {
		super(new MultiDTNode<I,O,D>(rootData), oracle);
	}

}
