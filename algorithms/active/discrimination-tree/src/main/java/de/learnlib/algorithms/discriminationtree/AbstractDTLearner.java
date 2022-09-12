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
package de.learnlib.algorithms.discriminationtree;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import de.learnlib.algorithms.discriminationtree.hypothesis.DTLearnerHypothesis;
import de.learnlib.algorithms.discriminationtree.hypothesis.HState;
import de.learnlib.algorithms.discriminationtree.hypothesis.HTransition;
import de.learnlib.api.Resumable;
import de.learnlib.api.algorithm.LearningAlgorithm;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.api.query.Query;
import de.learnlib.counterexamples.LocalSuffixFinder;
import de.learnlib.counterexamples.LocalSuffixFinders;
import de.learnlib.datastructure.discriminationtree.model.AbstractWordBasedDTNode;
import de.learnlib.datastructure.discriminationtree.model.AbstractWordBasedDiscriminationTree;
import de.learnlib.util.MQUtil;
import net.automatalib.SupportsGrowingAlphabet;
import net.automatalib.automata.concepts.SuffixOutput;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;
import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class AbstractDTLearner<M extends SuffixOutput<I, D>, I, D, SP, TP>
        implements LearningAlgorithm<M, I, D>, SupportsGrowingAlphabet<I>, Resumable<DTLearnerState<I, D, SP, TP>> {

    private final Alphabet<I> alphabet;
    private final MembershipOracle<I, D> oracle;
    private final LocalSuffixFinder<? super I, ? super D> suffixFinder;
    private final boolean repeatedCounterexampleEvaluation;
    private final List<HState<I, D, SP, TP>> newStates = new ArrayList<>();
    private final List<HTransition<I, D, SP, TP>> newTransitions = new ArrayList<>();
    private final Deque<HTransition<I, D, SP, TP>> openTransitions = new ArrayDeque<>();
    private AbstractWordBasedDiscriminationTree<I, D, HState<I, D, SP, TP>> dtree;
    private DTLearnerHypothesis<I, D, SP, TP> hypothesis;

    protected AbstractDTLearner(Alphabet<I> alphabet,
                                MembershipOracle<I, D> oracle,
                                LocalSuffixFinder<? super I, ? super D> suffixFinder,
                                boolean repeatedCounterexampleEvaluation,
                                AbstractWordBasedDiscriminationTree<I, D, HState<I, D, SP, TP>> dtree) {
        this.alphabet = alphabet;
        this.oracle = oracle;
        this.suffixFinder = suffixFinder;
        this.hypothesis = new DTLearnerHypothesis<>(alphabet);
        this.dtree = dtree;
        this.repeatedCounterexampleEvaluation = repeatedCounterexampleEvaluation;
    }

    @Override
    public void startLearning() {
        HState<I, D, SP, TP> init = hypothesis.createInitialState();
        AbstractWordBasedDTNode<I, D, HState<I, D, SP, TP>> initDt = dtree.sift(init.getAccessSequence());
        if (initDt.getData() != null) {
            throw new IllegalStateException("Decision tree already contains data");
        }
        initDt.setData(init);
        init.setDTLeaf(initDt);
        initializeState(init);

        updateHypothesis();
    }

    @Override
    public boolean refineHypothesis(DefaultQuery<I, D> ceQuery) {
        if (!refineHypothesisSingle(ceQuery)) {
            return false;
        }
        if (repeatedCounterexampleEvaluation) {
            while (refineHypothesisSingle(ceQuery)) {}
        }
        return true;
    }

    protected boolean refineHypothesisSingle(DefaultQuery<I, D> ceQuery) {
        if (!MQUtil.isCounterexample(ceQuery, getHypothesisModel())) {
            return false;
        }

        int suffixIdx = suffixFinder.findSuffixIndex(ceQuery, hypothesis, getHypothesisModel(), oracle);

        if (suffixIdx == -1) {
            throw new AssertionError("Suffix finder does not work correctly, found no suffix for valid counterexample");
        }

        Word<I> input = ceQuery.getInput();
        Word<I> oldStateAs = input.prefix(suffixIdx);
        HState<I, D, SP, TP> oldState = hypothesis.getState(oldStateAs);
        assert oldState != null;
        AbstractWordBasedDTNode<I, D, HState<I, D, SP, TP>> oldDt = oldState.getDTLeaf();

        Word<I> newPredAs = input.prefix(suffixIdx - 1);
        HState<I, D, SP, TP> newPred = hypothesis.getState(newPredAs);
        assert newPred != null;
        I transSym = input.getSymbol(suffixIdx - 1);
        int transIdx = alphabet.getSymbolIndex(transSym);
        HTransition<I, D, SP, TP> trans = newPred.getTransition(transIdx);

        HState<I, D, SP, TP> newState = createState(trans);

        Word<I> suffix = input.subWord(suffixIdx);

        D oldOut = oracle.answerQuery(oldState.getAccessSequence(), suffix);
        D newOut = oracle.answerQuery(newState.getAccessSequence(), suffix);

        AbstractWordBasedDTNode<I, D, HState<I, D, SP, TP>>.SplitResult sr =
                oldDt.split(suffix, oldOut, newOut, newState);

        oldState.fetchNonTreeIncoming(openTransitions);

        oldState.setDTLeaf(sr.nodeOld);
        newState.setDTLeaf(sr.nodeNew);

        updateHypothesis();

        return true;
    }

    protected void initializeState(HState<I, D, SP, TP> newState) {
        newStates.add(newState);

        int size = alphabet.size();
        for (int i = 0; i < size; i++) {
            I sym = alphabet.getSymbol(i);
            HTransition<I, D, SP, TP> newTrans = new HTransition<>(newState, sym, dtree.getRoot());
            newState.setTransition(i, newTrans);
            newTransitions.add(newTrans);
            openTransitions.offer(newTrans);
        }
    }

    protected void updateHypothesis() {
        while (!openTransitions.isEmpty()) {
            updateTransitions();
        }

        List<Query<I, D>> queries = new ArrayList<>();
        for (HState<I, D, SP, TP> state : newStates) {
            Query<I, D> spQuery = spQuery(state);
            if (spQuery != null) {
                queries.add(spQuery);
            }
        }
        newStates.clear();

        for (HTransition<I, D, SP, TP> trans : newTransitions) {
            Query<I, D> tpQuery = tpQuery(trans);
            if (tpQuery != null) {
                queries.add(tpQuery);
            }
        }
        newTransitions.clear();

        oracle.processQueries(queries);
    }

    protected void updateTransitions() {
        final List<HTransition<I, D, SP, TP>> transitionsToSift = new ArrayList<>(openTransitions.size());
        final List<AbstractWordBasedDTNode<I, D, HState<I, D, SP, TP>>> nodes = new ArrayList<>(openTransitions.size());
        final List<Word<I>> prefixes = new ArrayList<>(openTransitions.size());

        for (HTransition<I, D, SP, TP> t : openTransitions) {
            if (!t.isTree()) {
                transitionsToSift.add(t);
                nodes.add(t.getDT());
                prefixes.add(t.getAccessSequence());
            }
        }

        openTransitions.clear();

        final List<AbstractWordBasedDTNode<I, D, HState<I, D, SP, TP>>> results = dtree.sift(nodes, prefixes);

        for (int i = 0; i < transitionsToSift.size(); i++) {
            final HTransition<I, D, SP, TP> trans = transitionsToSift.get(i);
            final AbstractWordBasedDTNode<I, D, HState<I, D, SP, TP>> currDt = results.get(i);

            trans.setDT(currDt);

            HState<I, D, SP, TP> state = currDt.getData();
            if (state == null) {
                state = createState(trans);
                currDt.setData(state);
                state.setDTLeaf(currDt);
            } else {
                state.addNonTreeIncoming(trans);
            }
        }
    }

    protected abstract @Nullable Query<I, D> spQuery(HState<I, D, SP, TP> state);

    protected abstract @Nullable Query<I, D> tpQuery(HTransition<I, D, SP, TP> transition);

    protected HState<I, D, SP, TP> createState(HTransition<I, D, SP, TP> trans) {
        HState<I, D, SP, TP> newState = hypothesis.createState(trans);

        initializeState(newState);

        return newState;
    }

    public AbstractWordBasedDiscriminationTree<I, D, HState<I, D, SP, TP>> getDiscriminationTree() {
        return dtree;
    }

    public DTLearnerHypothesis<I, D, SP, TP> getHypothesisDS() {
        return hypothesis;
    }

    @Override
    public void addAlphabetSymbol(I symbol) {

        if (!this.alphabet.containsSymbol(symbol)) {
            Alphabets.toGrowingAlphabetOrThrowException(this.alphabet).addSymbol(symbol);
        }

        this.hypothesis.addAlphabetSymbol(symbol);

        // check if we already have information about the symbol (then the transition is defined) so we don't post
        // redundant queries
        if (this.hypothesis.getInitialState() != null &&
            this.hypothesis.getSuccessor(this.hypothesis.getInitialState(), symbol) == null) {
            final int newSymbolIdx = this.alphabet.getSymbolIndex(symbol);

            for (final HState<I, D, SP, TP> s : this.hypothesis.getStates()) {
                final HTransition<I, D, SP, TP> newTrans = new HTransition<>(s, symbol, dtree.getRoot());
                s.setTransition(newSymbolIdx, newTrans);
                newTransitions.add(newTrans);
                openTransitions.add(newTrans);
            }

            this.updateHypothesis();
        }
    }

    @Override
    public DTLearnerState<I, D, SP, TP> suspend() {
        return new DTLearnerState<>(dtree, hypothesis);
    }

    @Override
    public void resume(DTLearnerState<I, D, SP, TP> state) {
        this.hypothesis = state.getHypothesis();
        this.dtree = state.getDtree();
        this.dtree.setOracle(oracle);
    }

    public static final class BuilderDefaults {

        private BuilderDefaults() {
            // prevent instantiation
        }

        public static <I, O> LocalSuffixFinder<? super I, ? super O> suffixFinder() {
            return LocalSuffixFinders.RIVEST_SCHAPIRE;
        }

        public static boolean repeatedCounterexampleEvaluation() {
            return true;
        }
    }

}
