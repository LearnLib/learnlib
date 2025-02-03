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
package de.learnlib.datastructure.discriminationtree.model;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;

import de.learnlib.oracle.MembershipOracle;
import de.learnlib.query.DefaultQuery;
import net.automatalib.common.util.array.ArrayStorage;
import net.automatalib.common.util.collection.BitSetIterator;
import net.automatalib.common.util.collection.CollectionUtil;
import net.automatalib.graph.Graph;
import net.automatalib.visualization.DefaultVisualizationHelper;
import net.automatalib.visualization.VisualizationHelper;
import net.automatalib.word.Word;

/**
 * An abstract super class for aggregating some information/functionality for discrimination trees.
 *
 * @param <DSCR>
 *         type of discriminator
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbol type
 * @param <D>
 *         data symbol type
 * @param <N>
 *         node type
 */
public abstract class AbstractDiscriminationTree<DSCR, I, O, D, N extends AbstractDTNode<DSCR, O, D, N>>
        implements Graph<N, Map.Entry<O, N>> {

    private final N root;
    protected transient MembershipOracle<I, O> oracle;

    public AbstractDiscriminationTree(N root, MembershipOracle<I, O> oracle) {
        this.root = root;
        this.oracle = oracle;
    }

    public N sift(Word<I> prefix) {
        return sift(root, prefix);
    }

    public N sift(N start, Word<I> prefix) {
        return sift(start, prefix, n -> !n.isLeaf());
    }

    protected N sift(N start, Word<I> prefix, Predicate<N> continueExploring) {
        N curr = start;

        while (continueExploring.test(curr)) {
            final DefaultQuery<I, O> query = buildQuery(curr, prefix);
            oracle.processQuery(query);
            curr = curr.child(query.getOutput());
        }

        return curr;
    }

    public List<N> sift(List<N> starts, List<Word<I>> prefixes) {
        assert starts.size() == prefixes.size();
        return sift(starts, prefixes, n -> !n.isLeaf());
    }

    protected List<N> sift(List<N> starts, List<Word<I>> prefixes, Predicate<N> continueExploring) {
        assert starts.size() == prefixes.size();

        if (starts.isEmpty()) {
            return Collections.emptyList();
        } else if (starts.size() == 1) {
            return Collections.singletonList(sift(starts.get(0), prefixes.get(0), continueExploring));
        }

        final int size = starts.size();
        final ArrayStorage<N> result = new ArrayStorage<>(starts);
        final BitSet activeVector = new BitSet(size);

        for (int i = 0; i < size; i++) {
            activeVector.set(i, continueExploring.test(result.get(i)));
        }

        final List<DefaultQuery<I, O>> queries = new ArrayList<>(activeVector.cardinality());
        final List<Word<I>> prefixStorage = CollectionUtil.randomAccessList(prefixes);

        while (!activeVector.isEmpty()) {

            final BitSetIterator preIter = new BitSetIterator(activeVector);

            while (preIter.hasNext()) {
                final int idx = preIter.nextInt();
                queries.add(buildQuery(result.get(idx), prefixStorage.get(idx)));
            }

            oracle.processQueries(queries);

            final BitSetIterator postIter = new BitSetIterator(activeVector);
            final Iterator<DefaultQuery<I, O>> responseIter = queries.iterator();

            while (postIter.hasNext()) {
                final int idx = postIter.nextInt();
                final N current = result.get(idx);
                final O out = responseIter.next().getOutput();
                final N child = current.child(out);
                result.set(idx, child);

                if (!continueExploring.test(child)) {
                    activeVector.clear(idx);
                }
            }

            queries.clear();
        }

        return result;
    }

    public N getRoot() {
        return root;
    }

    public N leastCommonAncestor(N a, N b) {
        N lower, higher;

        if (a.getDepth() > b.getDepth()) {
            lower = a;
            higher = b;
        } else {
            lower = b;
            higher = a;
        }

        while (lower.getDepth() > higher.getDepth()) {
            lower = lower.getParent();
        }

        while (lower != higher) {
            lower = lower.getParent();
            higher = higher.getParent();
        }

        return lower;
    }

    /**
     * Fetches for two nodes information about their lowest common ancestor in {@code this} discrimination tree. {@link
     * LCAInfo#subtree1Label} will reference the label of the outgoing child transition for the node closer to the LCA,
     * {@link LCAInfo#subtree2Label} the label of the outgoing child transition for the node farther away from the LCA.
     * If both nodes have equal depth, {@link LCAInfo#subtree1Label} contains {@code node1}'s label and {@link
     * LCAInfo#subtree2Label} {@code node2}'s label.
     * <p>
     * Either {@link LCAInfo#subtree1Label} or {@link LCAInfo#subtree2Label} is {@code null}, if {@code node1} ({@code
     * node2} respectively) already is the LCA.
     *
     * @param node1
     *         first node
     * @param node2
     *         second node
     *
     * @return the corresponding {@link LCAInfo}.
     */
    public LCAInfo<O, N> lcaInfo(N node1, N node2) {
        int d1 = node1.depth;
        int d2 = node2.depth;

        int ddiff = d2 - d1;

        boolean swap = false;

        N curr1, curr2;
        if (ddiff >= 0) {
            curr1 = node1;
            curr2 = node2;
        } else {
            curr1 = node2;
            curr2 = node1;
            ddiff *= -1;
            swap = true;
        }

        O out1 = null, out2 = null;
        while (ddiff > 0) {
            out2 = curr2.parentOutcome;
            curr2 = curr2.parent;
            ddiff--;
        }

        if (curr1 == curr2) {
            return new LCAInfo<>(curr1, out1, out2, swap);
        }

        while (curr1 != curr2) {
            out1 = curr1.parentOutcome;
            curr1 = curr1.parent;
            out2 = curr2.parentOutcome;
            curr2 = curr2.parent;
        }

        return new LCAInfo<>(curr1, out1, out2, swap);
    }

    protected abstract DefaultQuery<I, O> buildQuery(N node, Word<I> prefix);

    /*
     * AutomataLib Graph API
     */
    @Override
    public Collection<N> getNodes() {
        final List<N> result = new ArrayList<>(); // a tree should have no cycles, so no checking for duplicates
        getNodes(result, root);
        return result;
    }

    private void getNodes(Collection<N> nodes, N node) {
        nodes.add(node);
        if (!node.isLeaf()) {
            for (N child : node.getChildren()) {
                getNodes(nodes, child);
            }
        }
    }

    @Override
    public Collection<Entry<O, N>> getOutgoingEdges(N node) {
        if (node.isLeaf()) {
            return Collections.emptySet();
        }
        return node.getChildEntries();
    }

    @Override
    public N getTarget(Entry<O, N> edge) {
        return edge.getValue();
    }

    @Override
    public VisualizationHelper<N, Entry<O, N>> getVisualizationHelper() {
        return new DefaultVisualizationHelper<N, Entry<O, N>>() {

            @Override
            public boolean getNodeProperties(N node, Map<String, String> properties) {
                super.getNodeProperties(node, properties);

                if (node.isLeaf()) {
                    properties.put(NodeAttrs.SHAPE, NodeShapes.BOX);
                    properties.put(NodeAttrs.LABEL, String.valueOf(node.getData()));
                } else {
                    final DSCR d = node.getDiscriminator();
                    properties.put(NodeAttrs.SHAPE, NodeShapes.OVAL);
                    properties.put(NodeAttrs.LABEL, d.toString());
                }

                return true;
            }

            @Override
            public boolean getEdgeProperties(N src, Entry<O, N> edge, N tgt, Map<String, String> properties) {
                super.getEdgeProperties(src, edge, tgt, properties);

                properties.put(EdgeAttrs.LABEL, String.valueOf(edge.getKey()));

                return true;
            }
        };
    }

    public void setOracle(MembershipOracle<I, O> oracle) {
        this.oracle = oracle;
    }
}
