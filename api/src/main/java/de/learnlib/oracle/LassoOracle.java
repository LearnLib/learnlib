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

import de.learnlib.query.DefaultQuery;
import de.learnlib.query.OmegaQuery;
import net.automatalib.automaton.concept.Output;
import net.automatalib.modelchecking.Lasso;
import net.automatalib.modelchecking.Lasso.DFALasso;
import net.automatalib.modelchecking.Lasso.MealyLasso;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * An automaton oracle for lassos.
 *
 * @see AutomatonOracle
 *
 * @param <L> the type of Lasso.
 * @param <I> the type of input.
 * @param <D> the type of output.
 */
public interface LassoOracle<L extends Lasso<I, D>, I, D> {

    /**
     * Processes the given omega query.
     *
     * @param prefix
     *          the prefix
     * @param loop
     *          the loop
     * @param repeat
     *          the maximum number of times the loop may be repeated
     *
     * @return the omega query.
     */
    OmegaQuery<I, D> processInput(Word<I> prefix, Word<I> loop, int repeat);

    /**
     * Returns whether the given input and output is a counter example for the given hypothesis.
     *
     * @param hypothesis
     *          the hypothesis
     * @param inputs
     *          the input sequence
     * @param output
     *          the output corresponding to the input.
     *
     * @return whether the given input and output is a counter example.
     */
    boolean isCounterExample(Output<I, D> hypothesis, Iterable<? extends I> inputs, D output);

    default @Nullable DefaultQuery<I, D> findCounterExample(L hypothesis, Collection<? extends I> inputs) {
        final Word<I> prefix = hypothesis.getPrefix();
        final Word<I> loop = hypothesis.getLoop();
        final int repeat = hypothesis.getUnfolds();

        final OmegaQuery<I, D> omegaQuery = processInput(prefix, loop, repeat);

        if (omegaQuery.isUltimatelyPeriodic()) {
            @SuppressWarnings("nullness") // when we are a counterexample, the output is valid
            final DefaultQuery<I, D> ce = omegaQuery.asDefaultQuery();

            if (isCounterExample(hypothesis.getAutomaton(), ce.getInput(), ce.getOutput())) {
                return ce;
            }
        }

        return null;
    }

    interface DFALassoOracle<I> extends LassoOracle<DFALasso<I>, I, Boolean> {}

    interface MealyLassoOracle<I, O> extends LassoOracle<MealyLasso<I, O>, I, Word<O>> {}
}
