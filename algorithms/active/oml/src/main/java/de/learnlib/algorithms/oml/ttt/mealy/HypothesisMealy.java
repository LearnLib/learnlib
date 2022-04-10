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
package de.learnlib.algorithms.oml.ttt.mealy;

import java.util.Collection;

import de.learnlib.algorithms.oml.ttt.dt.DTLeaf;
import de.learnlib.algorithms.oml.ttt.pt.PTNode;
import de.learnlib.algorithms.oml.ttt.pt.PrefixTree;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.words.Word;

/**
 * @author fhowar
 */
class HypothesisMealy<I, O> implements MealyMachine<DTLeaf<I, Word<O>>, I, MealyTransition<I, O>, O> {

    private final PrefixTree<I, Word<O>> ptree;
    private final DecisionTreeMealy<I, O> dtree;

    HypothesisMealy(PrefixTree<I, Word<O>> ptree, DecisionTreeMealy<I, O> dtree) {
        this.ptree = ptree;
        this.dtree = dtree;
    }

    @Override
    public Collection<DTLeaf<I, Word<O>>> getStates() {
        return dtree.leaves();
    }

    @Override
    public O getTransitionOutput(MealyTransition<I, O> o) {
        return dtree.getOutput(o.source, o.input).lastSymbol();
    }

    @Override
    public MealyTransition<I, O> getTransition(DTLeaf<I, Word<O>> iWordDTLeaf, I i) {
        return new MealyTransition<>(iWordDTLeaf, i);
    }

    @Override
    public DTLeaf<I, Word<O>> getSuccessor(MealyTransition<I, O> o) {
        PTNode<I, Word<O>> u = o.source.getShortPrefixes().get(0);
        assert u != null;
        PTNode<I, Word<O>> ua = u.succ(o.input);
        assert ua != null;
        DTLeaf<I, Word<O>> dst = ua.state();
        assert dst != null;
        return dst;
    }

    @Override
    public DTLeaf<I, Word<O>> getInitialState() {
        return ptree.root().state();
    }
}
