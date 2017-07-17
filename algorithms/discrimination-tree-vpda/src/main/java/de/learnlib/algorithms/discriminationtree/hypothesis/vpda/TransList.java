/* Copyright (C) 2017 TU Dortmund
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
package de.learnlib.algorithms.discriminationtree.hypothesis.vpda;

/**
 * @param <I> input symbol type
 *
 * @author Malte Isberner
 */
public class TransList<I> extends TransListElem<I> implements Iterable<HypTrans<I>> {

	private static final class Iterator<I> implements java.util.Iterator<HypTrans<I>> {

		private HypTrans<I> curr;

		public Iterator(HypTrans<I> start) {
			this.curr = start;
		}

		@Override
		public boolean hasNext() {
			return curr != null;
		}

		@Override
		public HypTrans<I> next() {
			HypTrans<I> result = curr;
			curr = curr.next;
			return result;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	public TransList() {
	}

	public boolean isEmpty() {
		return next == null;
	}

	public void add(HypTrans<I> trans) {
		assert !trans.isTree();
		trans.removeFromList();

		if (next != null) {
			trans.next = next;
			next.prev = trans;
		}
		trans.prev = this;
		next = trans;
	}

	public void addAll(TransList<I> list) {
		if (list.next != null) {
			HypTrans<I> last = list.next;
			while (last.next != null) {
				last = last.next;
			}
			if (next != null) {
				next.prev = last;
			}
			last.next = next;
			list.next.prev = this;
			next = list.next;
		}
		list.next = null;
	}

	public HypTrans<I> chooseMinimal() {
		HypTrans<I> curr = next;
		HypTrans<I> shortest = curr;
		int shortestLen = shortest.getAccessSequence().length();

		curr = curr.next;
		while (curr != null) {
			int transLen = curr.getAccessSequence().length();
			if (transLen < shortestLen) {
				shortestLen = transLen;
				shortest = curr;
			}
			curr = curr.next;
		}

		return shortest;
	}

	public HypTrans<I> poll() {
		if (next == null) {
			return null;
		}
		HypTrans<I> result = next;
		next = result.next;
		if (result.next != null) {
			result.next.prev = this;
		}

		result.prev = result.next = null;

		return result;
	}

	@Override
	public void setNext(HypTrans<I> next) {
		this.next = next;
	}

	@Override
	public Iterator<I> iterator() {
		return new Iterator<>(next);
	}

	public int size() {
		HypTrans<I> curr = next;
		int i = 0;
		while (curr != null) {
			i++;
			curr = curr.next;
		}

		return i;
	}

}
