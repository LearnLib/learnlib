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

import java.util.ArrayList;
import java.util.List;

import de.learnlib.algorithm.lambda.ttt.pt.PTNode;
import de.learnlib.algorithm.lambda.ttt.st.STNode;
import de.learnlib.oracle.MembershipOracle;
import net.automatalib.alphabet.Alphabet;

public abstract class AbstractDecisionTree<I, D> {

    private final STNode<I> stRoot;
    protected final MembershipOracle<I, D> mqOracle;
    protected final Alphabet<I> alphabet;

    protected AbstractDTNode<I, D> root;

    protected AbstractDecisionTree(Alphabet<I> alphabet, MembershipOracle<I, D> mqOracle, STNode<I> stRoot) {
        this.mqOracle = mqOracle;
        this.alphabet = alphabet;
        this.stRoot = stRoot;
    }

    protected abstract Children<I, D> newChildren();

    protected abstract D query(PTNode<I, D> prefix, STNode<I> suffix);

    public void sift(PTNode<I, D> prefix) {
        root.sift(prefix);
    }

    public void setRoot(AbstractDTNode<I, D> newRoot) {
        this.root = newRoot;
    }

    public List<DTLeaf<I, D>> leaves() {
        List<DTLeaf<I, D>> list = new ArrayList<>();
        root.leaves(list);
        return list;
    }

    public boolean makeConsistent() {
        List<DTLeaf<I, D>> leaves = new ArrayList<>();
        root.leaves(leaves);
        for (DTLeaf<I, D> n : leaves) {
            if (n.refineIfPossible()) {
                return true;
            }
        }
        return false;
    }

    AbstractDTNode<I, D> root() {
        return root;
    }

    Alphabet<I> getAlphabet() {
        return alphabet;
    }

    STNode<I> newSuffix(I a) {
        return stRoot.prepend(a);
    }
}
