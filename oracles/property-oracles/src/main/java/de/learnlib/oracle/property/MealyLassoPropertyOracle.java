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
package de.learnlib.oracle.property;

import java.util.Collection;

import de.learnlib.api.oracle.InclusionOracle;
import de.learnlib.api.oracle.LassoEmptinessOracle;
import de.learnlib.api.oracle.PropertyOracle;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.modelchecking.Lasso;
import net.automatalib.modelchecking.ModelCheckerLasso;
import net.automatalib.words.Word;

/**
 * A property oracle for Mealy machines that can check lassos from the model checker.
 *
 * @author Jeroen Meijer
 *
 * @param <I> the input type
 * @param <O> the output type
 * @param <P> the property type
 */
public class MealyLassoPropertyOracle<I, O, P>
        extends AbstractPropertyOracle<I, MealyMachine<?, I, ?, O>, P, Word<O>, Lasso.MealyLasso<I, O>>
        implements PropertyOracle.MealyPropertyOracle<I, O, P> {

    private final ModelCheckerLasso.MealyModelCheckerLasso<I, O, P> modelChecker;

    public MealyLassoPropertyOracle(P property,
                                    InclusionOracle.MealyInclusionOracle<I, O> inclusionOracle,
                                    LassoEmptinessOracle.MealyLassoEmptinessOracle<I, O> emptinessOracle,
                                    ModelCheckerLasso.MealyModelCheckerLasso<I, O, P> modelChecker) {
        super(property, inclusionOracle, emptinessOracle);
        this.modelChecker = modelChecker;
    }

    @Override
    protected Lasso.MealyLasso<I, O> modelCheck(MealyMachine<?, I, ?, O> hypothesis,
                                                          Collection<? extends I> inputs) {
        return modelChecker.findCounterExample(hypothesis, inputs, getProperty());
    }
}
