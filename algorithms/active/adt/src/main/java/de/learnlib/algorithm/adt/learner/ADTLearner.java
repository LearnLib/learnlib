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
package de.learnlib.algorithm.adt.learner;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;

import de.learnlib.Resumable;
import de.learnlib.algorithm.LearningAlgorithm;
import de.learnlib.algorithm.adt.adt.ADT;
import de.learnlib.algorithm.adt.adt.ADT.LCAInfo;
import de.learnlib.algorithm.adt.adt.ADTLeafNode;
import de.learnlib.algorithm.adt.adt.ADTNode;
import de.learnlib.algorithm.adt.adt.ADTResetNode;
import de.learnlib.algorithm.adt.api.ADTExtender;
import de.learnlib.algorithm.adt.api.LeafSplitter;
import de.learnlib.algorithm.adt.api.PartialTransitionAnalyzer;
import de.learnlib.algorithm.adt.api.SubtreeReplacer;
import de.learnlib.algorithm.adt.automaton.ADTHypothesis;
import de.learnlib.algorithm.adt.automaton.ADTState;
import de.learnlib.algorithm.adt.automaton.ADTTransition;
import de.learnlib.algorithm.adt.config.ADTExtenders;
import de.learnlib.algorithm.adt.config.LeafSplitters;
import de.learnlib.algorithm.adt.config.SubtreeReplacers;
import de.learnlib.algorithm.adt.model.ExtensionResult;
import de.learnlib.algorithm.adt.model.ObservationTree;
import de.learnlib.algorithm.adt.model.ReplacementResult;
import de.learnlib.algorithm.adt.util.ADTUtil;
import de.learnlib.counterexample.LocalSuffixFinder;
import de.learnlib.counterexample.LocalSuffixFinders;
import de.learnlib.logging.Category;
import de.learnlib.oracle.AdaptiveMembershipOracle;
import de.learnlib.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.query.DefaultQuery;
import de.learnlib.tooling.annotation.builder.GenerateBuilder;
import de.learnlib.util.MQUtil;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.SupportsGrowingAlphabet;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.common.util.HashUtil;
import net.automatalib.common.util.Pair;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The main learning algorithm.
 *
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbol type
 */
