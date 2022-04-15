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
package de.learnlib.algorithms.oml.ttt;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import de.learnlib.algorithms.oml.ttt.dt.AbstractDecisionTree;
import de.learnlib.algorithms.oml.ttt.dt.DTLeaf;
import de.learnlib.algorithms.oml.ttt.pt.PTNode;
import de.learnlib.algorithms.oml.ttt.pt.PrefixTree;
import de.learnlib.algorithms.oml.ttt.st.SuffixTrie;
import de.learnlib.api.algorithm.LearningAlgorithm;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.SupportsGrowingAlphabet;
import net.automatalib.automata.concepts.InputAlphabetHolder;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;

/**
 * @author fhowar
 */
public abstract class AbstractOptimalTTT<M, I, D>
        implements LearningAlgorithm<M, I, D>, SupportsGrowingAlphabet<I>, InputAlphabetHolder<I> {

    private final MembershipOracle<I, D> ceqs;
    private final Alphabet<I> alphabet;
    protected final SuffixTrie<I> strie;
    protected final PrefixTree<I, D> ptree;

    protected AbstractOptimalTTT(Alphabet<I> alphabet, MembershipOracle<I, D> ceqs) {
        this.alphabet = alphabet;
        this.ceqs = ceqs;

        this.strie = new SuffixTrie<>();
        this.ptree = new PrefixTree<>();
    }

    protected abstract int maxSearchIndex(int ceLength);

    protected abstract D hypOutput(Word<I> word, int length);

    protected abstract DTLeaf<I, D> getState(Word<I> prefix);

    protected abstract AbstractDecisionTree<I, D> dtree();

    protected abstract D suffix(D output, int length);

    protected abstract boolean isCanonical();

    @Override
    public void startLearning() {
        assert dtree() != null && getHypothesisModel() != null;
        dtree().sift(ptree.root());
        makeConsistent();
    }

    @Override
    public boolean refineHypothesis(DefaultQuery<I, D> counterexample) {
        Set<DefaultQuery<I, D>> witnesses = new LinkedHashSet<>();
        witnesses.add(counterexample);
        boolean refined = refineWithWitness(counterexample, witnesses);
        if (!refined) {
            return false;
        }

        if (isCanonical()) {
            return true;
        }

        do {
            for (DefaultQuery<I, D> w : witnesses) {
                refined = refineWithWitness(w, witnesses);
                if (refined) {
                    break;
                }
            }

        } while (refined && isCanonical());
        return true;
    }

    @Override
    public void addAlphabetSymbol(I symbol) {
        if (!this.alphabet.containsSymbol(symbol)) {
            Alphabets.toGrowingAlphabetOrThrowException(this.alphabet).addSymbol(symbol);
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

    @Override
    public Alphabet<I> getInputAlphabet() {
        return this.alphabet;
    }

    private boolean refineWithWitness(DefaultQuery<I, D> counterexample, Set<DefaultQuery<I, D>> witnesses) {
        if (counterexample.getOutput() == null) {
            counterexample.answer(ceqs.answerQuery(counterexample.getInput()));
        }
        D hypOut = hypOutput(counterexample.getInput(), counterexample.getSuffix().length());
        if (Objects.equals(hypOut, counterexample.getOutput())) {
            return false;
        }
        do {
            analyzeCounterexample(counterexample.getInput(), witnesses);
            makeConsistent();
            hypOut = hypOutput(counterexample.getInput(), counterexample.getSuffix().length());
        } while (!Objects.equals(hypOut, counterexample.getOutput()));
        return true;
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
        } while (cur.state().getShortPrefixes().contains(cur) && i < ce.length());

        assert !cur.state().getShortPrefixes().contains(cur);
        return cur;
    }

    private void analyzeCounterexample(Word<I> ce, Set<DefaultQuery<I, D>> witnesses) {
        PTNode<I, D> ua = null;
        PTNode<I, D> lsp = longestShortPrefixOf(ce);
        int upper = maxSearchIndex(ce.length());
        int lower = lsp.word().length() -1;
        D hypOut = hypOutput(ce, ce.length());
        while (upper - lower > 1) {
            int mid = (upper + lower) / 2;
            Word<I> prefix = ce.prefix(mid);
            Word<I> suffix = ce.suffix(ce.length() - mid);

            DTLeaf<I, D> q = getState(prefix);
            assert q != null;

            boolean stillCe = false;
            for (PTNode<I, D> u : q.getShortPrefixes()) {
                D sysOut = suffix(ceqs.answerQuery(u.word(), suffix), suffix.size());
                if (!Objects.equals(sysOut, suffix(hypOut, suffix.size()))) {
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
            assert lower == lsp.word().length()-1;
            ua = lsp;
        }

        // add witnesses
        int mid = (upper + lower) / 2;
        Word<I> sprime = ce.suffix(ce.length() - (mid + 1));
        DTLeaf<I, D> qnext = getState(ua.word());
        for (PTNode<I, D> uprime : qnext.getShortPrefixes()) {
            witnesses.add(new DefaultQuery<>(uprime.word(), sprime));
        }
        witnesses.add(new DefaultQuery<>(ua.word(), sprime));

        ua.makeShortPrefix();
    }
}
