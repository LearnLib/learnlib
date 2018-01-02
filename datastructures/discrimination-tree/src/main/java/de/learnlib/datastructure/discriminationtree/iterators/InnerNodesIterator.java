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
package de.learnlib.datastructure.discriminationtree.iterators;

import java.util.ArrayDeque;
import java.util.Deque;

import com.google.common.collect.AbstractIterator;
import de.learnlib.datastructure.discriminationtree.model.AbstractDTNode;

/**
 * Iterator that traverses all inner nodes (no leaves) of a subtree of a given discrimination tree node.
 *
 * @param <N>
 *         node type
 *
 * @author Malte Isberner
 * @author frohme
 */
public class InnerNodesIterator<N extends AbstractDTNode<?, ?, ?, N>> extends AbstractIterator<N> {

    private final Deque<N> stack = new ArrayDeque<>();

    public InnerNodesIterator(N root) {
        if (!root.isLeaf()) {
            stack.push(root);
        }
    }

    @Override
    protected N computeNext() {
        while (!stack.isEmpty()) {
            N curr = stack.pop();

            if (!curr.isLeaf()) {
                for (N child : curr.getChildren()) {
                    stack.push(child);
                }
                return curr;
            }
        }
        return endOfData();
    }
}
