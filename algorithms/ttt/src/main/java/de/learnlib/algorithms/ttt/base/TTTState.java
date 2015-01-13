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

/**
 * A state in a {@link TTTHypothesis}.
 * 
 * @author Malte Isberner
 *
 * @param <I> input symbol
 */
public class TTTState<I,D> implements AccessSequenceProvider<I> {
	
	final int id;
	
	final TTTTransition<I,D>[] transitions;
	final TTTTransition<I,D> parentTransition;
	
	DTNode<I,D> dtLeaf;

	@SuppressWarnings("unchecked")
	public TTTState(int alphabetSize, TTTTransition<I,D> parentTransition, int id) {
		this.id = id;
		this.parentTransition = parentTransition;
		this.transitions = new TTTTransition[alphabetSize];
	}
	
	/**
	 * Checks whether this state is the initial state (i.e., the root of the
	 * spanning tree).
	 * @return {@code true} if this state is the initial state, {@code false} otherwise
	 */
	public boolean isRoot() {
		return (parentTransition == null);
	}
	
	/**
	 * Retrieves the discrimination tree leaf associated with this state.
	 * @return the discrimination tree leaf associated with this state
	 */
	public DTNode<I,D> getDTLeaf() {
		return dtLeaf;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.learnlib.algorithms.ttt.dfa.AccessSequenceProvider#getAccessSequence()
	 */
	@Override
	public Word<I> getAccessSequence() {
		if(parentTransition != null) {
			return parentTransition.getAccessSequence();
		}
		return Word.epsilon(); // root
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "s" + id;
	}
}
