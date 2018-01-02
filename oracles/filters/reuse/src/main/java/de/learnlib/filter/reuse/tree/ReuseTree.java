/* Copyright (C) 2013-2018 TU Dortmund
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
package de.learnlib.filter.reuse.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

import de.learnlib.filter.reuse.ReuseCapableOracle;
import de.learnlib.filter.reuse.ReuseException;
import de.learnlib.filter.reuse.ReuseOracle;
import de.learnlib.filter.reuse.tree.BoundedDeque.AccessPolicy;
import de.learnlib.filter.reuse.tree.BoundedDeque.EvictPolicy;
import net.automatalib.graphs.Graph;
import net.automatalib.visualization.VisualizationHelper;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

/**
 * The {@link ReuseTree} is a tree like structure consisting of nodes (see {@link ReuseNode}) and edges (see {@link
 * ReuseEdge}) that is used by the {@link ReuseOracle}: <ul> <li>Nodes may contain a system state (see {@link
 * ReuseNode#fetchSystemState(boolean)}) that could be used for executing suffixes of membership queries. Each node
 * consists of a (possible empty) set of outgoing edges. <li>Edges consists beside source and target node of input and
 * output behavior. </ul> The {@link ReuseTree} is the central data structure that maintains observed behavior from the
 * SUL and maintains also available system states. The {@link ReuseTree} is only 'tree like' since it may contain
 * reflexive edges at nodes (only possible if {@link ReuseTreeBuilder#withFailureOutputs(Set)} or {@link
 * ReuseTreeBuilder#withInvariantInputs(Set)} is set).
 *
 * @param <S>
 *         system state class
 * @param <I>
 *         input symbol class
 * @param <O>
 *         output symbol class
 *
 * @author Oliver Bauer
 */
public final class ReuseTree<S, I, O> implements Graph<ReuseNode<S, I, O>, ReuseEdge<S, I, O>> {

    private final Alphabet<I> alphabet;
    private final int alphabetSize;
    private final Set<I> invariantInputSymbols;
    private final Set<O> failureOutputSymbols;
    private final boolean invalidateSystemstates;
    private final SystemStateHandler<S> systemStateHandler;
    private final int maxSystemStates;
    private final AccessPolicy accessPolicy;
    private final EvictPolicy evictPolicy;
    /** Maybe reset to zero, see {@link ReuseTree#clearTree()}. */
    private int nodeCount;
    /** Maybe reinitialized , see {@link ReuseTree#clearTree()}. */
    private ReuseNode<S, I, O> root;

    private ReuseTree(ReuseTreeBuilder<S, I, O> builder) {
        this.alphabet = builder.alphabet;
        this.invalidateSystemstates = builder.invalidateSystemstates;
        SystemStateHandler<S> handler = builder.systemStateHandler;
        // If the specified handler is null, no action is required
        if (handler == null) {
            handler = state -> {
            };
        }
        this.systemStateHandler = handler;
        this.invariantInputSymbols =
                (builder.invariantInputSymbols != null) ? builder.invariantInputSymbols : Collections.emptySet();
        this.failureOutputSymbols =
                (builder.failureOutputSymbols != null) ? builder.failureOutputSymbols : Collections.emptySet();

        this.maxSystemStates = builder.maxSystemStates;
        this.accessPolicy = builder.accessPolicy;
        this.evictPolicy = builder.evictPolicy;

        // local and not configurable
        this.alphabetSize = alphabet.size();
        this.root = createNode();
    }

    private ReuseNode<S, I, O> createNode() {
        return new ReuseNode<>(nodeCount++, alphabetSize, maxSystemStates, accessPolicy, evictPolicy);
    }

