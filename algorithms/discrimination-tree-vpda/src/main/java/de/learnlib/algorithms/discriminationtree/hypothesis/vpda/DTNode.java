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

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Predicate;

import com.google.common.collect.AbstractIterator;

/**
 * @param <I> input symbol type
 *
 * @author Malte Isberner
 */
public class DTNode<I> implements BlockListElem<I> {

	private static final class LocationsIterator<I> extends AbstractIterator<HypLoc<I>> {

		private final Deque<DTNode<I>> stack = new ArrayDeque<>();

		public LocationsIterator(DTNode<I> root) {
			stack.push(root);
		}

		@Override
		protected HypLoc<I> computeNext() {
			while (!stack.isEmpty()) {
				DTNode<I> curr = stack.pop();

				if (curr.isLeaf()) {
					if (curr.location != null) {
						return curr.location;
					}
				}
				else {
					for (DTNode<I> child : curr.getChildren()) {
						stack.push(child);
					}
				}
			}

			return endOfData();
		}
	}

	private static final class NodesIterator<I> extends AbstractIterator<DTNode<I>> {

		private final Deque<DTNode<I>> stack = new ArrayDeque<>();

		private final Predicate<? super DTNode<I>> pred;

		public NodesIterator(DTNode<I> root, Predicate<? super DTNode<I>> pred) {
			stack.push(root);
			this.pred = pred;
		}

		@Override
		protected DTNode<I> computeNext() {
			while (!stack.isEmpty()) {
				DTNode<I> curr = stack.pop();

				if (curr.isInner()) {
					for (DTNode<I> child : curr.getChildren()) {
						stack.push(child);
					}
				}

				if (pred.test(curr)) {
					return curr;
				}
			}

			return endOfData();
		}
	}

	private final int depth;

	private final DTNode<I> parent;
	private DTNode<I> nextBlock;

	private final boolean parentLabel;

	private boolean temp;

	private final TransList<I> nonTreeIncoming = new TransList<>();

	private HypLoc<I> location = null;

	private Map<Boolean, DTNode<I>> children;

	private ContextPair<I> discriminator;

	private BlockListElem<I> prevBlock;

	private SplitData<I> splitData;

	public DTNode(DTNode<I> parent, boolean parentLabel) {
		this.parent = parent;
		this.parentLabel = parentLabel;
		this.depth = (parent == null) ? 0 : parent.depth + 1;
	}

	public boolean isLeaf() {
		return children == null;
	}

	public void updateIncoming() {
		for (HypTrans<I> inc : nonTreeIncoming) {
			assert !inc.isTree();
			inc.setNonTreeTarget(this);
		}
	}

	public boolean isInner() {
		return children != null;
	}

	public void setLocation(HypLoc<I> location) {
		this.location = location;
	}

	public HypLoc<I> getLocation() {
		return location;
	}

	public SplitData<I> getSplitData() {
		return splitData;
	}

	public void setSplitData(SplitData<I> splitData) {
		this.splitData = splitData;
	}

	public DTNode<I>[] split(ContextPair<I> discriminator, boolean out1, boolean out2) {
		assert isLeaf();
		assert out1 != out2;

		DTNode<I> child1 = new DTNode<>(this, out1);
		DTNode<I> child2 = new DTNode<>(this, out2);
		Map<Boolean, DTNode<I>> childMap = new HashMap<>();
		childMap.put(out1, child1);
		childMap.put(out2, child2);

		this.discriminator = discriminator;
		this.children = childMap;

		DTNode<I>[] result = new DTNode[2];
		result[0] = child1;
		result[1] = child2;

		return result;
	}

	public void split(ContextPair<I> discriminator, Map<Boolean, DTNode<I>> children) {
		assert isLeaf();
		assert children.values().stream().allMatch(c -> c.parent == this);
		assert children.entrySet().stream().allMatch(e -> e.getKey().equals(e.getValue().parentLabel));

		this.discriminator = discriminator;
		this.children = children;
	}

	public boolean subtreeLabel(DTNode<I> desc) {
		DTNode<I> prev = null;
		DTNode<I> curr = desc;

		while (curr.depth > depth) {
			prev = curr;
			curr = prev.parent;
		}

		if (curr != this) {
			throw new IllegalArgumentException();
		}

		return prev.parentLabel;
	}

	public Collection<? extends DTNode<I>> getChildren() {
		return children.values();
	}

	public Iterator<HypLoc<I>> subtreeLocsIterator() {
		return new LocationsIterator<>(this);
	}

	public Iterable<HypLoc<I>> subtreeLocations() {
		return this::subtreeLocsIterator;
	}

	public boolean getParentLabel() {
		return parentLabel;
	}

	public boolean isRoot() {
		return parent == null;
	}

	public DTNode<I> getParent() {
		return parent;
	}

	public boolean isTemp() {
		return temp;
	}

	public void setTemp(boolean temp) {
		this.temp = temp;
	}

	public ContextPair<I> getDiscriminator() {
		return discriminator;
	}

	public int getDepth() {
		return depth;
	}

	public DTNode<I> getChild(Boolean outcome) {
		assert isInner();
		return children.get(outcome);
	}

	public void addIncoming(HypTrans<I> trans) {
		nonTreeIncoming.add(trans);
	}

	public TransList<I> getIncoming() {
		return nonTreeIncoming;
	}

	public BlockListElem<I> getPrevBlock() {
		return prevBlock;
	}

	public void setPrevBlock(BlockListElem<I> prevBlock) {
		this.prevBlock = prevBlock;
	}

	public DTNode<I> getNextBlock() {
		return nextBlock;
	}

	@Override
	public void setNextBlock(DTNode<I> next) {
		this.nextBlock = next;
	}

	public void replaceChildren(Map<Boolean, DTNode<I>> children) {
		this.children = children;
	}

	public void setDiscriminator(ContextPair<I> discriminator) {
		this.discriminator = discriminator;
	}

	public boolean isBlockRoot() {
		return temp && !parent.temp;
	}

	public void removeFromBlockList() {
		assert prevBlock != null;
		this.prevBlock.setNextBlock(nextBlock);
		if (nextBlock != null) {
			nextBlock.prevBlock = prevBlock;
		}
	}

	public Iterator<DTNode<I>> subtreeLeavesIterator() {
		return new NodesIterator<>(this, DTNode::isLeaf);
	}

	public Iterable<DTNode<I>> subtreeLeaves() {
		return this::subtreeLeavesIterator;
	}

	public Iterator<DTNode<I>> subtreeInnersIterator() {
		return new NodesIterator<>(this, DTNode::isInner);
	}

	public Iterable<DTNode<I>> subtreeInners() {
		return this::subtreeInnersIterator;
	}

}
