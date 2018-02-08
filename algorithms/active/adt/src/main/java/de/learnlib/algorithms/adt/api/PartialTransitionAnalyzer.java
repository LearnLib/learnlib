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
package de.learnlib.algorithms.adt.api;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * During the refinement process of the hypothesis, ADS/Ts may be computed on partially defined automata. These
 * computations may want to skip undefined transitions (as closing them results in resets, which we want to omit) and
 * only determine the information if necessary. This interface mediates between the learner and ADS/T computations by
 * defining the basic forms of communication.
 *
 * @param <S>
 *         (hypothesis) state type
 * @param <I>
 *         input alphabet type
 *
 * @author frohme
 */
@ParametersAreNonnullByDefault
public interface PartialTransitionAnalyzer<S, I> {

    /**
     * Global exception instance to avoid (unnecessary) re-instantiation.
     */
    RuntimeException HYPOTHESIS_MODIFICATION_EXCEPTION = new HypothesisModificationException();

    /**
     * Check whether the transition in question is defined or not.
     *
     * @param state
     *         the (source) state of the transition in question
     * @param input
     *         the input symbol of the transition in question
     *
     * @return {@code true} if the transition (and thus the successor/output) is defined, {@code false} otherwise
     */
    boolean isTransitionDefined(S state, I input);

    /**
     * Determine the successor/output of the transition in question (which is usually achieved by sifting the
     * corresponding long-prefix through the ADT).
     *
     * @param state
     *         the (source) state of the transition in question
     * @param input
     *         the input symbol of the transition in question
     *
     * @throws HypothesisModificationException
     *         if closing the transition (sifting in the ADT) discovered a new hypothesis state and thus potentially
     *         invalidates previously observed behavior
     */
    void closeTransition(S state, I input) throws HypothesisModificationException;

    /**
     * A helper exception to interrupt computations on an invalid hypothesis. Does not record the stacktrace when thrown
     * (to improve performance)
     */
    class HypothesisModificationException extends RuntimeException {

        public HypothesisModificationException() {
            super(null, null, false, false);
        }

        @Override
        public synchronized Throwable fillInStackTrace() {
            return this;
        }

    }
}
