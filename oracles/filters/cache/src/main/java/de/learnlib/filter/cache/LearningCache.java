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
package de.learnlib.filter.cache;

import de.learnlib.api.oracle.EquivalenceOracle;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.words.Word;

/**
 * Interface for a cache used in automata learning.
 * <p>
 * The idea of a cache is to save (potentially expensive) queries to the real system under learning by storing the
 * results of previous queries. This is particularly useful as many learning algorithms pose redundant queries, i.e.,
 * pose the same query twice or more times in different contexts.
 * <p>
 * A learning cache provides a {@link #createCacheConsistencyTest() cache consistency test}, which is an equivalence
 * query realization that only checks a given hypothesis against the contents of the cache.
 *
 * @param <A>
 *         the (maximally generic) automaton model for which the caches stores information. For example, for a {@link
 *         MealyMachine Mealy} cache this would be {@code MealyMachine<?,I,?,O>}. This type determines what the cache
 *         contents can be checked against by a {@link #createCacheConsistencyTest() cache consistency test}
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbol type
 *
 * @author Malte Isberner
 */
public interface LearningCache<A, I, O> {

    /**
     * Creates a <i>cache consistency test</i>. A cache consistency test is an equivalence oracle which checks a given
     * hypothesis against the current contents of the cache. Hence, no queries are posed to the underlying system.
     * <p>
     * The created cache consistency test is backed by the cache contents. This method does not need to be invoked
     * repeatedly when the cache contents change.
     *
     * @return a cache consistency test for the contents of this cache
     */
    EquivalenceOracle<A, I, O> createCacheConsistencyTest();

    /**
     * Specialization of the {@link LearningCache} interface for DFA learning.
     *
     * @param <I>
     *         input symbol type
     *
     * @author Malte Isberner
     */
    interface DFALearningCache<I> extends LearningCache<DFA<?, I>, I, Boolean> {}

    /**
     * Specialization of the {@link LearningCache} interface for Mealy machine learning.
     *
     * @param <I>
     *         input symbol type
     * @param <O>
     *         output symbol type
     *
     * @author Malte Isberner
     */
    interface MealyLearningCache<I, O> extends LearningCache<MealyMachine<?, I, ?, O>, I, Word<O>> {}
}
