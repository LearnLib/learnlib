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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;

import de.learnlib.datastructure.discriminationtree.model.AbstractDTNode;

/**
 * Iterator that traverses all nodes of a subtree of a given discrimination tree node.
 *
 * @param <N>
 *         node type
 */
class NodeIterator<N extends AbstractDTNode<?, ?, ?, N>> implements Iterator<N> {

    private final Deque<N> stack = new ArrayDeque<>();

    NodeIterator(N root) {
        stack.push(root);
    }

    @Override
    public boolean hasNext() {
        return !stack.isEmpty();
    }

    @Override
    public N next() {
        if (stack.isEmpty()) {
            throw new NoSuchElementException();
        }

        final N curr = stack.pop();

        if (!curr.isLeaf()) {
            for (N child : curr.getChildren()) {
                stack.push(child);
            }
        }

        return curr;
    }
}
