/* Copyright (C) 2015 TU Dortmund
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
 * http://www.gnu.de/documents/lgpl.en.html.
 */
package de.learnlib.passive.commons.bluefringe;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.PriorityQueue;
import java.util.Queue;

import net.automatalib.commons.util.comparison.CmpUtil;
import de.learnlib.passive.commons.pta.AbstractBlueFringePTAState;
import de.learnlib.passive.commons.pta.PTATransition;

/**
 * Standard processing orders that can be used for the RPNI algorithm.
 * 
 * @author Malte Isberner
 *
 */
public enum DefaultProcessingOrders implements ProcessingOrder {
	/**
	 * Processes blue states in ascending canonical order of their
	 * access sequences.
	 * 
	 * @see AbstractBlueFringePTAState#compareTo(AbstractBlueFringePTAState)
	 * @see CmpUtil#canonicalCompare(int[], int[])
	 */
	CANONICAL_ORDER {
		@Override
		public <S extends AbstractBlueFringePTAState<?, ?, S>> Queue<PTATransition<S>> createWorklist() {
			return new PriorityQueue<>((t1, t2) -> {
				int cmp = t1.getSource().compareTo(t2.getSource());
				if (cmp == 0) {
					cmp = t1.getIndex() - t2.getIndex();
				}
				return cmp;
			});
		}
	},
	/**
	 * Processes blue states in ascending lexicographical order of their
	 * access sequences.
	 * 
	 * @see AbstractBlueFringePTAState#lexCompareTo(AbstractBlueFringePTAState)
	 * @see CmpUtil#lexCompare(int[], int[])
	 */
	LEX_ORDER {
		@Override
		public <S extends AbstractBlueFringePTAState<?, ?, S>> Queue<PTATransition<S>> createWorklist() {
			return new PriorityQueue<>((t1, t2) -> {
				int cmp = t1.getSource().lexCompareTo(t2.getSource());
				if (cmp == 0) {
					cmp = t1.getIndex() - t2.getIndex();
				}
				return cmp;
			});
		}
	},
	/**
	 * Processes blue states in a first-in, first-out (queue-like) manner.
	 */
	FIFO_ORDER {
		@Override
		public <S extends AbstractBlueFringePTAState<?, ?, S>> Queue<PTATransition<S>> createWorklist() {
			return new ArrayDeque<>();
		}
	},
	/**
	 * Processes blue states in a last-in, first-out (stack-like) manner.
	 */
	LIFO_ORDER {
		@Override
		public <S extends AbstractBlueFringePTAState<?, ?, S>> Queue<PTATransition<S>> createWorklist() {
			return Collections.asLifoQueue(new ArrayDeque<>());
		}
	}
}
