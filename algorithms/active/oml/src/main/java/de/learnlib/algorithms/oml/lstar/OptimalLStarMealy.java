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
import java.util.List;
import java.util.Map;

import de.learnlib.api.algorithm.LearningAlgorithm.MealyLearner;
import de.learnlib.api.oracle.MembershipOracle;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

/**
 * @author fhowar
 */
public class OptimalLStarMealy<I, O> extends AbstractOptimalLStar<MealyMachine<?, I, ?, O>, I, Word<O>>
        implements MealyLearner<I, O> {

    private CompactMealy<I, O> hypothesis;
    private MutableMapping<Integer, List<Word<O>>> hypStateMap;

    public OptimalLStarMealy(Alphabet<I> alphabet, MembershipOracle<I, Word<O>> mqo) {
        this(alphabet, mqo, mqo);
    }

    public OptimalLStarMealy(Alphabet<I> alphabet,
                             MembershipOracle<I, Word<O>> mqs,
                             MembershipOracle<I, Word<O>> ceqs) {
        super(alphabet, mqs, ceqs, initialSuffixes(alphabet));
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
    int maxSearchIndex(int ceLength) {
        return ceLength - 1;
    }

    @Override
    void automatonFromTable() {
        Alphabet<I> alphabet = getInputAlphabet();

        this.hypothesis = new CompactMealy<>(alphabet);
        Map<List<Word<O>>, Integer> stateMap = new HashMap<>();
        List<Word<O>> rowData = getRow(Word.epsilon());
        Integer q = this.hypothesis.addInitialState();
        stateMap.put(rowData, q);

        for (Word<I> u : getShortPrefixes()) {
            rowData = getRow(u);
            if (stateMap.containsKey(rowData)) {
                continue;
            }
            q = this.hypothesis.addState();
            stateMap.put(rowData, q);
        }

        hypStateMap = this.hypothesis.createStaticStateMapping();

        for (Map.Entry<List<Word<O>>, Integer> e : stateMap.entrySet()) {
            List<Word<O>> sig = e.getKey();
            Integer state = e.getValue();

            hypStateMap.put(state, sig);
            Word<I> u = getShortPrefixes(sig).get(0);
            List<Word<O>> srcData = getRow(u);
            for (I a : alphabet) {
                List<Word<O>> destData = getRow(u.append(a));
                assert destData != null;
                Integer dst = stateMap.get(destData);
                O o = srcData.get(alphabet.getSymbolIndex(a)).lastSymbol();
                this.hypothesis.setTransition(state, a, dst, o);
            }
        }
    }

    @Override
    Word<O> suffix(Word<O> output, int length) {
        return output.suffix(length);
    }

    private static <I> List<Word<I>> initialSuffixes(Alphabet<I> alphabet) {
        List<Word<I>> suffixes = new ArrayList<>(alphabet.size());
        for (I a : alphabet) {
            suffixes.add(Word.fromLetter(a));
        }
        return suffixes;
    }

}
