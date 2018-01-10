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

import java.util.Collection;

import javax.annotation.Nullable;

import de.learnlib.api.exception.ModelCheckingException;
import de.learnlib.api.modelchecking.counterexample.Lasso.DFALasso;
import de.learnlib.api.modelchecking.counterexample.Lasso.MealyLasso;
import de.learnlib.api.modelchecking.modelchecker.ModelChecker;
import de.learnlib.api.modelchecking.modelchecker.ModelChecker.DFAModelChecker;
import de.learnlib.api.modelchecking.modelchecker.ModelChecker.DFAModelCheckerLasso;
import de.learnlib.api.modelchecking.modelchecker.ModelChecker.MealyModelChecker;
import de.learnlib.api.modelchecking.modelchecker.ModelChecker.MealyModelCheckerLasso;
import de.learnlib.api.oracle.BlackBoxOracle.BlackBoxProperty;
import de.learnlib.api.oracle.BlackBoxOracle.DFABlackBoxProperty;
import de.learnlib.api.oracle.BlackBoxOracle.MealyBlackBoxProperty;
import de.learnlib.api.oracle.EmptinessOracle;
import de.learnlib.api.oracle.EmptinessOracle.DFAEmptinessOracle;
import de.learnlib.api.oracle.EmptinessOracle.DFALassoEmptinessOracle;
import de.learnlib.api.oracle.EmptinessOracle.MealyEmptinessOracle;
import de.learnlib.api.oracle.EmptinessOracle.MealyLassoEmptinessOracle;
import de.learnlib.api.oracle.InclusionOracle;
import de.learnlib.api.oracle.InclusionOracle.DFAInclusionOracle;
import de.learnlib.api.oracle.InclusionOracle.MealyInclusionOracle;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.automata.concepts.Output;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.ts.simple.SimpleDTS;
import net.automatalib.words.Word;

/**
 * A {@link BlackBoxProperty} that needs a {@link ModelChecker} to verify.
 *
 * The main part of the implementation involves caching results in between calls to
 * {@link #disprove(Output, Collection)} {@link #findCounterExample(Output, Collection)}.
 *
 * @author Jeroen Meijer
 *
 * @param <P> the property type.
 * @param <A> the automaton type.
 * @param <I> the input type.
 * @param <D> the output type.
 * @param <R> the result type of the {@link ModelChecker}.
 */
