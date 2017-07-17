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
public class BlockList<I> implements BlockListElem<I>, Iterable<DTNode<I>> {

	private static final class Iterator<I> implements java.util.Iterator<DTNode<I>> {

		private DTNode<I> curr;

		public Iterator(DTNode<I> curr) {
			this.curr = curr;
		}

		@Override
		public boolean hasNext() {
			return curr != null;
		}

		@Override
		public DTNode<I> next() {
			DTNode<I> result = curr;
			curr = curr.getNextBlock();
			return result;
		}

	}

	private DTNode<I> nextBlock;

	@Override
	public void setNextBlock(DTNode<I> block) {
		this.nextBlock = block;
	}

	public void add(DTNode<I> block) {
		block.setNextBlock(nextBlock);
		if (nextBlock != null) {
			nextBlock.setPrevBlock(block);
		}
		block.setPrevBlock(this);
		this.nextBlock = block;
	}

	@Override
	public Iterator<I> iterator() {
		return new Iterator<>(nextBlock);
	}

}
