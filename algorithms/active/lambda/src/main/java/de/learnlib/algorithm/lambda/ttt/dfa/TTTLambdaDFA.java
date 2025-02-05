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
package de.learnlib.algorithm.lambda.ttt.dfa;

import de.learnlib.algorithm.LearningAlgorithm.DFALearner;
import de.learnlib.algorithm.lambda.ttt.AbstractTTTLambda;
import de.learnlib.algorithm.lambda.ttt.dt.AbstractDecisionTree;
import de.learnlib.algorithm.lambda.ttt.dt.DTInnerNode;
import de.learnlib.algorithm.lambda.ttt.dt.DTLeaf;
import de.learnlib.oracle.MembershipOracle;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.word.Word;

public class TTTLambdaDFA<I> extends AbstractTTTLambda<DFA<?, I>, I, Boolean> implements DFALearner<I> {

    private final HypothesisDFA<I> hypothesis;
    private final DecisionTreeDFA<I> dtree;

    public TTTLambdaDFA(Alphabet<I> alphabet, MembershipOracle<I, Boolean> mqo) {
        this(alphabet, mqo, mqo);
    }

    public TTTLambdaDFA(Alphabet<I> alphabet, MembershipOracle<I, Boolean> mqs, MembershipOracle<I, Boolean> ceqs) {
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
    public int size() {
        return hypothesis.size();
    }
}
