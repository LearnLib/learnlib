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
package de.learnlib.algorithm;

import de.learnlib.oracle.EquivalenceOracle;
import de.learnlib.query.DefaultQuery;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.fsa.NFA;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.automaton.transducer.MooreMachine;
import net.automatalib.word.Word;

/**
 * Basic interface for a model inference algorithm.
 * <p>
 * Actively inferring models (such as DFAs or Mealy machines) consists of the construction of an initial hypothesis,
 * which is subsequently refined using counterexamples (see {@link EquivalenceOracle}).
 *
 * @param <M>
 *         model type
 * @param <I>
 *         input symbol type
 * @param <D>
 *         output domain type
 */
public interface LearningAlgorithm<M, I, D> {

    /**
     * Starts the model inference process, creating an initial hypothesis in the provided model object. Please note that
     * it should be illegal to invoke this method twice.
     */
    void startLearning();

    /**
     * Triggers a refinement of the model by providing a counterexample. A counterexample is a query which exposes
     * different behavior of the real SUL compared to the hypothesis. Please note that invoking this method before an
     * initial invocation of {@link #startLearning()} should be illegal.
     *
     * @param ceQuery
     *         the query which exposes diverging behavior, as posed to the real SUL (i.e. with the SULs output).
     *
     * @return {@code true} if the counterexample triggered a refinement of the hypothesis, {@code false} otherwise
     * (i.e., it was no counterexample).
     */
    boolean refineHypothesis(DefaultQuery<I, D> ceQuery);

    /**
     * Returns the current hypothesis model.
     * <p>
     * N.B.: By the contract of this interface, the model returned by this method should not be modified by external
     * code (i.e., M generally should refer to an immutable interface), and its validity is retained only until the next
     * invocation of {@link #refineHypothesis(DefaultQuery)}. If older hypotheses have to be maintained, a copy of the
     * returned model must be made.
     * <p>
     * Please note that it should be illegal to invoke this method before an initial invocation of {@link
     * #startLearning()}.
     *
     * @return the current hypothesis model.
     */
    M getHypothesisModel();

    interface DFALearner<I> extends LearningAlgorithm<DFA<?, I>, I, Boolean> {}

    interface MealyLearner<I, O> extends LearningAlgorithm<MealyMachine<?, I, ?, O>, I, Word<O>> {}

    interface MooreLearner<I, O> extends LearningAlgorithm<MooreMachine<?, I, ?, O>, I, Word<O>> {}

    interface NFALearner<I> extends LearningAlgorithm<NFA<?, I>, I, Boolean> {}
}
