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
package de.learnlib.api.modelchecking.modelchecker;

import java.util.Collection;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import de.learnlib.api.exception.ModelCheckingException;
import de.learnlib.api.modelchecking.counterexample.Lasso;
import de.learnlib.api.modelchecking.counterexample.Lasso.DFALasso;
import de.learnlib.api.modelchecking.counterexample.Lasso.MealyLasso;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.transout.MealyMachine;

/**
 * A ModelChecker checks whether a given hypothesis satisfies a given property. If the property can not be satisfied
 * it provides a counterexample.
 *
 * @author Jeroen Meijer
 *
 * @param <I> the input type
 * @param <A> the automaton type
 * @param <P> the property type
 * @param <R> the type of a counterexample
 */
@ParametersAreNonnullByDefault
public interface ModelChecker<I, A, P, R> {

    /**
     * Try to find a counterexample for the given {@code property} and {@code hypothesis}.
     *
     * @param hypothesis the automaton to check the property on.
     * @param inputs the alphabet.
     * @param property the property.
     *
     * @return the counterexample, or {@code null} if a counterexample does not exist.
     *
     * @throws ModelCheckingException when a model checker can not check the property.
     */
    @Nullable
    R findCounterExample(A hypothesis, Collection<? extends I> inputs, P property) throws ModelCheckingException;

    interface DFAModelChecker<I, P, R extends DFA<?, I>> extends ModelChecker<I, DFA<?, I>, P, R> {}

    interface MealyModelChecker<I, O, P, R extends MealyMachine<?, I, ?, O>>
            extends ModelChecker<I, MealyMachine<?, I, ?, O>, P, R> {}

    /**
     * A model checker where the counterexample is a lasso.
     *
     * @param <I> the input type.
     * @param <A> the automaton type.
     * @param <P> the property type.
     * @param <R> the type of lasso.
     */
    interface ModelCheckerLasso<I, A, P, R extends Lasso<?, ? extends A, I, ?>> extends ModelChecker<I, A, P, R> {

        /**
         * Return the multiplier for the number of times a loop of the lasso must be unrolled, relative to the size
         * of the hypothesis.
         *
         * @return the multiplier
         */
        double getMultiplier();

        /**
         * Set the multiplier for the number of times a loop of the lasso must be unrolled, relative to the size of
         * the hypothesis.
         *
         * @param multiplier the multiplier
         *
         * @throws IllegalArgumentException when {@code multiplier < 0.0}.
         */
        void setMultiplier(double multiplier) throws IllegalArgumentException;

        /**
         * Returns the minimum number of times a loop must be unrolled.
         *
         * @return the minimum
         */
        int getMinimumUnfolds();

        /**
         * Set the minimum number of times a loop must be unrolled.
         *
         * @param minimumUnfolds the minimum
         *
         * @throws IllegalArgumentException when {@code minimumUnfolds < 1}.
         */
        void setMinimumUnfolds(int minimumUnfolds) throws IllegalArgumentException;
    }

    interface DFAModelCheckerLasso<I, P>
            extends ModelCheckerLasso<I, DFA<?, I>, P, DFALasso<?, I>>, DFAModelChecker<I, P, DFALasso<?, I>> {}

    interface MealyModelCheckerLasso<I, O, P> extends
            ModelCheckerLasso<I, MealyMachine<?, I, ?, O>, P, MealyLasso<?, I, O>>,
            MealyModelChecker<I, O, P, MealyLasso<?, I, O>> {}
}
