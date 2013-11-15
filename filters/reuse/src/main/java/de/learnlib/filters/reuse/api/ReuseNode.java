/* Copyright (C) 2013 TU Dortmund
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
package de.learnlib.filters.reuse.api;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import de.learnlib.filters.reuse.ReuseTreeImpl;

/**
 * A {@link ReuseNode} is a vertex in the {@link ReuseTreeImpl} that
 * contains (a possible empty) set of outgoing {@link ReuseEdge}s.
 * Each {@link ReuseNode} may contain a {@link SystemState} holding
 * relevant informations (e.g. database identifiers) that belongs
 * to the system state that 'represents' the system state after executing
 * a membership query.
 *
 * @author Oliver Bauer <oliver.bauer@tu-dortmund.de>
 */
public class ReuseNode<S, I, O> {
	private String name;
	private Map<I, ReuseEdge<S, I, O>> edges;
	private S systemstate;

	public ReuseNode(String name) {
		this.name = name;
		this.edges = new HashMap<>();
	}

	public boolean hasState() {
		return systemstate != null;
	}

	/**
	 * The {@link SystemState}, maybe <code>null</code>.
	 *
	 * @return
	 */
	public S getSystemState() {
		return systemstate;
	}

	public void setSystemState(S state) {
		this.systemstate = state;
	}

	public String getName() {
		return this.name;
	}

	/**
	 * Returns all outgoing {@link ReuseEdge}s from this {@link ReuseNode}.
	 * If there are none the returned {@link java.util.Collection} will be empty (but never
	 * <code>null</code>).
	 *
	 * @return
	 */
	public Collection<ReuseEdge<S, I, O>> getEdges() {
		return this.edges.values();
	}

	/**
	 * Adds a ougoing {@link ReuseEdge} to this {@link ReuseNode}.
	 *
	 * @param edge
	 */
	public void addEdge(ReuseEdge<S, I, O> edge) {
		this.edges.put(edge.getInput(), edge);
	}

	/**
	 * Maybe <code>null</code>.
	 *
	 * @param input
	 * @return
	 */
	public ReuseEdge<S, I,O> getEdgeWithInput(I input) {
		return this.edges.get(input);
	}

	public ReuseNode<S, I, O> getTargetNodeForInput(I input) {
		ReuseEdge<S, I, O> edge = this.getEdgeWithInput(input);
		if (edge == null) {
			return null;
		}
		return edge.getTarget();
	}

	@Override
	public String toString() {
		return this.name;
	}
}
