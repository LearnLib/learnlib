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
package de.learnlib.api.logging;

import java.util.Collection;

import de.learnlib.api.oracle.PropertyOracle;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.automata.concepts.Output;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.words.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A PropertyOracle that performs logging.
 *
 * This class will log whenever the property is disproved, or when a counterexample to an automaton is found (i.e. a
 * spurious counterexample is found).
 *
 * @author Jeroen Meijer
 *
 * @param <I> the input type
 * @param <A> the automaton type
 * @param <P> the property type
 * @param <D> the output type
 */
public class LoggingPropertyOracle<I, A extends Output<I, D>, P, D> implements PropertyOracle<I, A, P, D> {

    private static final LearnLogger LOGGER = LearnLogger.getLogger(LoggingPropertyOracle.class);

    /**
     * The wrapped {@link PropertyOracle}.
     */
    private final PropertyOracle<I, A, P, D> propertyOracle;

    /**
     * Constructs a new LoggingPropertyOracle.
     *
     * @param propertyOracle the {@link PropertyOracle} to wrap around.
     */
    public LoggingPropertyOracle(PropertyOracle<I, A, P, D> propertyOracle) {
        this.propertyOracle = propertyOracle;
    }

    @Override
    public boolean isDisproved() {
        return propertyOracle.isDisproved();
    }

    @Override
    public void setProperty(P property) {
        this.propertyOracle.setProperty(property);
    }

    @Override
    public P getProperty() {
        return propertyOracle.getProperty();
    }

    @Override
    public @Nullable DefaultQuery<I, D> getCounterExample() {
        return propertyOracle.getCounterExample();
    }

    /**
     * Try to disprove this propertyOracle, and log whenever it is disproved.
     *
     * @see PropertyOracle#disprove(Output, Collection)
     */
    @Override
    public @Nullable DefaultQuery<I, D> disprove(A hypothesis, Collection<? extends I> inputs) {
        final DefaultQuery<I, D> result = propertyOracle.disprove(hypothesis, inputs);
        if (result != null) {
            LOGGER.logEvent("Property violated: '" + toString() + "'");
            LOGGER.logQuery("Counter example for property: " + getCounterExample());
        }

        return result;
    }

    /**
     * Try to find a counterexample to the given hypothesis, and log whenever such a spurious counterexample is found.
     *
     * @see PropertyOracle#findCounterExample(Output, Collection)
     */
    @Override
    public @Nullable DefaultQuery<I, D> doFindCounterExample(A hypothesis, Collection<? extends I> inputs) {
        final DefaultQuery<I, D> result = propertyOracle.findCounterExample(hypothesis, inputs);
        if (result != null) {
            LOGGER.logEvent("Spurious counterexample found for property: '" + toString() + "'");
            LOGGER.logCounterexample("Spurious counterexample: " + result);
        }
        return result;
    }

    @Override
    public String toString() {
        return String.valueOf(propertyOracle.getProperty());
    }

    public static class DFALoggingPropertyOracle<I, P> extends LoggingPropertyOracle<I, DFA<?, I>, P, Boolean>
            implements DFAPropertyOracle<I, P> {

        public DFALoggingPropertyOracle(DFAPropertyOracle<I, P> property) {
            super(property);
        }
    }

    public static class MealyLoggingPropertyOracle<I, O, P>
            extends LoggingPropertyOracle<I, MealyMachine<?, I, ?, O>, P, Word<O>>
            implements MealyPropertyOracle<I, O, P> {

        public MealyLoggingPropertyOracle(MealyPropertyOracle<I, O, P> property) {
            super(property);
        }
    }
}
