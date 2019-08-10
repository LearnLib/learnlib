/* Copyright (C) 2013-2019 TU Dortmund
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
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.oracle.parallelism.ParallelOracle.PoolPolicy;
import net.automatalib.commons.smartcollections.ArrayStorage;
import net.automatalib.commons.util.concurrent.ScalingThreadPoolExecutor;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Builder class for a {@link DynamicParallelOracle}.
 *
 * @param <I>
 *         input symbol type
 * @param <D>
 *         output type
 *
 * @author Malte Isberner
 */
public class DynamicParallelOracleBuilder<I, D> {

    private static final int DEFAULT_KEEP_ALIVE_TIME = 60;

    private final Supplier<? extends MembershipOracle<I, D>> oracleSupplier;
    private final Collection<? extends MembershipOracle<I, D>> oracles;
    private ExecutorService customExecutor;
    @NonNegative
    private int batchSize = DynamicParallelOracle.BATCH_SIZE;
    @NonNegative
    private int poolSize = DynamicParallelOracle.POOL_SIZE;
    @NonNull
    private PoolPolicy poolPolicy = DynamicParallelOracle.POOL_POLICY;

    public DynamicParallelOracleBuilder(Supplier<? extends MembershipOracle<I, D>> oracleSupplier) {
        this.oracleSupplier = oracleSupplier;
        this.oracles = null;
    }

    public DynamicParallelOracleBuilder(Collection<? extends MembershipOracle<I, D>> oracles) {
        Preconditions.checkArgument(!oracles.isEmpty(), "No oracles specified");
        this.oracles = oracles;
        this.oracleSupplier = null;
    }

    @NonNull
    public DynamicParallelOracleBuilder<I, D> withCustomExecutor(ExecutorService executor) {
        this.customExecutor = executor;
        return this;
    }

    @NonNull
    public DynamicParallelOracleBuilder<I, D> withBatchSize(int batchSize) {
        this.batchSize = batchSize;
        return this;
    }

    @NonNull
    public DynamicParallelOracleBuilder<I, D> withPoolSize(@NonNegative int poolSize) {
        this.poolSize = poolSize;
        return this;
    }

    @NonNull
    public DynamicParallelOracleBuilder<I, D> withPoolPolicy(PoolPolicy policy) {
        this.poolPolicy = policy;
        return this;
    }

    @NonNull
    public DynamicParallelOracle<I, D> create() {

        final Supplier<? extends MembershipOracle<I, D>> supplier;
        final ExecutorService executor;

        if (oracles != null) {
            executor = Executors.newFixedThreadPool(oracles.size());
            supplier = new StaticOracleProvider<>(new ArrayStorage<>(oracles));
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

        return new DynamicParallelOracle<>(supplier, batchSize, executor);
    }

    static class StaticOracleProvider<I, D> implements Supplier<MembershipOracle<I, D>> {

        private final ArrayStorage<MembershipOracle<I, D>> oracles;
        private int idx;

        StaticOracleProvider(ArrayStorage<MembershipOracle<I, D>> oracles) {
            this.oracles = oracles;
        }

        StaticOracleProvider(Collection<? extends MembershipOracle<I, D>> oracles) {
            this.oracles = new ArrayStorage<>(oracles.size());
            int idx = 0;
            for (final MembershipOracle<I, D> oracle : oracles) {
                this.oracles.set(idx++, oracle);
            }
        }

        @Override
        public MembershipOracle<I, D> get() {
            synchronized (this) {
                if (idx < oracles.size()) {
                    return oracles.get(idx++);
                }
            }

            throw new IllegalStateException(
                    "The supplier should not have been called more than " + oracles.size() + " times");
        }
    }

}