public class ModelCheckingBBProperty<P, A extends Output<I, D> & SimpleDTS<?, I>, I, D, R extends A>
        implements BlackBoxProperty<P, A, I, D> {

    /**
     * The property to check.
     */
    private P property;

    private ModelChecker<I, A, P, R> modelChecker;

    private EmptinessOracle<R, I, D, ?> emptinessOracle;

    private InclusionOracle<A, I, D, ?> inclusionOracle;

    /**
     * The cached result of a model checking counterexample.
     */
    private R cacheResult;

    /**
     * The inputs used to find the cached result.
     */
    private Collection<? extends I> cacheInputs;

    /**
     * The counterexample for this property.
     */
    private R modelCheckingResult;

    /**
     * The counterexample for this property as a {@link DefaultQuery}.
     */
    private DefaultQuery<I, D> counterExample;

    /**
     * Whether to use the cache.
     */
    private boolean cache;

    public ModelCheckingBBProperty(
            ModelChecker<I, A, P, R> modelChecker,
            EmptinessOracle<R, I, D, ?> emptinessOracle,
            InclusionOracle<A, I, D, ?> inclusionOracle,
            P property) {
        this.modelChecker = modelChecker;
        this.emptinessOracle = emptinessOracle;
        this.inclusionOracle = inclusionOracle;
        this.property = property;
    }

    public ModelChecker<I, A, P, R> getModelChecker() {
        return modelChecker;
    }

    public void setModelChecker(ModelChecker<I, A, P, R> modelChecker) {
        this.modelChecker = modelChecker;
    }

    public EmptinessOracle<R, I, D, ?> getEmptinessOracle() {
        return emptinessOracle;
    }

    public void setEmptinessOracle(EmptinessOracle<R, I, D, ?> emptinessOracle) {
        this.emptinessOracle = emptinessOracle;
    }

    public InclusionOracle<A, I, D, ?> getInclusionOracle() {
        return inclusionOracle;
    }

    public void setInclusionOracle(InclusionOracle<A, I, D, ?> inclusionOracle) {
        this.inclusionOracle = inclusionOracle;
    }

    @Override
    public P getProperty() {
        return property;
    }

    @Override
    public void setProperty(P property) {
        this.property = property;
    }

    @Override
    public boolean isDisproved() {
        assert (modelCheckingResult == null) == (counterExample == null);
        return counterExample != null;
    }

    @Override
    @Nullable
    public DefaultQuery<I, D> getCounterExample() {
        assert (modelCheckingResult == null) == (counterExample == null);
        return counterExample;
    }

    /**
     * Finds a counterexample for the given {@code hypothesis} to this property.
     *
     * The model checker will only be invoked in case of a cache-miss.
     *
     * @param hypothesis the hypothesis.
     * @param inputs the alphabet.
     * @return the counterexample, or {@code null} if a counterexample could not be found.
     *
     * @throws ModelCheckingException see ModelChecker{@link #findCounterExample(Output, Collection)}.
     */
    @Nullable
    private R findCachedCounterExample(A hypothesis, Collection<? extends I> inputs) throws ModelCheckingException {
        if (!cache || !inputs.equals(cacheInputs)) {
            cacheInputs = inputs;
            cacheResult = modelChecker.findCounterExample(hypothesis, inputs, property);
        }

        return cacheResult;
    }

    @Override
    public void useCache() {
        cache = true;
    }

    @Override
    public void clearCache() {
        cacheInputs = null;
    }

    /**
     * Try to disprove this property given a {@code hypothesis} by means of an {@link EmptinessOracle}.
     *
     * @see BlackBoxProperty#disprove(Object, Collection)
     */
    @Override
    @Nullable
    public DefaultQuery<I, D> disprove(A hypothesis, Collection<? extends I> inputs) throws ModelCheckingException {
        modelCheckingResult = findCachedCounterExample(hypothesis, inputs);
        if (modelCheckingResult != null) {
            counterExample = emptinessOracle.findCounterExample(modelCheckingResult, inputs);
        }
        return counterExample;
    }

    /**
     * Try to find a counterexample to the given {@code hypothesis}.
     *
     * @see BlackBoxProperty#findCounterExample(Object, Collection)
     */
    @Override
    @Nullable
    public DefaultQuery<I, D> findCounterExample(A hypothesis, Collection<? extends I> inputs) throws ModelCheckingException {
        final R ce = findCachedCounterExample(hypothesis, inputs);
        final DefaultQuery<I, D> result;
        if (ce != null) {
            result = inclusionOracle.findCounterExample(ce, inputs);
        } else {
            result = null;
        }
        return result;
    }

    @Override
    public String toString() {
        return property.toString();
    }

    public static class DFABBPropertyDFA<P, I>
            extends ModelCheckingBBProperty<P, DFA<?, I>, I, Boolean, DFA<?, I>>
            implements DFABlackBoxProperty<P, I> {

        public DFABBPropertyDFA(
                DFAModelChecker<I, P, DFA<?, I>> modelChecker,
                DFAEmptinessOracle<I> emptinessOracle,
                DFAInclusionOracle<I> inclusionOracle,
                P property) {
            super(modelChecker, emptinessOracle, inclusionOracle, property);
        }
    }

    public static class MealyBBPropertyMealy<P, I, O>
            extends ModelCheckingBBProperty<P, MealyMachine<?, I, ?, O>, I, Word<O>, MealyMachine<?, I, ?, O>>
            implements MealyBlackBoxProperty<P, I, O> {

        public MealyBBPropertyMealy(
                MealyModelChecker<I, O, P, MealyMachine<?, I, ?, O>> modelChecker,
                MealyEmptinessOracle<I, O> emptinessOracle,
                MealyInclusionOracle<I, O> inclusionOracle,
                P property) {
            super(modelChecker, emptinessOracle, inclusionOracle, property);
        }
    }

    public static class DFABBPropertyDFALasso<P, I>
            extends ModelCheckingBBProperty<P, DFA<?, I>, I, Boolean, DFALasso<?, I>>
            implements DFABlackBoxProperty<P, I> {

        public DFABBPropertyDFALasso(
                DFAModelCheckerLasso<I, P> modelChecker,
                DFALassoEmptinessOracle<?, I> emptinessOracle,
                DFAInclusionOracle<I> inclusionOracle,
                P property) {
            super(modelChecker, emptinessOracle, inclusionOracle, property);
        }
    }

    public static class MealyBBPropertyMealyLasso<P, I, O>
            extends ModelCheckingBBProperty<P, MealyMachine<?, I, ?, O>, I, Word<O>, MealyLasso<?, I, O>>
            implements MealyBlackBoxProperty<P, I, O> {

        public MealyBBPropertyMealyLasso(
                MealyModelCheckerLasso<I, O, P> modelChecker,
                MealyLassoEmptinessOracle<?, I, O> emptinessOracle,
                MealyInclusionOracle<I, O> inclusionOracle,
                P property) {
            super(modelChecker, emptinessOracle, inclusionOracle, property);
        }
    }
}
