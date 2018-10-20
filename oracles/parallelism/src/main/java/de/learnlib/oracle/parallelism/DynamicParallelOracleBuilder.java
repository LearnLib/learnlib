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

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.google.common.base.Preconditions;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.oracle.parallelism.ParallelOracle.PoolPolicy;
import net.automatalib.commons.util.array.ArrayStorage;
import net.automatalib.commons.util.concurrent.ScalingThreadPoolExecutor;

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
@ParametersAreNonnullByDefault
public class DynamicParallelOracleBuilder<I, D> {

    private static final int DEFAULT_KEEP_ALIVE_TIME = 60;

    private final Supplier<? extends MembershipOracle<I, D>> oracleSupplier;
    private final Collection<? extends MembershipOracle<I, D>> oracles;
    private ExecutorService customExecutor;
    @Nonnegative
    private int batchSize = DynamicParallelOracle.BATCH_SIZE;
    @Nonnegative
    private int poolSize = DynamicParallelOracle.POOL_SIZE;
    @Nonnull
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

    @Nonnull
    public DynamicParallelOracleBuilder<I, D> withCustomExecutor(ExecutorService executor) {
        this.customExecutor = executor;
        return this;
    }

    @Nonnull
    public DynamicParallelOracleBuilder<I, D> withBatchSize(int batchSize) {
        this.batchSize = batchSize;
        return this;
    }

    @Nonnull
    public DynamicParallelOracleBuilder<I, D> withPoolSize(@Nonnegative int poolSize) {
        this.poolSize = poolSize;
        return this;
    }

    @Nonnull
    public DynamicParallelOracleBuilder<I, D> withPoolPolicy(PoolPolicy policy) {
        this.poolPolicy = policy;
        return this;
    }

    @Nonnull
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
        public synchronized MembershipOracle<I, D> get() {
            if (idx < oracles.size()) {
                return oracles.get(idx++);
            }

            throw new IllegalStateException(
                    "The supplier should not have been called more than " + oracles.size() + " times");
        }
    }

}
