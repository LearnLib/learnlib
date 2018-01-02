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
import java.util.function.Function;

import com.google.common.collect.AbstractIterator;
import de.learnlib.datastructure.discriminationtree.model.AbstractDTNode;

/**
 * Iterator that traverses all leaves (no inner nodes) of a subtree of a given discrimination tree node. Additionally
 * allows to specify a transformer that is applied to the leaf nodes. If the transformer yields {@code null} for an
 * iterated leaf, the transformed value will be skipped.
 *
 * @param <N>
 *         node type
 * @param <D>
 *         type of transformation result
 *
 * @author MalteIsberner
 * @author frohme
 */
public class TransformingLeavesIterator<N extends AbstractDTNode<?, ?, ?, N>, D> extends AbstractIterator<D> {

    private final Deque<N> stack = new ArrayDeque<>();
    private final Function<N, D> extractor;

    public TransformingLeavesIterator(N root, Function<N, D> extractor) {
        stack.push(root);
        this.extractor = extractor;
    }

    @Override
    protected D computeNext() {
        while (!stack.isEmpty()) {
            N curr = stack.pop();

            if (curr.isLeaf()) {
                final D value = this.extractor.apply(curr);
                if (value != null) {
                    return value;
                }
            } else {
                for (N child : curr.getChildren()) {
                    stack.push(child);
                }
            }
        }

        return endOfData();
    }
}
