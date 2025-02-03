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
package de.learnlib.algorithm.ttt.mealy;

import java.util.ArrayList;
import java.util.List;

import de.learnlib.acex.AcexAnalyzer;
import de.learnlib.acex.MealyOutInconsPrefixTransformAcex;
import de.learnlib.acex.OutInconsPrefixTransformAcex;
import de.learnlib.algorithm.LearningAlgorithm;
import de.learnlib.algorithm.ttt.base.AbstractBaseDTNode;
import de.learnlib.algorithm.ttt.base.AbstractTTTLearner;
import de.learnlib.algorithm.ttt.base.BaseTTTDiscriminationTree;
import de.learnlib.algorithm.ttt.base.OutputInconsistency;
import de.learnlib.algorithm.ttt.base.TTTState;
import de.learnlib.algorithm.ttt.base.TTTTransition;
import de.learnlib.datastructure.list.IntrusiveListEntry;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.query.DefaultQuery;
import de.learnlib.query.Query;
import de.learnlib.tooling.annotation.builder.GenerateBuilder;
import de.learnlib.util.mealy.MealyUtil;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;

/**
 * A {@link MealyMachine}-based specialization of {@link AbstractTTTLearner}.
 *
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbols type
 */
public class TTTLearnerMealy<I, O> extends AbstractTTTLearner<MealyMachine<?, I, ?, O>, I, Word<O>>
        implements LearningAlgorithm.MealyLearner<I, O> {

    @GenerateBuilder(defaults = AbstractTTTLearner.BuilderDefaults.class)
    public TTTLearnerMealy(Alphabet<I> alphabet, MembershipOracle<I, Word<O>> oracle, AcexAnalyzer analyzer) {
        super(alphabet,
              oracle,
              new TTTHypothesisMealy<>(alphabet),
              new BaseTTTDiscriminationTree<>(oracle, TTTDTNodeMealy::new),
              analyzer);
    }

    @Override
    @SuppressWarnings("unchecked")
    public MealyMachine<?, I, ?, O> getHypothesisModel() {
        return (TTTHypothesisMealy<I, O>) hypothesis;
    }

    @Override
    protected TTTTransition<I, Word<O>> createTransition(TTTState<I, Word<O>> state, I sym) {
        return new TTTTransitionMealy<>(state, sym);
    }

    @Override
    protected void initTransitions(TTTTransition<I, Word<O>> head, int num) {
        final List<TransitionOutputQuery<I, O>> queries = new ArrayList<>(num);
        IntrusiveListEntry<TTTTransition<I, Word<O>>> iter = head;

        for (int i = 0; i < num; i++) {
            assert iter != null;
            queries.add(new TransitionOutputQuery<>((TTTTransitionMealy<I, O>) iter.getElement()));
            iter = iter.getNext();
        }

        oracle.processQueries(queries);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected boolean refineHypothesisSingle(DefaultQuery<I, Word<O>> ceQuery) {
        DefaultQuery<I, Word<O>> shortenedCeQuery =
                MealyUtil.shortenCounterExample((TTTHypothesisMealy<I, O>) hypothesis, ceQuery);
        return shortenedCeQuery != null && super.refineHypothesisSingle(shortenedCeQuery);
    }

    @Override
    protected OutInconsPrefixTransformAcex<I, Word<O>> deriveAcex(OutputInconsistency<I, Word<O>> outIncons) {
        TTTState<I, Word<O>> source = outIncons.srcState;
        Word<I> suffix = outIncons.suffix;

        OutInconsPrefixTransformAcex<I, Word<O>> acex = new MealyOutInconsPrefixTransformAcex<>(suffix,
                                                                                                oracle,
                                                                                                w -> getDeterministicState(
                                                                                                        source,
                                                                                                        w).getAccessSequence());

        acex.setEffect(0, outIncons.targetOut);
        Word<O> lastHypOut = computeHypothesisOutput(getAnySuccessor(source, suffix.prefix(-1)), suffix.suffix(1));
        acex.setEffect(suffix.length() - 1, lastHypOut);
        return acex;
    }

    @Override
    protected Word<O> succEffect(Word<O> effect) {
        return effect.subWord(1);
    }

    @Override
    protected OutputInconsistency<I, Word<O>> findOutputInconsistency() {
        OutputInconsistency<I, Word<O>> best = null;

        for (TTTState<I, Word<O>> state : hypothesis.getStates()) {
            AbstractBaseDTNode<I, Word<O>> node = state.getDTLeaf();
            while (!node.isRoot()) {
                Word<O> expectedOut = node.getParentOutcome();
                node = node.getParent();
                Word<I> suffix = node.getDiscriminator();
                Word<O> hypOut = computeHypothesisOutput(state, suffix);
                int mismatchIdx = MealyUtil.findMismatch(expectedOut, hypOut);
                if (mismatchIdx != MealyUtil.NO_MISMATCH && (best == null || mismatchIdx <= best.suffix.length())) {
                    best = new OutputInconsistency<>(state,
                                                     suffix.prefix(mismatchIdx + 1),
                                                     expectedOut.prefix(mismatchIdx + 1));
                }
            }
        }
        return best;
    }

    @Override
    protected Word<O> predictSuccOutcome(TTTTransition<I, Word<O>> trans,
                                         AbstractBaseDTNode<I, Word<O>> succSeparator) {
        TTTTransitionMealy<I, O> mtrans = (TTTTransitionMealy<I, O>) trans;
        if (succSeparator == null) {
            return Word.fromLetter(mtrans.output);
        }
        Word<O> subtreeLabel = succSeparator.subtreeLabel(trans.getDTTarget());
        assert subtreeLabel != null;
        return subtreeLabel.prepend(mtrans.output);
    }

    @Override
    protected Word<O> computeHypothesisOutput(TTTState<I, Word<O>> state, Word<I> suffix) {
        TTTState<I, Word<O>> curr = state;

        WordBuilder<O> wb = new WordBuilder<>(suffix.length());

        for (I sym : suffix) {
            TTTTransitionMealy<I, O> trans = (TTTTransitionMealy<I, O>) hypothesis.getInternalTransition(curr, sym);
            wb.append(trans.output);
            curr = getAnyTarget(trans);
        }

        return wb.toWord();
    }

    @Override
    protected AbstractBaseDTNode<I, Word<O>> createNewNode(AbstractBaseDTNode<I, Word<O>> parent,
                                                           Word<O> parentOutput) {
        return new TTTDTNodeMealy<>(parent, parentOutput);
    }

    private static final class TransitionOutputQuery<I, O> extends Query<I, Word<O>> {

        private final TTTTransitionMealy<I, O> transition;

        TransitionOutputQuery(TTTTransitionMealy<I, O> transition) {
            this.transition = transition;
        }

        @Override
        public void answer(Word<O> output) {
            transition.output = output.firstSymbol();
        }

        @Override
        public Word<I> getPrefix() {
            return transition.getSource().getAccessSequence();
        }

        @Override
        public Word<I> getSuffix() {
            return Word.fromLetter(transition.getInput());
        }
    }
}
