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
package de.learnlib.api.oracle;

import java.util.Collection;
import java.util.Objects;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import de.learnlib.api.SUL;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.automata.concepts.Output;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.words.Word;

/**
 * Decides whether the intersection of the language of a given hypothesis and some other language (e.g. from a {@link
 * SUL} is empty. If the intersection is not empty it provides a counterexample, such that it is a word in the
 * intersection. More precisely an emptiness oracle decides whether L(H) ∩ L(SUL) = ∅.
 *
 * @param <A>
 *         the automaton type
 * @param <I>
 *         the input type
 * @param <D>
 *         the output type
 *
 * @author Jeroen Meijer
 */
@ParametersAreNonnullByDefault
public interface EmptinessOracle<A extends Output<I, D>, I, D> {

    default boolean isCounterExample(Output<I, D> hypothesis, Iterable<? extends I> input, @Nullable D output) {
        return Objects.equals(hypothesis.computeOutput(input), output);
    }

    @Nullable
    DefaultQuery<I, D> findCounterExample(A hypothesis, Collection<? extends I> inputs);

    interface DFAEmptinessOracle<I> extends EmptinessOracle<DFA<?, I>, I, Boolean> {}

    interface MealyEmptinessOracle<I, O> extends EmptinessOracle<MealyMachine<?, I, ?, O>, I, Word<O>> {}
}