public class ADTLearner<I, O> implements LearningAlgorithm.MealyLearner<I, O>,
                                         PartialTransitionAnalyzer<ADTState<I, O>, I>,
                                         SupportsGrowingAlphabet<I>,
                                         Resumable<ADTLearnerState<ADTState<I, O>, I, O>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ADTLearner.class);

    private final Alphabet<I> alphabet;
    private final AdaptiveMembershipOracle<I, O> oracle;
    private final MealyMembershipOracle<I, O> mqo;
    private final LeafSplitter leafSplitter;
    private final ADTExtender adtExtender;
    private final SubtreeReplacer subtreeReplacer;
    private final Queue<ADTTransition<I, O>> openTransitions;
    private final Queue<DefaultQuery<I, Word<O>>> openCounterExamples;
    private final Set<DefaultQuery<I, Word<O>>> allCounterExamples;
    private final ObservationTree<ADTState<I, O>, I, O> observationTree;
    private final LocalSuffixFinder<? super I, ? super Word<O>> suffixFinder;
    private ADTHypothesis<I, O> hypothesis;
    private ADT<ADTState<I, O>, I, O> adt;

    public ADTLearner(Alphabet<I> alphabet,
                      AdaptiveMembershipOracle<I, O> oracle,
                      LeafSplitter leafSplitter,
                      ADTExtender adtExtender,
                      SubtreeReplacer subtreeReplacer) {
        this(alphabet, oracle, leafSplitter, adtExtender, subtreeReplacer, true, LocalSuffixFinders.RIVEST_SCHAPIRE);
    }

    @GenerateBuilder(defaults = BuilderDefaults.class)
    public ADTLearner(Alphabet<I> alphabet,
                      AdaptiveMembershipOracle<I, O> oracle,
                      LeafSplitter leafSplitter,
                      ADTExtender adtExtender,
                      SubtreeReplacer subtreeReplacer,
                      boolean useObservationTree,
                      LocalSuffixFinder<? super I, ? super Word<O>> suffixFinder) {

        this.alphabet = alphabet;
        this.observationTree = new ObservationTree<>(this.alphabet, oracle, useObservationTree);
        this.oracle = this.observationTree;
        this.mqo = new Adaptive2MembershipWrapper<>(oracle);

        this.leafSplitter = leafSplitter;
        this.adtExtender = adtExtender;
        this.subtreeReplacer = subtreeReplacer;
        this.suffixFinder = suffixFinder;

        this.hypothesis = new ADTHypothesis<>(this.alphabet);
        this.openTransitions = new ArrayDeque<>();
        this.openCounterExamples = new ArrayDeque<>();
        this.allCounterExamples = new LinkedHashSet<>();
        this.adt = new ADT<>();
    }

    @Override
    public void startLearning() {

        final ADTState<I, O> initialState = this.hypothesis.addInitialState();
        initialState.setAccessSequence(Word.epsilon());
        this.observationTree.initialize(initialState);
        this.adt.initialize(initialState);

        for (I i : this.alphabet) {
            this.openTransitions.add(this.hypothesis.createOpenTransition(initialState, i, this.adt.getRoot()));
        }

        this.closeTransitions();
    }

    @Override
    public boolean refineHypothesis(DefaultQuery<I, Word<O>> ce) {

        if (!MQUtil.isCounterexample(ce, this.hypothesis)) {
            return false;
        }

        this.evaluateSubtreeReplacement();

        this.openCounterExamples.add(ce);

        while (!this.openCounterExamples.isEmpty()) {

            // normal refinement step
            while (!this.openCounterExamples.isEmpty()) {

                @SuppressWarnings("nullness")
                // false positive https://github.com/typetools/checker-framework/issues/399
                final @NonNull DefaultQuery<I, Word<O>> currentCE = this.openCounterExamples.poll();
                this.allCounterExamples.add(currentCE);

                while (this.refineHypothesisInternal(currentCE)) {
                    // refine exhaustively
                }
            }

            // subtree replacements may reactivate old CEs
            for (DefaultQuery<I, Word<O>> oldCE : this.allCounterExamples) {
                if (MQUtil.isCounterexample(oldCE, this.hypothesis)) {
                    this.openCounterExamples.add(oldCE);
                }
            }

            ADTUtil.collectLeaves(this.adt.getRoot()).forEach(this::ensureConsistency);
        }

        return true;
    }

    public boolean refineHypothesisInternal(DefaultQuery<I, Word<O>> ceQuery) {

        if (!MQUtil.isCounterexample(ceQuery, this.hypothesis)) {
            return false;
        }

        // Determine a counterexample decomposition (u, a, v)
        final int suffixIdx = suffixFinder.findSuffixIndex(ceQuery, this.hypothesis, this.hypothesis, this.mqo);

        if (suffixIdx == -1) {
            throw new IllegalStateException();
        }

        final Word<I> ceInput = ceQuery.getInput();

        final Word<I> u = ceInput.prefix(suffixIdx - 1);
        final Word<I> ua = ceInput.prefix(suffixIdx);
        final I a = ceInput.getSymbol(suffixIdx - 1);
        final Word<I> v = ceInput.subWord(suffixIdx);

        final ADTState<I, O> uState = this.hypothesis.getState(u);
        final ADTState<I, O> uaState = this.hypothesis.getState(ua);

        assert uState != null && uaState != null;

        final Word<I> uAccessSequence = uState.getAccessSequence();
        final Word<I> uaAccessSequence = uaState.getAccessSequence();
        final Word<I> uAccessSequenceWithA = uAccessSequence.append(a);

        final ADTState<I, O> newState = this.hypothesis.addState();
        newState.setAccessSequence(uAccessSequenceWithA);
        final ADTTransition<I, O> oldTrans = this.hypothesis.getTransition(uState, a);

        assert oldTrans != null;

        oldTrans.setTarget(newState);
        oldTrans.setIsSpanningTreeEdge(true);

        final ADTNode<ADTState<I, O>, I, O> nodeToSplit = findNodeForState(uaState);
        final ADTNode<ADTState<I, O>, I, O> newNode;

        // directly insert into observation tree, because we use it for finding a splitter
        this.observationTree.addState(newState, newState.getAccessSequence(), oldTrans.getOutput());
        this.observationTree.addTrace(newState, nodeToSplit);

        final Word<I> previousTrace = ADTUtil.buildTraceForNode(nodeToSplit).getFirst();
        final Word<I> extension = this.observationTree.findSeparatingWord(uaState, newState, previousTrace);

        if (extension == null) {
            // directly insert into observation tree, because we use it for finding a splitter
            this.observationTree.addTrace(uaState, v, this.mqo.answerQuery(uaAccessSequence, v));
            this.observationTree.addTrace(newState, v, this.mqo.answerQuery(uAccessSequenceWithA, v));

            // in doubt, we will always find v
            final Word<I> otSepWord = this.observationTree.findSeparatingWord(uaState, newState);
            final Word<I> splitter;
            assert otSepWord != null;

            if (otSepWord.length() < v.length()) {
                splitter = otSepWord;
            } else {
                splitter = v;
            }

            final Word<O> oldOutput = this.observationTree.trace(uaState, splitter);
            final Word<O> newOutput = this.observationTree.trace(newState, splitter);

            newNode = this.adt.splitLeaf(nodeToSplit, splitter, oldOutput, newOutput, this.leafSplitter);
        } else {
            final Word<I> completeSplitter = previousTrace.concat(extension);
            final Word<O> oldOutput = this.observationTree.trace(uaState, completeSplitter);
            final Word<O> newOutput = this.observationTree.trace(newState, completeSplitter);

            newNode = this.adt.extendLeaf(nodeToSplit, completeSplitter, oldOutput, newOutput, this.leafSplitter);
        }
        newNode.setState(newState);

        final ADTNode<ADTState<I, O>, I, O> temporarySplitter = ADTUtil.getStartOfADS(nodeToSplit);
        final List<ADTTransition<I, O>> newTransitions = new ArrayList<>(alphabet.size());

        for (I i : alphabet) {
            newTransitions.add(this.hypothesis.createOpenTransition(newState, i, this.adt.getRoot()));
        }

        final List<ADTTransition<I, O>> transitionsToRefine = getIncomingNonSpanningTreeTransitions(uaState);

        for (ADTTransition<I, O> x : transitionsToRefine) {
            x.setTarget(null);
            x.setSiftNode(temporarySplitter);
        }

        final ADTNode<ADTState<I, O>, I, O> finalizedSplitter = this.evaluateAdtExtension(temporarySplitter);

        for (ADTTransition<I, O> t : transitionsToRefine) {
            if (t.needsSifting()) {
                t.setSiftNode(finalizedSplitter);
                this.openTransitions.add(t);
            }
        }

        for (ADTTransition<I, O> t : newTransitions) {
            if (t.needsSifting()) {
                this.openTransitions.add(t);
            }
        }

        this.closeTransitions();
        return true;
    }

    private ADTNode<ADTState<I, O>, I, O> findNodeForState(ADTState<I, O> state) {

        for (ADTNode<ADTState<I, O>, I, O> leaf : ADTUtil.collectLeaves(this.adt.getRoot())) {
            if (leaf.getState().equals(state)) {
                return leaf;
            }
        }

        throw new IllegalStateException("Cannot find leaf for state " + state);

    }

    @Override
    public MealyMachine<?, I, ?, O> getHypothesisModel() {
        return this.hypothesis;
    }

    /**
     * Close all pending open transitions.
     */
    private void closeTransitions() {
        while (!this.openTransitions.isEmpty()) {

            final Collection<ADTAdaptiveQuery<I, O>> queries = new ArrayList<>(this.openTransitions.size());

            //create a query object for every transition
            for (ADTTransition<I, O> transition : this.openTransitions) {
                if (transition.needsSifting()) {
                    queries.add(new ADTAdaptiveQuery<>(transition, transition.getSiftNode()));
                }
            }

            this.openTransitions.clear();
            this.oracle.processQueries(queries);

            for (ADTAdaptiveQuery<I, O> query : queries) {
                processAnsweredQuery(query);
            }
        }
    }

    @Override
    public void closeTransition(ADTState<I, O> state, I input) {

        final ADTTransition<I, O> transition = this.hypothesis.getTransition(state, input);
        assert transition != null;

        if (transition.needsSifting()) {
            final ADTNode<ADTState<I, O>, I, O> ads = transition.getSiftNode();
            final int oldNumberOfFinalStates = ADTUtil.collectLeaves(ads).size();

            final ADTAdaptiveQuery<I, O> query = new ADTAdaptiveQuery<>(transition, transition.getSiftNode());
            this.oracle.processQueries(Collections.singleton(query));
            processAnsweredQuery(query);

            final int newNumberOfFinalStates = ADTUtil.collectLeaves(ads).size();

            if (oldNumberOfFinalStates < newNumberOfFinalStates) {
                throw PartialTransitionAnalyzer.HYPOTHESIS_MODIFICATION_EXCEPTION;
            }
        }
    }

    private void processAnsweredQuery(ADTAdaptiveQuery<I, O> query) {
        if (query.needsPostProcessing()) {
            final ADTNode<ADTState<I, O>, I, O> parent = query.getCurrentADTNode();
            final O out = query.getTempOut();
            final ADTNode<ADTState<I, O>, I, O> succ = parent.getChild(out);

            // first time we process the successor
            if (succ == null) {
                // add new state to the hypothesis and set the accessSequence
                final ADTState<I, O> newState = this.hypothesis.addState();
                final Word<I> longPrefix = query.getAccessSequence().append(query.getTransition().getInput());
                newState.setAccessSequence(longPrefix);

                // configure the transition
                final ADTTransition<I, O> transition = query.getTransition();
                transition.setTarget(newState);
                transition.setIsSpanningTreeEdge(true);

                // add new leaf node to ADT
                final ADTNode<ADTState<I, O>, I, O> result = new ADTLeafNode<>(parent, newState);
                parent.getChildren().put(out, result);

                // add the observations to the observation tree
                O transitionOutput = query.getTransition().getOutput();
                this.observationTree.addState(newState, longPrefix, transitionOutput);

                // query successors
                for (I i : this.alphabet) {
                    this.openTransitions.add(this.hypothesis.createOpenTransition(newState, i, this.adt.getRoot()));
                }
            } else {
                assert ADTUtil.isLeafNode(succ);
                // state has been created before, just update target
                query.getTransition().setTarget(succ.getState());
            }
        } else {
            // update target
            final ADTTransition<I, O> transition = query.getTransition();
            final ADTNode<ADTState<I, O>, I, O> adtNode = query.getCurrentADTNode();
            assert ADTUtil.isLeafNode(adtNode);
            transition.setTarget(adtNode.getState());
        }
    }

    @Override
    public boolean isTransitionDefined(ADTState<I, O> state, I input) {
        final ADTTransition<I, O> transition = this.hypothesis.getTransition(state, input);
        assert transition != null;
        return !transition.needsSifting();
    }

    @Override
    public void addAlphabetSymbol(I symbol) {

        if (!this.alphabet.containsSymbol(symbol)) {
            this.alphabet.asGrowingAlphabetOrThrowException().addSymbol(symbol);
        }

        this.hypothesis.addAlphabetSymbol(symbol);
        this.observationTree.addAlphabetSymbol(symbol);

        // check if we already have information about the symbol (then the transition is defined) so we don't post
        // redundant queries
        if (this.hypothesis.getInitialState() != null &&
            this.hypothesis.getSuccessor(this.hypothesis.getInitialState(), symbol) == null) {
            for (ADTState<I, O> s : this.hypothesis.getStates()) {
                this.openTransitions.add(this.hypothesis.createOpenTransition(s, symbol, this.adt.getRoot()));
            }

            this.closeTransitions();
        }
    }

    @Override
    public ADTLearnerState<ADTState<I, O>, I, O> suspend() {
        return new ADTLearnerState<>(this.hypothesis, this.adt);
    }

    @Override
    public void resume(ADTLearnerState<ADTState<I, O>, I, O> state) {
        this.hypothesis = state.getHypothesis();
        this.adt = state.getAdt();

        final Alphabet<I> oldAlphabet = this.hypothesis.getInputAlphabet();
        if (!oldAlphabet.equals(this.alphabet)) {
            LOGGER.warn(Category.DATASTRUCTURE,
                        "The current alphabet '{}' differs from the resumed alphabet '{}'. Future behavior may be inconsistent",
                        this.alphabet,
                        oldAlphabet);
        }

        // startLearning has already been invoked
        if (this.hypothesis.size() > 0) {
            this.observationTree.initialize(this.hypothesis.getStates(),
                                            ADTState::getAccessSequence,
                                            this.hypothesis::computeOutput);
        }
    }

    /**
     * Ensure that the output behavior of a hypothesis state matches the observed output behavior recorded in the ADT.
     * Any differences in output behavior yields new counterexamples.
     *
     * @param leaf
     *         the leaf whose hypothesis state should be checked
     */
    private void ensureConsistency(ADTNode<ADTState<I, O>, I, O> leaf) {

        final ADTState<I, O> state = leaf.getState();
        final Word<I> as = state.getAccessSequence();
        final Word<O> asOut = this.hypothesis.computeOutput(as);

        ADTNode<ADTState<I, O>, I, O> iter = leaf;

        while (iter != null) {
            final Pair<Word<I>, Word<O>> trace = ADTUtil.buildTraceForNode(iter);

            final Word<I> input = trace.getFirst();
            final Word<O> output = trace.getSecond();

            final Word<O> hypOut = this.hypothesis.computeStateOutput(state, input);

            if (!hypOut.equals(output)) {
                this.openCounterExamples.add(new DefaultQuery<>(as.concat(input), asOut.concat(output)));
            }

            iter = ADTUtil.getStartOfADS(iter).getParent();
        }
    }

    /**
     * Ask the current {@link #adtExtender} for a potential extension.
     *
     * @param ads
     *         the temporary ADS based on the inferred distinguishing suffix
     *
     * @return a validated ADT that can be used to distinguish the states referenced in the given temporary ADS
     */
    private ADTNode<ADTState<I, O>, I, O> evaluateAdtExtension(ADTNode<ADTState<I, O>, I, O> ads) {

        final ExtensionResult<ADTState<I, O>, I, O> potentialExtension =
                this.adtExtender.computeExtension(this.hypothesis, this, ads);

        if (potentialExtension.isCounterExample()) {
            this.openCounterExamples.add(potentialExtension.getCounterExample());
            return ads;
        } else if (!potentialExtension.isReplacement()) {
            return ads;
        }

        final ADTNode<ADTState<I, O>, I, O> extension = potentialExtension.getReplacement();
        final ADTNode<ADTState<I, O>, I, O> nodeToReplace = ads.getParent(); // reset node

        assert extension != null && nodeToReplace != null &&
               this.validateADS(nodeToReplace, extension, Collections.emptySet());

        final ADTNode<ADTState<I, O>, I, O> replacement = this.verifyADS(nodeToReplace,
                                                                         extension,
                                                                         ADTUtil.collectLeaves(this.adt.getRoot()),
                                                                         Collections.emptySet());

        // verification may have introduced reset nodes
        final int oldCosts = ADTUtil.computeEffectiveResets(nodeToReplace);
        final int newCosts = ADTUtil.computeEffectiveResets(replacement);

        if (newCosts >= oldCosts) {
            return ads;
        }

        // replace
        this.adt.replaceNode(nodeToReplace, replacement);

        final ADTNode<ADTState<I, O>, I, O> finalizedADS = ADTUtil.getStartOfADS(replacement);

        // update
        this.resiftAffectedTransitions(ADTUtil.collectLeaves(extension), finalizedADS);

        return finalizedADS;
    }

    /**
     * Ask the {@link #subtreeReplacer} for any replacements.
     */
    private void evaluateSubtreeReplacement() {

        if (this.hypothesis.size() == 1) {
            // skip replacement if only one node is discovered
            return;
        }

        final Set<ReplacementResult<ADTState<I, O>, I, O>> potentialReplacements =
                this.subtreeReplacer.computeReplacements(this.hypothesis, this.alphabet, this.adt);
        final List<ReplacementResult<ADTState<I, O>, I, O>> validReplacements =
                new ArrayList<>(potentialReplacements.size());
        final Set<ADTNode<ADTState<I, O>, I, O>> cachedLeaves =
                potentialReplacements.isEmpty() ? Collections.emptySet() : ADTUtil.collectLeaves(this.adt.getRoot());

        for (ReplacementResult<ADTState<I, O>, I, O> potentialReplacement : potentialReplacements) {
            final ADTNode<ADTState<I, O>, I, O> proposedReplacement = potentialReplacement.getReplacement();
            final ADTNode<ADTState<I, O>, I, O> nodeToReplace = potentialReplacement.getNodeToReplace();

            assert this.validateADS(nodeToReplace, proposedReplacement, potentialReplacement.getCutoutNodes());

            final ADTNode<ADTState<I, O>, I, O> replacement = this.verifyADS(nodeToReplace,
                                                                             proposedReplacement,
                                                                             cachedLeaves,
                                                                             potentialReplacement.getCutoutNodes());

            // verification may have introduced reset nodes
            final int oldCosts = ADTUtil.computeEffectiveResets(nodeToReplace);
            final int newCosts = ADTUtil.computeEffectiveResets(replacement);

            if (newCosts >= oldCosts) {
                continue;
            }

            validReplacements.add(new ReplacementResult<>(nodeToReplace, replacement));
        }

        for (ReplacementResult<ADTState<I, O>, I, O> potentialReplacement : validReplacements) {
            final ADTNode<ADTState<I, O>, I, O> replacement = potentialReplacement.getReplacement();
            final ADTNode<ADTState<I, O>, I, O> nodeToReplace = potentialReplacement.getNodeToReplace();

            this.adt.replaceNode(nodeToReplace, replacement);

            this.resiftAffectedTransitions(ADTUtil.collectLeaves(replacement), ADTUtil.getStartOfADS(replacement));
        }

        this.closeTransitions();
    }

    /**
     * Validate the well-definedness of an ADT replacement, i.e. both ADTs cover the same set of hypothesis states and
     * the output behavior described in the replacement matches the hypothesis output.
     *
     * @param oldADS
     *         the old ADT (subtree) to be replaced
     * @param newADS
     *         the new ADT (subtree)
     * @param cutout
     *         the set of states not covered by the new ADT
     *
     * @return {@code true} if the replacement is valid, {@code false} otherwise.
     */
    private boolean validateADS(ADTNode<ADTState<I, O>, I, O> oldADS,
                                ADTNode<ADTState<I, O>, I, O> newADS,
                                Set<ADTState<I, O>> cutout) {

        final Set<ADTNode<ADTState<I, O>, I, O>> oldNodes;

        if (ADTUtil.isResetNode(oldADS)) {
            oldNodes = ADTUtil.collectResetNodes(this.adt.getRoot());
        } else {
            oldNodes = ADTUtil.collectADSNodes(this.adt.getRoot(), true);
        }

        if (!oldNodes.contains(oldADS)) {
            throw new IllegalArgumentException("Subtree to replace does not exist");
        }

        final Set<ADTNode<ADTState<I, O>, I, O>> newFinalNodes = ADTUtil.collectLeaves(newADS);
        final Map<ADTState<I, O>, Pair<Word<I>, Word<O>>> traces =
                new HashMap<>(HashUtil.capacity(newFinalNodes.size()));

        for (ADTNode<ADTState<I, O>, I, O> n : newFinalNodes) {
            traces.put(n.getState(), ADTUtil.buildTraceForNode(n));
        }

        final Set<ADTState<I, O>> oldFinalStates = ADTUtil.collectHypothesisStates(oldADS);
        final Set<ADTState<I, O>> newFinalStates = new HashSet<>(traces.keySet());
        newFinalStates.addAll(cutout);

        if (!oldFinalStates.equals(newFinalStates)) {
            throw new IllegalArgumentException("New ADS does not cover all old nodes");
        }

        final Word<I> parentInputTrace = ADTUtil.buildTraceForNode(oldADS).getFirst();

        for (Map.Entry<ADTState<I, O>, Pair<Word<I>, Word<O>>> entry : traces.entrySet()) {

            final Word<I> accessSequence = entry.getKey().getAccessSequence();
            final Word<I> prefix = accessSequence.concat(parentInputTrace);
            final Word<I> input = entry.getValue().getFirst();
            final Word<O> output = entry.getValue().getSecond();

            if (!this.hypothesis.computeSuffixOutput(prefix, input).equals(output)) {
                throw new IllegalArgumentException("Output of new ADS does not match hypothesis");
            }
        }

        return true;
    }

    /**
     * Verify the proposed ADT replacement by checking the actual behavior of the system under learning. During the
     * verification process, the system under learning may behave differently from what the ADT replacement suggests:
     * This means a counterexample is witnessed and added to the queue of counterexamples for later investigation.
     * Albeit observing diverging behavior, this method continues to trying to construct a valid ADT using the observed
     * output. If for two states, no distinguishing output can be observed, the states a separated by means of
     * {@link #resolveAmbiguities(ADTNode, ADTNode, ADTState, Set)}.
     *
     * @param nodeToReplace
     *         the old ADT (subtree) to be replaced
     * @param replacement
     *         the new ADT (subtree). Must have the form of an ADS, i.e. no reset nodes
     * @param cachedLeaves
     *         a set containing the leaves of the current tree, so they don't have to be re-fetched for every
     *         replacement verification
     * @param cutout
     *         the set of states not covered by the new ADT
     *
     * @return A verified ADT that correctly distinguishes the states covered by the original ADT
     */
    private ADTNode<ADTState<I, O>, I, O> verifyADS(ADTNode<ADTState<I, O>, I, O> nodeToReplace,
                                                    ADTNode<ADTState<I, O>, I, O> replacement,
                                                    Set<ADTNode<ADTState<I, O>, I, O>> cachedLeaves,
                                                    Set<ADTState<I, O>> cutout) {
        final Set<ADTNode<ADTState<I, O>, I, O>> leaves = ADTUtil.collectLeaves(replacement);
        final Map<ADTState<I, O>, Pair<Word<I>, Word<O>>> traces =
                new LinkedHashMap<>(HashUtil.capacity(leaves.size()));

        for (ADTNode<ADTState<I, O>, I, O> leaf : leaves) {
            traces.put(leaf.getState(), ADTUtil.buildTraceForNode(leaf));
        }

        final Pair<Word<I>, Word<O>> parentTrace = ADTUtil.buildTraceForNode(nodeToReplace);

        ADTNode<ADTState<I, O>, I, O> result = null;

        final List<ADSVerificationQuery<I, O>> queries = new ArrayList<>(traces.size());

        for (Entry<ADTState<I, O>, Pair<Word<I>, Word<O>>> e : traces.entrySet()) {
            final ADTState<I, O> state = e.getKey();
            final Pair<Word<I>, Word<O>> ads = e.getValue();
            queries.add(new ADSVerificationQuery<>(state.getAccessSequence().concat(parentTrace.getFirst()),
                                                   ads.getFirst(),
                                                   ads.getSecond(),
                                                   state));
        }

        this.oracle.processQueries(queries);

        for (ADSVerificationQuery<I, O> query : queries) {
            final ADTNode<ADTState<I, O>, I, O> trace;
            final DefaultQuery<I, Word<O>> ce = query.getCounterexample();

            if (ce != null) {
                this.openCounterExamples.add(ce);
                trace = ADTUtil.buildADSFromObservation(ce.getSuffix(), ce.getOutput(), query.getState());
            } else {
                trace = ADTUtil.buildADSFromObservation(query.getSuffix(), query.getExpectedOutput(), query.getState());
            }

            if (result == null) {
                result = trace;
            } else {
                if (!ADTUtil.mergeADS(result, trace)) {
                    this.resolveAmbiguities(nodeToReplace, result, query.getState(), cachedLeaves);
                }
            }
        }

        for (ADTState<I, O> s : cutout) {
            this.resolveAmbiguities(nodeToReplace, result, s, cachedLeaves);
        }

        return result;
    }

    /**
     * If two states show the same output behavior resolve this ambiguity by adding a reset node and add a new (sub) ADS
     * based on the lowest common ancestor in the existing ADT.
     *
     * @param nodeToReplace
     *         the old ADT (subtree) to be replaced
     * @param newADS
     *         the new ADT (subtree)
     * @param state
     *         the state which cannot be distinguished using the given replacement
     * @param cachedLeaves
     *         a set containing the leaves of the current tree, so they don't have to be re-fetched for every
     *         replacement verification
     */
    private void resolveAmbiguities(ADTNode<ADTState<I, O>, I, O> nodeToReplace,
                                    ADTNode<ADTState<I, O>, I, O> newADS,
                                    ADTState<I, O> state,
                                    Set<ADTNode<ADTState<I, O>, I, O>> cachedLeaves) {

        final Pair<Word<I>, Word<O>> parentTrace = ADTUtil.buildTraceForNode(nodeToReplace);
        final ADSAmbiguityQuery<I, O> query =
                new ADSAmbiguityQuery<>(state.getAccessSequence(), parentTrace.getFirst(), newADS);

        this.oracle.processQuery(query);

        if (query.needsPostProcessing()) {
            final ADTNode<ADTState<I, O>, I, O> prev = query.getCurrentADTNode();
            final ADTNode<ADTState<I, O>, I, O> newFinal = new ADTLeafNode<>(prev, state);
            prev.getChildren().put(query.getTempOut(), newFinal);
            return;
        }

        final ADTNode<ADTState<I, O>, I, O> finalNode = query.getCurrentADTNode();
        ADTNode<ADTState<I, O>, I, O> oldReference = null, newReference = null;

        for (ADTNode<ADTState<I, O>, I, O> leaf : cachedLeaves) {
            final ADTState<I, O> hypState = leaf.getState();

            if (hypState.equals(finalNode.getState())) {
                oldReference = leaf;
            } else if (hypState.equals(state)) {
                newReference = leaf;
            }

            if (oldReference != null && newReference != null) {
                break;
            }
        }

        assert oldReference != null && newReference != null;
        final LCAInfo<ADTState<I, O>, I, O> lcaResult = this.adt.findLCA(oldReference, newReference);
        final ADTNode<ADTState<I, O>, I, O> lca = lcaResult.adtNode;
        final Pair<Word<I>, Word<O>> lcaTrace = ADTUtil.buildTraceForNode(lca);

        final Word<I> sepWord = lcaTrace.getFirst().append(lca.getSymbol());
        final Word<O> oldOutputTrace = lcaTrace.getSecond().append(lcaResult.firstOutput);
        final Word<O> newOutputTrace = lcaTrace.getSecond().append(lcaResult.secondOutput);

        final ADTNode<ADTState<I, O>, I, O> oldTrace =
                ADTUtil.buildADSFromObservation(sepWord, oldOutputTrace, finalNode.getState());
        final ADTNode<ADTState<I, O>, I, O> newTrace = ADTUtil.buildADSFromObservation(sepWord, newOutputTrace, state);

        if (!ADTUtil.mergeADS(oldTrace, newTrace)) {
            throw new IllegalStateException("Should never happen");
        }

        final ADTNode<ADTState<I, O>, I, O> reset = new ADTResetNode<>(oldTrace);
        final ADTNode<ADTState<I, O>, I, O> parent = finalNode.getParent();
        assert parent != null;
        final O parentOutput = ADTUtil.getOutputForSuccessor(parent, finalNode);

        parent.getChildren().put(parentOutput, reset);
        reset.setParent(parent);
        oldTrace.setParent(reset);
    }

    /**
     * Schedule all incoming transitions of the given states to be re-sifted against the given ADT (subtree).
     *
     * @param states
     *         A set of states, whose incoming transitions should be sifted
     * @param finalizedADS
     *         the ADT (subtree) to sift through
     */
    private void resiftAffectedTransitions(Set<ADTNode<ADTState<I, O>, I, O>> states,
                                           ADTNode<ADTState<I, O>, I, O> finalizedADS) {

        for (ADTNode<ADTState<I, O>, I, O> state : states) {

            for (ADTTransition<I, O> trans : getIncomingNonSpanningTreeTransitions(state.getState())) {
                trans.setTarget(null);
                trans.setSiftNode(finalizedADS);
                this.openTransitions.add(trans);
            }
        }
    }

    private List<ADTTransition<I, O>> getIncomingNonSpanningTreeTransitions(ADTState<I, O> state) {
        final Set<ADTTransition<I, O>> transitions = state.getIncomingTransitions();
        final List<ADTTransition<I, O>> result = new ArrayList<>(transitions.size());

        for (ADTTransition<I, O> t : transitions) {
            if (!t.isSpanningTreeEdge()) {
                result.add(t);
            }
        }

        return result;
    }

    public ADT<ADTState<I, O>, I, O> getADT() {
        return adt;
    }

    static final class BuilderDefaults {

        private BuilderDefaults() {
            // prevent instantiation
        }

        public static LeafSplitter leafSplitter() {
            return LeafSplitters.DEFAULT_SPLITTER;
        }

        public static ADTExtender adtExtender() {
            return ADTExtenders.EXTEND_BEST_EFFORT;
        }

        public static SubtreeReplacer subtreeReplacer() {
            return SubtreeReplacers.LEVELED_BEST_EFFORT;
        }

        public static boolean useObservationTree() {
            return true;
        }

        @SuppressWarnings("unchecked")
        public static <I, D> LocalSuffixFinder<I, D> suffixFinder() {
            return (LocalSuffixFinder<I, D>) LocalSuffixFinders.RIVEST_SCHAPIRE;
        }
    }
}
