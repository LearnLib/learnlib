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
package de.learnlib.algorithms.adt.adt;

import java.util.Collection;
import java.util.Map;

import de.learnlib.algorithms.adt.util.ADTUtil;
import de.learnlib.api.oracle.SymbolQueryOracle;
import net.automatalib.graphs.ads.RecursiveADSNode;
import net.automatalib.visualization.VisualizationHelper;
import net.automatalib.words.Word;

/**
 * The ADT equivalent of {@link net.automatalib.graphs.ads.ADSNode}. In contrast to regular adaptive distinguishing
 * sequences, an ADT node may also represent a reset node that semantically separates multiple ADSs.
 *
 * @param <S>
 *         (hypothesis) state type
 * @param <I>
 *         input alphabet type
 * @param <O>
 *         output alphabet type
 *
 * @author frohme
 */
public interface ADTNode<S, I, O> extends RecursiveADSNode<S, I, O, ADTNode<S, I, O>> {

    /**
     * Utility method, that sifts a given word through {@code this} ADTNode. If {@code this} node is a <ul> <li>symbol
     * node, the symbol is applied to the system under learning and the corresponding child node (based on the observed
     * output) is returned. If no matching child node is found, a new leaf node is returned instead </li> <li> reset
     * node, the system under learning is reset and the provided prefix is reapplied to the system </li> <li> leaf node,
     * an exception is thrown </li> </ul>
     *
     * @param oracle
     *         the oracle used to query the system under learning
     * @param prefix
     *         the prefix to be re-applied after encountering a reset node
     *
     * @return the corresponding child node
     *
     * @throws UnsupportedOperationException
     *         when invoked on a leaf node (see {@link #getNodeType()}.
     */
    ADTNode<S, I, O> sift(SymbolQueryOracle<I, O> oracle, Word<I> prefix) throws UnsupportedOperationException;

    // default methods for graph interface
    @Override
    default Collection<ADTNode<S, I, O>> getNodes() {
        return getNodesForRoot(this);
    }

    @Override
    default VisualizationHelper<ADTNode<S, I, O>, ADTNode<S, I, O>> getVisualizationHelper() {
        return new VisualizationHelper<ADTNode<S, I, O>, ADTNode<S, I, O>>() {

            @Override
            public boolean getNodeProperties(final ADTNode<S, I, O> node, final Map<String, String> properties) {
                if (ADTUtil.isResetNode(node)) {
                    properties.put(NodeAttrs.SHAPE, NodeShapes.OCTAGON);
                    properties.put(NodeAttrs.LABEL, "reset");
                } else if (ADTUtil.isLeafNode(node)) {
                    properties.put(NodeAttrs.SHAPE, NodeShapes.BOX);
                    properties.put(NodeAttrs.LABEL, String.valueOf(node.getHypothesisState()));
                } else {
                    properties.put(NodeAttrs.LABEL, node.toString());
                    properties.put(NodeAttrs.SHAPE, NodeShapes.OVAL);
                }

                return true;
            }

            @Override
            public boolean getEdgeProperties(final ADTNode<S, I, O> src,
                                             final ADTNode<S, I, O> edge,
                                             final ADTNode<S, I, O> tgt,
                                             final Map<String, String> properties) {

                for (final Map.Entry<O, ADTNode<S, I, O>> e : src.getChildren().entrySet()) {
                    if (e.getValue().equals(tgt) && !ADTUtil.isResetNode(src)) {
                        properties.put(EdgeAttrs.LABEL, e.getKey().toString());
                        return true;
                    }
                }
                return true;
            }
        };
    }

    default boolean isLeaf() {
        return NodeType.LEAF_NODE == this.getNodeType();
    }

    /**
     * Returns the node type of the current node.
     *
     * @return the node type
     */
    NodeType getNodeType();

    /**
     * Utility enum to distinguish the 3 possible types of ADT nodes.
     */
    enum NodeType {
        SYMBOL_NODE,
        RESET_NODE,
        LEAF_NODE
    }
}
