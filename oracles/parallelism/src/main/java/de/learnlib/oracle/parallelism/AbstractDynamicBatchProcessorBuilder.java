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

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import de.learnlib.oracle.BatchProcessor;
import de.learnlib.oracle.ThreadPool.PoolPolicy;
import net.automatalib.common.util.array.ArrayStorage;
import net.automatalib.common.util.concurrent.ScalingThreadPoolExecutor;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Builder class for a {@link AbstractDynamicBatchProcessor}.
 *
 * @param <Q>
 *         query type
 * @param <P>
 *         (sub-) processor type
 * @param <OR>
 *         constructed oracle type
 */
public abstract class AbstractDynamicBatchProcessorBuilder<Q, P extends BatchProcessor<Q>, OR> {

    private static final int DEFAULT_KEEP_ALIVE_TIME = 60;

    private final @Nullable Supplier<? extends P> oracleSupplier;
    private final @Nullable Collection<? extends P> oracles;

    private ExecutorService customExecutor;
    private @NonNegative int batchSize = BatchProcessorDefaults.BATCH_SIZE;
    private @NonNegative int poolSize = BatchProcessorDefaults.POOL_SIZE;
    private PoolPolicy poolPolicy = BatchProcessorDefaults.POOL_POLICY;

    public AbstractDynamicBatchProcessorBuilder(Supplier<? extends P> oracleSupplier) {
        this.oracleSupplier = oracleSupplier;
        this.oracles = null;
    }

    public AbstractDynamicBatchProcessorBuilder(Collection<? extends P> oracles) {
        this.oracles = oracles;
        this.oracleSupplier = null;
    }

    /**
     * Sets the size of batches that are submitted.
     *
     * @param batchSize
     *         the minimal size of batches
     *
     * @return {@code this}
     */
    public AbstractDynamicBatchProcessorBuilder<Q, P, OR> withBatchSize(int batchSize) {
        this.batchSize = batchSize;
        return this;
    }

    /**
     * Sets the executor service to use for submitting batches. Note that if the builder is initialized with a
     * collection of processors, an exception may be thrown if the thread pool tries to spawn more threads than oracles
     * are available.
     *
     * @param executor
     *         the executor to use
     *
     * @return {@code this}
     */
    public AbstractDynamicBatchProcessorBuilder<Q, P, OR> withCustomExecutor(ExecutorService executor) {
        this.customExecutor = executor;
        return this;
    }

    /**
     * Sets the pool policy in case the builder creates its own executor for processing batches.
     *
     * @param policy
     *         the policy
     *
     * @return {@code this}
     */
    public AbstractDynamicBatchProcessorBuilder<Q, P, OR> withPoolPolicy(PoolPolicy policy) {
        this.poolPolicy = policy;
        return this;
    }

    /**
     * Sets the number of instances that should process batches. Note that this value is ignored if the builder has been
     * initialized with a collection of processors in order to guarantee that no unavailable resources are accessed.
     *
     * @param poolSize
     *         the number of instances to delegate batches to
     *
     * @return {@code this}
     */
    public AbstractDynamicBatchProcessorBuilder<Q, P, OR> withPoolSize(@NonNegative int poolSize) {
        this.poolSize = poolSize;
        return this;
    }

    /**
     * Create the batch processor.
     *
     * @return the batch processor
     */
    public OR create() {
        final Supplier<? extends P> supplier;
        final int size;

        if (oracleSupplier == null) {
            if (oracles == null || oracles.isEmpty()) {
                throw new IllegalArgumentException("No oracles specified");
            }

            size = oracles.size();
            supplier = new StaticOracleProvider<>(oracles);
        } else {
            size = poolSize;
            supplier = oracleSupplier;
        }

        final ExecutorService executor;

        if (customExecutor != null) {
            executor = customExecutor;
        } else {
            switch (poolPolicy) {
                case FIXED:
                    executor = Executors.newFixedThreadPool(size);
                    break;
                case CACHED:
                    executor = new ScalingThreadPoolExecutor(0, size, DEFAULT_KEEP_ALIVE_TIME, TimeUnit.SECONDS);
                    break;
                default:
                    throw new IllegalStateException("Unknown pool policy: " + poolPolicy);
            }
        }

        return buildOracle(supplier, batchSize, executor);
    }

    protected abstract OR buildOracle(Supplier<? extends P> supplier, int batchSize, ExecutorService executorService);

    static class StaticOracleProvider<P extends BatchProcessor<?>> implements Supplier<P> {

        private final ArrayStorage<P> oracles;
        private final Lock lock;
        private int idx;

        StaticOracleProvider(P[] oracles) {
            this(Arrays.asList(oracles));
        }

        StaticOracleProvider(Collection<? extends P> oracles) {
            this.oracles = new ArrayStorage<>(oracles);
            this.lock = new ReentrantLock();
        }

        @Override
        public P get() {
            try {
                lock.lock();
                if (idx < oracles.size()) {
                    return oracles.get(idx++);
                }
            } finally {
                lock.unlock();
            }

            throw new IllegalStateException(
                    "The executor service tried to spawn more threads than there are oracles available (" +
                    oracles.size() + ')');
        }
    }
}