    /**
     * Returns the known output for the given query or {@code null} if not known.
     *
     * @param query
     *         Not allowed to be {@code null}.
     *
     * @return The output for {@code query} if already known from the {@link ReuseTree} or {@code null} if unknown.
     */
    public synchronized Word<O> getOutput(final Word<I> query) {
        if (query == null) {
            String msg = "Query is not allowed to be null.";
            throw new IllegalArgumentException(msg);
        }

        final WordBuilder<O> output = new WordBuilder<>();

        ReuseNode<S, I, O> sink = getRoot();
        for (final I symbol : query) {
            final ReuseEdge<S, I, O> edge = sink.getEdgeWithInput(alphabet.getSymbolIndex(symbol));
            if (edge == null) {
                return null;
            }
            output.add(edge.getOutput());
            sink = edge.getTarget();
        }

        return output.toWord();
    }

    /**
     * Returns the root {@link ReuseNode} of the {@link ReuseTree}.
     *
     * @return root The root of the tree, never {@code null}.
     */
    public ReuseNode<S, I, O> getRoot() {
        return this.root;
    }

    /**
     * Returns the known output for "reflexive" edges in the tree for the given query. All other symbols are set to
     * {@code null}.
     *
     * @param query
     *         Not allowed to be {@code null}.
     *
     * @return The partial output for {@code query} from the {@link ReuseTree} with outputs for "reflexive" edges filled
     * with {@code null} for "non-reflexive" and not-known parts of the input word.
     */
    public synchronized Word<O> getPartialOutput(Word<I> query) {
        if (query == null) {
            String msg = "Query is not allowed to be null.";
            throw new IllegalArgumentException(msg);
        }

        final WordBuilder<O> output = new WordBuilder<>();

        ReuseNode<S, I, O> sink = getRoot();
        for (final I symbol : query) {
            final ReuseEdge<S, I, O> edge = sink.getEdgeWithInput(alphabet.getSymbolIndex(symbol));
            // add null-pointers if no more outputs are available
            if (edge == null) {
                break;
            }
            // add output for "reflexive" edges
            if (sink.equals(edge.getTarget())) {
                output.add(edge.getOutput());
            } else { // for "non-reflexive" edges add a null-pointer.
                output.add(null);
            }
            sink = edge.getTarget();
        }
        // fill the output with null-pointers to the size of the query.
        output.repeatAppend(query.size() - output.size(), (O) null);
        return output.toWord();
    }

    /**
     * This method removes all system states from the tree. The tree structure remains, but there will be nothing for
     * reusage.
     * <p>
     * The {@link SystemStateHandler} will be informed about all disposings.
     */
    public synchronized void disposeSystemstates() {
        disposeSystemstates(getRoot());
    }

    private void disposeSystemstates(ReuseNode<S, I, O> node) {
        Iterator<S> stateIt = node.systemStatesIterator();
        while (stateIt.hasNext()) {
            S state = stateIt.next();
            systemStateHandler.dispose(state);
        }
        node.clearSystemStates();

        for (ReuseEdge<S, I, O> edge : node.getEdges()) {
            if (edge != null) {
                if (!edge.getTarget().equals(node)) {
                    // only for non reflexive edges, there are no circles in a tree
                    disposeSystemstates(edge.getTarget());
                }
            }
        }
    }

    /**
     * Clears the whole tree which means the root will be reinitialized by a new {@link ReuseNode} and all existing
     * system states will be disposed. All invariant input symbols as well as all failure output symbols will remain.
     * <p>
     * The {@link SystemStateHandler} will <b>not</b> be informed about any disposings.
     */
    public synchronized void clearTree() {
        this.nodeCount = 0;
        disposeSystemstates(root);
        this.root = createNode();
    }

