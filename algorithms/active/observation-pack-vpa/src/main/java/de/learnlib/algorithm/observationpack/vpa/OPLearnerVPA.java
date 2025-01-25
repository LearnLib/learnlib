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
package de.learnlib.algorithm.observationpack.vpa;

import java.util.ArrayList;
import java.util.List;

import de.learnlib.acex.AbstractBaseCounterexample;
import de.learnlib.acex.AcexAnalyzer;
import de.learnlib.algorithm.observationpack.vpa.hypothesis.AbstractHypTrans;
import de.learnlib.algorithm.observationpack.vpa.hypothesis.ContextPair;
import de.learnlib.algorithm.observationpack.vpa.hypothesis.DTNode;
import de.learnlib.algorithm.observationpack.vpa.hypothesis.HypLoc;
import de.learnlib.oracle.MembershipOracle.DFAMembershipOracle;
import de.learnlib.query.DefaultQuery;
import de.learnlib.tooling.annotation.builder.GenerateBuilder;
import net.automatalib.alphabet.VPAlphabet;
import net.automatalib.automaton.vpa.SEVPA;
import net.automatalib.automaton.vpa.StackContents;
import net.automatalib.automaton.vpa.State;
import net.automatalib.common.util.collection.IterableUtil;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A {@link SEVPA}-based adoption of the "observation pack" algorithm.
 *
 * @param <I>
 *         input symbol type
 */
public class OPLearnerVPA<I> extends AbstractVPALearner<I> {

    protected final AcexAnalyzer analyzer;

    @GenerateBuilder(defaults = BuilderDefaults.class)
    public OPLearnerVPA(VPAlphabet<I> alphabet, DFAMembershipOracle<I> oracle, AcexAnalyzer analyzer) {
        super(alphabet, oracle);
        this.analyzer = analyzer;
    }

    protected State<HypLoc<I>> getDefinitiveSuccessor(State<HypLoc<I>> baseState, Word<I> suffix) {
        return hypothesis.getSuccessor(baseState, suffix);
    }

    protected Word<I> transformAccessSequence(State<HypLoc<I>> state) {
        return transformAccessSequence(state.getStackContents(), state.getLocation());
    }

    protected Word<I> transformAccessSequence(@Nullable StackContents contents) {
        return transformAccessSequence(contents, hypothesis.getInitialLocation());
    }

    protected Word<I> transformAccessSequence(@Nullable StackContents contents, HypLoc<I> loc) {
        List<Integer> stackElems = new ArrayList<>();
        if (contents != null) {
            StackContents iter = contents;
            while (iter != null) {
                stackElems.add(iter.peek());
                iter = iter.pop();
            }
        }
        WordBuilder<I> wb = new WordBuilder<>();
        for (int i = stackElems.size() - 1; i >= 0; i--) {
            int elem = stackElems.get(i);
            HypLoc<I> stackLoc = hypothesis.getStackLoc(elem);
            wb.append(stackLoc.getAccessSequence());
            I callSym = hypothesis.getCallSym(elem);
            wb.append(callSym);
        }
        wb.append(loc.getAccessSequence());
        return wb.toWord();
    }

    @Override
    protected boolean refineHypothesisSingle(DefaultQuery<I, Boolean> ceQuery) {
        Word<I> ceWord = ceQuery.getInput();
        boolean hypOut = hypothesis.computeOutput(ceWord);
        if (hypOut == ceQuery.getOutput()) {
            return false;
        }
        PrefixTransformAcex acex = new PrefixTransformAcex(Word.epsilon(), new ContextPair<>(Word.epsilon(), ceWord));
        acex.setEffect(0, !hypOut);
        acex.setEffect(acex.getLength() - 1, hypOut);

        int breakpoint = analyzer.analyzeAbstractCounterexample(acex);

        Word<I> prefix = ceWord.prefix(breakpoint);
        I act = ceWord.getSymbol(breakpoint);
        Word<I> suffix = ceWord.subWord(breakpoint + 1);

        State<HypLoc<I>> state = hypothesis.getState(prefix);
        assert state != null;
        State<HypLoc<I>> succState = hypothesis.getSuccessor(state, act);
        assert succState != null;

        ContextPair<I> context = new ContextPair<>(transformAccessSequence(succState.getStackContents()), suffix);

        AbstractHypTrans<I> trans = hypothesis.getInternalTransition(state, act);
        assert trans != null;

        HypLoc<I> newLoc = makeTree(trans);
        DTNode<I> oldDtNode = succState.getLocation().getLeaf();
        openTransitions.concat(oldDtNode.getIncoming());
        DTNode<I>.SplitResult children = oldDtNode.split(context, acex.effect(breakpoint), acex.effect(breakpoint + 1));
        link(children.nodeOld, newLoc);
        link(children.nodeNew, succState.getLocation());
        initializeLocation(trans.getTreeTarget());

        closeTransitions();

        return true;
    }

    protected class PrefixTransformAcex extends AbstractBaseCounterexample<Boolean> {

        private final Word<I> suffix;

        private final State<HypLoc<I>> baseState;

        public PrefixTransformAcex(Word<I> word, ContextPair<I> context) {
            super(context.getSuffix().length() + 1);
            this.suffix = context.getSuffix();
            this.baseState = hypothesis.getState(IterableUtil.concat(context.getPrefix(), word));
        }

        public State<HypLoc<I>> getBaseState() {
            return baseState;
        }

        public Word<I> getSuffix() {
            return suffix;
        }

        @Override
        public boolean checkEffects(Boolean eff1, Boolean eff2) {
            return eff1.equals(eff2);
        }

        @Override
        protected Boolean computeEffect(int index) {
            Word<I> suffPref = suffix.prefix(index);
            State<HypLoc<I>> state = getDefinitiveSuccessor(baseState, suffPref);
            Word<I> suffSuff = suffix.subWord(index);

            return oracle.answerQuery(transformAccessSequence(state), suffSuff);
        }
    }
}
