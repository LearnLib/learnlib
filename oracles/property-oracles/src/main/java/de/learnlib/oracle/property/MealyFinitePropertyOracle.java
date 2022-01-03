/* Copyright (C) 2013-2022 TU Dortmund
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

import de.learnlib.api.oracle.EmptinessOracle.MealyEmptinessOracle;
import de.learnlib.api.oracle.PropertyOracle.MealyPropertyOracle;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.modelchecking.ModelChecker.MealyModelChecker;
import net.automatalib.words.Word;

/**
 * A property oracle for Mealy Machines where it is fine to only check finite words from the model checker.
 *
 * @param <I>
 *         the input type
 * @param <O>
 *         the output type
 * @param <P>
 *         the property type
 *
 * @author Jeroen Meijer
 */
public class MealyFinitePropertyOracle<I, O, P>
        extends AbstractPropertyOracle<I, MealyMachine<?, I, ?, O>, P, Word<O>, MealyMachine<?, I, ?, O>>
        implements MealyPropertyOracle<I, O, P> {

    private final MealyModelChecker<I, O, P, MealyMachine<?, I, ?, O>> modelChecker;

    public MealyFinitePropertyOracle(P property,
                                     MealyInclusionOracle<I, O> inclusionOracle,
                                     MealyEmptinessOracle<I, O> emptinessOracle,
                                     MealyModelChecker<I, O, P, MealyMachine<?, I, ?, O>> modelChecker) {
        super(property, inclusionOracle, emptinessOracle);
        this.modelChecker = modelChecker;
    }

    @Override
    protected MealyMachine<?, I, ?, O> modelCheck(MealyMachine<?, I, ?, O> hypothesis, Collection<? extends I> inputs) {

        return modelChecker.findCounterExample(hypothesis, inputs, getProperty());
    }
}
