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
import net.automatalib.automata.DeterministicAutomaton;
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
public interface LassoOracle<L extends Lasso<I, D>, I, D> extends AutomatonOracle<L, I, D> {

    /**
     * Processes the given omega query.
     *
     * @param query the omega query to process.
     *
     * @return the processed omega query.
     */
    OmegaQuery<I, D> processOmegaQuery(OmegaQuery<I, D> query);

    /**
     * Processes the given input word. The default implementation will check if the processed query actually loops.
     *
     * @see AutomatonOracle#processInput(DeterministicAutomaton, Word)
     *
     * @param lassoHypothesis
     *          the hypothesis lasso.
     * @param input
     *          the input to process.
     */
    @Nullable
    @Override
    default DefaultQuery<I, D> processInput(L lassoHypothesis, Word<I> input) {
        final Word<I> prefix = lassoHypothesis.getPrefix();
        final Word<I> loop = lassoHypothesis.getLoop();

        assert prefix.isPrefixOf(input);
        assert loop.isSuffixOf(input);

        int repeat = (input.length() - prefix.length()) / loop.length();

        final OmegaQuery<I, D> omegaQuery = processOmegaQuery(new OmegaQuery<>(prefix, loop, repeat));

        return omegaQuery.isUltimatelyPeriodic() ? omegaQuery.asDefaultQuery() : null;
    }

    /**
     * The default implementation accepts an input if it loops at least once and if it makes exactly a loop.
     * This behavior is fine for a {@link DFALasso}, because they are assumed to be prefix-closed.
     *
     * @see AutomatonOracle#accepts(DeterministicAutomaton, Iterable, int)
     *
     * @param hypothesis
     *          the hypothesis automaton.
     * @param input
     *          the input.
     * @param length
     *          the length of the input.
     *
     * @return whether the given lasso accepts the given input.
     */
    @Override
    default boolean accepts(L hypothesis, Iterable<? extends I> input, int length) {
        return length >= hypothesis.getPrefix().length() + hypothesis.getLoop().length()
                && (length - hypothesis.getPrefix().length()) % hypothesis.getLoop().length() == 0;
    }

    @Nullable
    @Override
    default DefaultQuery<I, D> findCounterExample(L hypothesis, Collection<? extends I> inputs) {
       final int maxQueries = hypothesis.getUnfolds();

       return findCounterExample(hypothesis, inputs, maxQueries);
    }

    interface DFALassoOracle<I> extends LassoOracle<DFALasso<I>, I, Boolean> {}

    interface MealyLassoOracle<I, O> extends LassoOracle<MealyLasso<I, O>, I, Word<O>> {}

}
