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
package de.learnlib.oracle.parallelism;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.learnlib.api.oracle.MembershipOracle;

/**
 * Basic interface for {@link MembershipOracle}s that can process queries in parallel.
 * <p>
 * Parallel oracles usually use one or more dedicated worker threads in which the processing of queries is performed.
 * Since these do not have a defined life span, they must be terminated explicitly using {@link #shutdown()} or {@link
 * #shutdownNow()}.
 *
 * @param <I>
 *         input symbol type
 * @param <D>
 *         output domain type
 *
 * @author Malte Isberner
 */
public interface ParallelOracle<I, D> extends MembershipOracle<I, D> {

    /**
     * Shuts down all worker threads, but waits for any queued queries to be processed.
     *
     * @see ExecutorService#shutdown()
     */
    void shutdown();

    /**
     * Shuts down all worker threads, and attempts to abort any query processing currently taking place.
     *
     * @see ExecutorService#shutdownNow()
     */
    void shutdownNow();

    /**
     * The policy for dealing with thread pools.
     *
     * @author Malte Isberner
     */
    enum PoolPolicy {
        /**
         * Maintain a fixed thread pool. The threads will be started immediately, and will terminate only if {@link
         * ParallelOracle#shutdown()} or {@link ParallelOracle#shutdownNow()} are called.
         *
         * @see Executors#newFixedThreadPool(int)
         */
        FIXED,
        /**
         * Maintain a "cached" thread pool. Threads will be created on-demand, but will be kept alive for re-use when
         * all jobs are processed. However, they will be terminated when they have been idle for 100 seconds.
         * <p>
         * Note that as opposed to {@link Executors#newCachedThreadPool()}, the specified pool size will never be
         * exceeded.
         *
         * @see Executors#newCachedThreadPool()
         */
        CACHED
    }
}
