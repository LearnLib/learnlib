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
package de.learnlib.algorithms.ttt.moore;

import com.github.misberner.buildergen.annotations.GenerateBuilder;
import de.learnlib.acex.AcexAnalyzer;
import de.learnlib.algorithms.ttt.base.AbstractBaseDTNode;
import de.learnlib.algorithms.ttt.base.AbstractTTTLearner;
import de.learnlib.algorithms.ttt.base.BaseTTTDiscriminationTree;
import de.learnlib.algorithms.ttt.base.OutputInconsistency;
import de.learnlib.algorithms.ttt.base.TTTState;
import de.learnlib.algorithms.ttt.base.TTTTransition;
import de.learnlib.api.algorithm.LearningAlgorithm.MooreLearner;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.counterexamples.acex.MooreOutInconsPrefixTransformAcex;
import de.learnlib.counterexamples.acex.OutInconsPrefixTransformAcex;
import net.automatalib.automata.transducers.MooreMachine;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

/**
 * A {@link MooreMachine}-based specialization of the TTT learner.
 *
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbols type
 *
 * @author bayram
 * @author frohme
 */
public class TTTLearnerMoore<I, O> extends AbstractTTTLearner<MooreMachine<?, I, ?, O>, I, Word<O>>
        implements MooreLearner<I, O> {

    @GenerateBuilder(defaults = AbstractTTTLearner.BuilderDefaults.class)
    public TTTLearnerMoore(Alphabet<I> alphabet, MembershipOracle<I, Word<O>> oracle, AcexAnalyzer analyzer) {
        super(alphabet,
              oracle,
              new TTTHypothesisMoore<>(alphabet),
              new BaseTTTDiscriminationTree<>(oracle, TTTDTNodeMoore::new),
              analyzer);

        dtree.getRoot().split(Word.epsilon(), oracle.answerQuery(Word.epsilon()));
    }

    @Override
    protected Word<O> succEffect(Word<O> effect) {
        return effect.subWord(1);
    }

    @Override
    protected Word<O> predictSuccOutcome(TTTTransition<I, Word<O>> trans,
                                         AbstractBaseDTNode<I, Word<O>> succSeparator) {
        TTTStateMoore<I, O> curr = (TTTStateMoore<I, O>) trans.getSource();
        return succSeparator.subtreeLabel(trans.getDTTarget()).prepend(curr.getOutput());
    }

    @Override
    protected void initializeState(TTTState<I, Word<O>> state) {
        super.initializeState(state);

        TTTStateMoore<I, O> mooreState = (TTTStateMoore<I, O>) state;
        O output = dtree.getRoot().subtreeLabel(mooreState.getDTLeaf()).firstSymbol();
        assert output != null;
        mooreState.setOutput(output);
    }

    @Override
    protected OutInconsPrefixTransformAcex<I, Word<O>> deriveAcex(OutputInconsistency<I, Word<O>> outIncons) {
        TTTState<I, Word<O>> source = outIncons.srcState;
        Word<I> suffix = outIncons.suffix;

        OutInconsPrefixTransformAcex<I, Word<O>> acex = new MooreOutInconsPrefixTransformAcex<>(suffix,
                                                                                                oracle,
                                                                                                w -> getDeterministicState(
                                                                                                        source,
                                                                                                        w).getAccessSequence());

        acex.setEffect(0, outIncons.targetOut);
        Word<O> lastHypOut = computeHypothesisOutput(getAnySuccessor(source, suffix), Word.epsilon());
        acex.setEffect(suffix.length(), lastHypOut);

        return acex;
    }

    @Override
    protected Word<O> computeHypothesisOutput(TTTState<I, Word<O>> state, Word<I> suffix) {

        TTTStateMoore<I, O> curr = (TTTStateMoore<I, O>) state;

        WordBuilder<O> wb = new WordBuilder<>(suffix.length());

        wb.append(curr.output);
        if (suffix.length() == 0) {

            return wb.toWord();
        }
        for (I sym : suffix) {
            curr = (TTTStateMoore<I, O>) getAnySuccessor(curr, sym);
            wb.append(curr.output);

        }
        return wb.toWord();

    }

    @Override
    protected AbstractBaseDTNode<I, Word<O>> createNewNode(AbstractBaseDTNode<I, Word<O>> parent,
                                                           Word<O> parentOutput) {
        return new TTTDTNodeMoore<>(parent, parentOutput);
    }

    @Override
    @SuppressWarnings("unchecked") // parent class uses the same instance that we pass in the constructor
    public TTTHypothesisMoore<I, O> getHypothesisModel() {
        return (TTTHypothesisMoore<I, O>) hypothesis;
    }

}