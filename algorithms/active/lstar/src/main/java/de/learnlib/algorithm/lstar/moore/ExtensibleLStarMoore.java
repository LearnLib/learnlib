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
package de.learnlib.algorithm.lstar.moore;

import java.util.Collections;
import java.util.List;

import de.learnlib.algorithm.lstar.AbstractExtensibleAutomatonLStar;
import de.learnlib.algorithm.lstar.ce.ObservationTableCEXHandler;
import de.learnlib.algorithm.lstar.closing.ClosingStrategy;
import de.learnlib.datastructure.observationtable.OTLearner.OTLearnerMoore;
import de.learnlib.datastructure.observationtable.ObservationTable;
import de.learnlib.datastructure.observationtable.Row;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.tooling.annotation.builder.GenerateBuilder;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.concept.SuffixOutput;
import net.automatalib.automaton.transducer.MooreMachine;
import net.automatalib.automaton.transducer.impl.CompactMoore;
import net.automatalib.word.Word;

/**
 * A {@link MooreMachine}-based specialization of the extensible L* learner.
 *
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbol type
 */
public class ExtensibleLStarMoore<I, O>
        extends AbstractExtensibleAutomatonLStar<MooreMachine<?, I, ?, O>, I, Word<O>, Integer, Integer, O, Void, CompactMoore<I, O>>
        implements OTLearnerMoore<I, O> {

    public ExtensibleLStarMoore(Alphabet<I> alphabet,
                                MembershipOracle<I, Word<O>> oracle,
                                List<Word<I>> initialSuffixes,
                                ObservationTableCEXHandler<? super I, ? super Word<O>> cexHandler,
                                ClosingStrategy<? super I, ? super Word<O>> closingStrategy) {
        this(alphabet, oracle, Collections.singletonList(Word.epsilon()), initialSuffixes, cexHandler, closingStrategy);
    }

    @GenerateBuilder(defaults = AbstractExtensibleAutomatonLStar.BuilderDefaults.class)
    public ExtensibleLStarMoore(Alphabet<I> alphabet,
                                MembershipOracle<I, Word<O>> oracle,
                                List<Word<I>> initialPrefixes,
                                List<Word<I>> initialSuffixes,
                                ObservationTableCEXHandler<? super I, ? super Word<O>> cexHandler,
                                ClosingStrategy<? super I, ? super Word<O>> closingStrategy) {
        super(alphabet,
              oracle,
              new CompactMoore<>(alphabet),
              initialPrefixes,
              LStarMooreUtil.ensureSuffixCompliancy(initialSuffixes),
              cexHandler,
              closingStrategy);
    }

    @Override
    public MooreMachine<?, I, ?, O> getHypothesisModel() {
        return internalHyp;
    }

    @Override
    protected O stateProperty(ObservationTable<I, Word<O>> table, Row<I> stateRow) {
        Word<O> word = table.cellContents(stateRow, 0);
        return word.getSymbol(0);
    }

    @Override
    protected Void transitionProperty(ObservationTable<I, Word<O>> table, Row<I> stateRow, int inputIdx) {
        return null;
    }

    @Override
    protected SuffixOutput<I, Word<O>> hypothesisOutput() {
        return internalHyp;
    }
}
