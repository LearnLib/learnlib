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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.learnlib.api.algorithm.LearningAlgorithm.DFALearner;
import de.learnlib.api.oracle.MembershipOracle;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.FastDFA;
import net.automatalib.automata.fsa.impl.FastDFAState;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

/**
 * @author fhowar
 */
public class OptimalLStarDFA<I> extends AbstractOptimalLStar<DFA<?, I>, I, Boolean> implements DFALearner<I> {

    private FastDFA<I> hypothesis;
    private final Map<FastDFAState, List<Boolean>> hypStateMap;

    public OptimalLStarDFA(Alphabet<I> sigma, MembershipOracle<I, Boolean> mqo) {
        this(sigma, mqo, mqo);
    }

    public OptimalLStarDFA(Alphabet<I> sigma, MembershipOracle<I, Boolean> mqs, MembershipOracle<I, Boolean> ceqs) {
        super(sigma, mqs, ceqs);
        this.hypStateMap = new LinkedHashMap<>();
    }

    @Override
    List<Word<I>> initSuffixes() {
        final List<Word<I>> result = new ArrayList<>();
        result.add(Word.epsilon());
        return result;
    }

    @Override
    int maxSearchIndex(int ceLength) {
        return ceLength;
    }

    @Override
    void automatonFromTable() {
        hypStateMap.clear();
        FastDFA<I> hyp = new FastDFA<>(getAlphabet());
        Map<List<Boolean>, FastDFAState> stateMap = new HashMap<>();
        List<Boolean> rowData = getRow(Word.epsilon());
        FastDFAState q = hyp.addInitialState(rowData.get(0));
        stateMap.put(rowData, q);
        hypStateMap.put(q, rowData);

        for (Word<I> u : getShortPrefixes()) {
            rowData = getRow(u);
            if (stateMap.containsKey(rowData)) {
                continue;
            }
            q = hyp.addState(rowData.get(0));
            stateMap.put(rowData, q);
            hypStateMap.put(q, rowData);
        }

        for (Map.Entry<FastDFAState, List<Boolean>> e : hypStateMap.entrySet()) {
            Word<I> u = getShortPrefixes(e.getValue()).get(0);
            for (I a : getAlphabet()) {
                List<Boolean> destData = getRow(u.append(a));
                assert destData != null;
                FastDFAState dst = stateMap.get(destData);
                hyp.setTransition(e.getKey(), a, dst);
            }
        }
        this.hypothesis = hyp;
    }

    @Override
    Boolean suffix(Boolean output, int length) {
        return output;
    }

    @Override
    public int size() {
        return hypothesis.size();
    }

    @Override
    public List<Boolean> rowForState(Word<I> input) {
        return hypStateMap.get(hypothesis.getState(input));
    }

    @Override
    public Boolean getOutput(Word<I> input, int length) {
        return hypothesis.accepts(input);
    }

    @Override
    public DFA<?, I> getHypothesisModel() {
        return hypothesis;
    }
}
