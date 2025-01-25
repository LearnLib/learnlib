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
package de.learnlib.oracle;

import java.util.Collection;
import java.util.Objects;

import de.learnlib.query.DefaultQuery;
import de.learnlib.sul.SUL;
import net.automatalib.automaton.concept.Output;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Decides whether the intersection of the language of a given hypothesis and some other language (e.g., from a {@link
 * SUL}) is empty. If the intersection is not empty it provides a counterexample, such that it is a word in the
 * intersection. More precisely an emptiness oracle decides whether L(H) ∩ L(SUL) = ∅.
 *
 * @param <A>
 *         the automaton type
 * @param <I>
 *         the input type
 * @param <D>
 *         the output type
 */
public interface EmptinessOracle<A extends Output<I, D>, I, D> {

    default boolean isCounterExample(Output<I, D> hypothesis, Iterable<? extends I> input, D output) {
        return Objects.equals(hypothesis.computeOutput(input), output);
    }

    @Nullable DefaultQuery<I, D> findCounterExample(A hypothesis, Collection<? extends I> inputs);

    interface DFAEmptinessOracle<I> extends EmptinessOracle<DFA<?, I>, I, Boolean> {}

    interface MealyEmptinessOracle<I, O> extends EmptinessOracle<MealyMachine<?, I, ?, O>, I, Word<O>> {}
}
