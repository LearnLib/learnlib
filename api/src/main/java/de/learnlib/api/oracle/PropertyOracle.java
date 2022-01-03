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
package de.learnlib.api.oracle;

import java.util.Collection;

import de.learnlib.api.query.DefaultQuery;
import net.automatalib.automata.concepts.Output;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.words.Word;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;

/**
 * A {@link PropertyOracle} can disprove a property, and used to find a counter example to an hypothesis.
 * <p>
 * Note that a property oracle is also an {@link InclusionOracle} and thus an {@link EquivalenceOracle}, hence it can
 * be use used to find counterexamples to hypotheses.
 * <p>
 * An implementation should keep track of whether the property is already disproved.
 *
 * @param <I> the input type
 * @param <A> the automaton type
 * @param <P> the property type
 * @param <D> the output type
 *
 * @author Jeroen Meijer
 */
public interface PropertyOracle<I, A extends Output<I, D>, P, D> extends InclusionOracle<A, I, D> {

    /**
     * Returns whether the property is disproved.
     *
     * @return whether the property is disproved.
     */
    default boolean isDisproved() {
        return getCounterExample() != null;
    }

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
    @Pure P getProperty();

    /**
     * Returns the counterexample for the property if {@link #isDisproved()}, {@code null} otherwise.
     *
     * If this method does not return {@code null}, a previous call to {@link #disprove(Output, Collection)} must
     * have returned a {@link DefaultQuery}.
     *
     * @return the counterexample for the property if {@link #isDisproved()}, {@code null} otherwise.
     */
    @Nullable DefaultQuery<I, D> getCounterExample();

    /**
     * Try to disprove the property with the given {@code hypothesis}.
     *
     * @param hypothesis the hypothesis.
     * @param inputs the inputs
     *
     * @return the {@link DefaultQuery} that is a counterexample the property, or {@code null}, if the property
     * could not be disproved.
     */
    @Nullable DefaultQuery<I, D> disprove(A hypothesis, Collection<? extends I> inputs);

    /**
     * Try to find a counterexample to the given {@code hypothesis} if the property can not be disproved.
     *
     * @param hypothesis the hypothesis to find a counterexample to.
     * @param inputs the input alphabet.
     *
     * @return the {@link DefaultQuery} that is a counterexample to the given {@code hypothesis}, or {@code
     * null}, a counterexample could not be found or the property could be disproved.
     */
    @Override
    default @Nullable DefaultQuery<I, D> findCounterExample(A hypothesis, Collection<? extends I> inputs) {
        return isDisproved() || disprove(hypothesis, inputs) != null ? null : doFindCounterExample(hypothesis, inputs);
    }

    /**
     * Unconditionally find a counterexample, i.e. regardless of whether the property can be disproved. In fact,
     * {@link #disprove(Output, Collection)} is not even be called.
     *
     * @see #findCounterExample(Output, Collection)
     */
    @Nullable DefaultQuery<I, D> doFindCounterExample(A hypothesis, Collection<? extends I> inputs);

    interface DFAPropertyOracle<I, P> extends PropertyOracle<I, DFA<?, I>, P, Boolean>, DFAInclusionOracle<I> {}

    interface MealyPropertyOracle<I, O, P>
            extends PropertyOracle<I, MealyMachine<?, I, ?, O>, P, Word<O>>, MealyInclusionOracle<I, O> {}
}

