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
package de.learnlib.algorithm.lambda.ttt;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

import de.learnlib.algorithm.LearningAlgorithm;
import de.learnlib.algorithm.lambda.ttt.dt.AbstractDecisionTree;
import de.learnlib.algorithm.lambda.ttt.dt.DTLeaf;
import de.learnlib.algorithm.lambda.ttt.pt.PTNode;
import de.learnlib.algorithm.lambda.ttt.pt.PrefixTree;
import de.learnlib.algorithm.lambda.ttt.st.SuffixTrie;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.query.DefaultQuery;
import de.learnlib.util.MQUtil;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.SupportsGrowingAlphabet;
import net.automatalib.automaton.concept.FiniteRepresentation;
import net.automatalib.automaton.concept.SuffixOutput;
import net.automatalib.word.Word;

public abstract class AbstractTTTLambda<M extends SuffixOutput<I, D>, I, D>
        implements LearningAlgorithm<M, I, D>, SupportsGrowingAlphabet<I>, FiniteRepresentation {

    private final MembershipOracle<I, D> ceqs;
    private final Alphabet<I> alphabet;
    protected final SuffixTrie<I> strie;
    protected final PrefixTree<I, D> ptree;

    protected AbstractTTTLambda(Alphabet<I> alphabet, MembershipOracle<I, D> ceqs) {
        this.alphabet = alphabet;
        this.ceqs = ceqs;

        this.strie = new SuffixTrie<>();
        this.ptree = new PrefixTree<>();
    }

    protected abstract int maxSearchIndex(int ceLength);

    protected abstract DTLeaf<I, D> getState(Word<I> prefix);

    protected abstract AbstractDecisionTree<I, D> dtree();

    @Override
    public void startLearning() {
        assert dtree() != null && getHypothesisModel() != null;
        dtree().sift(ptree.root());
        makeConsistent();
    }

    @Override
    public boolean refineHypothesis(DefaultQuery<I, D> counterexample) {
        final Deque<DefaultQuery<I, D>> witnesses = new ArrayDeque<>();
        witnesses.add(counterexample);
        boolean refined = false;

        while (MQUtil.isCounterexample(counterexample, getHypothesisModel())) {
            final DefaultQuery<I, D> witness = witnesses.getFirst();

            if (witness.getOutput() == null) {
                witness.answer(ceqs.answerQuery(witness.getPrefix(), witness.getSuffix()));
            }

            final boolean valid = MQUtil.isCounterexample(witness, getHypothesisModel());

            if (valid) {
                analyzeCounterexample(witness, witnesses);
                makeConsistent();
                refined = true;
            } else {
                witnesses.pop();
            }
        }

        assert size() == dtree().leaves().size();
        return refined;
    }

    @Override
    public void addAlphabetSymbol(I symbol) {
        if (!this.alphabet.containsSymbol(symbol)) {
            this.alphabet.asGrowingAlphabetOrThrowException().addSymbol(symbol);
        }

        // check if symbol is already part of ptree/hypothesis
        if (ptree.root().succ(symbol) == null) {

            List<DTLeaf<I, D>> leaves = dtree().leaves();

            for (DTLeaf<I, D> leaf : leaves) {
                PTNode<I, D> u = leaf.getShortPrefixes().get(0);
                assert u != null;
                PTNode<I, D> ua = u.append(symbol);
                assert ua != null;
                dtree().sift(ua);
            }

            makeConsistent();
        }
    }

    private void makeConsistent() {
        while (dtree().makeConsistent()) {
            // do nothing ...
        }
    }

    private PTNode<I, D> longestShortPrefixOf(Word<I> ce) {
        PTNode<I, D> cur = ptree.root();
        int i = 0;
        do {
            cur = cur.succ(ce.getSymbol(i++));
            assert cur != null;
        } while (cur.state().getShortPrefixes().contains(cur) && i < ce.length());

        assert !cur.state().getShortPrefixes().contains(cur);
        return cur;
    }

    private void analyzeCounterexample(DefaultQuery<I, D> counterexample, Deque<DefaultQuery<I, D>> witnesses) {
        M hyp = getHypothesisModel();
        Word<I> ce = counterexample.getInput();
        PTNode<I, D> ua = null;
        PTNode<I, D> lsp = longestShortPrefixOf(ce);
        int upper = maxSearchIndex(ce.length());
        int lower = lsp.word().length() - 1;
        while (upper - lower > 1) {
            int mid = (upper + lower) / 2;
            Word<I> prefix = ce.prefix(mid);
            Word<I> suffix = ce.suffix(ce.length() - mid);

            DTLeaf<I, D> q = getState(prefix);
            assert q != null;

            boolean stillCe = false;
            for (PTNode<I, D> u : q.getShortPrefixes()) {
                D sysOut = ceqs.answerQuery(u.word(), suffix);
                D hypOut = hyp.computeSuffixOutput(u.word(), suffix);
                if (!Objects.equals(sysOut, hypOut)) {
                    ua = u.succ(suffix.firstSymbol());
                    lower = mid;
                    stillCe = true;
                    break;
                }
            }
            if (stillCe) {
                continue;
            }
            upper = mid;
        }

        if (ua == null) {
            assert lower == lsp.word().length() - 1;
            ua = lsp;
        }

        // add witnesses
        int mid = (upper + lower) / 2;
        Word<I> sprime = ce.suffix(ce.length() - (mid + 1));
        DTLeaf<I, D> qnext = getState(ua.word());
        for (PTNode<I, D> uprime : qnext.getShortPrefixes()) {
            witnesses.push(new DefaultQuery<>(uprime.word(), sprime));
        }
        witnesses.push(new DefaultQuery<>(ua.word(), sprime));

        ua.makeShortPrefix();
    }
}
