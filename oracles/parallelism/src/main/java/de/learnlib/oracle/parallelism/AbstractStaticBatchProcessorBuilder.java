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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import de.learnlib.oracle.BatchProcessor;
import de.learnlib.oracle.ThreadPool.PoolPolicy;
import net.automatalib.common.util.array.ArrayStorage;
import net.automatalib.common.util.concurrent.ScalingThreadPoolExecutor;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A builder for a {@link AbstractStaticBatchProcessor}.
 *
 * @param <Q>
 *         query type
 * @param <P>
 *         (sub-) processor type
 * @param <OR>
 *         constructed oracle type
 */
public abstract class AbstractStaticBatchProcessorBuilder<Q, P extends BatchProcessor<Q>, OR> {

    private static final int DEFAULT_KEEP_ALIVE_TIME = 60;

    private final @Nullable Collection<? extends P> oracles;
    private final @Nullable Supplier<? extends P> oracleSupplier;

    private ExecutorService customExecutor;
    private @NonNegative int minBatchSize = BatchProcessorDefaults.MIN_BATCH_SIZE;
    private @NonNegative int numInstances = BatchProcessorDefaults.POOL_SIZE;
    private PoolPolicy poolPolicy = BatchProcessorDefaults.POOL_POLICY;

    public AbstractStaticBatchProcessorBuilder(Supplier<? extends P> oracleSupplier) {
        this.oracles = null;
        this.oracleSupplier = oracleSupplier;
    }

    public AbstractStaticBatchProcessorBuilder(Collection<? extends P> oracles) {
        this.oracles = oracles;
        this.oracleSupplier = null;
    }

    /**
     * Sets the executor service to use for submitting batches.
     *
     * @param executor
     *         the executor to use
     *
     * @return {@code this}
     */
    public AbstractStaticBatchProcessorBuilder<Q, P, OR> withCustomExecutor(ExecutorService executor) {
        this.customExecutor = executor;
        return this;
    }

    /**
     * Sets the minimal size of batches that are submitted.
     *
     * @param minBatchSize
     *         the minimal size of batches
     *
     * @return {@code this}
     */
    public AbstractStaticBatchProcessorBuilder<Q, P, OR> withMinBatchSize(@NonNegative int minBatchSize) {
        this.minBatchSize = minBatchSize;
        return this;
    }

    /**
     * Sets the number of instances that should process batches. Note that this value is ignored if the builder has been
     * initialized with a collection of processors in order to guarantee that no unavailable resources are accessed.
     *
     * @param numInstances
     *         the number of instances to delegate batches to
     *
     * @return {@code this}
     */
    public AbstractStaticBatchProcessorBuilder<Q, P, OR> withNumInstances(@NonNegative int numInstances) {
        this.numInstances = numInstances;
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
    public AbstractStaticBatchProcessorBuilder<Q, P, OR> withPoolPolicy(PoolPolicy policy) {
        this.poolPolicy = policy;
        return this;
    }

    /**
     * Create the batch processor.
     *
     * @return the batch processor
     */
    public OR create() {
        final ArrayStorage<P> instances;
        final int size;

        if (oracleSupplier == null) {
            if (oracles == null || oracles.isEmpty()) {
                throw new IllegalArgumentException("No oracles specified");
            }

            size = oracles.size();
            instances = new ArrayStorage<>(oracles);
        } else {
            size = numInstances;
            instances = new ArrayStorage<>(size);
            for (int i = 0; i < size; i++) {
                instances.set(i, oracleSupplier.get());
            }
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

        return buildOracle(instances, minBatchSize, executor);
    }

    protected abstract OR buildOracle(Collection<? extends P> oracleInstances,
                                      int minBatchSize,
                                      ExecutorService executor);

}
