package de.learnlib.discriminationtree;

import java.util.Map;

public class BinaryDTNode<I, D> extends DTNode<I, Boolean, D> {

	public BinaryDTNode(D data) {
		super(data);
	}

	protected BinaryDTNode(DTNode<I, Boolean, D> parent, Boolean parentOutcome,
			D data) {
		super(parent, parentOutcome, data);
	}

	@Override
	protected Map<Boolean, DTNode<I, Boolean, D>> createChildMap() {
		return new BooleanMap<>();
	}

	@Override
	protected DTNode<I, Boolean, D> createChild(Boolean outcome, D data) {
		return new BinaryDTNode<>(this, outcome, data);
	}

}
