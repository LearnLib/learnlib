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
