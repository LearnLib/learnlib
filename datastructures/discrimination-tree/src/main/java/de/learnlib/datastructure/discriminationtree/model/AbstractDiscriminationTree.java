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
package de.learnlib.datastructure.discriminationtree.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Iterables;
import de.learnlib.api.oracle.MembershipOracle;
import net.automatalib.graphs.Graph;
import net.automatalib.util.graphs.traversal.GraphTraversal;
import net.automatalib.visualization.DefaultVisualizationHelper;
import net.automatalib.visualization.VisualizationHelper;
import net.automatalib.words.Word;

/**
 * An abstract super class for aggregating several information/functionality for discrimination trees.
 *
 * @param <DSCR>
 *         type of discrimantor
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbol type
 * @param <D>
 *         data symbol type
 * @param <N>
 *         node type
 *
 * @author frohme
 */
public abstract class AbstractDiscriminationTree<DSCR, I, O, D, N extends AbstractDTNode<DSCR, O, D, N>>
        implements Graph<N, Map.Entry<O, N>>, Serializable {

    protected final N root;
    protected transient MembershipOracle<I, O> oracle;

    public AbstractDiscriminationTree(N root, MembershipOracle<I, O> oracle) {
        this.root = root;
        this.oracle = oracle;
    }

    public N sift(Word<I> prefix) {
        return sift(root, prefix);
    }

    public abstract N sift(N start, Word<I> prefix);

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

    /*
     * AutomataLib Graph API
     */
    @Override
    public Collection<N> getNodes() {
        List<N> nodes = new ArrayList<>();
        Iterables.addAll(nodes, GraphTraversal.breadthFirstOrder(this, Collections.singleton(root)));
        return nodes;
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
                if (!super.getNodeProperties(node, properties)) {
                    return false;
                }
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
                if (!super.getEdgeProperties(src, edge, tgt, properties)) {
                    return false;
                }
                properties.put(EdgeAttrs.LABEL, String.valueOf(edge.getKey()));
                return true;
            }
        };
    }

    public void setOracle(MembershipOracle<I, O> oracle) {
        this.oracle = oracle;
    }
}
