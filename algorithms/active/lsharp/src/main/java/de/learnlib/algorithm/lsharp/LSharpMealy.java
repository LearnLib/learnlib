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
package de.learnlib.algorithm.lsharp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import com.google.common.collect.HashBiMap;
import de.learnlib.algorithm.LearningAlgorithm.MealyLearner;
import de.learnlib.oracle.AdaptiveMembershipOracle;
import de.learnlib.query.DefaultQuery;
import de.learnlib.tooling.annotation.builder.GenerateBuilder;
import de.learnlib.util.mealy.MealyUtil;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.automaton.transducer.impl.CompactMealy;
import net.automatalib.common.util.Pair;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

public class LSharpMealy<I, O> implements MealyLearner<I, O> {

    private final LSOracle<I, O> oqOracle;
    private final Alphabet<I> inputAlphabet;
    private final List<Word<I>> basis;
    private final Map<Word<I>, List<Word<I>>> frontierToBasisMap;
    private final HashBiMap<Word<I>, Integer> basisMap;

    public LSharpMealy(Alphabet<I> alphabet, AdaptiveMembershipOracle<I, O> oracle, Rule2 rule2, Rule3 rule3) {
        this(alphabet, oracle, rule2, rule3, null, null);
    }

    public LSharpMealy(Alphabet<I> alphabet,
                       AdaptiveMembershipOracle<I, O> oracle,
                       Rule2 rule2,
                       Rule3 rule3,
                       Word<I> sinkState,
                       O sinkOutput) {
        this(alphabet, oracle, rule2, rule3, sinkState, sinkOutput, new Random());
    }

    @GenerateBuilder(defaults = BuilderDefaults.class)
    public LSharpMealy(Alphabet<I> alphabet,
                       AdaptiveMembershipOracle<I, O> oracle,
                       Rule2 rule2,
                       Rule3 rule3,
                       Word<I> sinkState,
                       O sinkOutput,
                       Random random) {
        this.oqOracle = new LSOracle<>(oracle,
                                       new NormalObservationTree<>(alphabet),
                                       rule2,
                                       rule3,
                                       sinkState,
                                       sinkOutput,
                                       random);
        this.inputAlphabet = alphabet;
        this.basis = new ArrayList<>();
        basis.add(Word.epsilon());
        this.frontierToBasisMap = new HashMap<>();
        this.basisMap = HashBiMap.create();
    }

    public boolean processCex(DefaultQuery<I, Word<O>> cex, MealyMachine<Integer, I, ?, O> mealy) {
        assert cex != null;
        Word<I> ceInput = cex.getInput();
        Word<O> ceOutput = cex.getOutput();
        oqOracle.addObservation(ceInput, ceOutput);
        int prefixIndex = MealyUtil.findMismatch(mealy, ceInput, ceOutput);
        if (prefixIndex == MealyUtil.NO_MISMATCH) {
            return false;
        }
        this.processBinarySearch(ceInput.prefix(prefixIndex), ceOutput.prefix(prefixIndex), mealy);
        return true;
    }

