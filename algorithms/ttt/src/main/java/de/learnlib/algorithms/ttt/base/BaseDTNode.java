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

import de.learnlib.datastructure.discriminationtree.iterators.NodesIterator;
import de.learnlib.datastructure.discriminationtree.iterators.TransformingLeavesIterator;
import de.learnlib.datastructure.discriminationtree.model.TemporaryIntrusiveDTNode;
import de.learnlib.datastructure.list.IntrusiveListElem;
import net.automatalib.words.Word;

public abstract class BaseDTNode<I, D>
		extends TemporaryIntrusiveDTNode<Word<I>, D, TTTState<I, D>, IncomingList<I, D>, BaseDTNode<I, D>>
		implements IntrusiveListElem<BaseDTNode<I, D>> {

	private final IncomingList<I, D> incoming = new IncomingList<>();

	public BaseDTNode() {
		this(null, null);
	}

	public BaseDTNode(BaseDTNode<I, D> parent, D parentEdgeLabel) {
		super(parent, parentEdgeLabel, null);
	}

	public TTTState<I, D> anySubtreeState() {
		BaseDTNode<I, D> curr = this;
		while (!curr.isLeaf()) {
			curr = curr.anyChild();
		}
		return curr.data;
	}

	public BaseDTNode<I, D> anyChild() {
		assert isInner();
		return children.values().iterator().next();
	}

	public Iterable<TTTState<I, D>> subtreeStates() {
		return this::subtreeStatesIterator;
	}

	public Iterator<TTTState<I, D>> subtreeStatesIterator() {
		return new TransformingLeavesIterator<>(this, BaseDTNode::getData);
	}

	public IncomingList<I, D> getIncoming() {
		return incoming;
	}

	public Iterator<BaseDTNode<I, D>> subtreeNodesIterator() {
		return new NodesIterator<>(this);
	}

	/**
	 * Updates the {@link TTTTransition#nonTreeTarget} attribute to
	 * point to this node for all transitions in the incoming
	 * list.
	 */
	void updateIncoming() {
		for (TTTTransition<I, D> trans : incoming) {
			trans.nonTreeTarget = this;
		}
	}
}
