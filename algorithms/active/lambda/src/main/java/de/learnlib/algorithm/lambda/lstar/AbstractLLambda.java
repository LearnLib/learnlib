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
package de.learnlib.algorithm.lambda.lstar;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import de.learnlib.Resumable;
import de.learnlib.algorithm.LearningAlgorithm;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.query.DefaultQuery;
import de.learnlib.util.MQUtil;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.SupportsGrowingAlphabet;
import net.automatalib.automaton.concept.FiniteRepresentation;
import net.automatalib.automaton.concept.SuffixOutput;
import net.automatalib.word.Word;

abstract class AbstractLLambda<M extends SuffixOutput<I, D>, I, D> implements LearningAlgorithm<M, I, D>,
                                                                              SupportsGrowingAlphabet<I>,
                                                                              Resumable<LLambdaState<I, D>>,
                                                                              FiniteRepresentation {

    final Alphabet<I> alphabet;
    final MembershipOracle<I, D> mqs;
    final MembershipOracle<I, D> ceqs;

    private final Set<Word<I>> shortPrefixes;
    private final Map<Word<I>, List<D>> rows;
    private final List<Word<I>> suffixes;

    AbstractLLambda(Alphabet<I> alphabet,
                    MembershipOracle<I, D> mqs,
                    MembershipOracle<I, D> ceqs,
                    List<Word<I>> initialSuffixes) {
        this.alphabet = alphabet;
        this.mqs = mqs;
        this.ceqs = ceqs;

        this.suffixes = new ArrayList<>(initialSuffixes);
        this.shortPrefixes = new HashSet<>();
        this.rows = new HashMap<>();
    }

    abstract int maxSearchIndex(int ceLength);

    abstract void automatonFromTable();

    abstract boolean symbolInconsistency(Word<I> u1, Word<I> u2, I a);

    protected abstract List<D> rowForState(Word<I> input);

    @Override
    public void startLearning() {
        initTable();
        learnLoop();
    }

    @Override
    public boolean refineHypothesis(DefaultQuery<I, D> counterexample) {
        final Deque<DefaultQuery<I, D>> witnesses = new ArrayDeque<>();
        witnesses.add(counterexample);
        boolean refined = false;

        while (!witnesses.isEmpty()) {
            final DefaultQuery<I, D> witness = witnesses.getFirst();

            if (witness.getOutput() == null) {
                witness.answer(ceqs.answerQuery(witness.getPrefix(), witness.getSuffix()));
            }

            final boolean valid = MQUtil.isCounterexample(witness, getHypothesisModel());

            if (valid) {
                analyzeCounterexample(witness, witnesses);
                learnLoop();
                refined = true;
            } else {
                witnesses.pop();
            }
        }

        assert size() == shortPrefixes.size();
        return refined;
    }

    private void initTable() {
        Word<I> epsilon = Word.epsilon();
        List<D> rowData = initRow(epsilon);
        rows.put(epsilon, rowData);
        addShortPrefix(epsilon);
    }

    private void analyzeCounterexample(DefaultQuery<I, D> counterexample, Deque<DefaultQuery<I, D>> witnesses) {
        M hyp = getHypothesisModel();
        Word<I> ceInput = counterexample.getInput();
        Word<I> ua = null;
        int upper = maxSearchIndex(ceInput.length());
        int lower = 0;
        while (upper - lower > 1) {
            int mid = (upper + lower) / 2;

            Word<I> prefix = ceInput.prefix(mid);
            Word<I> suffix = ceInput.suffix(ceInput.length() - mid);
            List<D> rowData = rowForState(prefix);
            boolean stillCe = false;
            for (Word<I> u : getShortPrefixes(rowData)) {
                D sysOut = ceqs.answerQuery(u, suffix);
                D hypOut = hyp.computeSuffixOutput(u, suffix);
                if (!Objects.equals(sysOut, hypOut)) {
                    ua = u.append(suffix.firstSymbol());
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
            assert upper == 1;
            ua = ceInput.prefix(1);
        }

        // add witnesses
        int mid = (upper + lower) / 2;
        Word<I> sprime = ceInput.suffix(ceInput.length() - (mid + 1));
        List<D> rnext = getRow(ua);
        for (Word<I> uprime : getShortPrefixes(rnext)) {
            witnesses.push(new DefaultQuery<>(uprime, sprime));
        }
        witnesses.push(new DefaultQuery<>(ua, sprime));

        addShortPrefix(ua);
    }

    private void learnLoop() {
        while (findInconsistency() || findUnclosedness()) {
            completeObservations();
        }
        automatonFromTable();
    }

    private boolean findInconsistency() {
        List<Word<I>> shortAsList = new ArrayList<>(shortPrefixes);
        for (int left = 0; left < shortAsList.size() - 1; left++) {
            for (int right = left + 1; right < shortAsList.size(); right++) {
                if (findInconsistency(shortAsList.get(left), shortAsList.get(right))) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean findInconsistency(Word<I> u1, Word<I> u2) {
        List<D> rowData1 = rows.get(u1);
        List<D> rowData2 = rows.get(u2);
        if (!Objects.equals(rowData1, rowData2)) {
            return false;
        }
        for (I a : alphabet) {
            rowData1 = rows.get(u1.append(a));
            rowData2 = rows.get(u2.append(a));
            assert rowData1 != null && rowData2 != null;
            if (!rowData1.equals(rowData2)) {
                for (int i = 0; i < rowData1.size(); i++) {
                    if (!Objects.equals(rowData1.get(i), rowData2.get(i))) {
                        Word<I> newSuffx = suffixes.get(i).prepend(a);
                        suffixes.add(newSuffx);
                        return true;
                    }
                }
            }
            if (symbolInconsistency(u1, u2, a)) {
                return true;
            }
        }
        return false;
    }

    private List<Word<I>> getShortPrefixes(Word<I> prefix) {
        List<D> rowData = rows.get(prefix);
        assert rowData != null;
        return getShortPrefixes(rowData);
    }

    protected List<Word<I>> getShortPrefixes(List<D> rowData) {
        List<Word<I>> shortReps = new ArrayList<>();
        for (Entry<Word<I>, List<D>> e : rows.entrySet()) {
            if (shortPrefixes.contains(e.getKey()) && rowData.equals(e.getValue())) {
                shortReps.add(e.getKey());
            }
        }
        return shortReps;
    }

    Collection<Word<I>> getShortPrefixes() {
        return shortPrefixes;
    }

    List<D> getRow(Word<I> key) {
        List<D> row = rows.get(key);
        assert row != null;
        return row;
    }

    void addSuffix(Word<I> suffix) {
        this.suffixes.add(suffix);
    }

    private boolean findUnclosedness() {
        for (Word<I> prefix : rows.keySet()) {
            List<Word<I>> shortReps = getShortPrefixes(prefix);
            if (shortReps.isEmpty()) {
                addShortPrefix(prefix);
                return true;
            }
        }
        return false;
    }

    private void completeObservations() {
        for (Entry<Word<I>, List<D>> e : rows.entrySet()) {
            List<D> rowData = completeRow(e.getKey(), e.getValue());
            e.setValue(rowData);
        }
    }

    private List<D> initRow(Word<I> prefix) {
        List<D> rowData = new ArrayList<>(suffixes.size());
        for (Word<I> suffix : suffixes) {
            rowData.add(mqs.answerQuery(prefix, suffix));

        }
        return rowData;
    }

    private List<D> completeRow(Word<I> prefix, List<D> oldData) {
        if (suffixes.size() == oldData.size()) {
            return oldData;
        }

        List<D> rowData = new ArrayList<>(suffixes.size());
        rowData.addAll(oldData);
        for (int i = oldData.size(); i < suffixes.size(); i++) {
            rowData.add(mqs.answerQuery(prefix, suffixes.get(i)));
        }
        return rowData;
    }

    private void addShortPrefix(Word<I> shortPrefix) {
        assert !shortPrefixes.contains(shortPrefix) && rows.containsKey(shortPrefix);

        shortPrefixes.add(shortPrefix);
        for (I a : alphabet) {
            Word<I> newPrefix = shortPrefix.append(a);
            List<D> rowData = initRow(newPrefix);
            rows.put(newPrefix, rowData);
        }
    }

    @Override
    public void addAlphabetSymbol(I symbol) {
        if (!this.alphabet.containsSymbol(symbol)) {
            this.alphabet.asGrowingAlphabetOrThrowException().addSymbol(symbol);
        }

        if (!this.rows.isEmpty()) {
            for (Word<I> as : new ArrayList<>(this.rows.keySet())) {
                if (this.shortPrefixes.contains(as)) {
                    Word<I> lp = as.append(symbol);
                    this.rows.put(lp, initRow(lp));
                }
            }

            learnLoop();
        }
    }

    @Override
    public LLambdaState<I, D> suspend() {
        return new LLambdaState<>(shortPrefixes, rows, suffixes);
    }

    @Override
    public void resume(LLambdaState<I, D> state) {
        this.shortPrefixes.clear();
        this.rows.clear();
        this.suffixes.clear();

        this.shortPrefixes.addAll(state.getShortPrefixes());
        this.rows.putAll(state.getRows());
        this.suffixes.addAll(state.getSuffixes());
        automatonFromTable();
    }
}
