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

import de.learnlib.algorithms.oml.ttt.AbstractOptimalTTT;
import de.learnlib.algorithms.oml.ttt.dt.AbstractDecisionTree;
import de.learnlib.algorithms.oml.ttt.dt.DTLeaf;
import de.learnlib.api.algorithm.LearningAlgorithm.MealyLearner;
import de.learnlib.api.oracle.MembershipOracle;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

/**
 * @author fhowar
 */
public class OptimalTTTMealy<I, O> extends AbstractOptimalTTT<MealyMachine<?, I, ?, O>, I, Word<O>>
        implements MealyLearner<I, O> {

    private final HypothesisMealy<I, O> hypothesis;
    private final DecisionTreeMealy<I, O> dtree;

    public OptimalTTTMealy(Alphabet<I> alphabet, MembershipOracle<I, Word<O>> mqo) {
        this(alphabet, mqo, mqo);
    }

    public OptimalTTTMealy(Alphabet<I> alphabet, MembershipOracle<I, Word<O>> mqs, MembershipOracle<I, Word<O>> ceqs) {
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
    protected Word<O> hypOutput(Word<I> word, int length) {
        return hypothesis.computeOutput(word).suffix(length);
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
    protected Word<O> suffix(Word<O> output, int length) {
        return output.suffix(length);
    }

    @Override
    protected boolean isCanonical() {
        return hypothesis.getStates().stream().noneMatch(it -> it.getShortPrefixes().size() > 1);
    }

}
