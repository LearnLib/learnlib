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
package de.learnlib.filter.cache;

import de.learnlib.oracle.MembershipOracle;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.automaton.transducer.MooreMachine;
import net.automatalib.word.Word;

/**
 * A {@link LearningCache learning cache} that also serves as a {@link MembershipOracle membership oracle}.
 *
 * @param <A>
 *         the (maximally generic) automaton model for which the cache stores information. See {@link LearningCache}
 * @param <I>
 *         input symbol type
 * @param <D>
 *         output domain type
 */
public interface LearningCacheOracle<A, I, D> extends LearningCache<A, I, D>, MembershipOracle<I, D> {

    /**
     * Specialization of the {@link LearningCacheOracle} interface for DFA learning.
     *
     * @param <I>
     *         input symbol type
     */
    interface DFALearningCacheOracle<I>
            extends LearningCacheOracle<DFA<?, I>, I, Boolean>, DFALearningCache<I>, DFAMembershipOracle<I> {}

    /**
     * Specialization of the {@link LearningCacheOracle} interface for Mealy machine learning.
     *
     * @param <I>
     *         input symbol type
     * @param <O>
     *         output symbol type
     */
    interface MealyLearningCacheOracle<I, O> extends LearningCacheOracle<MealyMachine<?, I, ?, O>, I, Word<O>>,
                                                     MealyLearningCache<I, O>,
                                                     MealyMembershipOracle<I, O> {}

    /**
     * Specialization of the {@link LearningCacheOracle} interface for Moore machine learning.
     *
     * @param <I>
     *         input symbol type
     * @param <O>
     *         output symbol type
     */
    interface MooreLearningCacheOracle<I, O> extends LearningCacheOracle<MooreMachine<?, I, ?, O>, I, Word<O>>,
                                                     MooreLearningCache<I, O>,
                                                     MooreMembershipOracle<I, O> {}
}
