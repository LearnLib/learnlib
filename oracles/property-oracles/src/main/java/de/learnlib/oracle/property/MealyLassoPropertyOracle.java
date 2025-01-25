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
package de.learnlib.oracle.property;

import java.util.Collection;

import de.learnlib.oracle.LassoEmptinessOracle.MealyLassoEmptinessOracle;
import de.learnlib.oracle.PropertyOracle.MealyPropertyOracle;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.modelchecking.Lasso.MealyLasso;
import net.automatalib.modelchecking.ModelCheckerLasso.MealyModelCheckerLasso;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A property oracle for Mealy machines that can check lassos from the model checker.
 *
 * @param <I>
 *         the input type
 * @param <O>
 *         the output type
 * @param <P>
 *         the property type
 */
public class MealyLassoPropertyOracle<I, O, P>
        extends AbstractPropertyOracle<I, MealyMachine<?, I, ?, O>, P, Word<O>, MealyLasso<I, O>>
        implements MealyPropertyOracle<I, O, P> {

    private final MealyModelCheckerLasso<I, O, P> modelChecker;

    public MealyLassoPropertyOracle(P property,
                                    MealyInclusionOracle<I, O> inclusionOracle,
                                    MealyLassoEmptinessOracle<I, O> emptinessOracle,
                                    MealyModelCheckerLasso<I, O, P> modelChecker) {
        super(property, inclusionOracle, emptinessOracle);
        this.modelChecker = modelChecker;
    }

    @Override
    protected @Nullable MealyLasso<I, O> modelCheck(MealyMachine<?, I, ?, O> hypothesis, Collection<? extends I> inputs) {
        return modelChecker.findCounterExample(hypothesis, inputs, getProperty());
    }
}
