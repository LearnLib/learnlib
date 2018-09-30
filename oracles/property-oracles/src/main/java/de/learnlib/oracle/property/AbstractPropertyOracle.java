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
package de.learnlib.oracle.property;

import java.util.Collection;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import de.learnlib.api.oracle.EmptinessOracle;
import de.learnlib.api.oracle.InclusionOracle;
import de.learnlib.api.oracle.PropertyOracle;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.automata.concepts.Output;

/**
 * A {@link PropertyOracle} that uses {@link InclusionOracle}s and {@link EmptinessOracle}s to find counter examples
 * and disprove properties.
 *
 * @author Jeroen Meijer
 *
 * @param <I> the input type
 * @param <A> the automaton type
 * @param <P> the property type
 * @param <D> the output type
 * @param <R> the result type of a model checker
 */
@ParametersAreNonnullByDefault
abstract class AbstractPropertyOracle<I, A extends Output<I, D>, P, D, R extends A>
        implements PropertyOracle<I, A, P, D> {

    private final InclusionOracle<A, I, D> inclusionOracle;
    private final EmptinessOracle<R, I, D> emptinessOracle;
    private P property;
    private DefaultQuery<I, D> counterExample;

    protected AbstractPropertyOracle(P property,
                                     InclusionOracle<A, I, D> inclusionOracle,
                                     EmptinessOracle<R, I, D> emptinessOracle) {
        this.property = property;
        this.inclusionOracle = inclusionOracle;
        this.emptinessOracle = emptinessOracle;
    }

    @Nullable
    protected DefaultQuery<I, D> setCounterExample(@Nullable DefaultQuery<I, D> counterExample) {
        this.counterExample = counterExample;
        assert this.counterExample == null || counterExample != null;
        return this.counterExample;
    }

    @Override
    public void setProperty(P property) {
        this.property = property;
    }

    @Override
    public P getProperty() {
        return property;
    }

    @Nullable
    @Override
    public DefaultQuery<I, D> getCounterExample() {
        return counterExample;
    }

    protected abstract R modelCheck(A hypothesis, Collection<? extends I> inputs);

    @Nullable
    @Override
    public DefaultQuery<I, D> doFindCounterExample(A hypothesis, Collection<? extends I> inputs) {
        final A result = modelCheck(hypothesis, inputs);
        return result != null ? inclusionOracle.findCounterExample(result, inputs) : null;
    }

    @Nullable
    @Override
    public DefaultQuery<I, D> disprove(A hypothesis, Collection<? extends I> inputs) {
        final R ce = modelCheck(hypothesis, inputs);

        return ce != null ? setCounterExample(emptinessOracle.findCounterExample(ce, inputs)) : null;
    }
}
