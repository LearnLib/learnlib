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
package de.learnlib.oracle.blackbox;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import de.learnlib.api.exception.ModelCheckingException;
import de.learnlib.api.oracle.BlackBoxOracle;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.words.Word;

/**
 * A {@link de.learnlib.api.oracle.BlackBoxOracle} that first tries to disprove a property, but before trying to
 * disprove the next property, first try to find a counterexample to the current hypothesis.
 *
 * This implementation may be used when refining a hypothesis is inexpensive compared to disproving properties.
 *
 * This oracle will use a cache on each {@link BlackBoxProperty}.
 *
 * @author Jeroen Meijer
 *
 * @see DisproveFirstBBOracle
 *
 * @param <A> the automaton type.
 * @param <I> the input type.
 * @param <D> the output type.
 * @param <P> the {@link BlackBoxProperty} type.
 */
public class CExFirstBBOracle<A, I, D, P extends BlackBoxOracle.BlackBoxProperty<?, A, I, D>>
        extends AbstractBlackBoxOracle<A, I, D, P> {

    public CExFirstBBOracle(Set<P> properties) {
        super(properties);
    }

    public CExFirstBBOracle(P property) {
        super(property);
    }

    public CExFirstBBOracle() {
        super();
    }

    /**
     * Find a counterexample to the given hypothesis according to strategy described at {@link CExFirstBBOracle}.
     *
     * @see de.learnlib.api.oracle.BlackBoxOracle#findCounterExample(Object, Collection)
     */
    @Nullable
    @Override
    public DefaultQuery<I, D> findCounterExample(A hypothesis, Collection<? extends I> inputs) throws ModelCheckingException {
        DefaultQuery<I, D> result = null;
        final List<P> properties = new ArrayList<>(getProperties());
        for (int i = 0; i < properties.size() && result == null; i++) {
            final P p = properties.get(i);
            if (!p.isDisproved()) {
                final DefaultQuery<I, D> ce = p.disprove(hypothesis, inputs);
                if (ce == null) {
                    result = p.findCounterExample(hypothesis, inputs);
                }
                p.clearCache();
            }
        }

        return result;
    }

    public static class CExFirstDFABBOracle<I>
            extends CExFirstBBOracle<DFA<?, I>, I, Boolean, DFABlackBoxProperty<?, I>>
            implements DFABlackBoxOracle<I> {

        public CExFirstDFABBOracle(Set<DFABlackBoxProperty<?, I>> properties) {
            super(properties);
        }

        public CExFirstDFABBOracle(DFABlackBoxProperty<?, I> property) {
            super(property);
        }

        public CExFirstDFABBOracle() {
            super();
        }
    }

    public static class CExFirstMealyBBOracle<I, O>
            extends CExFirstBBOracle<MealyMachine<?, I, ?, O>, I, Word<O>, MealyBlackBoxProperty<?, I, O>>
            implements MealyBlackBoxOracle<I, O> {

        public CExFirstMealyBBOracle(Set<MealyBlackBoxProperty<?, I, O>> properties) {
            super(properties);
        }

        public CExFirstMealyBBOracle(MealyBlackBoxProperty<?, I, O> property) {
            super(property);
        }

        public CExFirstMealyBBOracle() {
            super();
        }
    }
}
