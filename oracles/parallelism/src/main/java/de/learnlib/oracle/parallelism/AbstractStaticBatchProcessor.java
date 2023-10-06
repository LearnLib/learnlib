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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.google.common.base.Throwables;
import de.learnlib.api.oracle.parallelism.BatchInterruptedException;
import de.learnlib.api.oracle.parallelism.BatchProcessor;
import de.learnlib.api.oracle.parallelism.ThreadPool;
import de.learnlib.setting.LearnLibProperty;
import de.learnlib.setting.LearnLibSettings;
import net.automatalib.commons.smartcollections.ArrayStorage;
import org.checkerframework.checker.index.qual.NonNegative;

/**
 * A batch processor that statically distributes a set of queries among several threads.
 * <p>
 * An incoming set of queries is divided into a given number of batches, such that the sizes of all batches differ by at
 * most one. This keeps the required synchronization effort low, but if some batches are "harder" (for whatever reason)
 * than others, the load can be very unbalanced.
 *
 * @param <Q>
 *         query type
 * @param <P>
 *         (sub-) processor type
 */
public abstract class AbstractStaticBatchProcessor<Q, P extends BatchProcessor<Q>>
        implements ThreadPool, BatchProcessor<Q> {

    private static final int DEFAULT_MIN_BATCH_SIZE = 10;
    public static final int MIN_BATCH_SIZE;
    public static final int NUM_INSTANCES;
    public static final PoolPolicy POOL_POLICY;

    static {
        LearnLibSettings settings = LearnLibSettings.getInstance();

        int numCores = Runtime.getRuntime().availableProcessors();

        MIN_BATCH_SIZE = settings.getInt(LearnLibProperty.PARALLEL_BATCH_SIZE_STATIC, DEFAULT_MIN_BATCH_SIZE);
        NUM_INSTANCES = settings.getInt(LearnLibProperty.PARALLEL_POOL_SIZE, numCores);
        POOL_POLICY = settings.getEnumValue(LearnLibProperty.PARALLEL_POOL_SIZE, PoolPolicy.class, PoolPolicy.CACHED);
    }

    private final @NonNegative int minBatchSize;
    private final ArrayStorage<P> oracles;
    private final ExecutorService executor;

    public AbstractStaticBatchProcessor(Collection<? extends P> oracles,
                                        @NonNegative int minBatchSize,
                                        PoolPolicy policy) {

        this.oracles = new ArrayStorage<>(oracles);

        switch (policy) {
            case FIXED:
                this.executor = Executors.newFixedThreadPool(this.oracles.size() - 1);
                break;
            case CACHED:
                this.executor = Executors.newCachedThreadPool();
                break;
            default:
                throw new IllegalArgumentException("Illegal pool policy: " + policy);
        }
        this.minBatchSize = minBatchSize;
    }

    @Override
    public void processBatch(Collection<? extends Q> queries) {
        int num = queries.size();
        if (num == 0) {
            return;
        }

        int numBatches = (num - minBatchSize) / minBatchSize + 1;
        if (numBatches > oracles.size()) {
            numBatches = oracles.size();
        }

        // One batch is always executed in the local thread. This saves the thread creation
        // overhead for the common case where the batch size is quite small.
        int externalBatches = numBatches - 1;

        if (externalBatches == 0) {
            processQueriesLocally(queries);
            return;
        }

        // Calculate the number of full and non-full batches. The difference in size
        // will never exceed one (cf. pidgeonhole principle)
        int fullBatchSize = (num - 1) / numBatches + 1;
        int nonFullBatches = fullBatchSize * numBatches - num;

        List<Future<?>> futures = new ArrayList<>(externalBatches);

        Iterator<? extends Q> queryIt = queries.iterator();

        // Start the threads for the external batches
        for (int i = 0; i < externalBatches; i++) {
            int bs = fullBatchSize;
            if (i < nonFullBatches) {
                bs--;
            }
            List<Q> batch = new ArrayList<>(bs);
            for (int j = 0; j < bs; j++) {
                batch.add(queryIt.next());
            }

            Runnable job = new StaticQueriesJob<>(batch, oracles.get(i + 1));
            Future<?> future = executor.submit(job);
            futures.add(future);
        }

        // Finally, prepare and process the batch for the oracle executed in this thread.
        List<Q> localBatch = new ArrayList<>(fullBatchSize);
        for (int j = 0; j < fullBatchSize; j++) {
            localBatch.add(queryIt.next());
        }

        processQueriesLocally(localBatch);

        try {
            for (Future<?> f : futures) {
                f.get();
            }
        } catch (ExecutionException ex) {
            Throwables.throwIfUnchecked(ex.getCause());
            throw new AssertionError("Runnable must not throw checked exceptions", ex);
        } catch (InterruptedException ex) {
            Thread.interrupted();
            throw new BatchInterruptedException(ex);
        }
    }

    private void processQueriesLocally(Collection<? extends Q> localBatch) {
        oracles.get(0).processBatch(localBatch);
    }

    @Override
    public void shutdown() {
        executor.shutdown();
    }

    @Override
    public void shutdownNow() {
        executor.shutdownNow();
    }

    protected P getProcessor() {
        return oracles.get(0);
    }

}