    public void processBinarySearch(Word<I> ceInput, Word<O> ceOutput, MealyMachine<Integer, I, ?, O> mealy) {
        Integer r = oqOracle.getTree().getSucc(oqOracle.getTree().defaultState(), ceInput);
        assert r != null;
        this.updateFrontierAndBasis();
        if (this.frontierToBasisMap.containsKey(ceInput) || basis.contains(ceInput)) {
            return;
        }

        Integer q = mealy.getSuccessor(mealy.getInitialState(), ceInput);
        Word<I> accQT = basisMap.inverse().get(q);
        assert accQT != null;

        NormalObservationTree<I, O> oTree = oqOracle.getTree();
        Integer qt = oTree.getSucc(oTree.defaultState(), accQT);
        assert qt != null;

        int x = 0;
        for (Word<I> prefix : ceInput.prefixes(false)) {
            if (!prefix.isEmpty() && frontierToBasisMap.containsKey(prefix)) {
                x = prefix.length();
                break;
            }
        }

        assert x > 0;
        int y = ceInput.size();
        int h = Math.floorDiv(x + y, 2);

        Word<I> sigma1 = ceInput.prefix(h);
        Word<I> sigma2 = ceInput.suffix(ceInput.size() - h);
        Integer qp = mealy.getSuccessor(mealy.getInitialState(), sigma1);
        assert qp != null;
        Word<I> accQPt = basisMap.inverse().get(qp);
        assert accQPt != null;

        Word<I> eta = ApartnessUtil.computeWitness(oTree, r, qt);
        assert eta != null;

        Word<I> outputQuery = accQPt.concat(sigma2).concat(eta);
        Word<O> sulResponse = oqOracle.outputQuery(outputQuery);
        Integer qpt = oTree.getSucc(oTree.defaultState(), accQPt);
        assert qpt != null;

        Integer rp = oTree.getSucc(oTree.defaultState(), sigma1);
        assert rp != null;

        @Nullable
        Word<I> wit = ApartnessUtil.computeWitness(oTree, qpt, rp);
        if (wit != null) {
            processBinarySearch(sigma1, ceOutput.prefix(sigma1.length()), mealy);
        } else {
            Word<I> newInputs = accQPt.concat(sigma2);
            processBinarySearch(newInputs, sulResponse.prefix(newInputs.length()), mealy);
        }
    }

    public void makeObsTreeAdequate() {
        do {
            List<Pair<Word<I>, List<Word<I>>>> newFrontier = oqOracle.exploreFrontier(basis);
            for (Pair<Word<I>, List<Word<I>>> pair : newFrontier) {
                frontierToBasisMap.put(pair.getFirst(), pair.getSecond());
            }

            for (Entry<Word<I>, List<Word<I>>> entry : frontierToBasisMap.entrySet()) {
                if (entry.getValue().size() <= 1) {
                    continue;
                }
                List<Word<I>> newCands = oqOracle.identifyFrontier(entry.getKey(), entry.getValue());
                frontierToBasisMap.put(entry.getKey(), newCands);
            }

            this.promoteFrontierState();
        } while (!this.treeIsAdequate());
    }

    public void promoteFrontierState() {
        Word<I> newBS = null;
        for (Entry<Word<I>, List<Word<I>>> e : frontierToBasisMap.entrySet()) {
            if (e.getValue().isEmpty()) {
                newBS = e.getKey();
                break;
            }
        }
        if (newBS == null) {
            return;
        }

        Word<I> bs = Word.fromWords(newBS);
        basis.add(bs);
        frontierToBasisMap.remove(bs);
        NormalObservationTree<I, O> oTree = oqOracle.getTree();

        for (Entry<Word<I>, List<Word<I>>> e : frontierToBasisMap.entrySet()) {
            if (!ApartnessUtil.accStatesAreApart(oTree, e.getKey(), bs)) {
                e.getValue().add(bs);
            }
        }
    }

    public boolean treeIsAdequate() {
        this.checkFrontierConsistency();

        for (List<Word<I>> value : frontierToBasisMap.values()) {
            if (value.size() != 1) {
                return false;
            }
        }

        NormalObservationTree<I, O> oTree = oqOracle.getTree();
        List<Pair<Word<I>, I>> basisIpPairs = new ArrayList<>(basis.size() * inputAlphabet.size());
        for (Word<I> b : basis) {
            for (I i : inputAlphabet) {
                basisIpPairs.add(Pair.of(b, i));
            }
        }

        for (Pair<Word<I>, I> p : basisIpPairs) {
            Integer q = oTree.getSucc(oTree.defaultState(), p.getFirst());
            if (oTree.getOut(q, p.getSecond()) == null) {
                return false;
            }
        }

        return true;
    }

    public void updateFrontierAndBasis() {
        NormalObservationTree<I, O> oTree = oqOracle.getTree();

        for (Entry<Word<I>, List<Word<I>>> e : frontierToBasisMap.entrySet()) {
            e.getValue().removeIf(bs -> ApartnessUtil.accStatesAreApart(oTree, e.getKey(), bs));
        }

        this.promoteFrontierState();
        this.checkFrontierConsistency();

        for (Entry<Word<I>, List<Word<I>>> e : frontierToBasisMap.entrySet()) {
            e.getValue().removeIf(bs -> ApartnessUtil.accStatesAreApart(oTree, e.getKey(), bs));
        }
    }

