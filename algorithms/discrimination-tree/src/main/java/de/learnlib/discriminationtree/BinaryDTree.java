package de.learnlib.discriminationtree;

import de.learnlib.api.MembershipOracle;

public class BinaryDTree<I, D> extends DiscriminationTree<I, Boolean, D> {

	public BinaryDTree(D rootData,
			MembershipOracle<I, Boolean> oracle) {
		super(new BinaryDTNode<I,D>(rootData), oracle);
	}
	
	public BinaryDTree(MembershipOracle<I, Boolean> oracle) {
		this(null, oracle);
	}

}
