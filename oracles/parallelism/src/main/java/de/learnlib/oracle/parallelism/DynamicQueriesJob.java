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
package de.learnlib.oracle.parallelism;

import java.util.Collection;

import de.learnlib.oracle.BatchProcessor;

/**
 * A queries job that maintains a thread-local reference to a {@link BatchProcessor}, and dynamically selects that
 * oracle depending on the executing thread.
 * <p>
 * Note: This class assumes that the respective {@link ThreadLocal#get()} methods never returns a {@code null}
 * reference.
 *
 * @param <Q>
 *         query type
 */
final class DynamicQueriesJob<Q> extends AbstractQueriesJob<Q> {

    private final ThreadLocal<? extends BatchProcessor<Q>> threadLocalOracle;

    DynamicQueriesJob(Collection<? extends Q> queries, ThreadLocal<? extends BatchProcessor<Q>> threadLocalOracle) {
        super(queries);
        this.threadLocalOracle = threadLocalOracle;
    }

    @Override
    protected BatchProcessor<Q> getOracle() {
        return threadLocalOracle.get();
    }

}
