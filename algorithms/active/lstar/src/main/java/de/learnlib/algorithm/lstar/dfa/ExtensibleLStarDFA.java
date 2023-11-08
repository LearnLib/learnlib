/* Copyright (C) 2013-2023 TU Dortmund
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
package de.learnlib.algorithm.lstar.dfa;

import java.util.Collections;
import java.util.List;

import com.github.misberner.buildergen.annotations.GenerateBuilder;
import de.learnlib.algorithm.GlobalSuffixLearner.GlobalSuffixLearnerDFA;
import de.learnlib.algorithm.lstar.AbstractExtensibleAutomatonLStar;
import de.learnlib.algorithm.lstar.ce.ObservationTableCEXHandler;
import de.learnlib.algorithm.lstar.closing.ClosingStrategy;
import de.learnlib.datastructure.observationtable.OTLearner.OTLearnerDFA;
import de.learnlib.datastructure.observationtable.ObservationTable;
import de.learnlib.datastructure.observationtable.Row;
import de.learnlib.oracle.MembershipOracle;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.concept.SuffixOutput;
import net.automatalib.automaton.fsa.CompactDFA;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.word.Word;

/**
 * An implementation of Angluin's L* algorithm for learning DFAs, as described in the paper "Learning Regular Sets from
 * Queries and Counterexamples".
 *
 * @param <I>
 *         input symbol class.
 */
public class ExtensibleLStarDFA<I>
        extends AbstractExtensibleAutomatonLStar<DFA<?, I>, I, Boolean, Integer, Integer, Boolean, Void, CompactDFA<I>>
        implements OTLearnerDFA<I>, GlobalSuffixLearnerDFA<I> {

    /**
     * Constructor.
     *
     * @param alphabet
     *         the learning alphabet.
     * @param oracle
     *         the DFA oracle.
     */
    public ExtensibleLStarDFA(Alphabet<I> alphabet,
                              MembershipOracle<I, Boolean> oracle,
                              List<Word<I>> initialSuffixes,
                              ObservationTableCEXHandler<? super I, ? super Boolean> cexHandler,
                              ClosingStrategy<? super I, ? super Boolean> closingStrategy) {
        this(alphabet, oracle, Collections.singletonList(Word.epsilon()), initialSuffixes, cexHandler, closingStrategy);
    }

    @GenerateBuilder(defaults = AbstractExtensibleAutomatonLStar.BuilderDefaults.class)
    public ExtensibleLStarDFA(Alphabet<I> alphabet,
                              MembershipOracle<I, Boolean> oracle,
                              List<Word<I>> initialPrefixes,
                              List<Word<I>> initialSuffixes,
                              ObservationTableCEXHandler<? super I, ? super Boolean> cexHandler,
                              ClosingStrategy<? super I, ? super Boolean> closingStrategy) {
        super(alphabet,
              oracle,
              new CompactDFA<>(alphabet),
              initialPrefixes,
              LStarDFAUtil.ensureSuffixCompliancy(initialSuffixes),
              cexHandler,
              closingStrategy);
    }

    @Override
    public DFA<?, I> getHypothesisModel() {
        return internalHyp;
    }

    @Override
    protected Boolean stateProperty(ObservationTable<I, Boolean> table, Row<I> stateRow) {
        return table.cellContents(stateRow, 0);
    }

    @Override
    protected Void transitionProperty(ObservationTable<I, Boolean> table, Row<I> stateRow, int inputIdx) {
        return null;
    }

    @Override
    protected SuffixOutput<I, Boolean> hypothesisOutput() {
        return internalHyp;
    }

}
