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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import de.learnlib.query.DefaultQuery;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.fsa.FiniteStateAcceptor;
import net.automatalib.automaton.fsa.NFA;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.automaton.transducer.MooreMachine;
import net.automatalib.word.Word;

public interface PassiveLearningAlgorithm<M, I, D> {

    void addSamples(Collection<? extends DefaultQuery<I, D>> samples);

    @SuppressWarnings("unchecked")
    default void addSamples(DefaultQuery<I, D>... samples) {
        addSamples(Arrays.asList(samples));
    }

    @SuppressWarnings("unchecked")
    default void addSamples(D output, Word<I>... words) {
        addSamples(output, Arrays.asList(words));
    }

    default void addSamples(D output, Collection<? extends Word<I>> words) {
        List<DefaultQuery<I, D>> queries = new ArrayList<>(words.size());
        for (Word<I> word : words) {
            queries.add(new DefaultQuery<>(word, output));
        }

        addSamples(queries);
    }

    default void addSample(Word<I> input, D output) {
        addSample(new DefaultQuery<>(input, output));
    }

    default void addSample(DefaultQuery<I, D> sample) {
        addSamples(Collections.singleton(sample));
    }

    /**
     * Computes the model given the previously added samples.
     * <p>
     * <b>Implementation note:</b> It is up to the implementation if this operation is repeatable or not, An
     * implementation may throw an {@link IllegalStateException} if additional samples are added after the first model
     * construction.
     *
     * @return the computed model
     */
    M computeModel();

    /**
     * Basic interface for passive learning algorithms that infer {@link DFA}s.
     *
     * @param <I>
     *         input symbol type
     */
    interface PassiveDFALearner<I> extends PassiveAcceptorLearner<DFA<?, I>, I> {}

    /**
     * Basic interface for passive learning algorithms that infer {@link MealyMachine Mealy machines}.
     *
     * @param <I>
     *         input symbol type
     * @param <O>
     *         output symbol type
     */
    interface PassiveMealyLearner<I, O> extends PassiveLearningAlgorithm<MealyMachine<?, I, ?, O>, I, Word<O>> {}

    /**
     * Basic interface for passive learning algorithms that infer {@link MooreMachine Moore machines}.
     *
     * @param <I>
     *         input symbol type
     * @param <O>
     *         output symbol type
     */
    interface PassiveMooreLearner<I, O> extends PassiveLearningAlgorithm<MooreMachine<?, I, ?, O>, I, Word<O>> {}

    /**
     * Basic interface for passive learning algorithms that infer finite-state acceptors ({@link DFA}s or {@link
     * NFA}s).
     *
     * @param <M>
     *         model type
     * @param <I>
     *         input symbol type
     */
    interface PassiveAcceptorLearner<M extends FiniteStateAcceptor<?, I>, I>
            extends PassiveLearningAlgorithm<M, I, Boolean> {

        default void addPositiveSample(Word<I> word) {
            addSample(word, true);
        }

        default void addPositiveSamples(Collection<? extends Word<I>> words) {
            addSamples(true, words);
        }

        @SuppressWarnings("unchecked")
        default void addPositiveSamples(Word<I>... words) {
            addSamples(true, words);
        }

        default void addNegativeSample(Word<I> word) {
            addSample(word, false);
        }

        default void addNegativeSamples(Collection<? extends Word<I>> words) {
            addSamples(false, words);
        }

        @SuppressWarnings("unchecked")
        default void addNegativeSamples(Word<I>... words) {
            addSamples(false, words);
        }
    }

}
