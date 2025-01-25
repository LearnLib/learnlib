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
package de.learnlib.oracle;

import java.util.Collection;
import java.util.Collections;

import de.learnlib.query.AdaptiveQuery;
import de.learnlib.query.AdaptiveQuery.Response;

/**
 * An adaptive variation of the {@link MembershipOracle} that is tailored towards answering
 * {@link AdaptiveQuery adaptive queries}.
 *
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbol type
 */
public interface AdaptiveMembershipOracle<I, O> extends BatchProcessor<AdaptiveQuery<I, O>> {

    /**
     * Processes a single query. When this method returns, the provided inputs of the {@link AdaptiveQuery#getInput()}
     * method will have been evaluated on the system under learning and its responses will have been forwarded to the
     * {@link AdaptiveQuery#processOutput(Object)} method until the method has returned {@link Response#FINISHED}.
     * <p>
     * The default implementation of this method will simply wrap the provided {@link AdaptiveQuery} in a singleton
     * {@link Collection} using {@link Collections#singleton(Object)}. Implementations in subclasses should override
     * this method to circumvent the Collection object creation, if possible.
     *
     * @param query
     *         the query to process
     */
    default void processQuery(AdaptiveQuery<I, O> query) {
        processQueries(Collections.singleton(query));
    }

    /**
     * Processes the specified collection of queries. When this method returns, the provided inputs of the
     * {@link AdaptiveQuery#getInput()} method will have been evaluated on the system under learning and its responses
     * will have been forwarded to the {@link AdaptiveQuery#processOutput(Object)} method until the method has returned
     * {@link Response#FINISHED}.
     *
     * @param queries
     *         the queries to process
     */
    void processQueries(Collection<? extends AdaptiveQuery<I, O>> queries);

    @Override
    default void processBatch(Collection<? extends AdaptiveQuery<I, O>> batch) {
        processQueries(batch);
    }
}

