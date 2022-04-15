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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.learnlib.api.algorithm.LearningAlgorithm.DFALearner;
import de.learnlib.api.oracle.MembershipOracle;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

/**
 * @author fhowar
 */
public class OptimalLStarDFA<I> extends AbstractOptimalLStar<DFA<?, I>, I, Boolean> implements DFALearner<I> {

    private CompactDFA<I> hypothesis;
    private MutableMapping<Integer, List<Boolean>> hypStateMap;

    public OptimalLStarDFA(Alphabet<I> sigma, MembershipOracle<I, Boolean> mqo) {
        this(sigma, mqo, mqo);
    }

    public OptimalLStarDFA(Alphabet<I> sigma, MembershipOracle<I, Boolean> mqs, MembershipOracle<I, Boolean> ceqs) {
        super(sigma, mqs, ceqs, Collections.singletonList(Word.epsilon()));
    }

    @Override
    int maxSearchIndex(int ceLength) {
        return ceLength;
    }

    @Override
    boolean symbolInconsistency(Word<I> u1, Word<I> u2, I a) {
        return false;
    }

    @Override
    void automatonFromTable() {
        Alphabet<I> alphabet = getInputAlphabet();

        this.hypothesis = new CompactDFA<>(alphabet);
        Map<List<Boolean>, Integer> stateMap = new HashMap<>();
        List<Boolean> rowData = getRow(Word.epsilon());
        Integer q = this.hypothesis.addInitialState(rowData.get(0));
        stateMap.put(rowData, q);

        for (Word<I> u : getShortPrefixes()) {
            rowData = getRow(u);
            if (stateMap.containsKey(rowData)) {
                continue;
            }
            q = this.hypothesis.addState(rowData.get(0));
            stateMap.put(rowData, q);
        }

        hypStateMap = this.hypothesis.createStaticStateMapping();

        for (Map.Entry<List<Boolean>, Integer> e : stateMap.entrySet()) {
            List<Boolean> sig = e.getKey();
            Integer state = e.getValue();

            hypStateMap.put(state, sig);
            Word<I> u = getShortPrefixes(sig).get(0);
            for (I a : alphabet) {
                List<Boolean> dstData = getRow(u.append(a));
                assert dstData != null;
                Integer dst = stateMap.get(dstData);
                this.hypothesis.setTransition(state, a, dst);
            }
        }
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
