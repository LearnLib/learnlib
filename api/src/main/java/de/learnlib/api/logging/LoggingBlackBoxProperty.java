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
package de.learnlib.api.logging;

import java.util.Collection;

import javax.annotation.Nullable;

import de.learnlib.api.exception.ModelCheckingException;
import de.learnlib.api.oracle.BlackBoxOracle.BlackBoxProperty;
import de.learnlib.api.oracle.BlackBoxOracle.DFABlackBoxProperty;
import de.learnlib.api.oracle.BlackBoxOracle.MealyBlackBoxProperty;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.words.Word;

/**
 * A BlackBoxProperty that performs logging.
 *
 * This class will log whenever this property is disproved, or when a counterexample to an automaton is found (i.e. a
 * spurious counterexample is found).
 *
 * @author Jeroen Meijer
 *
 * @param <P> the property type
 * @param <A> the automaton type
 * @param <I> the input type
 * @param <D> the output type
 */
public class LoggingBlackBoxProperty<P, A, I, D> implements BlackBoxProperty<P, A, I, D> {

    private static final LearnLogger LOGGER = LearnLogger.getLogger(LoggingBlackBoxProperty.class);

    /**
     * The wrapped {@link BlackBoxProperty}.
     */
    private final BlackBoxProperty<P, A, I, D> blackBoxProperty;

    /**
     * Constructs a new LogginBlackBoxProperty.
     *
     * @param blackBoxProperty the {@link BlackBoxProperty} to wrap around.
     */
    public LoggingBlackBoxProperty(BlackBoxProperty<P, A, I, D> blackBoxProperty) {
        this.blackBoxProperty = blackBoxProperty;
    }

    @Override
    public boolean isDisproved() {
        return blackBoxProperty.isDisproved();
    }

    @Override
    public void setProperty(P property) {
        blackBoxProperty.setProperty(property);
    }

    @Override
    public P getProperty() {
        return blackBoxProperty.getProperty();
    }

    @Nullable
    @Override
    public DefaultQuery<I, D> getCounterExample() {
        return blackBoxProperty.getCounterExample();
    }

    /**
     * Try to disprove this property, and log whenever it is disproved.
     *
     * @see BlackBoxProperty#disprove(Object, Collection)
     */
    @Nullable
    @Override
    public DefaultQuery<I, D> disprove(A hypothesis, Collection<? extends I> inputs) throws ModelCheckingException {
        final DefaultQuery<I, D> result = blackBoxProperty.disprove(hypothesis, inputs);
        if (result != null) {
            LOGGER.logEvent("Property violated: '" + toString() + "'");
            LOGGER.logQuery("Counter example for property: " + getCounterExample());
        }

        return result;
    }

    /**
     * Try to find a counterexample to the given hypothesis, and log whenever such a spurious counterexample is found.
     *
     * @see BlackBoxProperty#findCounterExample(Object, Collection)
     */
    @Nullable
    @Override
    public DefaultQuery<I, D> findCounterExample(A hypothesis, Collection<? extends I> inputs) throws ModelCheckingException {
        final DefaultQuery<I, D> result = blackBoxProperty.findCounterExample(hypothesis, inputs);
        if (result != null) {
            LOGGER.logEvent("Spurious counterexample found for property: '" + toString() + "'");
            LOGGER.logCounterexample("Spurious counterexample: " + result);
        }
        return result;
    }

    @Override
    public void clearCache() {
        blackBoxProperty.clearCache();
    }

    @Override
    public void useCache() {
        blackBoxProperty.useCache();
    }

    @Override
    public String toString() {
        return blackBoxProperty.toString();
    }

    public static class DFALoggingBlackBoxProperty<P, I> extends LoggingBlackBoxProperty<P, DFA<?, I>, I, Boolean>
            implements DFABlackBoxProperty<P, I> {

        public DFALoggingBlackBoxProperty(DFABlackBoxProperty<P, I> blackBoxProperty) {
            super(blackBoxProperty);
        }
    }

    public static class MealyLoggingBlackBoxProperty<P, I, O>
            extends LoggingBlackBoxProperty<P, MealyMachine<?, I, ?, O>, I, Word<O>>
            implements MealyBlackBoxProperty<P, I, O> {

        public MealyLoggingBlackBoxProperty(MealyBlackBoxProperty<P, I, O> blackBoxProperty) {
            super(blackBoxProperty);
        }
    }
}
