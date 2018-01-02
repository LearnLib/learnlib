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
package de.learnlib.api.algorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import de.learnlib.api.query.DefaultQuery;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.FiniteStateAcceptor;
import net.automatalib.automata.fsa.NFA;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.words.Word;

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

    M computeModel();

    /**
     * Basic interface for passive learning algorithms that infer {@link DFA}s.
     *
     * @param <I>
     *         input symbol type
     *
     * @author Malte Isberner
     */
    interface PassiveDFALearner<I> extends PassiveAcceptorLearner<DFA<?, I>, I> {}

    /**
     * Basic interface for passive learning algorithms that infer {@link MealyMachine Mealy machines}.
     *
     * @param <I>
     *         input symbol type
     * @param <O>
     *         output symbol type
     *
     * @author Malte Isberner
     */
    interface PassiveMealyLearner<I, O> extends PassiveLearningAlgorithm<MealyMachine<?, I, ?, O>, I, Word<O>> {}

    /**
     * Basic interface for passive learning algorithms that infer {@link NFA}s.
     *
     * @param <I>
     *         input symbol type
     *
     * @author Malte Isberner
     */
    interface PassiveNFALearner<I> extends PassiveAcceptorLearner<NFA<?, I>, I> {}

    /**
     * Basic interface for passive learning algorithms that infer finite-state acceptors ({@link DFA}s or {@link
     * NFA}s).
     *
     * @param <M>
     *         model type
     * @param <I>
     *         input symbol type
     *
     * @author Malte Isberner
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
