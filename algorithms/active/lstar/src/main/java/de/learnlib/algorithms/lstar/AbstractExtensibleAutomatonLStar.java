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
package de.learnlib.algorithms.lstar;

import java.util.Collections;
import java.util.List;

import de.learnlib.algorithms.lstar.ce.ObservationTableCEXHandler;
import de.learnlib.algorithms.lstar.ce.ObservationTableCEXHandlers;
import de.learnlib.algorithms.lstar.closing.ClosingStrategies;
import de.learnlib.algorithms.lstar.closing.ClosingStrategy;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.datastructure.observationtable.Row;
import net.automatalib.automata.GrowableAlphabetAutomaton;
import net.automatalib.automata.MutableDeterministic;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

public abstract class AbstractExtensibleAutomatonLStar<A, I, D, S, T, SP, TP, AI extends MutableDeterministic<S, I, T, SP, TP> & GrowableAlphabetAutomaton<I>>
        extends AbstractAutomatonLStar<A, I, D, S, T, SP, TP, AI> {

    protected final ObservationTableCEXHandler<? super I, ? super D> cexHandler;
    protected final ClosingStrategy<? super I, ? super D> closingStrategy;
    protected final List<Word<I>> initialPrefixes;
    protected final List<Word<I>> initialSuffixes;

    protected AbstractExtensibleAutomatonLStar(Alphabet<I> alphabet,
                                               MembershipOracle<I, D> oracle,
                                               AI internalHyp,
                                               List<Word<I>> initialPrefixes,
                                               List<Word<I>> initialSuffixes,
                                               ObservationTableCEXHandler<? super I, ? super D> cexHandler,
                                               ClosingStrategy<? super I, ? super D> closingStrategy) {
        super(alphabet, oracle, internalHyp);
        this.initialPrefixes = initialPrefixes;
        this.initialSuffixes = initialSuffixes;
        this.cexHandler = cexHandler;
        this.closingStrategy = closingStrategy;
    }

    @Override
    protected void refineHypothesisInternal(DefaultQuery<I, D> ceQuery) {
        List<List<Row<I>>> unclosed = cexHandler.handleCounterexample(ceQuery, table, hypothesisOutput(), oracle);
        completeConsistentTable(unclosed, cexHandler.needsConsistencyCheck());
    }

    @Override
    protected List<Word<I>> initialPrefixes() {
        return initialPrefixes;
    }

    @Override
    protected List<Word<I>> initialSuffixes() {
        return initialSuffixes;
    }

    @Override
    protected List<Row<I>> selectClosingRows(List<List<Row<I>>> unclosed) {
        return closingStrategy.selectClosingRows(unclosed, table, oracle);
    }

    public static final class BuilderDefaults {

        public static <I> List<Word<I>> initialPrefixes() {
            return Collections.singletonList(Word.<I>epsilon());
        }

        public static <I> List<Word<I>> initialSuffixes() {
            return Collections.emptyList();
        }

        public static <I, D> ObservationTableCEXHandler<? super I, ? super D> cexHandler() {
            return ObservationTableCEXHandlers.CLASSIC_LSTAR;
        }

        public static <I, D> ClosingStrategy<? super I, ? super D> closingStrategy() {
            return ClosingStrategies.CLOSE_FIRST;
        }

    }

}
