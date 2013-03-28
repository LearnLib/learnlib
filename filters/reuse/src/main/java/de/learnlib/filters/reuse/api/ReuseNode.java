package de.learnlib.filters.reuse.api;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * A {@link ReuseNode} is a vertex in the {@link ReuseTree} that
 * contains (a possible empty) set of outgoing {@link ReuseEdge}s.
 * Each {@link ReuseNode} may contain a {@link SystemState} holding
 * relevant informations (e.g. database identifiers) that belongs
 * to the system state that 'represents' the system state after executing
 * a membership query.
 *
 * @author Oliver Bauer <oliver.bauer@tu-dortmund.de>
 */
public class ReuseNode {
	private final String name;
	private Map<String, ReuseEdge> edges;
	private SystemState avail;

	public ReuseNode(String name) {
		this.name = name;
		this.edges = new HashMap<String, ReuseEdge>();
	}

	public boolean hasState() {
		return avail != null;
	}

	/**
	 * The {@link SystemState}, maybe <code>null</code>.
	 *
	 * @return
	 */
	public SystemState getSystemState() {
		return avail;
	}

	public void setSystemState(SystemState state) {
		this.avail = state;
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
	public Collection<ReuseEdge> getEdges() {
		return this.edges.values();
	}

	/**
	 * Adds a ougoing {@link ReuseEdge} to this {@link ReuseNode}.
	 *
	 * @param edge
	 */
	public void addEdge(ReuseEdge edge) {
		this.edges.put(edge.getInput(), edge);
	}

	/**
	 * Maybe <code>null</code>.
	 *
	 * @param input
	 * @return
	 */
	public ReuseEdge getEdgeWithInput(String input) {
		return this.edges.get(input);
	}

	public ReuseNode getTargetNodeForInput(String input) {
		ReuseEdge edge = this.getEdgeWithInput(input);
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
