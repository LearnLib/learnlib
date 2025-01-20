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
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import de.learnlib.exception.BatchInterruptedException;
import de.learnlib.oracle.BatchProcessor;
import de.learnlib.oracle.ThreadPool;
import net.automatalib.common.util.collection.CollectionUtil;
import net.automatalib.common.util.exception.ExceptionUtil;
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

    private final @NonNegative int minBatchSize;
    private final List<? extends P> oracles;
    private final ExecutorService executor;

    public AbstractStaticBatchProcessor(Collection<? extends P> oracles,
                                        @NonNegative int minBatchSize,
                                        ExecutorService executor) {
        this.oracles = CollectionUtil.randomAccessList(oracles);
        this.minBatchSize = minBatchSize;
        this.executor = executor;
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
        } catch (ExecutionException e) {
            ExceptionUtil.throwIfUnchecked(e.getCause());
            throw new AssertionError("Runnable must not throw checked exceptions", e);
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
