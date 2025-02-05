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

import de.learnlib.algorithm.LearningAlgorithm.MealyLearner;
import de.learnlib.algorithm.lambda.ttt.AbstractTTTLambda;
import de.learnlib.algorithm.lambda.ttt.dt.AbstractDecisionTree;
import de.learnlib.algorithm.lambda.ttt.dt.DTLeaf;
import de.learnlib.oracle.MembershipOracle;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.word.Word;

public class TTTLambdaMealy<I, O> extends AbstractTTTLambda<MealyMachine<?, I, ?, O>, I, Word<O>>
        implements MealyLearner<I, O> {

    private final HypothesisMealy<I, O> hypothesis;
    private final DecisionTreeMealy<I, O> dtree;

    public TTTLambdaMealy(Alphabet<I> alphabet, MembershipOracle<I, Word<O>> mqo) {
        this(alphabet, mqo, mqo);
    }

    public TTTLambdaMealy(Alphabet<I> alphabet, MembershipOracle<I, Word<O>> mqs, MembershipOracle<I, Word<O>> ceqs) {
        super(alphabet, ceqs);
        dtree = new DecisionTreeMealy<>(mqs, alphabet, strie.root());
        DTLeaf<I, Word<O>> dtRoot = new DTLeaf<>(null, dtree, ptree.root());
        dtree.setRoot(dtRoot);
        ptree.root().setState(dtRoot);
        for (I a : alphabet) {
            dtree.sift(ptree.root().append(a));
        }
        hypothesis = new HypothesisMealy<>(ptree, dtree);
    }

    @Override
    protected int maxSearchIndex(int ceLength) {
        return ceLength - 1;
    }

    @Override
    protected DTLeaf<I, Word<O>> getState(Word<I> prefix) {
        return hypothesis.getState(prefix);
    }

    @Override
    public MealyMachine<?, I, ?, O> getHypothesisModel() {
        return hypothesis;
    }

    @Override
    protected AbstractDecisionTree<I, Word<O>> dtree() {
        return dtree;
    }

    @Override
    public int size() {
        return hypothesis.size();
    }
}
