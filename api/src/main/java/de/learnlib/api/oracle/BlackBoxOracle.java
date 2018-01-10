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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import de.learnlib.api.exception.ModelCheckingException;
import de.learnlib.api.oracle.BlackBoxOracle.BlackBoxProperty;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.words.Word;

/**
 * A wrapper around anything that involves black-box checking.
 *
 * The key component is a set of properties that may be disproved or used to find counterexamples to hypotheses.
 *
 * @author Jeroen Meijer
 *
 * @param <A> the automaton type
 * @param <I> the input type
 * @param <D> the output type
 * @param <P> the BlackBoxProperty type
 */
@ParametersAreNonnullByDefault
public interface BlackBoxOracle<A, I, D, P extends BlackBoxProperty<?, A, I, D>> {

    /**
     * Find a counterexample to the given {@code hypotheses} in a set of properties, and try to disprove all properties
     * with the given {@code hypothesis}.
     *
     * @param hypothesis the hypothesis to find a counterexample to
     * @param inputs the alphabet
     *
     * @return a counterexample, or {@code null} if a counterexample could not be found.
     *
     * @throws ModelCheckingException when a property can not be checked.
     */
    @Nullable
    DefaultQuery<I, D> findCounterExample(A hypothesis, Collection<? extends I> inputs) throws ModelCheckingException;

    /**
     * Retuns the set of {@link BlackBoxProperty}s.
     *
     * @return the set of {@link BlackBoxProperty}s.
     */
    Set<P> getProperties();

    /**
     * Returns the set of violated {@link BlackBoxProperty}s.
     *
     * @return the set of violated {@link BlackBoxProperty}s.
     */
    default Set<P> getViolatedProperties() {
        final Set<P> violated = new HashSet<>();
        for (P p : getProperties()) {
            if (p.isDisproved()) {
                violated.add(p);
            }
        }
        return Collections.unmodifiableSet(violated);
    }

    /**
     * Returns whether all {@link BlackBoxProperty} are violated.
     *
     * @return whether all {@link BlackBoxProperty} are violated.
     */
    default boolean allPropertiesViolated() {
        return getViolatedProperties().size() == getProperties().size();
    }

    interface DFABlackBoxOracle<I>
            extends BlackBoxOracle<DFA<?, I>, I, Boolean, DFABlackBoxProperty<?, I>> {}

    interface MealyBlackBoxOracle<I, O>
            extends BlackBoxOracle<MealyMachine<?, I, ?, O>, I, Word<O>, MealyBlackBoxProperty<?, I, O>> {}


    /**
     * A BlackBoxProperty can be disproved or used to find a counterexample to the property.
     *
     * An implementation should keep track of whether the property is already disproved.
     *
     * Furthermore, an implementation could implement a cache. A cache could for example cache a call to the model
     * checker in between calls to {@link #disprove(Object, Collection)}, or
     * {@link #findCounterExample(Object, Collection)}. The cache should be invalidated when
     * {@link #disprove(Object, Collection)}, or {@link #findCounterExample(Object, Collection)} is called with a
     * different alphabet. A client of this class is responsible for clearing the cache (with {@link #clearCache()},
     * before it calls {@link #disprove(Object, Collection)}, or {@link #findCounterExample(Object, Collection)},
     * with a different hypothesis than the previous call.
     *
     * @param <P> the property type
     * @param <A> the automaton type
     * @param <I> the input type
     * @param <D> the output type
     */
    @ParametersAreNonnullByDefault
    interface BlackBoxProperty<P, A, I, D> {

        /**
         * Returns whether this property is disproved.
         *
         * @return whether this property is disproved.
         */
        boolean isDisproved();

        /**
         * Set the property.
         *
         * @param property the property to set.
         */
        void setProperty(P property);

        /**
         * Returns the property.
         *
         * @return the property.
         */
        P getProperty();

        /**
         * Returns the counterexample for this property if {@link #isDisproved()}, {@code null} otherwise.
         *
         * If this method does not return {@code null}, a previous call to {@link #disprove(Object, Collection)} must
         * have returned a {@link DefaultQuery}.
         *
         * @return the counterexample for this property if {@link #isDisproved()}, {@code null} otherwise.
         */
        @Nullable
        DefaultQuery<I, D> getCounterExample();

        /**
         * Try to disprove this property with the given {@code hypothesis}.
         *
         * @param hypothesis the hypothesis.
         * @param inputs the alphabet.
         *
         * @return the {@link DefaultQuery} that is a counterexample this property, or {@code null}, if the property
         * could not be disproved.
         *
         * @throws ModelCheckingException when this property can not be checked.
         */
        @Nullable
        DefaultQuery<I, D> disprove(A hypothesis, Collection<? extends I> inputs) throws ModelCheckingException;

        /**
         * Try to find a counterexample to the given {@code hypothesis}.
         *
         * @param hypothesis the hypothesis to find a counterexample to.
         * @param inputs the alphabet.
         * @return the {@link DefaultQuery} that is a counterexample to the given {@code hypothesis}, or {@code
         * null}, a counterexample could not be found.
         *
         * @throws ModelCheckingException when this property can not be checked.
         */
        @Nullable
        DefaultQuery<I, D> findCounterExample(A hypothesis, Collection<? extends I> inputs) throws ModelCheckingException;

        /**
         * Clears a cache.
         *
         * @see BlackBoxProperty
         */
        void clearCache();

        /**
         * Use a cache.
         *
         * @see BlackBoxProperty
         */
        void useCache();
    }

    interface DFABlackBoxProperty<P, I> extends BlackBoxProperty<P, DFA<?, I>, I, Boolean> {}

    interface MealyBlackBoxProperty<P, I, O> extends BlackBoxProperty<P, MealyMachine<?, I, ?, O>, I, Word<O>> {}
}
