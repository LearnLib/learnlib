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
package de.learnlib.algorithms.lstar.mealy;

import java.util.Collections;
import java.util.List;

import com.github.misberner.buildergen.annotations.GenerateBuilder;
import de.learnlib.algorithms.lstar.AbstractExtensibleAutomatonLStar;
import de.learnlib.algorithms.lstar.ce.ObservationTableCEXHandler;
import de.learnlib.algorithms.lstar.closing.ClosingStrategy;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.datastructure.observationtable.ObservationTable;
import de.learnlib.datastructure.observationtable.Row;
import de.learnlib.util.mealy.MealyUtil;
import net.automatalib.automata.base.compact.CompactTransition;
import net.automatalib.automata.concepts.SuffixOutput;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * An implementation of the L*Mealy algorithm for inferring Mealy machines, as described by Oliver Niese in his Ph.D.
 * thesis.
 *
 * @param <I>
 *         input symbol class
 * @param <O>
 *         output symbol class
 *
 * @author Malte Isberner
 */
public class ClassicLStarMealy<I, O>
        extends AbstractExtensibleAutomatonLStar<MealyMachine<?, I, ?, O>, I, @Nullable O, Integer, CompactTransition<O>, Void, O, CompactMealy<I, O>> {

    /**
     * Constructor.
     *
     * @param alphabet
     *         the learning alphabet
     * @param oracle
     *         the (Mealy) oracle
     */
    public ClassicLStarMealy(Alphabet<I> alphabet,
                             MembershipOracle<I, @Nullable O> oracle,
                             ObservationTableCEXHandler<? super I, ? super O> cexHandler,
                             ClosingStrategy<? super I, ? super O> closingStrategy) {
        this(alphabet,
             oracle,
             Collections.singletonList(Word.epsilon()),
             Collections.emptyList(),
             cexHandler,
             closingStrategy);
    }

    @GenerateBuilder(defaults = AbstractExtensibleAutomatonLStar.BuilderDefaults.class)
    public ClassicLStarMealy(Alphabet<I> alphabet,
                             MembershipOracle<I, @Nullable O> oracle,
                             List<Word<I>> initialPrefixes,
                             List<Word<I>> initialSuffixes,
                             ObservationTableCEXHandler<? super I, ? super O> cexHandler,
                             ClosingStrategy<? super I, ? super O> closingStrategy) {
        super(alphabet,
              oracle,
              new CompactMealy<>(alphabet),
              initialPrefixes,
              LStarMealyUtil.ensureSuffixCompliancy(initialSuffixes, alphabet, true),
              cexHandler,
              closingStrategy);
    }

    public static <I, O> ClassicLStarMealy<I, O> createForSymbolOracle(Alphabet<I> alphabet,
                                                                       MembershipOracle<I, @Nullable O> oracle,
                                                                       ObservationTableCEXHandler<I, O> cexHandler,
                                                                       ClosingStrategy<? super I, ? super O> closingStrategy) {
        return new ClassicLStarMealy<>(alphabet, oracle, cexHandler, closingStrategy);
    }

    public static <I, O> ClassicLStarMealy<I, O> createForWordOracle(Alphabet<I> alphabet,
                                                                     MembershipOracle<I, Word<O>> oracle,
                                                                     ObservationTableCEXHandler<? super I, ? super O> cexHandler,
                                                                     ClosingStrategy<? super I, ? super O> closingStrategy) {
        return new ClassicLStarMealy<>(alphabet, MealyUtil.wrapWordOracle(oracle), cexHandler, closingStrategy);
    }

    @Override
    protected MealyMachine<?, I, ?, O> exposeInternalHypothesis() {
        return internalHyp;
    }

    @Override
    protected Void stateProperty(ObservationTable<I, @Nullable O> table, Row<I> stateRow) {
        return null;
    }

    @Override
    protected O transitionProperty(ObservationTable<I, @Nullable O> table, Row<I> stateRow, int inputIdx) {
        return table.cellContents(stateRow, inputIdx);
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
