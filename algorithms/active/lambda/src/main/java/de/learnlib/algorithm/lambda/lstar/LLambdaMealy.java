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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.learnlib.algorithm.LearningAlgorithm.MealyLearner;
import de.learnlib.oracle.MembershipOracle;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.automaton.transducer.impl.CompactMealy;
import net.automatalib.common.util.mapping.MutableMapping;
import net.automatalib.word.Word;

public class LLambdaMealy<I, O> extends AbstractLLambda<MealyMachine<?, I, ?, O>, I, Word<O>>
        implements MealyLearner<I, O> {

    private CompactMealy<I, O> hypothesis;
    private MutableMapping<Integer, List<Word<O>>> hypStateMap;

    public LLambdaMealy(Alphabet<I> alphabet, MembershipOracle<I, Word<O>> mqo) {
        this(alphabet, mqo, mqo);
    }

    public LLambdaMealy(Alphabet<I> alphabet, MembershipOracle<I, Word<O>> mqs, MembershipOracle<I, Word<O>> ceqs) {
        super(alphabet, mqs, ceqs, Collections.emptyList());
    }

    @Override
    public int size() {
        return hypothesis.size();
    }

    @Override
    protected List<Word<O>> rowForState(Word<I> input) {
        return hypStateMap.get(hypothesis.getState(input));
    }

    @Override
    public MealyMachine<?, I, ?, O> getHypothesisModel() {
        return hypothesis;
    }

    @Override
    int maxSearchIndex(int ceLength) {
        return ceLength;
    }

    @Override
    boolean symbolInconsistency(Word<I> u1, Word<I> u2, I a) {
        final Word<I> suff = Word.fromLetter(a);
        final O o1 = mqs.answerQuery(u1, suff).lastSymbol();
        final O o2 = mqs.answerQuery(u2, suff).lastSymbol();
        if (!Objects.equals(o1, o2)) {
            addSuffix(suff);
            return true;
        }
        return false;
    }

    @Override
    void automatonFromTable() {
        this.hypothesis = new CompactMealy<>(super.alphabet);
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
            for (I a : super.alphabet) {
                Word<I> ua = u.append(a);
                List<Word<O>> destData = getRow(ua);
                assert destData != null;
                Integer dst = stateMap.get(destData);
                O o = mqs.answerQuery(ua).lastSymbol();
                this.hypothesis.setTransition(state, a, dst, o);
            }
        }
    }

    @Override
    public void addAlphabetSymbol(I symbol) {
        if (this.hypothesis != null) {
            this.hypothesis.addAlphabetSymbol(symbol);
        }
        super.addAlphabetSymbol(symbol);
    }

}
