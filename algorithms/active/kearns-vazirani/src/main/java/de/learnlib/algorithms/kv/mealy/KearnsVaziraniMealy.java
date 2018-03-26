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
package de.learnlib.algorithms.kv.mealy;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

import com.github.misberner.buildergen.annotations.GenerateBuilder;
import de.learnlib.acex.AcexAnalyzer;
import de.learnlib.acex.analyzers.AcexAnalyzers;
import de.learnlib.acex.impl.AbstractBaseCounterexample;
import de.learnlib.algorithms.kv.StateInfo;
import de.learnlib.api.algorithm.LearningAlgorithm.MealyLearner;
import de.learnlib.api.algorithm.feature.ResumableLearner;
import de.learnlib.api.algorithm.feature.SupportsGrowingAlphabet;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.datastructure.discriminationtree.MultiDTree;
import de.learnlib.datastructure.discriminationtree.model.AbstractWordBasedDTNode;
import de.learnlib.datastructure.discriminationtree.model.AbstractWordBasedDiscriminationTree;
import de.learnlib.datastructure.discriminationtree.model.LCAInfo;
import de.learnlib.util.mealy.MealyUtil;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;

/**
 * An adaption of the Kearns/Vazirani algorithm for Mealy machines.
 *
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbol type
 *
 * @author Malte Isberner
 */
public class KearnsVaziraniMealy<I, O>
        implements MealyLearner<I, O>, SupportsGrowingAlphabet<I>, ResumableLearner<KearnsVaziraniMealyState<I, O>> {

    private Alphabet<I> alphabet;
    private final MembershipOracle<I, Word<O>> oracle;
    private final boolean repeatedCounterexampleEvaluation;
    private final AcexAnalyzer ceAnalyzer;
    protected AbstractWordBasedDiscriminationTree<I, Word<O>, StateInfo<I, Word<O>>> discriminationTree;
    protected List<StateInfo<I, Word<O>>> stateInfos = new ArrayList<>();
    private CompactMealy<I, O> hypothesis;

    @GenerateBuilder
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
        LCAInfo<Word<O>, AbstractWordBasedDTNode<I, Word<O>, StateInfo<I, Word<O>>>> lca = acex.getLCA(idx + 1);
        assert lca != null;

        splitState(srcStateInfo, prefix, sym, lca);

        return true;
    }

    private void splitState(StateInfo<I, Word<O>> stateInfo,
                            Word<I> newPrefix,
                            I sym,
                            LCAInfo<Word<O>, AbstractWordBasedDTNode<I, Word<O>, StateInfo<I, Word<O>>>> separatorInfo) {
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
            O transOut = hypothesis.getOutput(state, sym);
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
        for (int i = 0; i < numTrans; i++) {
            long encodedTrans = transList.get(i);

            int sourceState = (int) (encodedTrans >> StateInfo.INTEGER_WORD_WIDTH);
            int transIdx = (int) (encodedTrans);

            StateInfo<I, Word<O>> sourceInfo = stateInfos.get(sourceState);
            I symbol = alphabet.getSymbol(transIdx);

            StateInfo<I, Word<O>> succInfo = sift(oldDtTarget, sourceInfo.accessSequence.append(symbol));

            O output = hypothesis.getTransition(sourceState, transIdx).getOutput();
            setTransition(sourceState, transIdx, succInfo, output);
        }
    }

    private Word<I> newDiscriminator(I symbol, Word<I> succDiscriminator) {
        return succDiscriminator.prepend(symbol);
    }

    private StateInfo<I, Word<O>> createInitialState() {
        int state = hypothesis.addIntInitialState();
        assert state == stateInfos.size();

        StateInfo<I, Word<O>> stateInfo = new StateInfo<>(state, Word.<I>epsilon());
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

        for (int i = 0; i < alphabetSize; i++) {
            I sym = alphabet.getSymbol(i);

            O output = oracle.answerQuery(accessSequence, Word.fromLetter(sym)).firstSymbol();

            Word<I> transAs = accessSequence.append(sym);

            StateInfo<I, Word<O>> succInfo = sift(transAs);
            setTransition(state, i, succInfo, output);
        }
    }

    private void setTransition(int state, int symIdx, StateInfo<I, Word<O>> succInfo, O output) {
        succInfo.addIncoming(state, symIdx);
        hypothesis.setTransition(state, symIdx, succInfo.id, output);
    }

    private StateInfo<I, Word<O>> sift(Word<I> prefix) {
        return sift(discriminationTree.getRoot(), prefix);
    }

    private StateInfo<I, Word<O>> sift(AbstractWordBasedDTNode<I, Word<O>, StateInfo<I, Word<O>>> start,
                                       Word<I> prefix) {
        AbstractWordBasedDTNode<I, Word<O>, StateInfo<I, Word<O>>> leaf = discriminationTree.sift(start, prefix);

        StateInfo<I, Word<O>> succStateInfo = leaf.getData();
        if (succStateInfo == null) {
            // Special case: this is the *first* state with a different output
            // for some discriminator
            succStateInfo = createState(prefix);

            leaf.setData(succStateInfo);
            succStateInfo.dtNode = leaf;

            initState(succStateInfo);
        }

        return succStateInfo;
    }

    @Override
    public void addAlphabetSymbol(I symbol) {

        if (this.alphabet.containsSymbol(symbol)) {
            return;
        }

        final int inputIdx = this.alphabet.size();
        this.hypothesis.addAlphabetSymbol(symbol);

        // since we share the alphabet instance with our hypothesis, our alphabet might have already been updated (if it
        // was already a GrowableAlphabet)
        if (!this.alphabet.containsSymbol(symbol)) {
            this.alphabet = Alphabets.withNewSymbol(this.alphabet, symbol);
        }

        // use new list to prevent concurrent modification exception
        for (final StateInfo<I, Word<O>> si : new ArrayList<>(this.stateInfos)) {
            final int state = si.id;
            final Word<I> accessSequence = si.accessSequence;
            final Word<I> transAs = accessSequence.append(symbol);

            final O output = oracle.answerQuery(accessSequence, Word.fromLetter(symbol)).firstSymbol();

            final StateInfo<I, Word<O>> succ = sift(transAs);
            setTransition(state, inputIdx, succ, output);
        }
    }

    @Override
    public KearnsVaziraniMealyState<I, O> suspend() {
        return new KearnsVaziraniMealyState<>(hypothesis, discriminationTree, stateInfos);
    }

    @Override
    public void resume(final KearnsVaziraniMealyState<I, O> state) {
        this.hypothesis = state.getHypothesis();
        this.discriminationTree = state.getDiscriminationTree();
        this.discriminationTree.setOracle(oracle);
        this.stateInfos = state.getStateInfos();
    }

    static final class BuilderDefaults {

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
        private final LCAInfo<Word<O>, AbstractWordBasedDTNode<I, Word<O>, StateInfo<I, Word<O>>>>[] lcas;

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

        public LCAInfo<Word<O>, AbstractWordBasedDTNode<I, Word<O>, StateInfo<I, Word<O>>>> getLCA(int idx) {
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
                expect.push(node.getParentOutcome());
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
