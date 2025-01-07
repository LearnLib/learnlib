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
package de.learnlib.algorithm.kv.mealy;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import de.learnlib.Resumable;
import de.learnlib.acex.AbstractBaseCounterexample;
import de.learnlib.acex.AcexAnalyzer;
import de.learnlib.acex.AcexAnalyzers;
import de.learnlib.algorithm.LearningAlgorithm.MealyLearner;
import de.learnlib.algorithm.kv.StateInfo;
import de.learnlib.datastructure.discriminationtree.MultiDTree;
import de.learnlib.datastructure.discriminationtree.model.AbstractWordBasedDTNode;
import de.learnlib.datastructure.discriminationtree.model.LCAInfo;
import de.learnlib.logging.Category;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.query.DefaultQuery;
import de.learnlib.tooling.annotation.builder.GenerateBuilder;
import de.learnlib.util.mealy.MealyUtil;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.SupportsGrowingAlphabet;
import net.automatalib.automaton.impl.CompactTransition;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.automaton.transducer.impl.CompactMealy;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An adaption of the Kearns/Vazirani algorithm for Mealy machines.
 *
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbol type
 */
public class KearnsVaziraniMealy<I, O>
        implements MealyLearner<I, O>, SupportsGrowingAlphabet<I>, Resumable<KearnsVaziraniMealyState<I, O>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(KearnsVaziraniMealy.class);

    private final Alphabet<I> alphabet;
    private final MembershipOracle<I, Word<O>> oracle;
    private final boolean repeatedCounterexampleEvaluation;
    private final AcexAnalyzer ceAnalyzer;
    private MultiDTree<I, Word<O>, StateInfo<I, Word<O>>> discriminationTree;
    protected List<StateInfo<I, Word<O>>> stateInfos = new ArrayList<>();
    private CompactMealy<I, O> hypothesis;

    @GenerateBuilder(defaults = BuilderDefaults.class)
    public KearnsVaziraniMealy(Alphabet<I> alphabet,
                               MembershipOracle<I, Word<O>> oracle,
                               boolean repeatedCounterexampleEvaluation,
                               AcexAnalyzer counterexampleAnalyzer) {
        this.alphabet = alphabet;
        this.hypothesis = new CompactMealy<>(alphabet);
        this.oracle = oracle;
        this.repeatedCounterexampleEvaluation = repeatedCounterexampleEvaluation;
        this.discriminationTree = new MultiDTree<>(oracle);
        this.ceAnalyzer = counterexampleAnalyzer;
    }

    @Override
    public void startLearning() {
        initialize();
    }

    @Override
    public boolean refineHypothesis(DefaultQuery<I, Word<O>> ceQuery) {
        if (hypothesis.size() == 0) {
            throw new IllegalStateException("Not initialized");
        }
        Word<I> input = ceQuery.getInput();
        Word<O> output = ceQuery.getOutput();
        if (!refineHypothesisSingle(input, output)) {
            return false;
        }
        if (repeatedCounterexampleEvaluation) {
            while (refineHypothesisSingle(input, output)) {
                // refine exhaustively
            }
        }
        return true;
    }

    @Override
    public MealyMachine<?, I, ?, O> getHypothesisModel() {
        if (hypothesis.size() == 0) {
            throw new IllegalStateException("Not started");
        }
        return hypothesis;
    }

    public MultiDTree<I, Word<O>, StateInfo<I, Word<O>>> getDiscriminationTree() {
        return discriminationTree;
    }

    private boolean refineHypothesisSingle(Word<I> input, Word<O> output) {
        int inputLen = input.length();

        if (inputLen < 2) {
            return false;
        }

        int mismatchIdx = MealyUtil.findMismatch(hypothesis, input, output);

        if (mismatchIdx == MealyUtil.NO_MISMATCH) {
            return false;
        }

        Word<I> effInput = input.prefix(mismatchIdx + 1);
        Word<O> effOutput = output.prefix(mismatchIdx + 1);

        KVAbstractCounterexample acex = new KVAbstractCounterexample(effInput, effOutput, oracle);
        int idx = ceAnalyzer.analyzeAbstractCounterexample(acex, 0);

        Word<I> prefix = effInput.prefix(idx);
        StateInfo<I, Word<O>> srcStateInfo = acex.getStateInfo(idx);
        I sym = effInput.getSymbol(idx);
        LCAInfo<Word<O>, @Nullable AbstractWordBasedDTNode<I, Word<O>, StateInfo<I, Word<O>>>> lca =
                acex.getLCA(idx + 1);
        assert lca != null;

        splitState(srcStateInfo, prefix, sym, lca);

        return true;
    }

    private void splitState(StateInfo<I, Word<O>> stateInfo,
                            Word<I> newPrefix,
                            I sym,
                            LCAInfo<Word<O>, @Nullable AbstractWordBasedDTNode<I, Word<O>, StateInfo<I, Word<O>>>> separatorInfo) {
        int state = stateInfo.id;

        // TLongList oldIncoming = stateInfo.fetchIncoming();
        List<Long> oldIncoming = stateInfo.fetchIncoming(); // TODO: replace with primitive specialization

        StateInfo<I, Word<O>> newStateInfo = createState(newPrefix);

        AbstractWordBasedDTNode<I, Word<O>, StateInfo<I, Word<O>>> stateLeaf = stateInfo.dtNode;

        AbstractWordBasedDTNode<I, Word<O>, StateInfo<I, Word<O>>> separator = separatorInfo.leastCommonAncestor;
        Word<I> newDiscriminator;
        Word<O> oldOut, newOut;
        if (separator == null) {
            newDiscriminator = Word.fromLetter(sym);
            oldOut = separatorInfo.subtree1Label;
            newOut = separatorInfo.subtree2Label;
        } else {
            newDiscriminator = newDiscriminator(sym, separator.getDiscriminator());
            CompactTransition<O> transition = hypothesis.getTransition(state, sym);
            assert transition != null;
            O transOut = hypothesis.getTransitionOutput(transition);
            oldOut = newOutcome(transOut, separatorInfo.subtree1Label);
            newOut = newOutcome(transOut, separatorInfo.subtree2Label);
        }

        final AbstractWordBasedDTNode<I, Word<O>, StateInfo<I, Word<O>>>.SplitResult sr =
                stateLeaf.split(newDiscriminator, oldOut, newOut, newStateInfo);

        stateInfo.dtNode = sr.nodeOld;
        newStateInfo.dtNode = sr.nodeNew;

        initState(newStateInfo);

        updateTransitions(oldIncoming, stateLeaf);
    }

    private Word<O> newOutcome(O transOutput, Word<O> succOutcome) {
        return succOutcome.prepend(transOutput);
    }

    // private void updateTransitions(TLongList transList, DTNode<I,Word<O>,StateInfo<I,O>> oldDtTarget) {
    private void updateTransitions(List<Long> transList,
                                   AbstractWordBasedDTNode<I, Word<O>, StateInfo<I, Word<O>>> oldDtTarget) { // TODO: replace with primitive specialization
        int numTrans = transList.size();

        final List<Word<I>> transAs = new ArrayList<>(numTrans);

        for (int i = 0; i < numTrans; i++) {
            long encodedTrans = transList.get(i);

            int sourceState = (int) (encodedTrans >> Integer.SIZE);
            int transIdx = (int) encodedTrans;

            StateInfo<I, Word<O>> sourceInfo = stateInfos.get(sourceState);
            I symbol = alphabet.getSymbol(transIdx);

            transAs.add(sourceInfo.accessSequence.append(symbol));
        }

        final List<StateInfo<I, Word<O>>> succs = sift(Collections.nCopies(numTrans, oldDtTarget), transAs);

        for (int i = 0; i < numTrans; i++) {
            long encodedTrans = transList.get(i);

            int sourceState = (int) (encodedTrans >> Integer.SIZE);
            int transIdx = (int) encodedTrans;

            CompactTransition<O> trans = hypothesis.getTransition(sourceState, transIdx);
            assert trans != null;
            setTransition(sourceState, transIdx, succs.get(i), trans.getProperty());
        }
    }

    private Word<I> newDiscriminator(I symbol, Word<I> succDiscriminator) {
        return succDiscriminator.prepend(symbol);
    }

    private StateInfo<I, Word<O>> createInitialState() {
        int state = hypothesis.addIntInitialState();
        assert state == stateInfos.size();

        StateInfo<I, Word<O>> stateInfo = new StateInfo<>(state, Word.epsilon());
        stateInfos.add(stateInfo);

        return stateInfo;
    }

    private StateInfo<I, Word<O>> createState(Word<I> prefix) {
        int state = hypothesis.addIntState();
        assert state == stateInfos.size();

        StateInfo<I, Word<O>> stateInfo = new StateInfo<>(state, prefix);
        stateInfos.add(stateInfo);

        return stateInfo;
    }

    private void initialize() {
        StateInfo<I, Word<O>> init = createInitialState();
        discriminationTree.getRoot().setData(init);
        init.dtNode = discriminationTree.getRoot();
        initState(init);
    }

    private void initState(StateInfo<I, Word<O>> stateInfo) {
        int alphabetSize = alphabet.size();

        int state = stateInfo.id;
        Word<I> accessSequence = stateInfo.accessSequence;

        final List<Word<I>> transAs = new ArrayList<>(alphabetSize);
        final List<DefaultQuery<I, Word<O>>> outputQueries = new ArrayList<>(alphabetSize);

        for (int i = 0; i < alphabetSize; i++) {
            I sym = alphabet.getSymbol(i);
            transAs.add(accessSequence.append(sym));
            outputQueries.add(new DefaultQuery<>(accessSequence, Word.fromLetter(sym)));
        }

        final List<StateInfo<I, Word<O>>> succs = sift(transAs);
        this.oracle.processQueries(outputQueries);

        for (int i = 0; i < alphabetSize; i++) {
            setTransition(state, i, succs.get(i), outputQueries.get(i).getOutput().firstSymbol());
        }
    }

    private void setTransition(int state, int symIdx, StateInfo<I, Word<O>> succInfo, O output) {
        succInfo.addIncoming(state, symIdx);
        hypothesis.setTransition(state, symIdx, succInfo.id, output);
    }

    private List<StateInfo<I, Word<O>>> sift(List<Word<I>> prefixes) {
        return sift(Collections.nCopies(prefixes.size(), discriminationTree.getRoot()), prefixes);
    }

    private List<StateInfo<I, Word<O>>> sift(List<AbstractWordBasedDTNode<I, Word<O>, StateInfo<I, Word<O>>>> starts,
                                             List<Word<I>> prefixes) {

        final List<AbstractWordBasedDTNode<I, Word<O>, StateInfo<I, Word<O>>>> leaves =
                discriminationTree.sift(starts, prefixes);
        final List<StateInfo<I, Word<O>>> result = new ArrayList<>(leaves.size());

        for (int i = 0; i < leaves.size(); i++) {
            final AbstractWordBasedDTNode<I, Word<O>, StateInfo<I, Word<O>>> leaf = leaves.get(i);

            StateInfo<I, Word<O>> succStateInfo = leaf.getData();
            if (succStateInfo == null) {
                // Special case: this is the *first* state of a different
                // acceptance than the initial state
                succStateInfo = createState(prefixes.get(i));
                leaf.setData(succStateInfo);
                succStateInfo.dtNode = leaf;

                initState(succStateInfo);
            }

            result.add(succStateInfo);
        }

        return result;
    }

    @Override
    public void addAlphabetSymbol(I symbol) {

        if (!this.alphabet.containsSymbol(symbol)) {
            this.alphabet.asGrowingAlphabetOrThrowException().addSymbol(symbol);
        }

        this.hypothesis.addAlphabetSymbol(symbol);

        // check if we already have information about the symbol (then the transition is defined) so we don't post
        // redundant queries
        final Integer init = this.hypothesis.getInitialState();
        if (init != null && this.hypothesis.getSuccessor(init, symbol) == null) {
            // use new list to prevent concurrent modification exception
            final List<Word<I>> transAs = new ArrayList<>(this.stateInfos.size());
            final List<DefaultQuery<I, Word<O>>> outputQueries = new ArrayList<>(this.stateInfos.size());

            for (StateInfo<I, Word<O>> si : this.stateInfos) {
                transAs.add(si.accessSequence.append(symbol));
                outputQueries.add(new DefaultQuery<>(si.accessSequence, Word.fromLetter(symbol)));
            }

            final List<StateInfo<I, Word<O>>> succs = sift(transAs);
            this.oracle.processQueries(outputQueries);

            final Iterator<StateInfo<I, Word<O>>> stateIter = this.stateInfos.iterator();
            final Iterator<StateInfo<I, Word<O>>> leafsIter = succs.iterator();
            final Iterator<DefaultQuery<I, Word<O>>> outputsIter = outputQueries.iterator();
            final int inputIdx = this.alphabet.getSymbolIndex(symbol);

            while (stateIter.hasNext() && leafsIter.hasNext()) {
                setTransition(stateIter.next().id,
                              inputIdx,
                              leafsIter.next(),
                              outputsIter.next().getOutput().firstSymbol());
            }
        }
    }

    @Override
    public KearnsVaziraniMealyState<I, O> suspend() {
        return new KearnsVaziraniMealyState<>(hypothesis, discriminationTree, stateInfos);
    }

    @Override
    public void resume(KearnsVaziraniMealyState<I, O> state) {
        this.hypothesis = state.getHypothesis();
        this.discriminationTree = state.getDiscriminationTree();
        this.discriminationTree.setOracle(oracle);
        this.stateInfos = state.getStateInfos();

        final Alphabet<I> oldAlphabet = this.hypothesis.getInputAlphabet();
        if (!oldAlphabet.equals(this.alphabet)) {
            LOGGER.warn(Category.DATASTRUCTURE,
                        "The current alphabet '{}' differs from the resumed alphabet '{}'. Future behavior may be inconsistent",
                        this.alphabet,
                        oldAlphabet);
        }
    }

    static final class BuilderDefaults {

        private BuilderDefaults() {
            // prevent instantiation
        }

        public static boolean repeatedCounterexampleEvaluation() {
            return true;
        }

        public static AcexAnalyzer counterexampleAnalyzer() {
            return AcexAnalyzers.LINEAR_FWD;
        }
    }

    protected class KVAbstractCounterexample extends AbstractBaseCounterexample<Boolean> {

        private final Word<I> ceWord;
        private final MembershipOracle<I, Word<O>> oracle;
        private final StateInfo<I, Word<O>>[] states;
        private final LCAInfo<Word<O>, @Nullable AbstractWordBasedDTNode<I, Word<O>, StateInfo<I, Word<O>>>>[] lcas;

        @SuppressWarnings("unchecked")
        public KVAbstractCounterexample(Word<I> ceWord, Word<O> output, MembershipOracle<I, Word<O>> oracle) {
            super(ceWord.length() + 1);
            this.ceWord = ceWord;
            this.oracle = oracle;

            int m = ceWord.length();
            this.states = new StateInfo[m + 1];
            this.lcas = new LCAInfo[m + 1];

            int currState = hypothesis.getIntInitialState();
            int i = 0;
            states[i++] = stateInfos.get(currState);
            for (I sym : ceWord) {
                currState = hypothesis.getSuccessor(currState, sym);
                states[i++] = stateInfos.get(currState);
            }

            // Output of last transition separates hypothesis from target
            O lastHypOut = hypothesis.getOutput(states[m - 1].id, ceWord.lastSymbol());
            lcas[m] = new LCAInfo<>(null, Word.fromLetter(lastHypOut), Word.fromLetter(output.lastSymbol()));
            super.setEffect(m, false);
        }

        public StateInfo<I, Word<O>> getStateInfo(int idx) {
            return states[idx];
        }

        public LCAInfo<Word<O>, @Nullable AbstractWordBasedDTNode<I, Word<O>, StateInfo<I, Word<O>>>> getLCA(int idx) {
            return lcas[idx];
        }

        @Override
        protected Boolean computeEffect(int index) {
            Word<I> prefix = ceWord.prefix(index);
            StateInfo<I, Word<O>> info = states[index];

            // Save the expected outcomes on the path from the leaf representing the state
            // to the root on a stack
            AbstractWordBasedDTNode<I, Word<O>, StateInfo<I, Word<O>>> node = info.dtNode;
            Deque<Word<O>> expect = new ArrayDeque<>();
            while (!node.isRoot()) {
                Word<O> parentOutcome = node.getParentOutcome();
                assert parentOutcome != null;
                expect.push(parentOutcome);
                node = node.getParent();
            }

            AbstractWordBasedDTNode<I, Word<O>, StateInfo<I, Word<O>>> currNode = discriminationTree.getRoot();

            while (!expect.isEmpty()) {
                Word<I> suffix = currNode.getDiscriminator();
                Word<O> out = oracle.answerQuery(prefix, suffix);
                Word<O> e = expect.pop();
                if (!Objects.equals(out, e)) {
                    lcas[index] = new LCAInfo<>(currNode, e, out);
                    return false;
                }
                currNode = currNode.child(out);
            }

            assert currNode.isLeaf() && expect.isEmpty();
            return true;
        }

        @Override
        public boolean checkEffects(Boolean eff1, Boolean eff2) {
            return !eff1 || eff2;
        }
    }
}
