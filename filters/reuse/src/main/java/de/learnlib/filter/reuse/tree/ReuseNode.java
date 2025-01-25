/* Copyright (C) 2013-2025 TU Dortmund University
 * This file is part of LearnLib <https://learnlib.de>.
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
package de.learnlib.filter.reuse.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import de.learnlib.filter.reuse.tree.BoundedDeque.AccessPolicy;
import de.learnlib.filter.reuse.tree.BoundedDeque.EvictPolicy;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A {@link ReuseNode} is a vertex in the {@link ReuseTree} that contains (a possibly empty) set of outgoing {@link
 * ReuseEdge}s. Each {@link ReuseNode} may contain a system state holding relevant information (e.g. database
 * identifiers or an object) that belongs to the system state that 'represents' the system state after executing a
 * membership query.
 *
 * @param <S>
 *         system state class
 * @param <I>
 *         input symbol class
 * @param <O>
 *         output symbol class
 */
public class ReuseNode<S, I, O> {

    private final @Nullable ReuseEdge<S, I, O>[] edges;
    private final BoundedDeque<S> systemStates;
    private final int id;

    @SuppressWarnings("unchecked")
    public ReuseNode(int id,
                     int alphabetSize,
                     int maxSystemStates,
                     AccessPolicy accessPolicy,
                     EvictPolicy evictPolicy) {
        this.edges = new ReuseEdge[alphabetSize];
        this.id = id;
        this.systemStates = new BoundedDeque<>(maxSystemStates, accessPolicy, evictPolicy);
    }

    /**
     * Retrieve a system state.
     *
     * @param remove
     *         a flag whether the system state should be removed from the internal storage after retrieval
     *
     * @return a system state, May be {@code null}.
     */
    public @Nullable S fetchSystemState(boolean remove) {
        if (remove) {
            return systemStates.retrieve();
        }
        return systemStates.peek();
    }

    public @Nullable S addSystemState(S state) {
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
     * Returns all outgoing {@link ReuseEdge}s from this {@link ReuseNode}.
     *
     * @return the outgoing edges of this node
     */
    public Collection<ReuseEdge<S, I, O>> getEdges() {
        final List<ReuseEdge<S, I, O>> result = new ArrayList<>(edges.length);
        for (ReuseEdge<S, I, O> edge : edges) {
            if (edge != null) {
                result.add(edge);
            }
        }
        return result;
    }

    /**
     * Adds an outgoing {@link ReuseEdge} to this {@link ReuseNode}.
     *
     * @param index
     *         the position (index) of the edge to add
     * @param edge
     *         the edge to add
     */
    public void addEdge(int index, ReuseEdge<S, I, O> edge) {
        this.edges[index] = edge;
    }

    public @Nullable ReuseNode<S, I, O> getTargetNodeForInput(int index) {
        ReuseEdge<S, I, O> edge = this.getEdgeWithInput(index);
        if (edge == null) {
            return null;
        }
        return edge.getTarget();
    }

    /**
     * Return the edge with the given index.
     *
     * @param index
     *         the index of the edge
     *
     * @return the edge with the given index. May be {@code null}.
     */
    public @Nullable ReuseEdge<S, I, O> getEdgeWithInput(int index) {
        return this.edges[index];
    }

    public int getId() {
        return this.id;
    }

    public static final class NodeResult<S, I, O> {

        public final ReuseNode<S, I, O> reuseNode;
        public final S systemState;
        /**
         * The prefix length for a membership query that leads to the {@link ReuseNode} in the reuse tree.
         */
        public final int prefixLength;

        public NodeResult(ReuseNode<S, I, O> reuseNode, S systemState, int prefixLength) {
            this.reuseNode = reuseNode;
            this.systemState = systemState;
            this.prefixLength = prefixLength;
        }
    }

}
