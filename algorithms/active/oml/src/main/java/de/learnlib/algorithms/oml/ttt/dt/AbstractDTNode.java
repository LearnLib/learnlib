/* Copyright (C) 2013-2022 TU Dortmund
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
package de.learnlib.algorithms.oml.ttt.dt;

import java.util.ArrayList;
import java.util.List;

import de.learnlib.algorithms.oml.ttt.pt.PTNode;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * @author fhowar
 */
public abstract class AbstractDTNode<I, D> {

    final @Nullable DTInnerNode<I, D> parent;
    final AbstractDecisionTree<I, D> tree;

    public AbstractDTNode(@Nullable DTInnerNode<I, D> parent, AbstractDecisionTree<I, D> tree) {
        this.parent = parent;
        this.tree = tree;
    }

    public List<AbstractDTNode<I, D>> path() {
        List<AbstractDTNode<I, D>> path = new ArrayList<>();
        this.path(path);
        return path;
    }

    void path(List<AbstractDTNode<I, D>> path) {
        path.add(0, this);
        if (parent != null) {
            parent.path(path);
        }
    }

    abstract void sift(PTNode<I, D> prefix);

    abstract void leaves(List<DTLeaf<I, D>> list);

}

