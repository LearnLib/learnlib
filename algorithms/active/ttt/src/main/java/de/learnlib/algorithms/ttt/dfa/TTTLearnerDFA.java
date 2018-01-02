/* Copyright (C) 2013-2018 TU Dortmund
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
package de.learnlib.algorithms.ttt.dfa;

import java.util.function.Supplier;

import com.github.misberner.buildergen.annotations.GenerateBuilder;
import de.learnlib.acex.AcexAnalyzer;
import de.learnlib.algorithms.ttt.base.AbstractBaseDTNode;
import de.learnlib.algorithms.ttt.base.AbstractTTTLearner;
import de.learnlib.algorithms.ttt.base.BaseTTTDiscriminationTree;
import de.learnlib.algorithms.ttt.base.OutputInconsistency;
import de.learnlib.algorithms.ttt.base.TTTState;
import de.learnlib.algorithms.ttt.base.TTTTransition;
import de.learnlib.api.algorithm.LearningAlgorithm.DFALearner;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.counterexamples.acex.OutInconsPrefixTransformAcex;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

public class TTTLearnerDFA<I> extends AbstractTTTLearner<DFA<?, I>, I, Boolean> implements DFALearner<I> {

    @GenerateBuilder(defaults = AbstractTTTLearner.BuilderDefaults.class)
    public TTTLearnerDFA(Alphabet<I> alphabet, MembershipOracle<I, Boolean> oracle, AcexAnalyzer analyzer) {
        this(alphabet, oracle, analyzer, TTTDTNodeDFA::new);
    }

    protected TTTLearnerDFA(Alphabet<I> alphabet,
                            MembershipOracle<I, Boolean> oracle,
                            AcexAnalyzer analyzer,
                            Supplier<? extends AbstractBaseDTNode<I, Boolean>> rootSupplier) {
        super(alphabet,
              oracle,
              new TTTHypothesisDFA<>(alphabet),
              new BaseTTTDiscriminationTree<>(oracle, rootSupplier),
              analyzer);

        split(dtree.getRoot(), Word.epsilon(), false, true);
    }

    @Override
    @SuppressWarnings("unchecked")
    public DFA<?, I> getHypothesisModel() {
        return (TTTHypothesisDFA<I>) hypothesis;
    }

    @Override
    protected void initializeState(TTTState<I, Boolean> state) {
        super.initializeState(state);

        TTTStateDFA<I> dfaState = (TTTStateDFA<I>) state;
        dfaState.accepting = dtree.getRoot().subtreeLabel(dfaState.getDTLeaf());
    }

    @Override
    protected OutInconsPrefixTransformAcex<I, Boolean> deriveAcex(OutputInconsistency<I, Boolean> outIncons) {
        OutInconsPrefixTransformAcex<I, Boolean> acex = super.deriveAcex(outIncons);
        acex.setEffect(acex.getLength() - 1, !outIncons.targetOut);
        return acex;
    }

    @Override
    protected Boolean succEffect(Boolean effect) {
        return effect;
    }

    @Override
    protected Boolean predictSuccOutcome(TTTTransition<I, Boolean> trans,
                                         AbstractBaseDTNode<I, Boolean> succSeparator) {
        return succSeparator.subtreeLabel(trans.getDTTarget());
    }

    @Override
    protected Boolean computeHypothesisOutput(TTTState<I, Boolean> state, Word<I> suffix) {
        TTTState<I, Boolean> endState = getAnySuccessor(state, suffix);
        return ((TTTStateDFA<I>) endState).accepting;
    }

    @Override
    @SuppressWarnings("unchecked")
    public TTTHypothesisDFA<I> getHypothesisDS() {
        return (TTTHypothesisDFA<I>) hypothesis;
    }

    @Override
    protected AbstractBaseDTNode<I, Boolean> createNewNode(AbstractBaseDTNode<I, Boolean> parent,
                                                           Boolean parentOutput) {
        return new TTTDTNodeDFA<>(parent, parentOutput);
    }
}
