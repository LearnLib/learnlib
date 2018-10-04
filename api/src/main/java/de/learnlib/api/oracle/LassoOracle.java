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

import javax.annotation.Nullable;

import de.learnlib.api.query.DefaultQuery;
import de.learnlib.api.query.OmegaQuery;
import net.automatalib.automata.concepts.Output;
import net.automatalib.modelchecking.Lasso;
import net.automatalib.modelchecking.Lasso.DFALasso;
import net.automatalib.modelchecking.Lasso.MealyLasso;
import net.automatalib.words.Word;

/**
 * An automaton oracle for lassos.
 *
 * @see AutomatonOracle
 *
 * @param <L> the type of Lasso.
 * @param <I> the type of input.
 * @param <D> the type of output.
 *
 * @author Jeroen Meijer
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
    boolean isCounterExample(Output<I, D> hypothesis, Iterable<? extends I> inputs, @Nullable D output);

    /**
     * Returns whether a lasso that is ultimately periodic could serve as a counter example.
     *
     * @param isUltimatelyPeriodic
     *          whether the lasso is ultimately periodic
     *
     * @return true when lasso that is ultimately periodic could serve as a counter example, false otherwise.
     */
    boolean isOmegaCounterExample(boolean isUltimatelyPeriodic);

    @Nullable
    default DefaultQuery<I, D> findCounterExample(L hypothesis, Collection<? extends I> inputs) {
        final Word<I> prefix = hypothesis.getPrefix();
        final Word<I> loop = hypothesis.getLoop();
        final int repeat = hypothesis.getUnfolds();

        final OmegaQuery<I, D> omegaQuery = processInput(prefix, loop, repeat);

        final DefaultQuery<I, D> query;
        if (isOmegaCounterExample(omegaQuery.isUltimatelyPeriodic())) {
            final DefaultQuery<I, D> ce = omegaQuery.asDefaultQuery();
            query = isCounterExample(hypothesis.getAutomaton(), ce.getInput(), ce.getOutput()) ? ce : null;
        } else {
            query = null;
        }

        return query;
    }

    interface DFALassoOracle<I> extends LassoOracle<DFALasso<I>, I, Boolean> {}

    interface MealyLassoOracle<I, O> extends LassoOracle<MealyLasso<I, O>, I, Word<O>> {}
}