    /**
     * Returns a reuseable {@link ReuseNode.NodeResult} with system state or {@code null} if none such exists. If
     * ''oldInvalidated'' was set to {@code true} (in the {@link ReuseOracle}) the system state is already removed from
     * the tree whenever one was available.
     *
     * @param query
     *         Not allowed to be {@code null}.
     */
    public synchronized ReuseNode.NodeResult<S, I, O> fetchSystemState(Word<I> query) {
        if (query == null) {
            String msg = "Query is not allowed to be null.";
            throw new IllegalArgumentException(msg);
        }

        int length = 0;

        ReuseNode<S, I, O> sink = getRoot();
        ReuseNode<S, I, O> lastState = null;
        if (sink.hasSystemStates()) {
            lastState = sink;
        }

        ReuseNode<S, I, O> node;
        for (int i = 0; i < query.size(); i++) {
            node = sink.getTargetNodeForInput(alphabet.getSymbolIndex(query.getSymbol(i)));

            if (node == null) {
                // we have reached longest known prefix
                break;
            }

            sink = node;
            if (sink.hasSystemStates()) {
                lastState = sink;
                length = i + 1;
            }
        }

        if (lastState == null) {
            return null;
        }

        S systemState = lastState.fetchSystemState(invalidateSystemstates);

        return new ReuseNode.NodeResult<>(lastState, systemState, length);
    }

    /**
     * Inserts the given {@link Word} with {@link ReuseCapableOracle.QueryResult} into the tree starting from the root
     * node of the tree. For the longest known prefix of the given {@link Word} there will be no new nodes or edges
     * created.
     * <p>
     * Will be called from the {@link ReuseOracle} if no system state was available for reusage for the query (otherwise
     * {@link #insert(Word, ReuseNode, ReuseCapableOracle.QueryResult)} would be called). The last node reached by the
     * last symbol of the query will hold the system state from the given {@link ReuseCapableOracle.QueryResult}.
     * <p>
     * This method should only be invoked internally from the {@link ReuseOracle} unless you know exactly what you are
     * doing (you may want to create a predefined reuse tree before start learning).
     *
     * @throws ReuseException
     *         if non deterministic behavior is detected
     */
    public synchronized void insert(Word<I> query, ReuseCapableOracle.QueryResult<S, O> queryResult) {
        insert(query, getRoot(), queryResult);
    }

    /**
     * Inserts the given {@link Word} (suffix of a membership query) with {@link ReuseCapableOracle.QueryResult} (suffix
     * output) into the tree starting from the {@link ReuseNode} (contains prefix with prefix output) in the tree. For
     * the longest known prefix of the suffix from the given {@link Word} there will be no new nodes or edges created.
     * <p>
     * Will be called from the {@link ReuseOracle} if an available system state was reused for the query (otherwise
     * {@link #insert(Word, ReuseCapableOracle.QueryResult)} would be called). The old system state was already removed
     * from the {@link ReuseNode} (through {@link #fetchSystemState(Word)}) if the ''invalidateSystemstates'' flag in
     * the {@link ReuseOracle} was set to {@code true}.
     * <p>
     * This method should only be invoked internally from the {@link ReuseOracle} unless you know exactly what you are
     * doing (you may want to create a predefined reuse tree before start learning).
     *
     * @throws ReuseException
     *         if non deterministic behavior is detected
     */
    public synchronized void insert(Word<I> query,
                                    ReuseNode<S, I, O> sink,
                                    ReuseCapableOracle.QueryResult<S, O> queryResult) {
        if (queryResult == null) {
            String msg = "The queryResult is not allowed to be null.";
            throw new IllegalArgumentException(msg);
        }
        if (sink == null) {
            String msg = "Node is not allowed to be null, called wrong method?";
            throw new IllegalArgumentException(msg);
        }
        if (query.size() != queryResult.output.size()) {
            String msg = "Size mismatch: " + query + "/" + queryResult.output;
            throw new IllegalArgumentException(msg);
        }

        ReuseNode<S, I, O> effectiveSink = sink;

        for (int i = 0; i < query.size(); i++) {
            I in = query.getSymbol(i);
            O out = queryResult.output.getSymbol(i);

            ReuseEdge<S, I, O> edge = effectiveSink.getEdgeWithInput(alphabet.getSymbolIndex(in));
            if (edge != null) {
                if (Objects.equals(edge.getOutput(), out)) {
                    effectiveSink = edge.getTarget();
                    continue;
                }

                throw new ReuseException(
                        "Conflict: input '" + query + "', output '" + queryResult.output + "', i=" + i +
                        ", cached output '" + edge.getOutput() + "'");
            }

            ReuseNode<S, I, O> rn;

            if (failureOutputSymbols.contains(out)) {
                rn = effectiveSink;
            } else if (invariantInputSymbols.contains(in)) {
                rn = effectiveSink;
            } else {
                rn = createNode();
            }

            int index = alphabet.getSymbolIndex(in);
            effectiveSink.addEdge(index, new ReuseEdge<>(effectiveSink, rn, in, out));
            effectiveSink = rn;
        }

        S evictedState = effectiveSink.addSystemState(queryResult.newState);
        if (evictedState != null) {
            systemStateHandler.dispose(evictedState);
        }
    }

