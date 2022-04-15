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

import de.learnlib.algorithms.oml.ttt.AbstractOptimalTTT;
import de.learnlib.algorithms.oml.ttt.dt.AbstractDecisionTree;
import de.learnlib.algorithms.oml.ttt.dt.DTInnerNode;
import de.learnlib.algorithms.oml.ttt.dt.DTLeaf;
import de.learnlib.api.oracle.MembershipOracle;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

public class OptimalTTTDFA<I> extends AbstractOptimalTTT<DFA<?, I>, I, Boolean> {

    private final HypothesisDFA<I> hypothesis;
    private final DecisionTreeDFA<I> dtree;

    public OptimalTTTDFA(Alphabet<I> alphabet, MembershipOracle<I, Boolean> mqo) {
        this(alphabet, mqo, mqo);
    }

    public OptimalTTTDFA(Alphabet<I> alphabet, MembershipOracle<I, Boolean> mqs, MembershipOracle<I, Boolean> ceqs) {
        super(alphabet, ceqs);
        dtree = new DecisionTreeDFA<>(mqs, alphabet, strie.root());
        DTInnerNode<I, Boolean> dtRoot = new DTInnerNode<>(null, dtree, new ChildrenDFA<>(), strie.root());
        dtree.setRoot(dtRoot);
        hypothesis = new HypothesisDFA<>(ptree, dtree);
    }

    @Override
    protected int maxSearchIndex(int ceLength) {
        return ceLength;
    }

    @Override
    protected DTLeaf<I, Boolean> getState(Word<I> prefix) {
        return hypothesis.getState(prefix);
    }

    @Override
    public DFA<?, I> getHypothesisModel() {
        return hypothesis;
    }

    @Override
    protected AbstractDecisionTree<I, Boolean> dtree() {
        return dtree;
    }

    @Override
    protected Boolean suffix(Boolean output, int length) {
        return output;
    }

    @Override
    protected boolean isCanonical() {
        return hypothesis.getStates().stream().noneMatch(it -> it.getShortPrefixes().size() > 1);
    }

    @Override
    protected Boolean hypOutput(Word<I> word, int length) {
        return hypOutput(word);
    }

    protected Boolean hypOutput(Word<I> word) {
        DTLeaf<I, Boolean> s = getState(word);
        return dtree.isAccepting(s);
    }
}
