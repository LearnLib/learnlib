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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import de.learnlib.acex.AcexAnalyzer;
import de.learnlib.acex.AcexAnalyzers;
import de.learnlib.algorithm.LearningAlgorithm;
import de.learnlib.algorithm.observationpack.vpa.hypothesis.AbstractHypTrans;
import de.learnlib.algorithm.observationpack.vpa.hypothesis.ContextPair;
import de.learnlib.algorithm.observationpack.vpa.hypothesis.DTNode;
import de.learnlib.algorithm.observationpack.vpa.hypothesis.DTree;
import de.learnlib.algorithm.observationpack.vpa.hypothesis.HypIntTrans;
import de.learnlib.algorithm.observationpack.vpa.hypothesis.HypLoc;
import de.learnlib.algorithm.observationpack.vpa.hypothesis.HypRetTrans;
import de.learnlib.algorithm.observationpack.vpa.hypothesis.OneSEVPAHypothesis;
import de.learnlib.algorithm.observationpack.vpa.hypothesis.TransList;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.query.DefaultQuery;
import net.automatalib.alphabet.VPAlphabet;
import net.automatalib.automaton.vpa.OneSEVPA;
import net.automatalib.common.smartcollection.ElementReference;
import net.automatalib.common.smartcollection.UnorderedCollection;
import net.automatalib.word.Word;

public abstract class AbstractVPALearner<I> implements LearningAlgorithm<OneSEVPA<?, I>, I, Boolean> {

    protected final VPAlphabet<I> alphabet;

    protected final MembershipOracle<I, Boolean> oracle;

    protected final DTree<I> dtree;

    protected final OneSEVPAHypothesis<I> hypothesis;

    protected final TransList<I> openTransitions = new TransList<>();

    public AbstractVPALearner(VPAlphabet<I> alphabet, MembershipOracle<I, Boolean> oracle) {
        this.alphabet = alphabet;
        this.oracle = oracle;
        this.dtree = new DTree<>(oracle);
        dtree.getRoot().split(new ContextPair<>(Word.epsilon(), Word.epsilon()), false, true);
        this.hypothesis = new OneSEVPAHypothesis<>(alphabet);
    }

    @Override
    public void startLearning() {
        HypLoc<I> initLoc = hypothesis.initialize();
        DTNode<I> leaf = dtree.sift(initLoc.getAccessSequence());
        link(leaf, initLoc);
        initializeLocation(initLoc);

        closeTransitions();
    }

    @Override
    public boolean refineHypothesis(DefaultQuery<I, Boolean> ceQuery) {
        if (hypothesis.computeSuffixOutput(ceQuery.getPrefix(), ceQuery.getSuffix()).equals(ceQuery.getOutput())) {
            return false;
        }

        while (refineHypothesisSingle(ceQuery)) {
            // refine exhaustively
        }

        return true;
    }

    protected abstract boolean refineHypothesisSingle(DefaultQuery<I, Boolean> ceQuery);

    @Override
    public OneSEVPA<?, I> getHypothesisModel() {
        return hypothesis;
    }

    public DTree<I> getDiscriminationTree() {
        return dtree;
    }

    protected static <I> void link(DTNode<I> leaf, HypLoc<I> loc) {
        assert leaf.isLeaf();
        leaf.setData(loc);
        loc.setLeaf(leaf);
    }

    protected void initializeLocation(HypLoc<I> loc) {
        final Boolean subtreeLabel = dtree.getRoot().subtreeLabel(loc.getLeaf());
        assert subtreeLabel != null;
        loc.setAccepting(subtreeLabel);

        for (int i = 0; i < alphabet.getNumInternals(); i++) {
            I intSym = alphabet.getInternalSymbol(i);
            HypIntTrans<I> trans = new HypIntTrans<>(loc, intSym);
            loc.setInternalTransition(i, trans);
            openTransitions.add(trans);
        }
        loc.updateStackAlphabetSize(hypothesis.getNumStackSymbols());
        for (int i = 0; i < alphabet.getNumCalls(); i++) {
            I callSym = alphabet.getCallSymbol(i);
            int myStackSym = hypothesis.encodeStackSym(loc, i);
            for (HypLoc<I> stackLoc : hypothesis.getLocations()) {
                stackLoc.updateStackAlphabetSize(hypothesis.getNumStackSymbols());
                int stackSym = hypothesis.encodeStackSym(stackLoc, i);
                for (int j = 0; j < alphabet.getNumReturns(); j++) {
                    I retSym = alphabet.getReturnSymbol(j);
                    HypRetTrans<I> trans = new HypRetTrans<>(loc, retSym, callSym, stackLoc);
                    loc.setReturnTransition(j, stackSym, trans);
                    openTransitions.add(trans);

                    if (loc != stackLoc) {
                        HypRetTrans<I> retTrans = new HypRetTrans<>(stackLoc, retSym, callSym, loc);
                        stackLoc.setReturnTransition(j, myStackSym, retTrans);
                        openTransitions.add(retTrans);
                    }
                }
            }
        }
    }

