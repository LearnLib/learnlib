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
package de.learnlib.modelchecking.modelchecker;

import de.learnlib.api.modelchecking.counterexample.Lasso;
import de.learnlib.api.modelchecking.modelchecker.ModelChecker.ModelCheckerLasso;
import net.automatalib.automata.concepts.Output;
import net.automatalib.ts.simple.SimpleDTS;

/**
 * An {@link ModelCheckerLasso} that can unfold loops of lassos.
 *
 * Unfolding a lasso is done according to two conditions:
 *  1. the lasso has to be unfolded a minimum number of times ({@link #getMinimumUnfolds()}.
 *  2. the lasso has to be unfolded relative to the number of states in an hypothesis, multiplied by some double
 *  ({@link #getMultiplier()}.
 *
 * Note that one can unfold a lasso a fixed number of times if the multiplier is set to {@code 0.0}.
 * Also note that a lasso needs to be unfolded at least once, and the multiplier can not be negative.
 *
 * @param <I> the input type
 * @param <A> the automaton type
 * @param <P> the property type
 * @param <L> the Lasso type
 */
public abstract class AbstractUnfoldingModelChecker<I,
                                            A extends SimpleDTS<?, I> & Output<I, ?>,
                                            P,
                                            L extends Lasso<?, ? extends A, I, ?>>
        implements ModelCheckerLasso<I, A, P, L> {

    /**
     * The minimum number of unfolds.
     *
     * @see AbstractUnfoldingModelChecker
     */
    private int minimumUnfolds;

    /**
     * The multiplier.
     *
     * @see AbstractUnfoldingModelChecker
     */
    private double multiplier;

    /**
     * Constructs a new AbstractUnfoldingModelChecker.
     *
     * @param minimumUnfolds the minimum number of unfolds.
     * @param multiplier the multiplier
     *
     * @throws IllegalArgumentException when {@code minimumUnfolds < 1 || multiplier < 0.0}.
     */
    protected AbstractUnfoldingModelChecker(int minimumUnfolds, double multiplier) throws IllegalArgumentException {
        setMinimumUnfolds(minimumUnfolds);
        setMultiplier(multiplier);
    }

    /**
     * Compute the number of unfolds according to {@code size}.
     *
     * @param size the number of states in the hypothesis.
     *
     * @return the number of times the loop of a lasso has to be unfolded.
     */
    protected final int computeUnfolds(int size) {
        if (size < 1) {
            throw new IllegalArgumentException("Illegal size: " + size);
        }
        final int relativeUnfolds = (int) Math.ceil(size * multiplier);
        return Math.max(minimumUnfolds, relativeUnfolds);
    }

    @Override
    public int getMinimumUnfolds() {
        assert minimumUnfolds > 0;
        return minimumUnfolds;
    }

    @Override
    public void setMinimumUnfolds(int minimumUnfolds) throws IllegalArgumentException {
        if (minimumUnfolds < 1) {
            throw new IllegalArgumentException("must unfold at least once");
        }
        this.minimumUnfolds = minimumUnfolds;
    }

    @Override
    public void setMultiplier(double multiplier) throws IllegalArgumentException {
        if (multiplier < 0.0) {
            throw new IllegalArgumentException("multiplier must be >= 0.0");
        }
        this.multiplier = multiplier;
    }

    @Override
    public double getMultiplier() {
        return multiplier;
    }
}
