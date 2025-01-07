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
package de.learnlib.algorithm.adt.adt;

import java.util.Collection;
import java.util.Map;

import de.learnlib.algorithm.adt.util.ADTUtil;
import net.automatalib.graph.ads.RecursiveADSNode;
import net.automatalib.visualization.VisualizationHelper;

/**
 * The ADT equivalent of {@link net.automatalib.graph.ads.ADSNode}. In contrast to regular adaptive distinguishing
 * sequences, an ADT node may also represent a reset node that semantically separates multiple ADSs.
 *
 * @param <S>
 *         (hypothesis) state type
 * @param <I>
 *         input alphabet type
 * @param <O>
 *         output alphabet type
 */
public interface ADTNode<S, I, O> extends RecursiveADSNode<S, I, O, ADTNode<S, I, O>> {

    /**
     * Convenience method for directly accessing this node's {@link #getChildren() children}.
     *
     * @param output
     *         the output symbol to determine the child to returned
     *
     * @return the child node that is mapped to given output. May be {@code null},
     */
    default ADTNode<S, I, O> getChild(O output) {
        return getChildren().get(output);
    }

    // default methods for graph interface
    @Override
    default Collection<ADTNode<S, I, O>> getNodes() {
        return getNodesForRoot(this);
    }

    @Override
    default VisualizationHelper<ADTNode<S, I, O>, ADTNode<S, I, O>> getVisualizationHelper() {
        return new VisualizationHelper<ADTNode<S, I, O>, ADTNode<S, I, O>>() {

            @Override
            public boolean getNodeProperties(ADTNode<S, I, O> node, Map<String, String> properties) {
                if (ADTUtil.isResetNode(node)) {
                    properties.put(NodeAttrs.SHAPE, NodeShapes.OCTAGON);
                    properties.put(NodeAttrs.LABEL, "reset");
                } else if (ADTUtil.isLeafNode(node)) {
                    properties.put(NodeAttrs.SHAPE, NodeShapes.BOX);
                    properties.put(NodeAttrs.LABEL, String.valueOf(node.getState()));
                } else {
                    properties.put(NodeAttrs.LABEL, node.toString());
                    properties.put(NodeAttrs.SHAPE, NodeShapes.OVAL);
                }

                return true;
            }

            @Override
            public boolean getEdgeProperties(ADTNode<S, I, O> src,
                                             ADTNode<S, I, O> edge,
                                             ADTNode<S, I, O> tgt,
                                             Map<String, String> properties) {

                for (Map.Entry<O, ADTNode<S, I, O>> e : src.getChildren().entrySet()) {
                    if (e.getValue().equals(tgt) && !ADTUtil.isResetNode(src)) {
                        properties.put(EdgeAttrs.LABEL, e.getKey().toString());
                        return true;
                    }
                }
                return true;
            }
        };
    }

    @Override
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
