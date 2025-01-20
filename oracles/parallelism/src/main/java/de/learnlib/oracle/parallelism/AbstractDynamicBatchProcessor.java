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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Supplier;

import de.learnlib.exception.BatchInterruptedException;
import de.learnlib.oracle.BatchProcessor;
import de.learnlib.oracle.ThreadPool;
import net.automatalib.common.util.exception.ExceptionUtil;
import org.checkerframework.checker.index.qual.NonNegative;

/**
 * A batch processor that dynamically distributes queries to worker threads.
 * <p>
 * An incoming set of queries is split into batches of the given size. The number of batches may exceed the available
 * threads so that they are dynamically scheduled once a job finishes.
 *
 * @param <Q>
 *         query type
 * @param <P>
 *         (sub-) processor type
 */
public abstract class AbstractDynamicBatchProcessor<Q, P extends BatchProcessor<Q>>
        implements ThreadPool, BatchProcessor<Q> {

    private final ThreadLocal<P> threadLocalOracle;
    private final ExecutorService executor;
    private final @NonNegative int batchSize;

    public AbstractDynamicBatchProcessor(Supplier<? extends P> oracleSupplier,
                                         @NonNegative int batchSize,
                                         ExecutorService executor) {
        this.threadLocalOracle = ThreadLocal.withInitial(oracleSupplier);
        this.executor = executor;
        this.batchSize = batchSize;
    }

    @Override
    public void shutdown() {
        executor.shutdown();
    }

    @Override
    public void shutdownNow() {
        executor.shutdownNow();
    }

    @Override
    public void processBatch(Collection<? extends Q> queries) {
        if (queries.isEmpty()) {
            return;
        }

        int numQueries = queries.size();
        int numJobs = (numQueries - 1) / batchSize + 1;
        List<Q> currentBatch = null;

        List<Future<?>> futures = new ArrayList<>(numJobs);

        for (Q query : queries) {

            if (currentBatch == null) {
                currentBatch = new ArrayList<>(batchSize);
            }

            currentBatch.add(query);
            if (currentBatch.size() == batchSize) {
                Future<?> future = executor.submit(new DynamicQueriesJob<>(currentBatch, threadLocalOracle));
                futures.add(future);
                currentBatch = null;
            }
        }

        if (currentBatch != null) {
            Future<?> future = executor.submit(new DynamicQueriesJob<>(currentBatch, threadLocalOracle));
            futures.add(future);
        }

        try {
            // Await completion of all jobs
            for (Future<?> future : futures) {
                future.get();
            }
        } catch (ExecutionException e) {
            ExceptionUtil.throwIfUnchecked(e.getCause());
            throw new AssertionError("Runnables must not throw checked exceptions", e);
        } catch (InterruptedException e) {
            Thread.interrupted();
            throw new BatchInterruptedException(e);
        }
    }

    protected P getProcessor() {
        return threadLocalOracle.get();
    }

}
