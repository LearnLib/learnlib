package de.learnlib.discriminationtree;

import java.util.HashMap;
import java.util.Map;


public class MultiDTNode<I, O, D> extends DTNode<I,O,D> {

	public MultiDTNode(D data) {
		super(data);
	}
	
	protected MultiDTNode(DTNode<I, O, D> parent, O parentOutcome, D data) {
		super(parent, parentOutcome, data);
	}

	@Override
	protected Map<O, DTNode<I, O, D>> createChildMap() {
		return new HashMap<>();
	}

	@Override
	protected DTNode<I, O, D> createChild(O outcome, D data) {
		return new MultiDTNode<>(this, outcome, data);
	}


}
