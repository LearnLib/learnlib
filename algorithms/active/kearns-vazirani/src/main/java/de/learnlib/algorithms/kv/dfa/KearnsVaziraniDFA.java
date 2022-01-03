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
package de.learnlib.algorithms.kv.dfa;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.function.BooleanSupplier;

import com.github.misberner.buildergen.annotations.GenerateBuilder;
import de.learnlib.acex.AcexAnalyzer;
import de.learnlib.acex.analyzers.AcexAnalyzers;
import de.learnlib.acex.impl.AbstractBaseCounterexample;
import de.learnlib.algorithms.kv.StateInfo;
import de.learnlib.api.Resumable;
import de.learnlib.api.algorithm.LearningAlgorithm.DFALearner;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.datastructure.discriminationtree.BinaryDTree;
import de.learnlib.datastructure.discriminationtree.model.AbstractWordBasedDTNode;
import de.learnlib.datastructure.discriminationtree.model.LCAInfo;
import net.automatalib.SupportsGrowingAlphabet;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.commons.smartcollections.ArrayStorage;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Kearns/Vazirani algorithm for learning DFA, as described in the book "An Introduction to Computational Learning
 * Theory" by Michael Kearns and Umesh Vazirani.
 *
 * @param <I>
 *         input symbol type
 *
 * @author Malte Isberner
 */
