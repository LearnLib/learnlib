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

import java.util.Objects;

import de.learnlib.sul.SUL;
import net.automatalib.automaton.concept.Output;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.word.Word;

/**
 * Decides whether the language of a given hypothesis is included in some other language (e.g., from a {@link SUL}). If
 * the whole language is not included, it provides a counterexample, such that it is a word in the given hypothesis, and
 * not in the other language.
 * <p>
 * Note that from the perspective of a learner an inclusion oracle is also an equivalence oracle, but a poor one, i.e.
 * an inclusion oracle only implements L(H) ⊆ L(SUL), not L(H) ⊇ L(SUL).
 *
 * @param <A>
 *         the automaton type
 * @param <I>
 *         the input type
 * @param <D>
 *         the output type
 */
public interface InclusionOracle<A extends Output<I, D>, I, D> extends EquivalenceOracle<A, I, D> {

    default boolean isCounterExample(Output<I, D> hypothesis, Iterable<? extends I> input, D output) {
        return !Objects.equals(hypothesis.computeOutput(input), output);
    }

    interface DFAInclusionOracle<I> extends InclusionOracle<DFA<?, I>, I, Boolean>, DFAEquivalenceOracle<I> {}

    interface MealyInclusionOracle<I, O>
            extends InclusionOracle<MealyMachine<?, I, ?, O>, I, Word<O>>, MealyEquivalenceOracle<I, O> {}
}
