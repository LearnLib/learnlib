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
package de.learnlib.algorithm.lambda.ttt.mealy;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import de.learnlib.algorithm.lambda.ttt.dt.AbstractDTNode;
import de.learnlib.algorithm.lambda.ttt.dt.Children;
import de.learnlib.algorithm.lambda.ttt.dt.DTInnerNode;
import de.learnlib.algorithm.lambda.ttt.dt.DTLeaf;
import org.checkerframework.checker.nullness.qual.Nullable;

class ChildrenMealy<I, D> implements Children<I, D> {

    private final Map<D, AbstractDTNode<I, D>> children = new HashMap<>();

    @Override
    public @Nullable AbstractDTNode<I, D> child(D out) {
        return children.get(out);
    }

    @Override
    public D key(AbstractDTNode<I, D> child) {
        for (Map.Entry<D, AbstractDTNode<I, D>> e : children.entrySet()) {
            if (e.getValue() == child) {
                return e.getKey();
            }
        }
        throw new IllegalArgumentException("No valid child specified");
    }

    @Override
    public void addChild(D out, AbstractDTNode<I, D> child) {
        children.put(out, child);
    }

    @Override
    public void replace(DTLeaf<I, D> oldNode, DTInnerNode<I, D> newNode) {
        children.put(key(oldNode), newNode);
    }

    @Override
    public Collection<AbstractDTNode<I, D>> all() {
        return children.values();
    }
}
