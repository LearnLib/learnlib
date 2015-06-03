/* Copyright (C) 2014 TU Dortmund
 * This file is part of LearnLib, http://www.learnlib.de/.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
