/* Copyright (C) 2014 TU Dortmund
 * This file is part of LearnLib, http://www.learnlib.de/.
 *
 * LearnLib is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 3.0 as published by the Free Software Foundation.
 *
 * LearnLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with LearnLib; if not, see
 * <http://www.gnu.de/documents/lgpl.en.html>.
 */
package de.learnlib.algorithms.ttt.base;

import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

/**
 * A transition in a {@link TTTHypothesis}.
 * 
 * @author Malte Isberner
 *
 * @param <I> input symbol type
 */
public class TTTTransition<I,D> extends IncomingListElem<I,D> implements AccessSequenceProvider<I> {
	
	private final TTTState<I,D> source;
	private final I input;

	// TREE TRANSITION
	private TTTState<I,D> treeTarget;
	
	// NON-TREE TRANSITION
	DTNode<I,D> nonTreeTarget;
	
	protected IncomingListElem<I,D> prevIncoming;
	

	public TTTTransition(TTTState<I,D> source, I input) {
		this.source = source;
		this.input = input;
	}
	
	
	public boolean isTree() {
		return (treeTarget != null);
	}
	
	public TTTState<I,D> getTreeTarget() {
		assert isTree();
		
		return treeTarget;
	}
	
	public DTNode<I,D> getNonTreeTarget() {
		assert !isTree();
		
		return nonTreeTarget;
	}
	
	public DTNode<I,D> getDTTarget() {
		if(treeTarget != null) {
			return treeTarget.dtLeaf;
		}
		return nonTreeTarget;
	}
	
	
	public TTTState<I,D> getTarget() {
		if(treeTarget != null) {
			return treeTarget;
		}
		
		return nonTreeTarget.state;
	}
	
	public TTTState<I,D> getSource() {
		return source;
	}
	
	public I getInput() {
		return input;
	}
	
	protected Object getProperty() {
		return null;
	}
	
	@Override
	public Word<I> getAccessSequence() {
		WordBuilder<I> wb = new WordBuilder<>(); // FIXME capacity hint
		
		TTTTransition<I,D> curr = this;
		
		while(curr != null) {
			wb.add(curr.input);
			curr = curr.source.parentTransition;
		}
		
		return wb.reverse().toWord();
	}
	
	void makeTree(TTTState<I,D> treeTarget) {
		removeFromList();
		this.treeTarget = treeTarget;
		this.nonTreeTarget = null;
	}
	
	void setNonTreeTarget(DTNode<I,D> nonTreeTarget) {
		this.nonTreeTarget = nonTreeTarget;
		nonTreeTarget.getIncoming().insertIncoming(this);
	}
	
	
	void removeFromList() {
		if(prevIncoming != null) {
			prevIncoming.nextIncoming = nextIncoming;
		}
		if(nextIncoming != null) {
			nextIncoming.prevIncoming = prevIncoming;
		}
	}
}
