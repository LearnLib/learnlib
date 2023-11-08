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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import com.google.common.base.Preconditions;
import de.learnlib.oracle.parallelism.ThreadPool.PoolPolicy;
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
        Preconditions.checkArgument(!oracles.isEmpty(), "No oracles specified");
        this.oracles = oracles;
        this.oracleSupplier = null;
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

    @SuppressWarnings("PMD.CloseResource") // false positive on JDK21 builds
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
        private int idx;

        StaticOracleProvider(P[] oracles) {
            this.oracles = oracles;
        }

        @SuppressWarnings("unchecked")
        StaticOracleProvider(Collection<? extends P> oracles) {
            this.oracles = oracles.toArray((P[]) new BatchProcessor[oracles.size()]);
        }

        @Override
        public P get() {
            synchronized (this) {
                if (idx < oracles.length) {
                    return oracles[idx++];
                }
            }

            throw new IllegalStateException(
                    "The supplier should not have been called more than " + oracles.length + " times");
        }
    }
}
