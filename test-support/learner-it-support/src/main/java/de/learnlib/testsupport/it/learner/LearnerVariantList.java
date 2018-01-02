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
package de.learnlib.testsupport.it.learner;

import de.learnlib.api.algorithm.LearningAlgorithm;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.automata.vpda.OneSEVPA;
import net.automatalib.words.Word;

/**
 * A write-only list to store multiple variants of a learning algorithm.
 * <p>
 * Usually, there should be one integration test class per learning algorithm. However, in many cases a single learning
 * algorithm can be configured in numerous ways, all (or many) of which should be tested independently. Due to the large
 * number of possible combinations, it is undesirable to create a single integration test class for each configuration;
 * instead, these variants should be configured and created programmatically. The purpose of the variant list is to
 * offer a convenient interface for storing all these variants.
 *
 * @param <M>
 *         hypothesis model type (upper bound)
 * @param <I>
 *         input symbol type
 * @param <D>
 *         output type
 *
 * @author Malte Isberner
 */
public interface LearnerVariantList<M, I, D> {

    /**
     * Adds a learner variant with the default maximum number of rounds (i.e., the size of the target automaton) to the
     * list.
     * <p>
     * This is a convenience method, equivalent to invoking {@code addLearnerVariant(name, learner, -1)}.
     *
     * @param name
     *         the name of the variant
     * @param learner
     *         the algorithm instance for this variant
     */
    void addLearnerVariant(String name, LearningAlgorithm<? extends M, I, D> learner);

    /**
     * Adds a learner variant with a given maximum number of rounds to the list.
     *
     * @param name
     *         the name of the variant
     * @param learner
     *         the algorithm instance for this variant
     * @param maxRounds
     *         the maximum number of rounds for the specified target automaton. If a value less than or equal to zero is
     *         specified, the default maximum number of rounds (the size of the target automaton) is assumed.
     */
    void addLearnerVariant(String name, LearningAlgorithm<? extends M, I, D> learner, int maxRounds);

    interface DFALearnerVariantList<I> extends LearnerVariantList<DFA<?, I>, I, Boolean> {}

    interface MealyLearnerVariantList<I, O> extends LearnerVariantList<MealyMachine<?, I, ?, O>, I, Word<O>> {}

    interface MealySymLearnerVariantList<I, O> extends LearnerVariantList<MealyMachine<?, I, ?, O>, I, O> {}

    interface OneSEVPALearnerVariantList<I> extends LearnerVariantList<OneSEVPA<?, I>, I, Boolean> {}

}
