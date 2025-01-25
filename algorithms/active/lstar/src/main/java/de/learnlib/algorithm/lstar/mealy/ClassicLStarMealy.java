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
package de.learnlib.algorithm.lstar.mealy;

import java.util.Collections;
import java.util.List;

import de.learnlib.algorithm.lstar.AbstractExtensibleAutomatonLStar;
import de.learnlib.algorithm.lstar.ce.ObservationTableCEXHandler;
import de.learnlib.algorithm.lstar.ce.ObservationTableCEXHandlers;
import de.learnlib.algorithm.lstar.closing.ClosingStrategies;
import de.learnlib.algorithm.lstar.closing.ClosingStrategy;
import de.learnlib.datastructure.observationtable.ObservationTable;
import de.learnlib.datastructure.observationtable.Row;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.tooling.annotation.builder.GenerateBuilder;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.concept.SuffixOutput;
import net.automatalib.automaton.impl.CompactTransition;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.automaton.transducer.impl.CompactMealy;
import net.automatalib.word.Word;

/**
 * An implementation of the L*Mealy algorithm for inferring Mealy machines, as described by Oliver Niese in his Ph.D.
 * thesis.
 *
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbol type
 */
public class ClassicLStarMealy<I, O>
        extends AbstractExtensibleAutomatonLStar<MealyMachine<?, I, ?, O>, I, O, Integer, CompactTransition<O>, Void, O, CompactMealy<I, O>> {

    private final O emptyOutput;

    /**
     * Constructor.
     *
     * @param alphabet
     *         the learning alphabet
     * @param oracle
     *         the (Mealy) oracle
     */
    public ClassicLStarMealy(Alphabet<I> alphabet, MembershipOracle<I, O> oracle) {
        this(alphabet, oracle, ObservationTableCEXHandlers.CLASSIC_LSTAR, ClosingStrategies.CLOSE_FIRST);
    }

    /**
     * Constructor.
     *
     * @param alphabet
     *         the learning alphabet
     * @param oracle
     *         the (Mealy) oracle
     * @param cexHandler
     *         the counterexample handler
     * @param closingStrategy
     *         the closing strategy
     */
    public ClassicLStarMealy(Alphabet<I> alphabet,
                             MembershipOracle<I, O> oracle,
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
                             MembershipOracle<I, O> oracle,
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
        this.emptyOutput = oracle.answerQuery(Word.epsilon());
    }

    @Override
    public MealyMachine<?, I, ?, O> getHypothesisModel() {
        return internalHyp;
    }

    @Override
    protected Void stateProperty(ObservationTable<I, O> table, Row<I> stateRow) {
        return null;
    }

    @Override
    protected O transitionProperty(ObservationTable<I, O> table, Row<I> stateRow, int inputIdx) {
        return table.cellContents(stateRow, inputIdx);
    }

    @Override
    public void addAlphabetSymbol(I symbol) {
        /*
         * This implementation extracts the transition outputs from the observation table. Therefore, it assumes that
         * the i-th input symbol is the i-th suffix of the observation table. When adding new input symbols (and
         * therefore new suffixes) this mapping may be broken because of other suffixes that have been added in previous
         * refinement steps.
         *
         * Until this mapping is fixed, the code cannot reliably add new input symbols. Instead of running into issues
         * way into the learning process, fail-fast here.
         */
        throw new UnsupportedOperationException(
                "This implementation does not correct support adding new alphabet symbols. " +
                "Use the ExtensibleLStarMealy implementation with the classic counterexample handler instead.");
    }

    @Override
    protected SuffixOutput<I, O> hypothesisOutput() {
        return (prefix, suffix) -> {
            final Word<O> wordOut = internalHyp.computeSuffixOutput(prefix, suffix);
            return wordOut.isEmpty() ? emptyOutput : wordOut.lastSymbol();
        };
    }
}
