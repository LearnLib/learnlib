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

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import net.automatalib.words.Word;

import com.google.common.collect.AbstractIterator;

public class DTNode<I,D> extends BlockListElem<I,D> {
	
	
	private static final class StatesIterator<I,D> extends AbstractIterator<TTTState<I,D>> {
		private final Deque<DTNode<I,D>> stack = new ArrayDeque<>();
		public StatesIterator(DTNode<I,D> root) {
			stack.push(root);
		}
		@Override
		protected TTTState<I,D> computeNext() {
			while(!stack.isEmpty()) {
				DTNode<I,D> curr = stack.pop();
				
				if(curr.isLeaf()) {
					if(curr.state != null) {
						return curr.state;
					}
				}
				else {
					for (DTNode<I,D> child : curr.getChildren()) {
						stack.push(child);
					}
				}
			}
			
			return endOfData();
		}
	}
	
	private static final class LeavesIterator<I,D> extends AbstractIterator<DTNode<I,D>> {
		private final Deque<DTNode<I,D>> stack = new ArrayDeque<>();
		public LeavesIterator(DTNode<I,D> root) {
			stack.push(root);
		}
		@Override
		protected DTNode<I,D> computeNext() {
			while(!stack.isEmpty()) {
				DTNode<I,D> curr = stack.pop();
				
				if(curr.isLeaf()) {
					return curr;
				}
				else {
					for (DTNode<I,D> child : curr.getChildren()) {
						stack.push(child);
					}
				}
			}
			
			return endOfData();
		}
	}
	
	private static final class InnerNodesIterator<I,D> extends AbstractIterator<DTNode<I,D>> {
		private final Deque<DTNode<I,D>> stack = new ArrayDeque<>();
		public InnerNodesIterator(DTNode<I,D> root) {
			if(root.isInner()) {
				stack.push(root);
			}
		}
		
		@Override
		protected DTNode<I,D> computeNext() {
			while(!stack.isEmpty()) {
				DTNode<I,D> curr = stack.pop();
				
				if(curr.isInner()) {
					for (DTNode<I,D> child : curr.getChildren()) {
						stack.push(child);
					}
					return curr;
				}
			}
			return endOfData();
		}
	}
	
	private static final class NodesIterator<I,D> extends AbstractIterator<DTNode<I,D>> {
		private final Deque<DTNode<I,D>> stack = new ArrayDeque<>();
		public NodesIterator(DTNode<I,D> root) {
			stack.push(root);
		}
		
		@Override
		protected DTNode<I,D> computeNext() {
			while(!stack.isEmpty()) {
				DTNode<I,D> curr = stack.pop();
				
				if(curr.isInner()) {
					for (DTNode<I,D> child : curr.getChildren()) {
						stack.push(child);
					}
				}
				
				return curr;
			}
			return endOfData();
		}
	}

	private final DTNode<I,D> parent;
	private final D parentEdgeLabel;
	private final int depth;
	
	SplitData<I,D> splitData = null;
	
	private final IncomingList<I,D> incoming = new IncomingList<>();
	
	// INNER NODE DATA
	private Word<I> discriminator;
	private Map<D,DTNode<I,D>> children;
	BlockListElem<I,D> prevBlock;
	
	// LEAF NODE DATA
	TTTState<I,D> state;
	
	boolean temp = false;
	
	
	public DTNode() {
		this(null, null);
	}
	
	public DTNode(DTNode<I,D> parent, D parentEdgeLabel) {
		this.parent = parent;
		this.parentEdgeLabel = parentEdgeLabel;
		this.depth = (parent != null) ? parent.depth + 1 : 0; 
	}
	
	public Word<I> getDiscriminator() {
		return discriminator;
	}
	
	public TTTState<I,D> getState() {
		assert isLeaf();
		return state;
	}
	
	public boolean isInner() {
		return (discriminator != null);
	}
	
	public boolean isLeaf() {
		return (discriminator == null);
	}
	
	public Collection<? extends Map.Entry<D, DTNode<I,D>>> getChildEntries() {
		return children.entrySet();
	}
	
	public Collection<? extends DTNode<I,D>> getChildren() {
		return children.values();
	}
	
	public DTNode<I,D> getExtremalChild(D label) {
		DTNode<I,D> curr = this;
		
		while(!curr.isLeaf()) {
			curr = curr.getChild(label);
		}
		
		return curr;
	}
	
