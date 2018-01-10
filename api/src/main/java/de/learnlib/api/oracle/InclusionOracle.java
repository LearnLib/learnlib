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

import javax.annotation.ParametersAreNonnullByDefault;

import de.learnlib.api.query.DefaultQuery;
import de.learnlib.api.query.Query;
import net.automatalib.automata.concepts.Output;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.ts.simple.SimpleDTS;
import net.automatalib.words.Word;

/**
 * Decides whether the language of a given hypothesis is included in some other language (e.g. from a
 * {@link de.learnlib.api.SUL}. If the whole language is not included, it provides a counterexample, such that is
 * a word in the given hypothesis, and not in the other language.
 *
 * @author Jeroen Meijer
 *
 * @param <A> the automaton type
 * @param <I> the input type
 * @param <D> the output type
 * @param <Q> the DefaultQuery type
 */
@ParametersAreNonnullByDefault
public interface InclusionOracle<A extends Output<I, D> & SimpleDTS<?, I>, I, D, Q extends DefaultQuery<I, D>>
        extends AutomatonOracle<A, I, D, Q> {

    /**
     * Returns whether the given (answered) {@code query} indicates the word is not in the language of the given {@code
     * hypothesis}.
     *
     * @see AutomatonOracle#isCounterExample(SimpleDTS, Query)
     */
    default boolean isCounterExample(A hypothesis, Q query) {
        return !query.getOutput().equals(hypothesis.computeOutput(query.getInput()));
    }

    interface DFAInclusionOracle<I>
            extends InclusionOracle<DFA<?, I>, I, Boolean, DefaultQuery<I, Boolean>>, DFADefaultOracle<I> {}

    interface MealyInclusionOracle<I, O> extends
            InclusionOracle<MealyMachine<?, I, ?, O>, I, Word<O>, DefaultQuery<I, Word<O>>>, MealyDefaultOracle<I, O> {}
}
