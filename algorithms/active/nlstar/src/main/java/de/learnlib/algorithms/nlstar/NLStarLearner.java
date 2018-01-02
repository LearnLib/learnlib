/* Copyright (C) 2013-2018 TU Dortmund
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
package de.learnlib.algorithms.nlstar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.misberner.buildergen.annotations.GenerateBuilder;
import de.learnlib.api.algorithm.NFALearner;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.util.MQUtil;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.automata.fsa.impl.compact.CompactNFA;
import net.automatalib.util.automata.fsa.NFAs;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

/**
 * The NL* algorithm, as described in the paper <a href="http://ijcai.org/papers09/Papers/IJCAI09-170.pdf">"Angluin-Style
 * Learning of NFA"</a> by B. Bollig et al. (IJCAI'09).
 *
 * @param <I>
 *         input symbol type
 *
 * @author Malte Isberner
 */
public class NLStarLearner<I> implements NFALearner<I> {

    private final Alphabet<I> alphabet;
    private final ObservationTable<I> table;

    private CompactNFA<I> hypothesis;

    /**
     * Constructor.
     *
     * @param alphabet
     *         the input alphabet
     * @param oracle
     *         the membership oracle
     */
    @GenerateBuilder
    public NLStarLearner(Alphabet<I> alphabet, MembershipOracle<I, Boolean> oracle) {
        this.alphabet = alphabet;
        this.table = new ObservationTable<>(alphabet, oracle);
    }

    @Override
    public void startLearning() {
        if (hypothesis != null) {
            throw new IllegalStateException();
        }

        List<List<Row<I>>> unclosed = table.initialize();
        completeConsistentTable(unclosed);

        constructHypothesis();
    }

    /**
     * Retrieves a view of this learner as a DFA learner. The DFA is obtained by determinizing and minimizing the NFA
     * hypothesis.
     *
     * @return a DFA learner view of this learner
     *
     * @see #getDeterminizedHypothesis()
     */
    public DFALearner<I> asDFALearner() {
        return new DFALearner<I>() {

            @Override
            public String toString() {
                return NLStarLearner.this.toString();
            }

            @Override
            public void startLearning() {
                NLStarLearner.this.startLearning();
            }

            @Override
            public boolean refineHypothesis(DefaultQuery<I, Boolean> ceQuery) {
                return NLStarLearner.this.refineHypothesis(ceQuery);
            }

            @Override
            public CompactDFA<I> getHypothesisModel() {
                return NLStarLearner.this.getDeterminizedHypothesis();
            }

        };
    }

    /**
     * Retrieves a deterministic version of the hypothesis. The DFA is obtained through {@link
     * NFAs#determinize(net.automatalib.automata.fsa.NFA)}.
     *
     * @return a deterministic version of the hypothesis
     */
    public CompactDFA<I> getDeterminizedHypothesis() {
        if (hypothesis == null) {
            throw new IllegalStateException();
        }
        return NFAs.determinize(hypothesis);
    }

    private void completeConsistentTable(List<List<Row<I>>> initialUnclosed) {
        List<List<Row<I>>> unclosed = initialUnclosed;

        Inconsistency<I> incons;

        do {
            while (!unclosed.isEmpty()) {
                unclosed = fixUnclosed(unclosed);
            }

            incons = table.findInconsistency();
            if (incons != null) {
                unclosed = fixInconsistency(incons);
            }
        } while (!unclosed.isEmpty() || incons != null);
    }

    private List<List<Row<I>>> fixUnclosed(List<List<Row<I>>> unclosed) {
        List<Row<I>> newShort = new ArrayList<>(unclosed.size());

        for (List<Row<I>> unclosedClass : unclosed) {
            newShort.add(unclosedClass.get(0));
        }

        return table.makeUpper(newShort);
    }

    private List<List<Row<I>>> fixInconsistency(Inconsistency<I> incons) {
        I sym = alphabet.getSymbol(incons.getSymbolIdx());
        Word<I> oldSuffix = table.getSuffix(incons.getSuffixIdx());

        Word<I> newSuffix = oldSuffix.prepend(sym);

        return table.addSuffix(newSuffix);
    }

    @Override
    public boolean refineHypothesis(DefaultQuery<I, Boolean> ceQuery) {
        if (hypothesis == null) {
            throw new IllegalStateException();
        }

        boolean refined = false;
        while (MQUtil.isCounterexample(ceQuery, hypothesis)) {
            Word<I> ceWord = ceQuery.getInput();

            List<List<Row<I>>> unclosed = table.addSuffixes(ceWord.suffixes(false));
            completeConsistentTable(unclosed);
            constructHypothesis();

            refined = true;
        }
        return refined;
    }

    private void constructHypothesis() {
        hypothesis = new CompactNFA<>(alphabet);

        int[] stateMap = new int[table.getNumUpperRows()];
        Arrays.fill(stateMap, -1);

        List<Row<I>> upperPrimes = table.getUpperPrimes();

        for (Row<I> row : upperPrimes) {
            int state = hypothesis.addIntState(row.getContent(0));
            stateMap[row.getUpperId()] = state;
        }

        Row<I> firstRow = table.getUpperRow(0);

        if (firstRow.isPrime()) {
            int state = stateMap[firstRow.getUpperId()];
            hypothesis.setInitial(state, true);
        } else {
            for (Row<I> row : table.getCoveredRows(firstRow)) {
                if (row.isPrime()) {
                    int state = stateMap[row.getUpperId()];
                    hypothesis.setInitial(state, true);
                }
            }
        }

        // Transition relation
        for (Row<I> row : upperPrimes) {
            int state = stateMap[row.getUpperId()];

            for (int i = 0; i < alphabet.size(); i++) {
                Row<I> succRow = row.getSuccessorRow(i);

                if (succRow.isPrime()) {
                    int succState = stateMap[succRow.getUpperId()];
                    hypothesis.addTransition(state, i, succState);
                } else {
                    for (Row<I> r : succRow.getCoveredRows()) {
                        if (r.isPrime()) {
                            int succState = stateMap[r.getUpperId()];
                            hypothesis.addTransition(state, i, succState);
                        }
                    }
                }
            }
        }
    }

    @Override
    public CompactNFA<I> getHypothesisModel() {
        if (hypothesis == null) {
            throw new IllegalStateException();
        }
        return hypothesis;
    }

}
