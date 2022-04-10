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

import java.util.Collection;

import de.learnlib.algorithms.oml.ttt.dt.DTLeaf;
import de.learnlib.algorithms.oml.ttt.pt.PTNode;
import de.learnlib.algorithms.oml.ttt.pt.PrefixTree;
import net.automatalib.automata.fsa.DFA;

/**
 * @author fhowar
 */
class HypothesisDFA<I> implements DFA<DTLeaf<I, Boolean>, I> {

    private final PrefixTree<I, Boolean> ptree;
    private final DecisionTreeDFA<I> dtree;

    HypothesisDFA(PrefixTree<I, Boolean> ptree, DecisionTreeDFA<I> dtree) {
        this.ptree = ptree;
        this.dtree = dtree;
    }

    @Override
    public DTLeaf<I, Boolean> getTransition(DTLeaf<I, Boolean> s, I a) {
        PTNode<I, Boolean> u = s.getShortPrefixes().get(0);
        assert u != null;
        PTNode<I, Boolean> ua = u.succ(a);
        assert ua != null;
        DTLeaf<I, Boolean> dst = ua.state();
        assert dst != null;
        return dst;
    }

    @Override
    public boolean isAccepting(DTLeaf<I, Boolean> s) {
        assert s != null;
        return dtree.isAccepting(s);
    }

    @Override
    public DTLeaf<I, Boolean> getInitialState() {
        return ptree.root().state();
    }

    @Override
    public Collection<DTLeaf<I, Boolean>> getStates() {
        return dtree.leaves();
    }

}
