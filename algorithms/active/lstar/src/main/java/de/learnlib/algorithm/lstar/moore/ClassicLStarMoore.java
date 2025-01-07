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

import java.util.List;

import de.learnlib.algorithm.lstar.AbstractExtensibleAutomatonLStar;
import de.learnlib.algorithm.lstar.ce.ObservationTableCEXHandler;
import de.learnlib.algorithm.lstar.closing.ClosingStrategy;
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
 * A {@link MooreMachine}-based specialization of the classic L* learner.
 *
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbol type
 */
public class ClassicLStarMoore<I, O>
        extends AbstractExtensibleAutomatonLStar<MooreMachine<?, I, ?, O>, I, O, Integer, Integer, O, Void, CompactMoore<I, O>> {

    @GenerateBuilder(defaults = AbstractExtensibleAutomatonLStar.BuilderDefaults.class)
    public ClassicLStarMoore(Alphabet<I> alphabet,
                             MembershipOracle<I, O> oracle,
                             List<Word<I>> initialPrefixes,
                             List<Word<I>> initialSuffixes,
                             ObservationTableCEXHandler<? super I, ? super O> cexHandler,
                             ClosingStrategy<? super I, ? super O> closingStrategy) {
        super(alphabet,
              oracle,
              new CompactMoore<>(alphabet),
              initialPrefixes,
              LStarMooreUtil.ensureSuffixCompliancy(initialSuffixes),
              cexHandler,
              closingStrategy);
    }

    @Override
    public MooreMachine<?, I, Integer, O> getHypothesisModel() {
        return internalHyp;
    }

    @Override
    protected O stateProperty(ObservationTable<I, O> table, Row<I> stateRow) {
        return table.cellContents(stateRow, 0);
    }

    @Override
    protected Void transitionProperty(ObservationTable<I, O> table, Row<I> stateRow, int inputIdx) {
        return null;
    }

    @Override
    protected SuffixOutput<I, O> hypothesisOutput() {
        return (prefix, suffix) -> {
            final Word<O> wordOut = internalHyp.computeSuffixOutput(prefix, suffix);
            return wordOut.lastSymbol();
        };
    }
}
