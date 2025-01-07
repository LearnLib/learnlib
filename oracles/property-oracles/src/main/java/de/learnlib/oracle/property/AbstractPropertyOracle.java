/* Copyright (C) 2013-2025 TU Dortmund University
 * This file is part of LearnLib <https://learnlib.de>.
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

import de.learnlib.oracle.EmptinessOracle;
import de.learnlib.oracle.InclusionOracle;
import de.learnlib.oracle.PropertyOracle;
import de.learnlib.query.DefaultQuery;
import net.automatalib.automaton.concept.Output;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A {@link PropertyOracle} that uses {@link InclusionOracle}s and {@link EmptinessOracle}s to find counter examples and
 * disprove properties.
 *
 * @param <I>
 *         the input type
 * @param <A>
 *         the automaton type
 * @param <P>
 *         the property type
 * @param <D>
 *         the output type
 * @param <R>
 *         the result type of model checker
 */
abstract class AbstractPropertyOracle<I, A extends Output<I, D>, P, D, R extends A>
        implements PropertyOracle<I, A, P, D> {

    private final InclusionOracle<A, I, D> inclusionOracle;
    private final EmptinessOracle<R, I, D> emptinessOracle;
    private final P property;
    private @Nullable DefaultQuery<I, D> counterExample;

    protected AbstractPropertyOracle(P property,
                                     InclusionOracle<A, I, D> inclusionOracle,
                                     EmptinessOracle<R, I, D> emptinessOracle) {
        this.property = property;
        this.inclusionOracle = inclusionOracle;
        this.emptinessOracle = emptinessOracle;
    }

    protected @Nullable DefaultQuery<I, D> setCounterExample(@Nullable DefaultQuery<I, D> counterExample) {
        this.counterExample = counterExample;
        return counterExample;
    }

    @Override
    public P getProperty() {
        return property;
    }

    @Override
    public @Nullable DefaultQuery<I, D> getCounterExample() {
        return counterExample;
    }

    protected abstract @Nullable R modelCheck(A hypothesis, Collection<? extends I> inputs);

    @Override
    public @Nullable DefaultQuery<I, D> doFindCounterExample(A hypothesis, Collection<? extends I> inputs) {
        final A result = modelCheck(hypothesis, inputs);
        return result != null ? inclusionOracle.findCounterExample(result, inputs) : null;
    }

    @Override
    public @Nullable DefaultQuery<I, D> disprove(A hypothesis, Collection<? extends I> inputs) {
        final R ce = modelCheck(hypothesis, inputs);

        return ce != null ? setCounterExample(emptinessOracle.findCounterExample(ce, inputs)) : null;
    }
}
