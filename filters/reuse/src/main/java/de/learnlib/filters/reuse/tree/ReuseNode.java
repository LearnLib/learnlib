/* Copyright (C) 2013-2014 TU Dortmund
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
package de.learnlib.filters.reuse.tree;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import de.learnlib.filters.reuse.tree.BoundedDeque.AccessPolicy;
import de.learnlib.filters.reuse.tree.BoundedDeque.EvictPolicy;

/**
 * A {@link ReuseNode} is a vertex in the {@link ReuseTree} that contains (a
 * possible empty) set of outgoing {@link ReuseEdge}s. Each {@link ReuseNode}
 * may contain a system state holding relevant informations (e.g.
 * database identifiers or an object) that belongs to the system state that 
 * 'represents' the system state after executing a membership query.
 * 
 * @author Oliver Bauer 
 * 
 * @param <S> system state class
 * @param <I> input symbol class
 * @param <O> output symbol class
 */
public class ReuseNode<S, I, O> {
	public static final class NodeResult<S,I,O> {
		public final ReuseNode<S, I, O> reuseNode;
		public final S systemState;
		/**
		 * The prefix length for a membership query that leads to
		 * the {@link ReuseNode} in the reuse tree.
		 */
		public final int prefixLength;
		public NodeResult(ReuseNode<S, I, O> reuseNode, S systemState, int prefixLength) {
			super();
			this.reuseNode = reuseNode;
			this.systemState = systemState;
			this.prefixLength = prefixLength;
		}
	}
	
	private final ReuseEdge<S,I,O>[] edges;
	private final BoundedDeque<S> systemStates;
	// private S systemstate;
	private final int id;

	@SuppressWarnings("unchecked")
	public ReuseNode(int id, int alphabetSize, int maxSystemStates, AccessPolicy accessPolicy, EvictPolicy evictPolicy) {
		this.edges = new ReuseEdge[alphabetSize];
		this.id = id;
		this.systemStates = new BoundedDeque<>(maxSystemStates, accessPolicy, evictPolicy);
	}

	/**
	 * The system state, may be {@code null}.
	 * 
	 * @return
	 */
	public S fetchSystemState(boolean remove) {
		if(remove) {
			return systemStates.retrieve();
		}
		return systemStates.peek();
	}

	public S addSystemState(S state) {
		return systemStates.insert(state);
	}
	
	public Iterator<S> systemStatesIterator() {
		return systemStates.iterator();
	}
	
	public boolean hasSystemStates() {
		return !systemStates.isEmpty();
	}
	
	public void clearSystemStates() {
		systemStates.clear();
	}
	
	/**
	 * Returns all outgoing {@link ReuseEdge}s from this {@link ReuseNode}. If
	 * there are none the returned {@link java.util.Collection} will be empty
	 * (but never {@code null}).
	 * 
	 * @return
	 */
	public Collection<ReuseEdge<S, I, O>> getEdges() {
		return Arrays.asList(edges);
	}

	/**
	 * Adds an outgoing {@link ReuseEdge} to this {@link ReuseNode}.
	 * 
	 * @param edge
	 */
	public void addEdge(int index, ReuseEdge<S, I, O> edge) {
		this.edges[index] = edge;
	}

	/**
	 * May be {@code null}.
	 *
	 * @param index
	 * @return
	 */
	public ReuseEdge<S, I, O> getEdgeWithInput(int index) {
		return this.edges[index];
	}

	public ReuseNode<S, I, O> getTargetNodeForInput(int index) {
		ReuseEdge<S, I, O> edge = this.getEdgeWithInput(index);
		if (edge == null) {
			return null;
		}
		return edge.getTarget();
	}

	public int getId() {
		return this.id;
	}
	
	
	/*
	 * Defining equals via the ID only leads to confusion if the
	 * tree is cleared and somehow references to old nodes with the
	 * same id as new ones exist. Identity semantics are just fine
	 * here.
	 */
//	@Override
//	public int hashCode() {
//		final int prime = 31;
//		int result = 1;
//		result = prime * result + id;
//		return result;
//	}
//
//	@Override
//	public boolean equals(Object obj) {
//		if (obj instanceof ReuseNode) {
//			@SuppressWarnings("unchecked")
//			ReuseNode<S, I, O> other = ((ReuseNode<S,I,O>)obj);
//			return other.id == id;
//		}
//		return false;
//	}
}