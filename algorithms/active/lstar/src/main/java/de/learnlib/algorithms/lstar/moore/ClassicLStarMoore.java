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
package de.learnlib.algorithms.lstar.moore;

import java.util.Collections;
import java.util.List;

import com.github.misberner.buildergen.annotations.GenerateBuilder;
import de.learnlib.algorithms.lstar.AbstractExtensibleAutomatonLStar;
import de.learnlib.algorithms.lstar.ce.ObservationTableCEXHandler;
import de.learnlib.algorithms.lstar.closing.ClosingStrategy;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.datastructure.observationtable.ObservationTable;
import de.learnlib.datastructure.observationtable.Row;
import net.automatalib.automata.concepts.SuffixOutput;
import net.automatalib.automata.transducers.MooreMachine;
import net.automatalib.automata.transducers.impl.compact.CompactMoore;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A {@link MooreMachine}-based specialization of the classic L* learner.
 *
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbol type
 *
 * @author bayram
 * @author frohme
 */
public class ClassicLStarMoore<I, O>
        extends AbstractExtensibleAutomatonLStar<MooreMachine<?, I, ?, O>, I, @Nullable O, Integer, Integer, O, Void, CompactMoore<I, O>> {

    @GenerateBuilder(defaults = AbstractExtensibleAutomatonLStar.BuilderDefaults.class)
    public ClassicLStarMoore(Alphabet<I> alphabet,
                             MembershipOracle<I, @Nullable O> oracle,
                             List<Word<I>> initialPrefixes,
                             List<Word<I>> initialSuffixes,
                             ObservationTableCEXHandler<? super I, ? super @Nullable O> cexHandler,
                             ClosingStrategy<? super I, ? super @Nullable O> closingStrategy) {
        super(alphabet,
              oracle,
              new CompactMoore<>(alphabet),
              initialPrefixes,
              LStarMooreUtil.ensureSuffixCompliancy(initialSuffixes),
              cexHandler,
              closingStrategy);
    }

    @Override
    protected MooreMachine<?, I, Integer, O> exposeInternalHypothesis() {
        return internalHyp;
    }

    @Override
    protected O stateProperty(ObservationTable<I, @Nullable O> table, Row<I> stateRow) {
        return table.cellContents(stateRow, 0);
    }

    @Override
    protected Void transitionProperty(ObservationTable<I, @Nullable O> table, Row<I> stateRow, int inputIdx) {
        return null;
    }

    @Override
    protected SuffixOutput<I, @Nullable O> hypothesisOutput() {

        return new SuffixOutput<I, @Nullable O>() {

            @Override
            public @Nullable O computeOutput(Iterable<? extends I> input) {
                return computeSuffixOutput(Collections.emptyList(), input);
            }

            @Override
            public @Nullable O computeSuffixOutput(Iterable<? extends I> prefix, Iterable<? extends I> suffix) {
                Word<O> wordOut = internalHyp.computeSuffixOutput(prefix, suffix);
                if (wordOut.isEmpty()) {
                    return null;
                }
                return wordOut.lastSymbol();
            }
        };
    }
}