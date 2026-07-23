/* Copyright (C) 2013-2026 TU Dortmund University
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
package de.learnlib.cli.util;

import java.util.function.BiFunction;
import java.util.function.Function;

import de.learnlib.cli.option.Options;
import de.learnlib.filter.statistic.oracle.CounterAdaptiveQueryOracle;
import de.learnlib.oracle.AdaptiveMembershipOracle;
import de.learnlib.oracle.EquivalenceOracle;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.concept.FiniteRepresentation;
import net.automatalib.serialization.InputModelSerializer;
import net.automatalib.ts.simple.SimpleTS;
import net.automatalib.word.Word;

public class AdaptiveRunner<A extends Alphabet<I>, M extends SimpleTS<?, I> & FiniteRepresentation, I, O>
        extends AbstractRunner<A, M, I, Word<O>, AdaptiveMembershipOracle<I, O>> {

    public AdaptiveRunner(Function<Options, A> alphabetCreator,
                          BiFunction<Options, ? super A, AdaptiveMembershipOracle<I, O>> mqoCreator,
                          Function<Options, Constructor<A, M, I, Word<O>, AdaptiveMembershipOracle<I, O>>> learnerCreator,
                          BiFunction<Options, AdaptiveMembershipOracle<I, O>, EquivalenceOracle<M, I, Word<O>>> eqoCreator,
                          Function<Options, InputModelSerializer<I, M>> serializerCreator) {
        super(alphabetCreator, mqoCreator, learnerCreator, eqoCreator, serializerCreator);
    }

    @Override
    protected AdaptiveMembershipOracle<I, O> getCounter(AdaptiveMembershipOracle<I, O> delegate, String id) {
        return new CounterAdaptiveQueryOracle<>(delegate, id);
    }
}
