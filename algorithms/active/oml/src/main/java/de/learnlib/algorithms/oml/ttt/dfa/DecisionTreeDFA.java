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
package de.learnlib.algorithms.oml.ttt.dfa;

import de.learnlib.algorithms.oml.ttt.dt.AbstractDTNode;
import de.learnlib.algorithms.oml.ttt.dt.AbstractDecisionTree;
import de.learnlib.algorithms.oml.ttt.dt.Children;
import de.learnlib.algorithms.oml.ttt.dt.DTInnerNode;
import de.learnlib.algorithms.oml.ttt.dt.DTLeaf;
import de.learnlib.algorithms.oml.ttt.pt.PTNode;
import de.learnlib.algorithms.oml.ttt.st.STNode;
import de.learnlib.api.oracle.MembershipOracle;
import net.automatalib.words.Alphabet;

/**
 * @author fhowar
 */
class DecisionTreeDFA<I> extends AbstractDecisionTree<I, Boolean> {

    DecisionTreeDFA(MembershipOracle<I, Boolean> mqOracle, Alphabet<I> sigma, STNode<I> stRoot) {
        super(sigma, mqOracle, stRoot);
    }

    boolean isAccepting(DTLeaf<I, Boolean> s) {
        AbstractDTNode<I, Boolean> n = s.path().get(1);
        return localRoot().getChildren().key(n);
    }

    @Override
    protected Children<I, Boolean> newChildren() {
        return new ChildrenDFA<>();
    }

    @Override
    protected Boolean query(PTNode<I, Boolean> prefix, STNode<I> suffix) {
        return mqOracle.answerQuery(prefix.word(), suffix.word());
    }

    private DTInnerNode<I, Boolean> localRoot() {
        return (DTInnerNode<I, Boolean>) root;
    }

}
