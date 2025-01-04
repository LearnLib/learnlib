/* Copyright (C) 2013-2024 TU Dortmund University
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import de.learnlib.oracle.BatchProcessor;
import de.learnlib.oracle.ThreadPool.PoolPolicy;
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
    private @NonNegative int batchSize = AbstractDynamicBatchProcessor.BATCH_SIZE;
    private @NonNegative int poolSize = AbstractDynamicBatchProcessor.POOL_SIZE;
    private PoolPolicy poolPolicy = AbstractDynamicBatchProcessor.POOL_POLICY;

    public AbstractDynamicBatchProcessorBuilder(Supplier<? extends P> oracleSupplier) {
        this.oracleSupplier = oracleSupplier;
        this.oracles = null;
    }

    public AbstractDynamicBatchProcessorBuilder(Collection<? extends P> oracles) {
        this(validateInputs(oracles), oracles);
    }

    // utility constructor to prevent finalizer attacks, see SEI CERT Rule OBJ-11
    @SuppressWarnings("PMD.UnusedFormalParameter")
    private AbstractDynamicBatchProcessorBuilder(boolean valid, Collection<? extends P> oracles) {
        this.oracles = oracles;
        this.oracleSupplier = null;
    }

    private static boolean validateInputs(Collection<?> oracles) {
        if (oracles.isEmpty()) {
            throw new IllegalArgumentException("No oracles specified");
        }
        return true;
    }

    public AbstractDynamicBatchProcessorBuilder<Q, P, OR> withCustomExecutor(ExecutorService executor) {
        this.customExecutor = executor;
        return this;
    }

    public AbstractDynamicBatchProcessorBuilder<Q, P, OR> withBatchSize(int batchSize) {
        this.batchSize = batchSize;
        return this;
    }

    public AbstractDynamicBatchProcessorBuilder<Q, P, OR> withPoolSize(@NonNegative int poolSize) {
        this.poolSize = poolSize;
        return this;
    }

    public AbstractDynamicBatchProcessorBuilder<Q, P, OR> withPoolPolicy(PoolPolicy policy) {
        this.poolPolicy = policy;
        return this;
    }

    public OR create() {

        final Supplier<? extends P> supplier;
        final ExecutorService executor;

        if (oracles != null) {
            executor = Executors.newFixedThreadPool(oracles.size());
            supplier = new StaticOracleProvider<>(oracles);
        } else if (customExecutor != null) {
            executor = customExecutor;
            supplier = oracleSupplier;
        } else {
            switch (poolPolicy) {
                case FIXED:
                    executor = Executors.newFixedThreadPool(poolSize);
                    break;
                case CACHED:
                    executor = new ScalingThreadPoolExecutor(0, poolSize, DEFAULT_KEEP_ALIVE_TIME, TimeUnit.SECONDS);
                    break;
                default:
                    throw new IllegalStateException("Unknown pool policy: " + poolPolicy);
            }
            supplier = oracleSupplier;
        }

        return buildOracle(supplier, batchSize, executor);
    }

    protected abstract OR buildOracle(Supplier<? extends P> supplier, int batchSize, ExecutorService executorService);

    static class StaticOracleProvider<P extends BatchProcessor<?>> implements Supplier<P> {

        private final P[] oracles;
        private final Lock lock;
        private int idx;

        @SuppressWarnings("unchecked")
        StaticOracleProvider(Collection<? extends P> oracles) {
            this(oracles.toArray((P[]) new BatchProcessor[oracles.size()]));
        }

        StaticOracleProvider(P[] oracles) {
            this.oracles = oracles;
            this.lock = new ReentrantLock();
        }

        @Override
        public P get() {
            try {
                lock.lock();
                if (idx < oracles.length) {
                    return oracles[idx++];
                }
            } finally {
                lock.unlock();
            }

            throw new IllegalStateException(
                    "The supplier should not have been called more than " + oracles.length + " times");
        }
    }
}
