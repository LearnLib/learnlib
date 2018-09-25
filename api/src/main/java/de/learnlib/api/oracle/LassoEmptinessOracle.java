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

import java.util.Objects;

import javax.annotation.Nullable;

import net.automatalib.modelchecking.Lasso;
import net.automatalib.words.Word;

/**
 * An emptiness oracle for lassos.
 *
 * @see EmptinessOracle
 *
 * @param <L> the lasso type
 * @param <I> the input type
 * @param <D> the output type
 *
 * @author Jeroen Meijer
 */
public interface LassoEmptinessOracle<L extends Lasso<I, D>, I, D> extends EmptinessOracle<L, I, D> {

    @Override
    default boolean isCounterExample(L hypothesis, Iterable<? extends I> inputs, @Nullable D output) {
        return Objects.equals(hypothesis.getAutomaton().computeOutput(inputs), output);
    }

    interface DFALassoEmptinessOracle<I> extends LassoEmptinessOracle<Lasso.DFALasso<I>, I, Boolean> {}

    interface MealyLassoEmptinessOracle<I, O> extends LassoEmptinessOracle<Lasso.MealyLasso<I, O>, I, Word<O>> {}
}
