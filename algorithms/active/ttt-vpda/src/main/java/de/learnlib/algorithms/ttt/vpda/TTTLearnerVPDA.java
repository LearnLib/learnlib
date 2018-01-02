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
package de.learnlib.algorithms.ttt.vpda;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.github.misberner.buildergen.annotations.GenerateBuilder;
import com.google.common.collect.Iterables;
import de.learnlib.acex.AcexAnalyzer;
import de.learnlib.algorithms.discriminationtree.hypothesis.vpda.AbstractHypTrans;
import de.learnlib.algorithms.discriminationtree.hypothesis.vpda.BlockList;
import de.learnlib.algorithms.discriminationtree.hypothesis.vpda.ContextPair;
import de.learnlib.algorithms.discriminationtree.hypothesis.vpda.DTNode;
import de.learnlib.algorithms.discriminationtree.hypothesis.vpda.HypLoc;
import de.learnlib.algorithms.discriminationtree.hypothesis.vpda.TransList;
import de.learnlib.algorithms.discriminationtree.vpda.DTLearnerVPDA;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.datastructure.discriminationtree.SplitData;
import net.automatalib.automata.vpda.StackContents;
import net.automatalib.automata.vpda.State;
import net.automatalib.words.VPDAlphabet;
import net.automatalib.words.Word;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @param <I>
 *         input symbol type
 *
 * @author Malte Isberner
 */