public class KearnsVaziraniDFA<I>
        implements DFALearner<I>, SupportsGrowingAlphabet<I>, Resumable<KearnsVaziraniDFAState<I>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(KearnsVaziraniDFA.class);

    private final Alphabet<I> alphabet;
    private final MembershipOracle<I, Boolean> oracle;
    private final boolean repeatedCounterexampleEvaluation;
    private final AcexAnalyzer ceAnalyzer;
    private BinaryDTree<I, StateInfo<I, Boolean>> discriminationTree;
    protected List<StateInfo<I, Boolean>> stateInfos = new ArrayList<>();
    private CompactDFA<I> hypothesis;

    /**
     * Constructor.
     *
     * @param alphabet
     *         the learning alphabet
     * @param oracle
     *         the membership oracle
     */
    @GenerateBuilder
    public KearnsVaziraniDFA(Alphabet<I> alphabet,
                             MembershipOracle<I, Boolean> oracle,
                             boolean repeatedCounterexampleEvaluation,
                             AcexAnalyzer counterexampleAnalyzer) {
        this.alphabet = alphabet;
        this.hypothesis = new CompactDFA<>(alphabet);
        this.discriminationTree = new BinaryDTree<>(oracle);
        this.oracle = oracle;
        this.repeatedCounterexampleEvaluation = repeatedCounterexampleEvaluation;
        this.ceAnalyzer = counterexampleAnalyzer;
    }

    @Override
    public void startLearning() {
        initialize();
    }

    @Override
    public boolean refineHypothesis(DefaultQuery<I, Boolean> ceQuery) {
        if (hypothesis.size() == 0) {
            throw new IllegalStateException("Not initialized");
        }
        Word<I> input = ceQuery.getInput();
        boolean output = ceQuery.getOutput();
        if (!refineHypothesisSingle(input, output)) {
            return false;
        }
        if (repeatedCounterexampleEvaluation) {
            while (refineHypothesisSingle(input, output)) {}
        }
        return true;
    }

    @Override
    public DFA<?, I> getHypothesisModel() {
        if (hypothesis.size() == 0) {
            throw new IllegalStateException("Not started");
        }
        return hypothesis;
    }

    public BinaryDTree<I, StateInfo<I, Boolean>> getDiscriminationTree() {
        return discriminationTree;
    }

    private boolean refineHypothesisSingle(Word<I> input, boolean output) {
        int inputLen = input.length();

        if (inputLen < 2) {
            return false;
        }

        if (hypothesis.accepts(input) == output) {
            return false;
        }

        KVAbstractCounterexample acex = new KVAbstractCounterexample(input, output, oracle);
        int idx = ceAnalyzer.analyzeAbstractCounterexample(acex, 1);

        Word<I> prefix = input.prefix(idx);
        StateInfo<I, Boolean> srcStateInfo = acex.getStateInfo(idx);
        I sym = input.getSymbol(idx);
        LCAInfo<Boolean, AbstractWordBasedDTNode<I, Boolean, StateInfo<I, Boolean>>> lca = acex.getLCA(idx + 1);
        assert lca != null;

        splitState(srcStateInfo, prefix, sym, lca);

        return true;
    }

    private void splitState(StateInfo<I, Boolean> stateInfo,
                            Word<I> newPrefix,
                            I sym,
                            LCAInfo<Boolean, AbstractWordBasedDTNode<I, Boolean, StateInfo<I, Boolean>>> separatorInfo) {
        int state = stateInfo.id;
        boolean oldAccepting = hypothesis.isAccepting(state);
        // TLongList oldIncoming = stateInfo.fetchIncoming();
        List<Long> oldIncoming = stateInfo.fetchIncoming(); // TODO: replace with primitive specialization

        StateInfo<I, Boolean> newStateInfo = createState(newPrefix, oldAccepting);

        AbstractWordBasedDTNode<I, Boolean, StateInfo<I, Boolean>> stateLeaf = stateInfo.dtNode;

        AbstractWordBasedDTNode<I, Boolean, StateInfo<I, Boolean>> separator = separatorInfo.leastCommonAncestor;
        Word<I> newDiscriminator = newDiscriminator(sym, separator.getDiscriminator());

        AbstractWordBasedDTNode<I, Boolean, StateInfo<I, Boolean>>.SplitResult sr = stateLeaf.split(newDiscriminator,
                                                                                                    separatorInfo.subtree1Label,
                                                                                                    separatorInfo.subtree2Label,
                                                                                                    newStateInfo);

        stateInfo.dtNode = sr.nodeOld;
        newStateInfo.dtNode = sr.nodeNew;

        initState(newStateInfo);

        updateTransitions(oldIncoming, stateLeaf);
    }

    // private void updateTransitions(TLongList transList, DTNode<I, Boolean, StateInfo<I>> oldDtTarget) {
    private void updateTransitions(List<Long> transList,
                                   AbstractWordBasedDTNode<I, Boolean, StateInfo<I, Boolean>> oldDtTarget) { // TODO: replace with primitive specialization
        int numTrans = transList.size();

        final List<Word<I>> transAs = new ArrayList<>(numTrans);

        for (int i = 0; i < numTrans; i++) {
            long encodedTrans = transList.get(i);

            int sourceState = (int) (encodedTrans >> Integer.SIZE);
            int transIdx = (int) (encodedTrans);

            StateInfo<I, Boolean> sourceInfo = stateInfos.get(sourceState);
            I symbol = alphabet.getSymbol(transIdx);

            transAs.add(sourceInfo.accessSequence.append(symbol));
        }

        final List<StateInfo<I, Boolean>> succs = sift(Collections.nCopies(numTrans, oldDtTarget), transAs);

        for (int i = 0; i < numTrans; i++) {
            long encodedTrans = transList.get(i);

            int sourceState = (int) (encodedTrans >> Integer.SIZE);
            int transIdx = (int) (encodedTrans);

            setTransition(sourceState, transIdx, succs.get(i));
        }
    }

    private Word<I> newDiscriminator(I symbol, Word<I> succDiscriminator) {
        return succDiscriminator.prepend(symbol);
    }

    private void initialize() {
        boolean initAccepting = oracle.answerQuery(Word.epsilon());
        StateInfo<I, Boolean> initStateInfo = createInitialState(initAccepting);

        AbstractWordBasedDTNode<I, Boolean, StateInfo<I, Boolean>> root = discriminationTree.getRoot();
        root.setData(initStateInfo);
        initStateInfo.dtNode = root.split(Word.epsilon(), initAccepting, !initAccepting).nodeOld;

        initState(initStateInfo);
    }

    private StateInfo<I, Boolean> createInitialState(boolean accepting) {
        int state = hypothesis.addIntInitialState(accepting);
        StateInfo<I, Boolean> si = new StateInfo<>(state, Word.epsilon());
        assert stateInfos.size() == state;
        stateInfos.add(si);

        return si;
    }

    private StateInfo<I, Boolean> createState(Word<I> accessSequence, boolean accepting) {
        int state = hypothesis.addIntState(accepting);
        StateInfo<I, Boolean> si = new StateInfo<>(state, accessSequence);
        assert stateInfos.size() == state;
        stateInfos.add(si);

        return si;
    }

    private void initState(StateInfo<I, Boolean> stateInfo) {
        int alphabetSize = alphabet.size();

        int state = stateInfo.id;
        Word<I> accessSequence = stateInfo.accessSequence;

        final ArrayStorage<Word<I>> transAs = new ArrayStorage<>(alphabetSize);

        for (int i = 0; i < alphabetSize; i++) {
            I sym = alphabet.getSymbol(i);
            transAs.set(i, accessSequence.append(sym));
        }

        final List<StateInfo<I, Boolean>> succs = sift(transAs);

        for (int i = 0; i < alphabetSize; i++) {
            setTransition(state, i, succs.get(i));
        }
    }

    private void setTransition(int state, int symIdx, StateInfo<I, Boolean> succInfo) {
        succInfo.addIncoming(state, symIdx);
        hypothesis.setTransition(state, symIdx, succInfo.id);
    }

    private List<StateInfo<I, Boolean>> sift(List<Word<I>> prefixes) {
        return sift(Collections.nCopies(prefixes.size(), discriminationTree.getRoot()), prefixes);
    }

    private List<StateInfo<I, Boolean>> sift(List<AbstractWordBasedDTNode<I, Boolean, StateInfo<I, Boolean>>> starts,
                                             List<Word<I>> prefixes) {

        final List<AbstractWordBasedDTNode<I, Boolean, StateInfo<I, Boolean>>> leaves =
                discriminationTree.sift(starts, prefixes);
        final ArrayStorage<StateInfo<I, Boolean>> result = new ArrayStorage<>(leaves.size());

        for (int i = 0; i < leaves.size(); i++) {
            final AbstractWordBasedDTNode<I, Boolean, StateInfo<I, Boolean>> leaf = leaves.get(i);

            StateInfo<I, Boolean> succStateInfo = leaf.getData();
            if (succStateInfo == null) {
                // Special case: this is the *first* state of a different
                // acceptance than the initial state
                boolean initAccepting = hypothesis.isAccepting(hypothesis.getIntInitialState());
                succStateInfo = createState(prefixes.get(i), !initAccepting);
                leaf.setData(succStateInfo);
                succStateInfo.dtNode = leaf;

                initState(succStateInfo);
            }

            result.set(i, succStateInfo);
        }

        return result;
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
            // use new list to prevent concurrent modification exception
            final List<Word<I>> transAs = new ArrayList<>(this.stateInfos.size());
            for (final StateInfo<I, Boolean> si : this.stateInfos) {
                transAs.add(si.accessSequence.append(symbol));
            }

            final List<StateInfo<I, Boolean>> succs = sift(transAs);

            final Iterator<StateInfo<I, Boolean>> stateIter = this.stateInfos.iterator();
            final Iterator<StateInfo<I, Boolean>> leafsIter = succs.iterator();
            final int inputIdx = this.alphabet.getSymbolIndex(symbol);

            while (stateIter.hasNext() && leafsIter.hasNext()) {
                setTransition(stateIter.next().id, inputIdx, leafsIter.next());
            }

            // in case the new symbol added a new state (see sift method) we allow at max one additional state
            assert !stateIter.hasNext() || !((BooleanSupplier) () -> {
                stateIter.next();
                return stateIter.hasNext();
            }).getAsBoolean();
            assert !leafsIter.hasNext();
        }
    }

    @Override
    public KearnsVaziraniDFAState<I> suspend() {
        return new KearnsVaziraniDFAState<>(hypothesis, discriminationTree, stateInfos);
    }

    @Override
    public void resume(final KearnsVaziraniDFAState<I> state) {
        this.hypothesis = state.getHypothesis();
        this.discriminationTree = state.getDiscriminationTree();
        this.discriminationTree.setOracle(oracle);
        this.stateInfos = state.getStateInfos();

        final Alphabet<I> oldAlphabet = this.hypothesis.getInputAlphabet();
        if (!oldAlphabet.equals(this.alphabet)) {
            LOGGER.warn(
                    "The current alphabet '{}' differs from the resumed alphabet '{}'. Future behavior may be inconsistent",
                    this.alphabet,
                    oldAlphabet);
        }
    }

    public static final class BuilderDefaults {

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
        private final MembershipOracle<I, Boolean> oracle;
        private final StateInfo<I, Boolean>[] states;
        private final LCAInfo<Boolean, AbstractWordBasedDTNode<I, Boolean, StateInfo<I, Boolean>>>[] lcas;

        @SuppressWarnings("unchecked")
        public KVAbstractCounterexample(Word<I> ceWord, boolean output, MembershipOracle<I, Boolean> oracle) {
            super(ceWord.length() + 1);
            this.ceWord = ceWord;
            this.oracle = oracle;

            int m = ceWord.length();
            this.states = new StateInfo[m + 1];
            this.lcas = new LCAInfo[m + 1];
            int i = 0;

            int currState = hypothesis.getIntInitialState();
            states[i++] = stateInfos.get(currState);
            for (I sym : ceWord) {
                currState = hypothesis.getSuccessor(currState, sym);
                states[i++] = stateInfos.get(currState);
            }

            // Acceptance/Non-acceptance separates hypothesis from target
            lcas[m] = new LCAInfo<>(discriminationTree.getRoot(), !output, output);
        }

        public StateInfo<I, Boolean> getStateInfo(int idx) {
            return states[idx];
        }

        public LCAInfo<Boolean, AbstractWordBasedDTNode<I, Boolean, StateInfo<I, Boolean>>> getLCA(int idx) {
            return lcas[idx];
        }

        @Override
        protected Boolean computeEffect(int index) {
            Word<I> prefix = ceWord.prefix(index);
            StateInfo<I, Boolean> info = states[index];

            // Save the expected outcomes on the path from the leaf representing the state
            // to the root on a stack
            AbstractWordBasedDTNode<I, Boolean, StateInfo<I, Boolean>> node = info.dtNode;
            Deque<Boolean> expect = new ArrayDeque<>();
            while (!node.isRoot()) {
                Boolean parentOutcome = node.getParentOutcome();
                assert parentOutcome != null;
                expect.push(parentOutcome);
                node = node.getParent();
            }

            AbstractWordBasedDTNode<I, Boolean, StateInfo<I, Boolean>> currNode = discriminationTree.getRoot();

            while (!expect.isEmpty()) {
                Word<I> suffix = currNode.getDiscriminator();
                boolean out = oracle.answerQuery(prefix, suffix);
                if (out != expect.pop()) {
                    lcas[index] = new LCAInfo<>(currNode, !out, out);
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
