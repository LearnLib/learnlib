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
package de.learnlib.algorithms.oml.lstar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import de.learnlib.api.algorithm.LearningAlgorithm;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.automata.concepts.InputAlphabetHolder;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

/**
 * @author fhowar
 */
abstract class AbstractOptimalLStar<M, I, D>
        implements LearningAlgorithm<M, I, D>, Hypothesis<I, D>, InputAlphabetHolder<I> {

    private final Alphabet<I> alphabet;
    final MembershipOracle<I, D> mqs;
    final MembershipOracle<I, D> ceqs;

    private final Set<Word<I>> shortPrefixes;
    private final Map<Word<I>, List<D>> rows;
    final List<Word<I>> suffixes;

    AbstractOptimalLStar(Alphabet<I> alphabet,
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

    abstract D suffix(D output, int length);

    abstract boolean symbolInconsistency(Word<I> u1, Word<I> u2, I a);

    @Override
    public void startLearning() {
        initTable();
        learnLoop();
    }

    @Override
    public boolean refineHypothesis(DefaultQuery<I, D> counterexample) {
        Set<DefaultQuery<I, D>> witnesses = new LinkedHashSet<>();
        witnesses.add(counterexample);
        boolean refined = refineWithWitness(counterexample, witnesses);
        if (!refined) {
            return false;
        }
        do {
            for (DefaultQuery<I, D> w : witnesses) {
                refined = refineWithWitness(w, witnesses);
                if (refined) {
                    break;
                }
            }

        } while (refined);
        assert size() == shortPrefixes.size();
        return true;
    }

    @Override
    public Alphabet<I> getInputAlphabet() {
        return alphabet;
    }

    private boolean refineWithWitness(DefaultQuery<I, D> counterexample, Set<DefaultQuery<I, D>> witnesses) {
        boolean valid = false;
        while (counterExampleValid(counterexample)) {
            valid = true;
            analyzeCounterexample(counterexample, witnesses);
            learnLoop();
        }

        return valid;
    }

    private void initTable() {
        Word<I> epsilon = Word.epsilon();
        List<D> rowData = initRow(epsilon);
        rows.put(epsilon, rowData);
        addShortPrefix(epsilon);
    }

    private void analyzeCounterexample(DefaultQuery<I, D> counterexample, Set<DefaultQuery<I, D>> witnesses) {
        Word<I> ceInput = counterexample.getInput();
        Word<I> ua = null;
        int upper = maxSearchIndex(ceInput.length());
        int lower = 0;
        D hypOut = getOutput(ceInput, ceInput.length());
        while (upper - lower > 1) {
            int mid = (upper + lower) / 2;

            Word<I> prefix = ceInput.prefix(mid);
            Word<I> suffix = ceInput.suffix(ceInput.length() - mid);
            List<D> rowData = rowForState(prefix);
            boolean stillCe = false;
            for (Word<I> u : getShortPrefixes(rowData)) {
                D sysOut = suffix(ceqs.answerQuery(u, suffix), suffix.length());
                if (!Objects.equals(sysOut, suffix(hypOut, suffix.size()))) {
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
            witnesses.add(new DefaultQuery<>(uprime, sprime, ceqs.answerQuery(uprime, sprime)));
        }
        witnesses.add(new DefaultQuery<>(ua, sprime, ceqs.answerQuery(ua, sprime)));

        addShortPrefix(ua);
    }

    private boolean counterExampleValid(DefaultQuery<I, D> counterexample) {
        assert !counterexample.getSuffix().isEmpty();
        D hypOut = getOutput(counterexample.getInput(), counterexample.getSuffix().length());
        return !Objects.equals(hypOut, counterexample.getOutput());
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
        if (!rowData1.equals(rowData2)) {
            return false;
        }
        for (I a : alphabet) {
            rowData1 = rows.get(u1.append(a));
            rowData2 = rows.get(u2.append(a));
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
        return getShortPrefixes(rowData);
    }

    List<Word<I>> getShortPrefixes(List<D> rowData) {
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
        return rows.get(key);
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
            rowData.add(suffix(mqs.answerQuery(prefix, suffix), suffix.size()));

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
            rowData.add(suffix(mqs.answerQuery(prefix, suffixes.get(i)), suffixes.get(i).size()));
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
}
