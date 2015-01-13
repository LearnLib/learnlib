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
 * A list for storing blocks (identified by their root {@link DTNode}s). The
 * list is implemented as a singly-linked list, and allows O(1) insertion
 * and removal of elements.
 * 
 * @author Malte Isberner
 *
 * @param <I> input symbol type
 */
public class BlockList<I,D> extends BlockListElem<I,D> implements Iterable<DTNode<I,D>> {
	
	/**
	 * Iterator for a {@link BlockList}.
	 * 
	 * @author Malte Isberner
	 *
	 * @param <I> input symbol type
	 */
	private static final class ListIterator<I,D> implements Iterator<DTNode<I,D>> {
		private DTNode<I,D> cursor;
		public ListIterator(DTNode<I,D> start) {
			this.cursor = start;
		}
		@Override
		public boolean hasNext() {
			return cursor != null;
		}
		@Override
		public DTNode<I,D> next() {
			DTNode<I,D> current = cursor;
			cursor = cursor.nextBlock;
			return current;
		}
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
	
	/**
	 * Inserts a block into the list. Currently, the block is inserted
	 * at the head of the list. However, callers should not rely on this.
	 * @param blockRoot the root node of the block to be inserted
	 */
	public void insertBlock(DTNode<I,D> blockRoot) {
		blockRoot.removeFromBlockList();
		
		blockRoot.nextBlock = nextBlock;
		if(nextBlock != null) {
			nextBlock.prevBlock = blockRoot;
		}
		blockRoot.prevBlock = this;
		nextBlock = blockRoot;
	}
	
	/**
	 * Retrieves any block from the list. If the list is empty,
	 * {@code null} is returned.
	 * @return any block from the list, or {@code null} if the list is empty.
	 */
	public DTNode<I,D> chooseBlock() {
		return nextBlock;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<DTNode<I,D>> iterator() {
		return new ListIterator<>(nextBlock);
	}

	/**
	 * Checks whether this list is empty.
	 * @return {@code true} if the list is empty, {@code false} otherwise
	 */
	public boolean isEmpty() {
		return (nextBlock == null);
	}
}
