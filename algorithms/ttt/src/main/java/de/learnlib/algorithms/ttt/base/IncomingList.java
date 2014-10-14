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

import java.util.Iterator;

/**
 * The head of the intrusive linked list for storing incoming transitions of a DT node.
 * 
 * @author Malte Isberner
 *
 * @param <I> input symbol type
 */
public class IncomingList<I,D> extends IncomingListElem<I,D> implements Iterable<TTTTransition<I,D>> {
	
	private static final class ListIterator<I,D> implements Iterator<TTTTransition<I,D>> {
		private TTTTransition<I,D> cursor;
		
		public ListIterator(TTTTransition<I,D> start) {
			this.cursor = start;
		}

		@Override
		public boolean hasNext() {
			return cursor != null;
		}

		@Override
		public TTTTransition<I,D> next() {
			TTTTransition<I,D> curr = cursor;
			cursor = cursor.nextIncoming;
			return curr;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
		
	}

	public void insertIncoming(TTTTransition<I,D> transition) {
		transition.removeFromList();
		
		transition.nextIncoming = nextIncoming;
		transition.prevIncoming = this;
		if(nextIncoming != null) {
			nextIncoming.prevIncoming = transition;
		}
		this.nextIncoming = transition;
	}
	
	public void insertAllIncoming(IncomingList<I,D> list) {
		insertAllIncoming(list.nextIncoming);
	}
	
	public void insertAllIncoming(TTTTransition<I,D> firstTransition) {
		if(firstTransition == null) {
			return;
		}
		
		if(nextIncoming == null) {
			nextIncoming = firstTransition;
			firstTransition.prevIncoming = this;
		}
		else {
			TTTTransition<I,D> oldNext = nextIncoming;
			nextIncoming = firstTransition;
			firstTransition.prevIncoming = this;
			TTTTransition<I,D> last = firstTransition;
			
			while(last.nextIncoming != null) {
				last = last.nextIncoming;
			}
			
			last.nextIncoming = oldNext;
			oldNext.prevIncoming = last;
		}
	}
	
	public TTTTransition<I,D> choose() {
		return nextIncoming;
	}

	@Override
	public Iterator<TTTTransition<I,D>> iterator() {
		return new ListIterator<>(nextIncoming);
	}
}
