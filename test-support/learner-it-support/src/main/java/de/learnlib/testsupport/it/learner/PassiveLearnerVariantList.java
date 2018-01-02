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

import de.learnlib.api.algorithm.PassiveLearningAlgorithm;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.automata.vpda.OneSEVPA;
import net.automatalib.words.Word;

public interface PassiveLearnerVariantList<M, I, D> {

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
    void addLearnerVariant(String name, PassiveLearningAlgorithm<? extends M, I, D> learner);

    interface DFALearnerVariantList<I> extends PassiveLearnerVariantList<DFA<?, I>, I, Boolean> {}

    interface MealyLearnerVariantList<I, O> extends PassiveLearnerVariantList<MealyMachine<?, I, ?, O>, I, Word<O>> {}

    interface MealySymLearnerVariantList<I, O> extends PassiveLearnerVariantList<MealyMachine<?, I, ?, O>, I, O> {}

    interface OneSEVPALearnerVariantList<I> extends PassiveLearnerVariantList<OneSEVPA<?, I>, I, Boolean> {}
}
