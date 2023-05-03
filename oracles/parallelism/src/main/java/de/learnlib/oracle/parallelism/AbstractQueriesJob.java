/* Copyright (C) 2013-2023 TU Dortmund
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
package de.learnlib.oracle.parallelism;

import java.util.Collection;

import de.learnlib.api.oracle.parallelism.BatchProcessor;

/**
 * Abstract base class for jobs (i.e., {@link Runnable}s) that process queries.
 * <p>
 * Subclasses specify how the delegate batch processor is obtained.
 *
 * @param <Q>
 *         query type
 *
 * @author Malte Isberner
 */
abstract class AbstractQueriesJob<Q> implements Runnable {

    private final Collection<? extends Q> queries;

    AbstractQueriesJob(Collection<? extends Q> queries) {
        this.queries = queries;
    }

    @Override
    public void run() {
        BatchProcessor<Q> oracle = getOracle();

        oracle.processBatch(queries);
    }

    protected abstract BatchProcessor<Q> getOracle();
}
