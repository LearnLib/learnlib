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
 * A {@link de.learnlib.api.oracle.BlackBoxOracle} that first tries to disprove all properties before finding a
 * counterexample to the current hypothesis.
 *
 * One may favor this implementation if refining a hypothesis is expensive compared to trying to disprove properties.
 *
 * @author Jeroen Meijer
 *
 * @param <A> the automaton type.
 * @param <I> the input type.
 * @param <D> the output type.
 * @param <P> the {@link BlackBoxProperty} type.
 */
public class DisproveFirstBBOracle<A, I, D, P extends BlackBoxOracle.BlackBoxProperty<?, A, I, D>>
        extends AbstractBlackBoxOracle<A, I, D, P> {

    public DisproveFirstBBOracle(Set<P> properties) {
        super(properties);
    }

    public DisproveFirstBBOracle(P property) {
        super(property);
    }

    public DisproveFirstBBOracle() {
        super();
    }

    /**
     * Find a counterexample to the current hypothesis according to the strategy described at
     * {@link DisproveFirstBBOracle}.
     *
     * @see de.learnlib.api.oracle.BlackBoxOracle#findCounterExample(Object, Collection)
     */
    @Nullable
    @Override
    public DefaultQuery<I, D> findCounterExample(A hypothesis, Collection<? extends I> inputs) throws ModelCheckingException {
        for (P p : getProperties()) {
            if (!p.isDisproved()) {
                p.disprove(hypothesis, inputs);
            }
        }

        DefaultQuery<I, D> ce = null;
        final List<P> properties = new ArrayList<>(getProperties());
        for (int i = 0; i < properties.size() && ce == null; i++) {
            final P p = properties.get(i);
            if (!p.isDisproved()) {
                ce = p.findCounterExample(hypothesis, inputs);
                p.clearCache();
            }
        }

        return ce;
    }

    public static class DisproveFirstDFABBOracle<I>
            extends DisproveFirstBBOracle<DFA<?, I>, I, Boolean, DFABlackBoxProperty<?, I>>
            implements DFABlackBoxOracle<I> {

        public DisproveFirstDFABBOracle(Set<DFABlackBoxProperty<?, I>> properties) {
            super(properties);
        }

        public DisproveFirstDFABBOracle(DFABlackBoxProperty<?, I> property) {
            super(property);
        }

        public DisproveFirstDFABBOracle() {
            super();
        }
    }

    public static class DisproveFirstMealyBBOracle<I, O>
            extends DisproveFirstBBOracle<MealyMachine<?, I, ?, O>, I, Word<O>, MealyBlackBoxProperty<?, I, O>>
            implements MealyBlackBoxOracle<I, O> {

        public DisproveFirstMealyBBOracle(Set<MealyBlackBoxProperty<?, I, O>> properties) {
            super(properties);
        }

        public DisproveFirstMealyBBOracle(MealyBlackBoxProperty<?, I, O> property) {
            super(property);
        }

        public DisproveFirstMealyBBOracle() {
            super();
        }
    }
}
