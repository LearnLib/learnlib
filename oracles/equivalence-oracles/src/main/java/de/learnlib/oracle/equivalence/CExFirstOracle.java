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
package de.learnlib.oracle.equivalence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import de.learnlib.api.oracle.BlackBoxOracle;
import de.learnlib.api.oracle.PropertyOracle;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.automata.concepts.Output;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.words.Word;

/**
 * The strategy of this black-box oracle is to first try out a property, to see if it can be disproved. If it can not be
 * disproved it tries the same property to find a counter example to the hypothesis, before continuing with the next
 * property.
 * <p>
 * This implementation may be used when refining a hypothesis is inexpensive compared to disproving propertyOracles.
 *
 * @author Jeroen Meijer
 *
 * @see DisproveFirstOracle
 *
 * @param <A> the automaton type
 * @param <I> the input type
 * @param <D> the output type
 */
public class CExFirstOracle<A extends Output<I, D>, I, D> implements BlackBoxOracle<A, I, D> {

    private final List<PropertyOracle<I, ? super A, ?, D>> propertyOracles;

    public CExFirstOracle() {
        this(Collections.emptySet());
    }

    public CExFirstOracle(PropertyOracle<I, A, ?, D> propertyOracle) {
        this(Collections.singleton(propertyOracle));
    }

    public CExFirstOracle(Collection<? extends PropertyOracle<I, ? super A, ?, D>> propertyOracles) {
        this.propertyOracles = new ArrayList<>(propertyOracles);
    }

    @Override
    public List<PropertyOracle<I, ? super A, ?, D>> getPropertyOracles() {
        return propertyOracles;
    }

    @Nullable
    @Override
    public DefaultQuery<I, D> findCounterExample(A hypothesis, Collection<? extends I> inputs) {
        for (PropertyOracle<I, ? super A, ?, D> propertyOracle : propertyOracles) {
            final DefaultQuery<I, D> result = propertyOracle.findCounterExample(hypothesis, inputs);
            if (result != null) {
                assert isCounterExample(hypothesis, result.getInput(), result.getOutput());
                return result;
            }
        }

        return null;
    }

    public static class DFACExFirstOracle<I> extends CExFirstOracle<DFA<?, I>, I, Boolean>
            implements DFABlackBoxOracle<I> {

        public DFACExFirstOracle() {
            super();
        }

        public DFACExFirstOracle(PropertyOracle<I, DFA<?, I>, ?, Boolean> propertyOracle) {
            super(propertyOracle);
        }

        public DFACExFirstOracle(Collection<? extends PropertyOracle<I, DFA<?, I>, ?, Boolean>> propertyOracles) {
            super(propertyOracles);
        }
    }

    public static class MealyCExFirstOracle<I, O> extends CExFirstOracle<MealyMachine<?, I, ?, O>, I, Word<O>>
            implements MealyBlackBoxOracle<I, O> {

        public MealyCExFirstOracle() {
            super();
        }

        public MealyCExFirstOracle(PropertyOracle<I, MealyMachine<?, I, ?, O>, ?, Word<O>> propertyOracle) {
            super(propertyOracle);
        }

        public MealyCExFirstOracle(Collection<? extends PropertyOracle<I, MealyMachine<?, I, ?, O>, ?, Word<O>>> propertyOracles) {
            super(propertyOracles);
        }
    }
}
