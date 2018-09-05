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

import java.util.Collection;
import java.util.Collections;

import javax.annotation.Nullable;

import de.learnlib.api.oracle.BlackBoxOracle;
import de.learnlib.api.oracle.PropertyOracle;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.automata.concepts.Output;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.words.Word;

/**
 * The strategy of this black-box oracle is to first try to disprove all properties before finding a counter example
 * to the given hypothesis.
 * <p>
 * One may favor this implementation if refining a hypothesis is expensive compared to trying to disprove properties.
 *
 * @author Jeroen Meijer
 *
 * @see CExFirstOracle
 *
 * @param <A> the automaton type
 * @param <I> the input type
 * @param <D> the output type
 */
public class DisproveFirstOracle<A extends Output<I, D>, I, D> implements BlackBoxOracle<A, I, D> {

    private final Collection<PropertyOracle<I, A, ?, D>> propertyOracles;

    public DisproveFirstOracle() {
        this(Collections.emptySet());
    }

    public DisproveFirstOracle(PropertyOracle<I, A, ?, D> propertyOracle) {
        this(Collections.singleton(propertyOracle));
    }

    public DisproveFirstOracle(Collection<? extends PropertyOracle<I, A, ?, D>> propertyOracles) {
        this.propertyOracles = Collections.unmodifiableCollection(propertyOracles);
    }

    @Override
    public Collection<PropertyOracle<I, A, ?, D>> getPropertyOracles() {
        return propertyOracles;
    }

    @Nullable
    @Override
    public DefaultQuery<I, D> findCounterExample(A hypothesis, Collection<? extends I> inputs) {
        for (PropertyOracle<I, A, ?, D> po : propertyOracles) {
            if (!po.isDisproved()) {
                po.disprove(hypothesis, inputs);
            }
        }

        DefaultQuery<I, D> ce = null;
        for (PropertyOracle<I, A, ?, D> po : propertyOracles) {
            if (!po.isDisproved()) {
                ce = po.findCounterExample(hypothesis, inputs);
                if (ce != null) {
                    break;
                }
            }
        }

        assert ce == null || isCounterExample(hypothesis, ce.getInput(), ce.getOutput());

        return ce;
    }

    public static class DFADisproveFirstOracle<I> extends DisproveFirstOracle<DFA<?, I>, I, Boolean>
            implements DFABlackBoxOracle<I> {

        public DFADisproveFirstOracle() {
            super();
        }

        public DFADisproveFirstOracle(PropertyOracle<I, DFA<?, I>, ?, Boolean> propertyOracle) {
            super(propertyOracle);
        }

        public DFADisproveFirstOracle(Collection<? extends PropertyOracle<I, DFA<?, I>, ?, Boolean>> propertyOracles) {
            super(propertyOracles);
        }
    }

    public static class MealyDisproveFirstOracle<I, O> extends DisproveFirstOracle<MealyMachine<?, I, ?, O>, I, Word<O>>
            implements MealyBlackBoxOracle<I, O> {

        public MealyDisproveFirstOracle() {
            super();
        }

        public MealyDisproveFirstOracle(PropertyOracle<I, MealyMachine<?, I, ?, O>, ?, Word<O>> propertyOracle) {
            super(propertyOracle);
        }

        public MealyDisproveFirstOracle(Collection<? extends PropertyOracle<I, MealyMachine<?, I, ?, O>, ?, Word<O>>> propertyOracles) {
            super(propertyOracles);
        }
    }
}
