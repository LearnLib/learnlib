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
package de.learnlib.algorithms.discriminationtree.vpda;

import de.learnlib.algorithms.discriminationtree.hypothesis.vpda.AbstractHypTrans;
import de.learnlib.algorithms.discriminationtree.hypothesis.vpda.ContextPair;
import de.learnlib.algorithms.discriminationtree.hypothesis.vpda.DTNode;
import de.learnlib.algorithms.discriminationtree.hypothesis.vpda.DTree;
import de.learnlib.algorithms.discriminationtree.hypothesis.vpda.HypIntTrans;
import de.learnlib.algorithms.discriminationtree.hypothesis.vpda.HypLoc;
import de.learnlib.algorithms.discriminationtree.hypothesis.vpda.HypRetTrans;
import de.learnlib.algorithms.discriminationtree.hypothesis.vpda.OneSEVPAHypothesis;
import de.learnlib.algorithms.discriminationtree.hypothesis.vpda.TransList;
import de.learnlib.api.AccessSequenceProvider;
import de.learnlib.api.algorithm.LearningAlgorithm;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.automata.vpda.OneSEVPA;
import net.automatalib.commons.smartcollections.ElementReference;
import net.automatalib.commons.smartcollections.UnorderedCollection;
import net.automatalib.words.VPDAlphabet;
import net.automatalib.words.Word;

/**
 * @param <I>
 *         input alphabet type
 *
 * @author Malte Isberner
 */
public abstract class AbstractVPDALearner<I> implements LearningAlgorithm<OneSEVPA<?, I>, I, Boolean> {

    protected final VPDAlphabet<I> alphabet;

    protected final MembershipOracle<I, Boolean> oracle;

    protected final DTree<I> dtree;

    protected final OneSEVPAHypothesis<I> hypothesis;

    protected final TransList<I> openTransitions = new TransList<>();

    public AbstractVPDALearner(VPDAlphabet<I> alphabet, MembershipOracle<I, Boolean> oracle) {
        this.alphabet = alphabet;
        this.oracle = oracle;
        this.dtree = new DTree<>(oracle);
        dtree.getRoot().split(new ContextPair<>(Word.epsilon(), Word.epsilon()), false, true);
        this.hypothesis = new OneSEVPAHypothesis<>(alphabet);
    }

    @Override
    public void startLearning() {
        HypLoc<I> initLoc = hypothesis.initialize();
        DTNode<I> leaf = dtree.sift(initLoc);
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
        }
        return true;
    }

    protected abstract boolean refineHypothesisSingle(DefaultQuery<I, Boolean> ceQuery);

    @Override
    public OneSEVPA<?, I> getHypothesisModel() {
        return hypothesis;
    }

    protected static <I> void link(DTNode<I> leaf, HypLoc<I> loc) {
        assert leaf.isLeaf();
        leaf.setData(loc);
        loc.setLeaf(leaf);
    }

    protected void initializeLocation(HypLoc<I> loc) {
        assert loc.getLeaf() != null;
        loc.setAccepting(dtree.getRoot().subtreeLabel(loc.getLeaf()));

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
        AbstractHypTrans<I> next;
        UnorderedCollection<DTNode<I>> newStateNodes = new UnorderedCollection<>();

        do {
            while ((next = openTransitions.poll()) != null) {
                DTNode<I> newStateNode = closeTransition(next, false);
                if (newStateNode != null) {
                    newStateNodes.add(newStateNode);
                }
            }
            if (!newStateNodes.isEmpty()) {
                addNewStates(newStateNodes);
            }
        } while (!openTransitions.isEmpty());
    }

    /**
     * Ensures that the specified transition points to a leaf-node. If the transition is a tree transition, this method
     * has no effect.
     *
     * @param trans
     *         the transition
     */
    private DTNode<I> closeTransition(AbstractHypTrans<I> trans, boolean hard) {
        if (trans.isTree()) {
            return null;
        }

        DTNode<I> node = updateDTTarget(trans, hard);
        if (node.isLeaf() && node.getData() == null && trans.getNextElement() == null) {
            return node;
        }
        return null;
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

    protected DTNode<I> updateDTTarget(AbstractHypTrans<I> trans, boolean hard) {
        if (trans.isTree()) {
            return trans.getTargetNode();
        }

        DTNode<I> start = trans.getNonTreeTarget();
        if (start == null) {
            trans.setNonTreeTarget(dtree.getRoot());
            start = dtree.getRoot();
        }
        DTNode<I> result = dtree.sift(start, trans, hard);
        trans.setNonTreeTarget(result);
        result.addIncoming(trans);

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

    protected HypLoc<I> createLocation(Word<I> as) {
        return hypothesis.createLocation(false, as);
    }

    protected Boolean query(AccessSequenceProvider<I> asp, ContextPair<I> context) {
        return oracle.answerQuery(context.getPrefix().concat(asp.getAccessSequence()), context.getSuffix());
    }
}