    @Override
    public Collection<ReuseNode<S, I, O>> getNodes() {
        Collection<ReuseNode<S, I, O>> collection = new ArrayList<>();
        appendNodesRecursively(collection, getRoot());
        return collection;
    }

    private void appendNodesRecursively(Collection<ReuseNode<S, I, O>> nodes, ReuseNode<S, I, O> current) {
        nodes.add(current);
        for (int i = 0; i < alphabetSize; i++) {
            ReuseEdge<S, I, O> reuseEdge = current.getEdgeWithInput(i);
            if (reuseEdge == null) {
                continue;
            }
            if (!current.equals(reuseEdge.getTarget())) {
                appendNodesRecursively(nodes, reuseEdge.getTarget());
            }
        }
    }

    @Override
    public synchronized Collection<ReuseEdge<S, I, O>> getOutgoingEdges(ReuseNode<S, I, O> node) {
        return node.getEdges();
    }

    @Override
    public synchronized ReuseNode<S, I, O> getTarget(ReuseEdge<S, I, O> edge) {
        if (edge != null) {
            return edge.getTarget();
        }
        return null;
    }

    @Override
    public synchronized VisualizationHelper<ReuseNode<S, I, O>, ReuseEdge<S, I, O>> getVisualizationHelper() {
        return new ReuseTreeDotHelper<>();
    }

    public static class ReuseTreeBuilder<S, I, O> {

        // mandatory
        private final Alphabet<I> alphabet;

        // optional
        private boolean invalidateSystemstates = true;
        private SystemStateHandler<S> systemStateHandler;
        private Set<I> invariantInputSymbols;
        private Set<O> failureOutputSymbols;
        private int maxSystemStates = -1;
        private AccessPolicy accessPolicy = AccessPolicy.LIFO;
        private EvictPolicy evictPolicy = EvictPolicy.EVICT_OLDEST;

        public ReuseTreeBuilder(Alphabet<I> alphabet) {
            this.alphabet = alphabet;
            this.systemStateHandler = null;
            this.invariantInputSymbols = Collections.emptySet();
            this.failureOutputSymbols = Collections.emptySet();
        }

        public ReuseTreeBuilder<S, I, O> withSystemStateHandler(SystemStateHandler<S> systemStateHandler) {
            this.systemStateHandler = systemStateHandler;
            return this;
        }

        public ReuseTreeBuilder<S, I, O> withEnabledSystemstateInvalidation(boolean invalidate) {
            this.invalidateSystemstates = invalidate;
            return this;
        }

        public ReuseTreeBuilder<S, I, O> withInvariantInputs(Set<I> inputs) {
            this.invariantInputSymbols = inputs;
            return this;
        }

        public ReuseTreeBuilder<S, I, O> withFailureOutputs(Set<O> outputs) {
            this.failureOutputSymbols = outputs;
            return this;
        }

        public ReuseTreeBuilder<S, I, O> withMaxSystemStates(int maxSystemStates) {
            this.maxSystemStates = maxSystemStates;
            return this;
        }

        public ReuseTreeBuilder<S, I, O> withAccessPolicy(AccessPolicy accessPolicy) {
            this.accessPolicy = accessPolicy;
            return this;
        }

        public ReuseTreeBuilder<S, I, O> withEvictPolicy(EvictPolicy evictPolicy) {
            this.evictPolicy = evictPolicy;
            return this;
        }

        public ReuseTree<S, I, O> build() {
            return new ReuseTree<>(this);
        }
    }
}
