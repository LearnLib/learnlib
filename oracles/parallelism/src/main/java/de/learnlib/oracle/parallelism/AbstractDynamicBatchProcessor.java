/* Copyright (C) 2013-2022 TU Dortmund
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Supplier;

import com.google.common.base.Throwables;
import de.learnlib.api.oracle.parallelism.BatchInterruptedException;
import de.learnlib.api.oracle.parallelism.BatchProcessor;
import de.learnlib.api.oracle.parallelism.ThreadPool;
import de.learnlib.setting.LearnLibProperty;
import de.learnlib.setting.LearnLibSettings;
import org.checkerframework.checker.index.qual.NonNegative;

/**
 * A batch processor that dynamically distributes queries to worker threads.
 *
 * @param <Q>
 *         query type
 * @param <P>
 *         (sub-) processor type
 *
 * @author Malte Isberner
 */
public abstract class AbstractDynamicBatchProcessor<Q, P extends BatchProcessor<Q>>
        implements ThreadPool, BatchProcessor<Q> {

    public static final int BATCH_SIZE;
    public static final int POOL_SIZE;
    public static final PoolPolicy POOL_POLICY;

    static {
        LearnLibSettings settings = LearnLibSettings.getInstance();

        int numProcessors = Runtime.getRuntime().availableProcessors();

        BATCH_SIZE = settings.getInt(LearnLibProperty.PARALLEL_BATCH_SIZE_DYNAMIC, 1);
        POOL_SIZE = settings.getInt(LearnLibProperty.PARALLEL_POOL_SIZE, numProcessors);
        POOL_POLICY = settings.getEnumValue(LearnLibProperty.PARALLEL_POOL_POLICY, PoolPolicy.class, PoolPolicy.CACHED);
    }

    private final ThreadLocal<P> threadLocalOracle;
    private final ExecutorService executor;
    private final @NonNegative int batchSize;

    public AbstractDynamicBatchProcessor(final Supplier<? extends P> oracleSupplier,
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
            Throwables.throwIfUnchecked(e.getCause());
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