    public CompactMealy<I, O> constructHypothesis() {

        CompactMealy<I, O> result = new CompactMealy<>(inputAlphabet, basis.size());
        basisMap.clear();

        for (Word<I> bAcc : basis) {
            basisMap.put(bAcc, result.addState());
        }

        NormalObservationTree<I, O> oTree = oqOracle.getTree();
        for (Word<I> q : basis) {
            for (I i : inputAlphabet) {
                Integer bs = oTree.getSucc(oTree.defaultState(), q);
                assert bs != null;
                O output = oTree.getOut(bs, i);
                assert output != null;
                Word<I> fAcc = q.append(i);

                Pair<Word<I>, Boolean> pair = this.identifyFrontierOrBasis(fAcc);
                Word<I> dest = pair.getFirst();

                Integer hypBS = basisMap.get(q);
                assert hypBS != null;
                Integer hypDest = basisMap.get(dest);
                assert hypDest != null;
                result.addTransition(hypBS, i, hypDest, output);
            }
        }

        result.setInitialState(0);
        return result;
    }

    public Pair<Word<I>, Boolean> identifyFrontierOrBasis(Word<I> seq) {
        if (basis.contains(seq)) {
            return Pair.of(seq, false);
        }

        Word<I> bs = frontierToBasisMap.get(seq).get(0);
        return Pair.of(bs, true);
    }

    public void initObsTree(@Nullable List<Pair<Word<I>, Word<O>>> logs) {
        if (logs != null) {
            for (Pair<Word<I>, Word<O>> pair : logs) {
                oqOracle.addObservation(pair.getFirst(), pair.getSecond());
            }
        }
    }

    public void checkFrontierConsistency() {
        List<Word<I>> basisSet = new ArrayList<>(basis);
        NormalObservationTree<I, O> oTree = oqOracle.getTree();

        for (Word<I> bs : basisSet) {
            for (I i : inputAlphabet) {
                Word<I> fsAcc = bs.append(i);
                if (oTree.getSucc(oTree.defaultState(), fsAcc) != null && !basis.contains(fsAcc) &&
                    !frontierToBasisMap.containsKey(fsAcc)) {
                    List<Word<I>> candidates = new ArrayList<>(basis.size());
                    for (Word<I> b : basis) {
                        if (!ApartnessUtil.accStatesAreApart(oTree, fsAcc, b)) {
                            candidates.add(b);
                        }
                    }
                    frontierToBasisMap.put(fsAcc, candidates);
                }
            }
        }
    }

    public DefaultQuery<I, Word<O>> checkConsistency(MealyMachine<Integer, I, ?, O> mealy) {
        NormalObservationTree<I, O> oTree = oqOracle.getTree();
        @Nullable
        Word<I> wit = ApartnessUtil.treeAndHypComputeWitness(oTree, oTree.defaultState(), mealy, 0);
        if (wit == null) {
            return null;
        }

        Word<O> os = oTree.getObservation(null, wit);
        assert os != null;
        return new DefaultQuery<>(wit, os);
    }

    @Override
    public void startLearning() {
        this.initObsTree(null);
    }

    @Override
    public boolean refineHypothesis(DefaultQuery<I, Word<O>> ceQuery) {
        return processCex(ceQuery, getHypothesisModel());
    }

    @Override
    public CompactMealy<I, O> getHypothesisModel() {
        while (true) {
            this.makeObsTreeAdequate();
            CompactMealy<I, O> hyp = this.constructHypothesis();

            DefaultQuery<I, Word<O>> ce = this.checkConsistency(hyp);
            if (ce != null) {
                this.processCex(ce, hyp);
            } else {
                return hyp;
            }
        }
    }

    static final class BuilderDefaults {

        private BuilderDefaults() {
            // prevent instantiation
        }

        public static <I> Word<I> sinkState() {
            return null;
        }

        public static <O> O sinkOutput() {
            return null;
        }

        public static Random random() {
            return new Random();
        }
    }
}