public class TTTLearnerVPDA<I> extends DTLearnerVPDA<I> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TTTLearnerVPDA.class);

    private final BlockList<I> blockList = new BlockList<>();

    @GenerateBuilder
    public TTTLearnerVPDA(VPDAlphabet<I> alphabet, MembershipOracle<I, Boolean> oracle, AcexAnalyzer analyzer) {
        super(alphabet, oracle, analyzer);
    }

    protected State<HypLoc<I>> getDefinitiveSuccessor(State<HypLoc<I>> baseState, Word<I> suffix) {
        NonDetState<HypLoc<I>> curr = NonDetState.fromDet(baseState);
        int lastDet = 0;
        NonDetState<HypLoc<I>> lastDetState = curr;
        int i = 0;
        for (I sym : suffix) {
            if (alphabet.isCallSymbol(sym)) {
                Set<Integer> stackSyms = new HashSet<>();
                for (HypLoc<I> loc : curr.getLocations()) {
                    int stackSym = hypothesis.encodeStackSym(loc, sym);
                    stackSyms.add(stackSym);
                }
                NondetStackContents nsc = NondetStackContents.push(stackSyms, curr.getStack());
                curr = new NonDetState<>(Collections.singleton(hypothesis.getInitialLocation()), nsc);
            } else if (alphabet.isReturnSymbol(sym)) {
                Set<HypLoc<I>> succs = new HashSet<>();
                for (HypLoc<I> loc : curr.getLocations()) {
                    for (int stackSym : curr.getStack().peek()) {
                        AbstractHypTrans<I> trans = hypothesis.getReturnTransition(loc, sym, stackSym);
                        if (trans.isTree()) {
                            succs.add(trans.getTreeTarget());
                        } else {
                            Iterables.addAll(succs, trans.getNonTreeTarget().subtreeLocations());
                        }
                    }
                }
                curr = new NonDetState<>(succs, curr.getStack().pop());
            } else {
                Set<HypLoc<I>> succs = new HashSet<>();
                for (HypLoc<I> loc : curr.getLocations()) {
                    AbstractHypTrans<I> trans = hypothesis.getInternalTransition(loc, sym);
                    if (trans.isTree()) {
                        succs.add(trans.getTreeTarget());
                    } else {
                        Iterables.addAll(succs, trans.getNonTreeTarget().subtreeLocations());
                    }
                }
                curr = new NonDetState<>(succs, curr.getStack());
            }
            i++;
            if (!curr.isNonDet()) {
                lastDet = i;
                lastDetState = curr;
            }
        }

        if (lastDet < suffix.length()) {
            LOGGER.debug("last det: {}", lastDet);
            determinize(lastDetState.determinize(), suffix.subWord(lastDet));
        }
        return hypothesis.getSuccessor(baseState, suffix);
    }

    @Override
    protected boolean refineHypothesisSingle(DefaultQuery<I, Boolean> ceQuery) {
        Word<I> ceWord = ceQuery.getInput();

        Boolean out = computeHypothesisOutput(ceWord);

        if (Objects.equals(out, ceQuery.getOutput())) {
            return false;
        }

        OutputInconsistency<I> outIncons = new OutputInconsistency<>(hypothesis.getInitialLocation(),
                                                                     new ContextPair<>(Word.epsilon(), ceWord),
                                                                     ceQuery.getOutput());

        do {
            splitState(outIncons);
            closeTransitions();
            while (finalizeAny()) {
                closeTransitions();
            }

            outIncons = findOutputInconsistency();
        } while (outIncons != null);

        return true;
    }

    protected boolean computeHypothesisOutput(Word<I> word) {
        State<HypLoc<I>> curr = hypothesis.getInitialState();
        for (I sym : word) {
            curr = getAnySuccessor(curr, sym);
        }
        return hypothesis.isAccepting(curr);
    }

    private void splitState(OutputInconsistency<I> outIncons) {
        PrefixTransformAcex acex = deriveAcex(outIncons);
        int breakpoint = analyzer.analyzeAbstractCounterexample(acex);

        Word<I> acexSuffix = acex.getSuffix();
        Word<I> prefix = acexSuffix.prefix(breakpoint);
        I act = acexSuffix.getSymbol(breakpoint);
        Word<I> suffix = acexSuffix.subWord(breakpoint + 1);

        State<HypLoc<I>> state = hypothesis.getSuccessor(acex.getBaseState(), prefix);
        State<HypLoc<I>> succState = hypothesis.getSuccessor(state, act);

        ContextPair<I> context = new ContextPair<>(transformAccessSequence(succState.getStackContents()), suffix);

        AbstractHypTrans<I> trans = hypothesis.getInternalTransition(state, act);

        HypLoc<I> newLoc = makeTree(trans);
        DTNode<I> oldDtNode = succState.getLocation().getLeaf();
        openTransitions.addAll(oldDtNode.getIncoming());
        DTNode<I>.SplitResult children = oldDtNode.split(context, acex.effect(breakpoint), acex.effect(breakpoint + 1));
        oldDtNode.setTemp(true);
        if (!oldDtNode.getParent().isTemp()) {
            blockList.add(oldDtNode);
        }
        link(children.nodeOld, newLoc);
        link(children.nodeNew, succState.getLocation());
        initializeLocation(newLoc);
    }

    protected boolean finalizeAny() {
        assert openTransitions.isEmpty();

        GlobalSplitter<I> splitter = findSplitterGlobal();
        if (splitter != null) {
            finalizeDiscriminator(splitter.blockRoot, splitter.localSplitter);
            return true;
        }
        return false;
    }

    private OutputInconsistency<I> findOutputInconsistency() {
        OutputInconsistency<I> best = null;

        for (HypLoc<I> loc : hypothesis.getLocations()) {
            int locAsLen = loc.getAccessSequence().length();
            DTNode<I> node = loc.getLeaf();
            while (!node.isRoot()) {
                boolean expectedOut = node.getParentOutcome();
                node = node.getParent();
                ContextPair<I> discr = node.getDiscriminator();
                if (best == null || discr.getLength() + locAsLen < best.totalLength()) {
                    boolean hypOut = computeHypothesisOutput(discr.getPrefix()
                                                                  .concat(loc.getAccessSequence(), discr.getSuffix()));
                    if (hypOut != expectedOut) {
                        best = new OutputInconsistency<>(loc, discr, expectedOut);
                    }
                }
            }
        }
        return best;
    }

    protected State<HypLoc<I>> getAnySuccessor(State<HypLoc<I>> state, I sym) {
        final VPDAlphabet.SymbolType type = alphabet.getSymbolType(sym);

        switch (type) {
            case INTERNAL: {
                AbstractHypTrans<I> trans = hypothesis.getInternalTransition(state.getLocation(), sym);
                HypLoc<I> succLoc;
                if (trans.isTree()) {
                    succLoc = trans.getTreeTarget();
                } else {
                    succLoc = trans.getNonTreeTarget().subtreeLocsIterator().next();
                }
                return new State<>(succLoc, state.getStackContents());
            }
            case CALL: {
                int stackSym = hypothesis.encodeStackSym(state.getLocation(), sym);
                return new State<>(hypothesis.getInitialLocation(),
                                   StackContents.push(stackSym, state.getStackContents()));
            }
            case RETURN: {
                AbstractHypTrans<I> trans =
                        hypothesis.getReturnTransition(state.getLocation(), sym, state.getStackContents().peek());
                HypLoc<I> succLoc;
                if (trans.isTree()) {
                    succLoc = trans.getTreeTarget();
                } else {
                    succLoc = trans.getNonTreeTarget().subtreeLocsIterator().next();
                }
                return new State<>(succLoc, state.getStackContents().pop());
            }
            default:
                throw new IllegalStateException("Unhandled type " + type);
        }
    }

    protected PrefixTransformAcex deriveAcex(OutputInconsistency<I> outIncons) {
        PrefixTransformAcex acex =
                new PrefixTransformAcex(outIncons.location.getAccessSequence(), outIncons.discriminator);
        acex.setEffect(0, outIncons.expectedOut);
        acex.setEffect(acex.getLength() - 1, !outIncons.expectedOut);

        return acex;
    }

    /**
     * Determines a global splitter, i.e., a splitter for any block. This method may (but is not required to) employ
     * heuristics to obtain a splitter with a relatively short suffix length.
     *
     * @return a splitter for any of the blocks
     */
    private GlobalSplitter<I> findSplitterGlobal() {
        DTNode<I> bestBlockRoot = null;
        Splitter<I> bestSplitter = null;

        for (DTNode<I> blockRoot : blockList) {
            Splitter<I> splitter = findSplitter(blockRoot);

            if (splitter != null) {
                if (bestSplitter == null ||
                    splitter.getNewDiscriminatorLength() < bestSplitter.getNewDiscriminatorLength()) {
                    bestSplitter = splitter;
                    bestBlockRoot = blockRoot;
                }
            }
        }

        if (bestSplitter == null) {
            return null;
        }

        return new GlobalSplitter<>(bestBlockRoot, bestSplitter);
    }

    /**
     * Finalize a discriminator. Given a block root and a {@link Splitter}, replace the discriminator at the block root
     * by the one derived from the splitter, and update the discrimination tree accordingly.
     *
     * @param blockRoot
     *         the block root whose discriminator to finalize
     * @param splitter
     *         the splitter to use for finalization
     */
    private void finalizeDiscriminator(DTNode<I> blockRoot, Splitter<I> splitter) {
        assert blockRoot.isBlockRoot();

        ContextPair<I> newDiscr = splitter.getNewDiscriminator();

        if (!blockRoot.getDiscriminator().equals(newDiscr)) {
            ContextPair<I> finalDiscriminator = prepareSplit(blockRoot, splitter);
            Map<Boolean, DTNode<I>> repChildren = new HashMap<>();
            for (Boolean label : blockRoot.getSplitData().getLabels()) {
                repChildren.put(label, extractSubtree(blockRoot, label));
            }
            blockRoot.replaceChildren(repChildren);

            blockRoot.setDiscriminator(finalDiscriminator);
        } else {
            LOGGER.debug("Weird..");
        }

        declareFinal(blockRoot);
    }

    /**
     * Determines a (local) splitter for a given block. This method may (but is not required to) employ heuristics to
     * obtain a splitter with a relatively short suffix.
     *
     * @param blockRoot
     *         the root of the block
     *
     * @return a splitter for this block, or {@code null} if no such splitter could be found.
     */
    private Splitter<I> findSplitter(DTNode<I> blockRoot) {
        int alphabetSize =
                alphabet.getNumInternals() + alphabet.getNumCalls() * alphabet.getNumReturns() * hypothesis.size() * 2;

        @SuppressWarnings("unchecked")
        DTNode<I>[] lcas = new DTNode[alphabetSize];

        for (HypLoc<I> loc : blockRoot.subtreeLocations()) {
            int i = 0;
            for (I intSym : alphabet.getInternalSymbols()) {
                DTNode<I> currLca = lcas[i];
                AbstractHypTrans<I> trans = hypothesis.getInternalTransition(loc, intSym);
                assert trans.getTargetNode() != null;
                if (currLca == null) {
                    lcas[i] = trans.getTargetNode();
                } else {
                    lcas[i] = dtree.leastCommonAncestor(currLca, trans.getTargetNode());
                }
                i++;
            }
            for (I retSym : alphabet.getReturnSymbols()) {
                for (I callSym : alphabet.getCallSymbols()) {
                    for (HypLoc<I> stackLoc : hypothesis.getLocations()) {
                        AbstractHypTrans<I> trans = hypothesis.getReturnTransition(loc, retSym, stackLoc, callSym);
                        DTNode<I> currLca = lcas[i];
                        assert trans.getTargetNode() != null;
                        if (currLca == null) {
                            lcas[i] = trans.getTargetNode();
                        } else {
                            lcas[i] = dtree.leastCommonAncestor(currLca, trans.getTargetNode());
                        }
                        i++;

                        trans = hypothesis.getReturnTransition(stackLoc, retSym, loc, callSym);
                        currLca = lcas[i];
                        if (currLca == null) {
                            lcas[i] = trans.getTargetNode();
                        } else {
                            lcas[i] = dtree.leastCommonAncestor(currLca, trans.getTargetNode());
                        }
                        i++;
                    }
                }
            }
        }

        int shortestLen = Integer.MAX_VALUE;
        Splitter<I> shortestSplitter = null;

        int i = 0;
        for (I intSym : alphabet.getInternalSymbols()) {
            DTNode<I> currLca = lcas[i];
            if (!currLca.isLeaf() && !currLca.isTemp()) {
                Splitter<I> splitter = new Splitter<>(intSym, currLca);
                int newLen = splitter.getNewDiscriminatorLength();
                if (shortestSplitter == null || shortestLen > newLen) {
                    shortestSplitter = splitter;
                    shortestLen = newLen;
                }
            }
            i++;
        }
        for (I retSym : alphabet.getReturnSymbols()) {
            for (I callSym : alphabet.getCallSymbols()) {
                for (HypLoc<I> stackLoc : hypothesis.getLocations()) {
                    DTNode<I> currLca = lcas[i];
                    assert currLca != null;
                    if (!currLca.isLeaf() && !currLca.isTemp()) {
                        Splitter<I> splitter = new Splitter<>(retSym, stackLoc, callSym, false, currLca);
                        int newLen = splitter.getNewDiscriminatorLength();
                        if (shortestSplitter == null || shortestLen > newLen) {
                            shortestSplitter = splitter;
                            shortestLen = newLen;
                        }
                    }
                    i++;

                    currLca = lcas[i];
                    assert currLca != null;
                    if (!currLca.isLeaf() && !currLca.isTemp()) {
                        Splitter<I> splitter = new Splitter<>(callSym, stackLoc, retSym, true, currLca);
                        int newLen = splitter.getNewDiscriminatorLength();
                        if (shortestSplitter == null || shortestLen > newLen) {
                            shortestSplitter = splitter;
                            shortestLen = newLen;
                        }
                    }
                    i++;
                }
            }
        }

        return shortestSplitter;
    }

    /**
     * Prepare a split operation on a block, by marking all the nodes and transitions in the subtree (and annotating
     * them with {@link SplitData} objects).
     *
     * @param node
     *         the block root to be split
     * @param splitter
     *         the splitter to use for splitting the block
     *
     * @return the discriminator to use for splitting
     */
    private ContextPair<I> prepareSplit(DTNode<I> node, Splitter<I> splitter) {
        ContextPair<I> discriminator = splitter.getNewDiscriminator();

        Deque<DTNode<I>> dfsStack = new ArrayDeque<>();

        DTNode<I> succSeparator = splitter.succSeparator;

        dfsStack.push(node);
        assert node.getSplitData() == null;

        while (!dfsStack.isEmpty()) {
            DTNode<I> curr = dfsStack.pop();
            assert curr.getSplitData() == null;

            curr.setSplitData(new SplitData<>(TransList::new));

            for (AbstractHypTrans<I> trans : curr.getIncoming()) {
                Boolean outcome = query(trans, discriminator);
                curr.getSplitData().getIncoming(outcome).add(trans);
                markAndPropagate(curr, outcome);
            }

            if (curr.isInner()) {
                for (DTNode<I> child : curr.getChildren()) {
                    dfsStack.push(child);
                }
            } else {
                HypLoc<I> loc = curr.getData();
                assert loc != null;

                // Try to deduct the outcome from the DT target of
                // the respective transition
                AbstractHypTrans<I> trans = getSplitterTrans(loc, splitter);
                Boolean outcome = succSeparator.subtreeLabel(trans.getTargetNode());
                assert outcome != null;
                curr.getSplitData().setStateLabel(outcome);
                markAndPropagate(curr, outcome);
            }

        }

        return discriminator;
    }

    /**
     * Extract a (reduced) subtree containing all nodes with the given label from the subtree given by its root.
     * "Reduced" here refers to the fact that the resulting subtree will contain no inner nodes with only one child. The
     * tree returned by this method (represented by its root) will have as a parent node the root that was passed to
     * this method.
     *
     * @param root
     *         the root of the subtree from which to extract
     * @param label
     *         the label of the nodes to extract
     *
     * @return the extracted subtree
     */
    private DTNode<I> extractSubtree(DTNode<I> root, Boolean label) {
        assert root.getSplitData() != null;
        assert root.getSplitData().isMarked(label);

        Deque<ExtractRecord<I>> stack = new ArrayDeque<>();

        DTNode<I> firstExtracted = new DTNode<>(root, label);

        stack.push(new ExtractRecord<>(root, firstExtracted));
        while (!stack.isEmpty()) {
            ExtractRecord<I> curr = stack.pop();

            DTNode<I> original = curr.original;
            DTNode<I> extracted = curr.extracted;

            moveIncoming(extracted, original, label);

            if (original.isLeaf()) {
                if (Objects.equals(original.getSplitData().getStateLabel(), label)) {
                    link(extracted, original.getData());
                } else {
                    createNewState(extracted);
                }
                extracted.updateIncoming();
            } else {
                List<DTNode<I>> markedChildren = new ArrayList<>();

                for (DTNode<I> child : original.getChildren()) {
                    if (child.getSplitData().isMarked(label)) {
                        markedChildren.add(child);
                    }
                }

                if (markedChildren.size() > 1) {
                    Map<Boolean, DTNode<I>> childMap = new HashMap<>();
                    for (DTNode<I> c : markedChildren) {
                        Boolean childLabel = c.getParentOutcome();
                        DTNode<I> extractedChild = new DTNode<>(extracted, childLabel);
                        childMap.put(childLabel, extractedChild);
                        stack.push(new ExtractRecord<>(c, extractedChild));
                    }
                    extracted.split(original.getDiscriminator(), childMap);
                    extracted.updateIncoming();
                    extracted.setTemp(true);
                } else if (markedChildren.size() == 1) {
                    stack.push(new ExtractRecord<>(markedChildren.get(0), extracted));
                } else { // markedChildren.isEmppty()
                    createNewState(extracted);
                    extracted.updateIncoming();
                }
            }

            assert extracted.getSplitData() == null;
        }

        return firstExtracted;
    }

    protected void declareFinal(DTNode<I> blockRoot) {
        blockRoot.setTemp(false);
        blockRoot.setSplitData(null);

        blockRoot.removeFromBlockList();

        for (DTNode<I> subtree : blockRoot.getChildren()) {
            assert subtree.getSplitData() == null;
            //blockRoot.setChild(subtree.getParentLabel(), subtree);
            // Register as blocks, if they are non-trivial subtrees
            if (subtree.isInner()) {
                blockList.add(subtree);
            }
        }

        openTransitions.addAll(blockRoot.getIncoming());
    }

    /**
     * Marks a node, and propagates the label up to all nodes on the path from the block root to this node.
     *
     * @param node
     *         the node to mark
     * @param label
     *         the label to mark the node with
     */
    private static <I> void markAndPropagate(DTNode<I> node, Boolean label) {
        DTNode<I> curr = node;

        while (curr != null && curr.getSplitData() != null) {
            if (!curr.getSplitData().mark(label)) {
                return;
            }
            curr = curr.getParent();
        }
    }

    public AbstractHypTrans<I> getSplitterTrans(HypLoc<I> loc, Splitter<I> splitter) {
        switch (splitter.type) {
            case INTERNAL:
                return hypothesis.getInternalTransition(loc, splitter.symbol);
            case RETURN:
                return hypothesis.getReturnTransition(loc, splitter.symbol, splitter.location, splitter.otherSymbol);
            case CALL:
                return hypothesis.getReturnTransition(splitter.location, splitter.otherSymbol, loc, splitter.symbol);
            default:
                throw new IllegalStateException("Unhandled type " + splitter.type);
        }
    }

    private static <I> void moveIncoming(DTNode<I> newNode, DTNode<I> oldNode, Boolean label) {
        newNode.getIncoming().addAll(oldNode.getSplitData().getIncoming(label));
    }

    /**
     * Create a new state during extraction on-the-fly. This is required if a node in the DT has an incoming transition
     * with a certain label, but in its subtree there are no leaves with this label as their state label.
     *
     * @param newNode
     *         the extracted node
     */
    private void createNewState(DTNode<I> newNode) {
        LOGGER.debug("Create new state");
        AbstractHypTrans<I> newTreeTrans = newNode.getIncoming().chooseMinimal();
        assert newTreeTrans != null;

        HypLoc<I> newLoc = makeTree(newTreeTrans);
        link(newNode, newLoc);
        initializeLocation(newLoc);
    }

    protected void determinize(State<HypLoc<I>> state, Word<I> suffix) {
        State<HypLoc<I>> curr = state;
        for (I sym : suffix) {
            if (!alphabet.isCallSymbol(sym)) {
                AbstractHypTrans<I> trans = hypothesis.getInternalTransition(curr, sym);
                if (!trans.isTree() && !trans.getNonTreeTarget().isLeaf()) {
                    updateDTTarget(trans, true);
                }
            }
            curr = hypothesis.getSuccessor(curr, sym);
        }
    }

}
