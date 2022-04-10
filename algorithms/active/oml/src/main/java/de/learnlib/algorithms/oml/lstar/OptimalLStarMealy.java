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

import de.learnlib.api.algorithm.LearningAlgorithm.MealyLearner;
import de.learnlib.api.oracle.MembershipOracle;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.automata.transducers.impl.FastMealy;
import net.automatalib.automata.transducers.impl.FastMealyState;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

/**
 * @author fhowar
 */
public class OptimalLStarMealy<I, O> extends AbstractOptimalLStar<MealyMachine<?, I, ?, O>, I, Word<O>>
        implements MealyLearner<I, O> {

    private FastMealy<I, O> hypothesis;
    private final Map<FastMealyState<O>, List<Word<O>>> hypStateMap;

    public OptimalLStarMealy(Alphabet<I> sigma, MembershipOracle<I, Word<O>> mqo) {
        this(sigma, mqo, mqo);
    }

    public OptimalLStarMealy(Alphabet<I> sigma, MembershipOracle<I, Word<O>> mqs, MembershipOracle<I, Word<O>> ceqs) {
        super(sigma, mqs, ceqs);
        this.hypStateMap = new LinkedHashMap<>();
    }

    @Override
    public int size() {
        return hypothesis.size();
    }

    @Override
    public List<Word<O>> rowForState(Word<I> input) {
        return hypStateMap.get(hypothesis.getState(input));
    }

    @Override
    public Word<O> getOutput(Word<I> input, int length) {
        assert !input.isEmpty();
        return hypothesis.computeOutput(input).suffix(length);
    }

    @Override
    public MealyMachine<?, I, ?, O> getHypothesisModel() {
        return hypothesis;
    }

    @Override
    List<Word<I>> initSuffixes() {
        List<Word<I>> suffixes = new ArrayList<>(getAlphabet().size());
        for (I a : getAlphabet()) {
            suffixes.add(Word.fromLetter(a));
        }
        return suffixes;
    }

    @Override
    int maxSearchIndex(int ceLength) {
        return ceLength - 1;
    }

    @Override
    void automatonFromTable() {
        hypStateMap.clear();
        FastMealy<I, O> hyp = new FastMealy<>(getAlphabet());
        Map<List<Word<O>>, FastMealyState<O>> stateMap = new HashMap<>();
        List<Word<O>> rowData = getRow(Word.epsilon());
        FastMealyState<O> q = hyp.addInitialState();
        stateMap.put(rowData, q);
        hypStateMap.put(q, rowData);

        for (Word<I> u : getShortPrefixes()) {
            rowData = getRow(u);
            if (stateMap.containsKey(rowData)) {
                continue;
            }
            q = hyp.addState();
            stateMap.put(rowData, q);
            hypStateMap.put(q, rowData);
        }

        for (Map.Entry<FastMealyState<O>, List<Word<O>>> e : hypStateMap.entrySet()) {
            Word<I> u = getShortPrefixes(e.getValue()).get(0);
            List<Word<O>> srcData = getRow(u);
            for (I a : getAlphabet()) {
                List<Word<O>> destData = getRow(u.append(a));
                assert destData != null;
                FastMealyState<O> dst = stateMap.get(destData);
                O o = srcData.get(getAlphabet().getSymbolIndex(a)).lastSymbol();
                hyp.setTransition(e.getKey(), a, dst, o);
            }
        }
        this.hypothesis = hyp;
    }

    @Override
    Word<O> suffix(Word<O> output, int length) {
        return output.suffix(length);
    }

}
