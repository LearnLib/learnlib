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

import javax.annotation.ParametersAreNonnullByDefault;

import net.automatalib.automata.concepts.Output;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.words.Word;

/**
 * Decides whether some words that do not satisfy properties evaluated by {@link #getPropertyOracles()} on a given
 * hypothesis, are included in a language.
 * If there is such a word not included, it serves as a counter example for the given hypothesis.
 *
 * @author Jeroen Meijer
 *
 * @param <A> the automaton type
 * @param <I> the input type
 * @param <D> the output type
 */
@ParametersAreNonnullByDefault
public interface BlackBoxOracle<A extends Output<I, D>, I, D> extends InclusionOracle<A, I, D> {

    /**
     * Returns the property oracles that this black-box oracle uses to evaluate properties.
     *
     * @return the property oracles.
     */
    Collection<PropertyOracle<I, A, ?, D>> getPropertyOracles();

    interface DFABlackBoxOracle<I> extends BlackBoxOracle<DFA<?, I>, I, Boolean> {}

    interface MealyBlackBoxOracle<I, O> extends BlackBoxOracle<MealyMachine<?, I, ?, O>, I, Word<O>> {}
}
