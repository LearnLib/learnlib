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
package de.learnlib.algorithm.ttt.dfa;

import java.util.function.Supplier;

import de.learnlib.acex.AcexAnalyzer;
import de.learnlib.acex.OutInconsPrefixTransformAcex;
import de.learnlib.algorithm.LearningAlgorithm.DFALearner;
import de.learnlib.algorithm.ttt.base.AbstractBaseDTNode;
import de.learnlib.algorithm.ttt.base.AbstractTTTLearner;
import de.learnlib.algorithm.ttt.base.BaseTTTDiscriminationTree;
import de.learnlib.algorithm.ttt.base.OutputInconsistency;
import de.learnlib.algorithm.ttt.base.TTTState;
import de.learnlib.algorithm.ttt.base.TTTTransition;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.tooling.annotation.builder.GenerateBuilder;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.word.Word;

/**
 * A {@link DFA}-based specialization of {@link AbstractTTTLearner}.
 *
 * @param <I>
 *         input symbol type
 */
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

        dtree.getRoot().split(Word.epsilon(), false, true);
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
        Boolean aBoolean = dtree.getRoot().subtreeLabel(dfaState.getDTLeaf());
        assert aBoolean != null;
        dfaState.accepting = aBoolean;
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
