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

import java.util.Iterator;

import com.google.common.collect.AbstractIterator;
import de.learnlib.acex.AbstractCounterexample;
import de.learnlib.acex.AcexAnalyzer;
import de.learnlib.algorithms.ttt.base.TTTState;
import de.learnlib.algorithms.ttt.base.TTTTransition;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.commons.util.array.RichArray;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

/**
 * @author Malte Isberner
 */
public class PrefixTTTLearnerDFA<I> extends TTTLearnerDFA<I> {

    private final ExtDTNode<I> unlabeledList = new ExtDTNode<>();

    public PrefixTTTLearnerDFA(Alphabet<I> alphabet, MembershipOracle<I, Boolean> oracle, AcexAnalyzer analyzer) {
        super(alphabet, oracle, analyzer, ExtDTNode::new);
    }

    @Override
    public boolean refineHypothesis(DefaultQuery<I, Boolean> ceQuery) {
        boolean refined = refineHypothesisSingle(ceQuery);
        if (!refined) {
            return false;
        }

        while (refineHypothesisSingle(ceQuery)) {
        }

        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected boolean refineHypothesisSingle(DefaultQuery<I, Boolean> ceQuery) {
        if (((TTTHypothesisDFA<I>) hypothesis).computeSuffixOutput(ceQuery.getPrefix(), ceQuery.getSuffix())
                                              .equals(ceQuery.getOutput())) {
            return false;
        }

        Word<I> ceWord = ceQuery.getInput();
        int currReachInconsLength = ceWord.length();

        EasyTTTPrefAcex acex = new EasyTTTPrefAcex(ceWord);
        do {
            acex.update(currReachInconsLength);
            int breakpoint = analyzer.analyzeAbstractCounterexample(acex, 0, currReachInconsLength);
            ExtDTNode<I> toSplit = acex.getHypNode(breakpoint);
            TTTState<I, Boolean> splitState = toSplit.getData();
            ExtDTNode<I> lca = acex.getLCA(breakpoint + 1);
            I sym = ceWord.getSymbol(breakpoint);
            Word<I> newDiscr = lca.getDiscriminator().prepend(sym);
            ExtDTNode<I> succHyp = acex.getHypNode(breakpoint + 1);
            boolean hypOut = lca.subtreeLabel(succHyp);
            openTransitions.insertAllIncoming(toSplit.getIncoming());
            ExtDTNode.SplitResult splitResult = toSplit.split(newDiscr, hypOut, !hypOut);
            link((ExtDTNode<I>) splitResult.nodeOld, splitState);
            ExtDTNode<I> extUnlabeled = (ExtDTNode<I>) splitResult.nodeNew;
            extUnlabeled.tempPrefix = currReachInconsLength;
            unlabeledList.addUnlabeled(extUnlabeled);
            closeTransitions();

            currReachInconsLength = findMinReachIncons();
        } while (currReachInconsLength != -1);

        return true;
    }

    @Override
    protected TTTState<I, Boolean> makeTree(TTTTransition<I, Boolean> trans) {
        ExtDTNode<I> node = (ExtDTNode<I>) trans.getNonTreeTarget();
        if (node.tempPrefix != -1) {
            node.removeFromUnlabeledList();
        }
        return super.makeTree(trans);
    }

    private int findMinReachIncons() {
        int minLength = -1;
        for (ExtDTNode<I> n : unlabeledList.unlabeled()) {
            int len = n.tempPrefix;
            if (minLength == -1 || len < minLength) {
                minLength = len;
            }
        }
        return minLength;
    }

    protected static class ExtDTNode<I> extends TTTDTNodeDFA<I> {

        private ExtDTNode<I> prevUnlabeled, nextUnlabeled;
        private int tempPrefix = -1;

        public ExtDTNode() {
            super();
        }

        public ExtDTNode(ExtDTNode<I> parent, Boolean parentOut) {
            super(parent, parentOut);
        }

        public void removeFromUnlabeledList() {
            prevUnlabeled.nextUnlabeled = nextUnlabeled;
            if (nextUnlabeled != null) {
                nextUnlabeled.prevUnlabeled = prevUnlabeled;
            }
        }

        @Override
        protected ExtDTNode<I> createChild(Boolean outcome, TTTState<I, Boolean> data) {
            return new ExtDTNode<>(this, outcome);
        }

        public boolean hasUnlabeled() {
            return nextUnlabeled != null;
        }

        public void addUnlabeled(ExtDTNode<I> node) {
            node.nextUnlabeled = nextUnlabeled;
            if (nextUnlabeled != null) {
                nextUnlabeled.prevUnlabeled = node;
            }
            node.prevUnlabeled = this;
            this.nextUnlabeled = node;
        }

        public Iterable<ExtDTNode<I>> unlabeled() {
            return this::unlabeledIterator;
        }

        public Iterator<ExtDTNode<I>> unlabeledIterator() {
            return new UnlabeledIterator<>(this);
        }

        private static class UnlabeledIterator<I> extends AbstractIterator<ExtDTNode<I>> {

            private ExtDTNode<I> curr;

            UnlabeledIterator(ExtDTNode<I> curr) {
                this.curr = curr;
            }

            @Override
            protected ExtDTNode<I> computeNext() {
                curr = curr.nextUnlabeled;
                if (curr == null) {
                    return endOfData();
                }
                return curr;
            }
        }
    }

    private final class EasyTTTPrefAcex implements AbstractCounterexample<Boolean> {

        private final Word<I> ceWord;
        private final RichArray<ExtDTNode<I>> hypNodes;
        private final RichArray<ExtDTNode<I>> siftNodes;

        EasyTTTPrefAcex(Word<I> ceWord) {
            this.ceWord = ceWord;
            this.hypNodes = new RichArray<>(ceWord.length() + 1);
            this.siftNodes = new RichArray<>(ceWord.length() + 1);

            update(ceWord.length());
        }

        public void update(int len) {
            TTTStateDFA<I> curr = (TTTStateDFA<I>) hypothesis.getInitialState();
            hypNodes.set(0, (ExtDTNode<I>) curr.getDTLeaf());
            siftNodes.set(0, (ExtDTNode<I>) curr.getDTLeaf());

            boolean wasTree = true;
            for (int i = 0; i < len; i++) {
                I sym = ceWord.getSymbol(i);
                TTTTransition<I, Boolean> trans = hypothesis.getInternalTransition(curr, sym);
                curr = (TTTStateDFA<I>) trans.getTarget();

                hypNodes.set(i + 1, (ExtDTNode<I>) curr.getDTLeaf());
                if (wasTree) {
                    siftNodes.set(i + 1, (ExtDTNode<I>) curr.getDTLeaf());
                    if (!trans.isTree()) {
                        wasTree = false;
                    }
                }

            }
        }

        @Override
        public int getLength() {
            return ceWord.length() + 1;
        }

        @Override
        public boolean checkEffects(Boolean eff1, Boolean eff2) {
            return !eff1 || eff2;
        }

        @Override
        public Boolean effect(int index) {
            ExtDTNode<I> hypNode = hypNodes.get(index);
            ExtDTNode<I> siftNode = siftNodes.get(index);
            if (siftNode == null) {
                siftNode = (ExtDTNode<I>) dtree.getRoot();
            }

            ExtDTNode<I> lca = (ExtDTNode<I>) dtree.leastCommonAncestor(hypNode, siftNode);
            Word<I> cePref = ceWord.prefix(index);
            while (lca == siftNode && siftNode != hypNode) {
                Boolean out = oracle.answerQuery(cePref, siftNode.getDiscriminator());
                siftNode = (ExtDTNode<I>) siftNode.getChild(out);
                lca = (ExtDTNode<I>) dtree.leastCommonAncestor(hypNode, siftNode);
            }
            siftNodes.set(index, siftNode);

            return siftNode == hypNode;
        }

        public ExtDTNode<I> getLCA(int index) {
            return (ExtDTNode<I>) dtree.leastCommonAncestor(hypNodes.get(index), siftNodes.get(index));
        }

        public ExtDTNode<I> getHypNode(int index) {
            return hypNodes.get(index);
        }

    }

}
