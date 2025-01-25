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
package de.learnlib.algorithm.lambda.ttt.dt;

import java.util.List;

import de.learnlib.algorithm.lambda.ttt.pt.PTNode;
import de.learnlib.algorithm.lambda.ttt.st.STNode;
import org.checkerframework.checker.nullness.qual.Nullable;

public class DTInnerNode<I, D> extends AbstractDTNode<I, D> {

    private final STNode<I> suffix;
    private final Children<I, D> children;

    public DTInnerNode(@Nullable DTInnerNode<I, D> parent,
                       AbstractDecisionTree<I, D> tree,
                       Children<I, D> children,
                       STNode<I> suffix) {
        super(parent, tree);
        this.children = children;
        this.suffix = suffix;
    }

    public Children<I, D> getChildren() {
        return children;
    }

    @Override
    void sift(PTNode<I, D> prefix) {
        D out = tree.query(prefix, suffix);
        AbstractDTNode<I, D> succ = children.child(out);
        if (succ != null) {
            succ.sift(prefix);
        } else {
            DTLeaf<I, D> newLeaf = new DTLeaf<>(this, tree, prefix);
            children.addChild(out, newLeaf);
            prefix.setState(newLeaf);

            for (I a : tree.getAlphabet()) {
                PTNode<I, D> ua = prefix.append(a);
                tree.root().sift(ua);
            }
        }
    }

    @Override
    void leaves(List<DTLeaf<I, D>> list) {
        for (AbstractDTNode<I, D> n : children.all()) {
            n.leaves(list);
        }
    }

    void replace(DTLeaf<I, D> oldNode, DTInnerNode<I, D> newNode) {
        this.children.replace(oldNode, newNode);
    }

    STNode<I> suffix() {
        return suffix;
    }
}
