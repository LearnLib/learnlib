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
package de.learnlib.datastructure.discriminationtree.iterators;

import java.util.Iterator;
import java.util.function.Function;

import de.learnlib.datastructure.discriminationtree.model.AbstractDTNode;
import net.automatalib.common.util.collection.IteratorUtil;

/**
 * Factory methods for several kinds of discrimination tree node iterators.
 */
public final class DiscriminationTreeIterators {

    private DiscriminationTreeIterators() {
        // prevent instantiation
    }

    /**
     * Returns an iterator that traverses all nodes of a subtree of a given discrimination tree node.
     *
     * @param root
     *         the root node, from which traversal should start
     * @param <N>
     *         node type
     *
     * @return an iterator that traverses all nodes of a subtree of the given node
     */
    public static <N extends AbstractDTNode<?, ?, ?, N>> Iterator<N> nodeIterator(N root) {
        return new NodeIterator<>(root);
    }

    /**
     * Returns an iterator that traverses all inner nodes (no leaves) of a subtree of a given discrimination tree node.
     *
     * @param root
     *         the root node, from which traversal should start
     * @param <N>
     *         node type
     *
     * @return an iterator that traverses all inner nodes of a subtree of the given node
     */
    @SuppressWarnings("nullness") // lambda is only called with our non-null nodes as argument
    public static <N extends AbstractDTNode<?, ?, ?, N>> Iterator<N> innerNodeIterator(N root) {
        return IteratorUtil.filter(nodeIterator(root), n -> !n.isLeaf());
    }

    /**
     * Returns an iterator that traverses all leaves (no inner nodes) of a subtree of a given discrimination tree node.
     *
     * @param root
     *         the root node, from which traversal should start
     * @param <N>
     *         node type
     *
     * @return an iterator that traverses all leaf nodes of a subtree of the given node
     */
    public static <N extends AbstractDTNode<?, ?, ?, N>> Iterator<N> leafIterator(N root) {
        return IteratorUtil.filter(nodeIterator(root), AbstractDTNode::isLeaf);
    }

    /**
     * Returns an iterator that traverses all leaves (no inner nodes) of a subtree of a given discrimination tree node.
     * Additionally, allows one to specify a transformer that is applied to the leaf nodes
     *
     * @param root
     *         the root node, from which traversal should start
     * @param transformer
     *         the transformer that transforms iterated nodes
     * @param <N>
     *         node type
     * @param <D>
     *         transformation target data type
     *
     * @return an iterator that traverses all (transformed) leaf nodes of a subtree of the given node
     */
    public static <N extends AbstractDTNode<?, ?, ?, N>, D> Iterator<D> transformingLeafIterator(N root,
                                                                                                 Function<? super N, D> transformer) {
        return IteratorUtil.map(leafIterator(root), transformer);
    }
}
