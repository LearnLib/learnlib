package de.learnlib.algorithms.ttt;

import de.learnlib.algorithms.ttt.dtree.DTNode;
import de.learnlib.algorithms.ttt.dtree.TempDTNode;
import de.learnlib.algorithms.ttt.hypothesis.HypothesisState;

final class CEHandlingContext<I,O,SP,TP> {
	private final DTNode<I,O,SP,TP> dtNode;
	private final TempDTNode<I, O, SP, TP> tempDtNode;
	private final HypothesisState<I, O, SP, TP> oldState;
	private final HypothesisState<I, O, SP, TP> newState;
	private I sym;
	
	
	public CEHandlingContext(DTNode<I, O, SP, TP> dtNode, TempDTNode<I,O,SP,TP> tempDtNode, HypothesisState<I, O, SP, TP> oldState,
			HypothesisState<I, O, SP, TP> newState) {
		this.dtNode = dtNode;
		this.tempDtNode = tempDtNode;
		this.oldState = oldState;
		this.newState = newState;
		this.sym = null;
	}
	
	
	
	public HypothesisState<I, O, SP, TP> getOldState() {
		return oldState;
	}
	
	public HypothesisState<I, O, SP, TP> getNewState() {
		return newState;
	}
	
	public DTNode<I, O, SP, TP> getDTNode() {
		return dtNode;
	}
	
	public TempDTNode<I, O, SP, TP> getTempDTNode() {
		return tempDtNode;
	}
	
	public I getSym() {
		return sym;
	}
	
	public void setSym(I sym) {
		this.sym = sym;
	}
}