    protected void closeTransitions() {
        UnorderedCollection<DTNode<I>> newStateNodes = new UnorderedCollection<>();

        do {
            newStateNodes.addAll(closeTransitions(openTransitions, false));
            if (!newStateNodes.isEmpty()) {
                addNewStates(newStateNodes);
            }
        } while (!openTransitions.isEmpty());
    }

    /**
     * Ensures that the specified transitions point to a leaf-node. If a transition is a tree transition, this method
     * has no effect.
     * <p>
     * The provided transList is consumed in this process.
     * <p>
     * If a transition needs sifting, the reached leaf node will be collected in the returned collection.
     *
     * @param transList
     *         the list of transitions
     *
     * @return a collection containing the reached leaves of transitions that needed sifting
     */
    private List<DTNode<I>> closeTransitions(TransList<I> transList, boolean hard) {

        final List<AbstractHypTrans<I>> transToSift = new ArrayList<>(transList.size());

        AbstractHypTrans<I> t;
        while ((t = transList.poll()) != null) {
            if (!t.isTree()) {
                transToSift.add(t);
            }
        }

        if (transToSift.isEmpty()) {
            return Collections.emptyList();
        }

        final Iterator<DTNode<I>> leavesIter = updateDTTargets(transToSift, hard).iterator();
        final List<DTNode<I>> result = new ArrayList<>(transToSift.size());

        for (AbstractHypTrans<I> transition : transToSift) {
            final DTNode<I> node = leavesIter.next();
            if (node.isLeaf() && node.getData() == null && transition.getNext() == null) {
                result.add(node);
            }
        }

        assert !leavesIter.hasNext();
        return result;
    }

    private void addNewStates(UnorderedCollection<DTNode<I>> newStateNodes) {
        DTNode<I> minTransNode = null;
        AbstractHypTrans<I> minTrans = null;
        int minAsLen = Integer.MAX_VALUE;
        ElementReference minTransNodeRef = null;
        for (ElementReference ref : newStateNodes.references()) {
            DTNode<I> newStateNode = newStateNodes.get(ref);
            for (AbstractHypTrans<I> trans : newStateNode.getIncoming()) {
                Word<I> as = trans.getAccessSequence();
                int asLen = as.length();
                if (asLen < minAsLen) {
                    minTransNode = newStateNode;
                    minTrans = trans;
                    minAsLen = asLen;
                    minTransNodeRef = ref;
                }
            }
        }

        assert minTransNode != null;
        newStateNodes.remove(minTransNodeRef);
        assert minTrans.getNonTreeTarget().getData() == null;
        HypLoc<I> newLoc = makeTree(minTrans);
        link(minTransNode, newLoc);
        initializeLocation(newLoc);
    }

    protected List<DTNode<I>> updateDTTargets(List<AbstractHypTrans<I>> trans, boolean hard) {

        final List<DTNode<I>> nodes = new ArrayList<>(trans.size());
        final List<Word<I>> prefixes = new ArrayList<>(trans.size());

        for (AbstractHypTrans<I> t : trans) {
            if (!t.isTree()) {
                DTNode<I> start = t.getNonTreeTarget();

                if (start == null) {
                    t.setNonTreeTarget(dtree.getRoot());
                    start = dtree.getRoot();
                }

                nodes.add(start);
                prefixes.add(t.getAccessSequence());
            }
        }

        final Iterator<DTNode<I>> leavesIter = dtree.sift(nodes, prefixes, hard).iterator();
        final List<DTNode<I>> result = new ArrayList<>(trans.size());

        for (AbstractHypTrans<I> t : trans) {
            if (t.isTree()) {
                result.add(t.getTargetNode());
            } else {
                final DTNode<I> leaf = leavesIter.next();
                t.setNonTreeTarget(leaf);
                leaf.addIncoming(t);
                result.add(leaf);
            }
        }

        assert !leavesIter.hasNext();
        return result;
    }

    protected HypLoc<I> makeTree(AbstractHypTrans<I> trans) {
        assert !trans.isTree();
        HypLoc<I> newLoc = createLocation(trans);
        trans.makeTree(newLoc);
        return newLoc;
    }

    protected HypLoc<I> createLocation(AbstractHypTrans<I> trans) {
        return hypothesis.createLocation(false, trans);
    }

    public static final class BuilderDefaults {

        private BuilderDefaults() {
            // prevent instantiation
        }

        public static AcexAnalyzer analyzer() {
            return AcexAnalyzers.BINARY_SEARCH_BWD;
        }
    }
}


