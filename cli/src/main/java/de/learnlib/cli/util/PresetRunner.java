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
import de.learnlib.filter.statistic.oracle.CounterOracle;
import de.learnlib.oracle.EquivalenceOracle;
import de.learnlib.oracle.MembershipOracle;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.concept.FiniteRepresentation;
import net.automatalib.serialization.InputModelSerializer;
import net.automatalib.ts.simple.SimpleTS;

public class PresetRunner<A extends Alphabet<I>, M extends SimpleTS<?, I> & FiniteRepresentation, I, D>
        extends AbstractRunner<A, M, I, D, MembershipOracle<I, D>> {

    public PresetRunner(Function<Options, A> alphabetCreator,
                        BiFunction<Options, ? super A, MembershipOracle<I, D>> mqoCreator,
                        Function<Options, Constructor<A, M, I, D, MembershipOracle<I, D>>> learnerCreator,
                        BiFunction<Options, MembershipOracle<I, D>, EquivalenceOracle<M, I, D>> eqoCreator,
                        Function<Options, InputModelSerializer<I, M>> serializerCreator) {
        super(alphabetCreator, mqoCreator, learnerCreator, eqoCreator, serializerCreator);
    }

    @Override
    protected MembershipOracle<I, D> getCounter(MembershipOracle<I, D> delegate, String id) {
        return new CounterOracle<>(delegate, id);
    }
}