	public void setChild(D label, DTNode<I,D> newChild) {
		assert newChild.parent == this;
		assert Objects.equals(newChild.parentEdgeLabel, label);
		
		children.put(label, newChild);
	}
	
	public DTNode<I,D> getChild(D value) {
		assert isInner();
		
		return children.get(value);
	}
	
	public DTNode<I,D> child(D value) {
		assert isInner();
		
		DTNode<I,D> child = children.get(value);
		
		if (child == null) {
			child = new DTNode<>(this, value);
			children.put(value, child);
		}
		
		return child;
	}
	
	public DTNode<I,D> getParent() {
		return parent;
	}
	
	public D getParentEdgeLabel() {
		return parentEdgeLabel;
	}
	
	public int getDepth() {
		return depth;
	}
	
	public boolean isTemp() {
		return temp;
	}
	
	public Iterable<TTTState<I,D>> subtreeStates() {
		return new Iterable<TTTState<I,D>>() {
			@Override
			public Iterator<TTTState<I,D>> iterator() {
				return subtreeStatesIterator();
			}
		};
	}
	public Iterator<TTTState<I,D>> subtreeStatesIterator() {
		return new StatesIterator<>(this);
	}
	
	
	void split(Word<I> discriminator, Map<D,DTNode<I,D>> newChildMap) {
		assert state == null;
		
		this.discriminator = discriminator;
		this.children = newChildMap;
	}
	
	@SafeVarargs
	@SuppressWarnings("unchecked")
	final DTNode<I,D>[] split(Word<I> discriminator, Map<D,DTNode<I,D>> newChildMap, D... outputs) {
		int numOutputs = outputs.length;
		
		assert numOutputs > 1;
		
		DTNode<I,D>[] children = new DTNode[numOutputs];
		
		this.state = null;
		
		split(discriminator, newChildMap);
		
		for (int i = 0; i < numOutputs; i++) {
			D output = outputs[i];
			DTNode<I,D> child = new DTNode<I,D>(this, output);
			this.children.put(output, child);
			children[i] = child;
		}
		
		return children;
	}

	public IncomingList<I,D> getIncoming() {
		return incoming;
	}
	
	public Iterator<DTNode<I,D>> subtreeLeavesIterator() {
		return new LeavesIterator<>(this);
	}
	
	public Iterable<DTNode<I,D>> subtreeLeaves() {
		return new Iterable<DTNode<I,D>>() {
			@Override
			public Iterator<DTNode<I,D>> iterator() {
				return subtreeLeavesIterator();
			}
		};
	}
	
	public Iterator<DTNode<I,D>> innerNodesIterator() {
		return new InnerNodesIterator<>(this);
	}
	
	public Iterable<DTNode<I,D>> innerNodes() {
		return new Iterable<DTNode<I,D>>() {
			@Override
			public Iterator<DTNode<I,D>> iterator() {
				return innerNodesIterator();
			}
		};
	}
	
	public Iterator<DTNode<I,D>> subtreeNodesIterator() {
		return new NodesIterator<>(this);
	}
	
	public D subtreeLabel(DTNode<I,D> descendant) {
		DTNode<I,D> curr = descendant;
		
		while(curr.depth > this.depth + 1) {
			curr = curr.parent;
		}
		
		if(curr.parent != this) {
			return null;
		}
		
		return curr.parentEdgeLabel;
	}
	
	void replaceChildren(Map<D,DTNode<I,D>> repChildren) {
		this.children = repChildren;
	}
	
	/**
	 * Updates the {@link TTTTransition#nonTreeTarget} attribute to
	 * point to this node for all transitions in the incoming
	 * list.
	 */
	void updateIncoming() {
		for(TTTTransition<I,D> trans : incoming) {
			trans.nonTreeTarget = this;
		}
	}
	
	
	boolean isBlockRoot() {
		return (prevBlock != null);
	}
	
	DTNode<I,D> getBlockRoot() {
		DTNode<I,D> curr = this;
		
		while(curr != null && !curr.isBlockRoot()) {
			curr = curr.parent;
		}
		
		return curr;
	}
	
	
	void removeFromBlockList() {
		if(prevBlock != null) {
			prevBlock.nextBlock = nextBlock;
			if(nextBlock != null) {
				nextBlock.prevBlock = prevBlock;
			}
			prevBlock = nextBlock = null;
		}
	}
	
	void setDiscriminator(Word<I> newDiscriminator) {
		assert isInner();
		
		this.discriminator = newDiscriminator;
	}
	
	
}
