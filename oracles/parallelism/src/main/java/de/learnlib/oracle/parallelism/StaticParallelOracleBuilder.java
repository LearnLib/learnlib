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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.oracle.parallelism.ParallelOracle.PoolPolicy;

/**
 * A builder for a {@link StaticParallelOracle}.
 *
 * @param <I>
 *         input symbol type
 * @param <D>
 *         output type
 *
 * @author Malte Isberner
 */
@ParametersAreNonnullByDefault
public class StaticParallelOracleBuilder<I, D> {

    private final Collection<? extends MembershipOracle<I, D>> oracles;
    private final Supplier<? extends MembershipOracle<I, D>> oracleSupplier;
    @Nonnegative
    private int minBatchSize = StaticParallelOracle.MIN_BATCH_SIZE;
    @Nonnegative
    private int numInstances = StaticParallelOracle.NUM_INSTANCES;
    @Nonnull
    private PoolPolicy poolPolicy = StaticParallelOracle.POOL_POLICY;

    public StaticParallelOracleBuilder(Collection<? extends MembershipOracle<I, D>> oracles) {
        this.oracles = oracles;
        this.oracleSupplier = null;
    }

    public StaticParallelOracleBuilder(Supplier<? extends MembershipOracle<I, D>> oracleSupplier) {
        this.oracles = null;
        this.oracleSupplier = oracleSupplier;
    }

    @Nonnull
    public StaticParallelOracleBuilder<I, D> withDefaultMinBatchSize() {
        this.minBatchSize = StaticParallelOracle.MIN_BATCH_SIZE;
        return this;
    }

    @Nonnull
    public StaticParallelOracleBuilder<I, D> withMinBatchSize(@Nonnegative int minBatchSize) {
        this.minBatchSize = minBatchSize;
        return this;
    }

    @Nonnull
    public StaticParallelOracleBuilder<I, D> withDefaultPoolPolicy() {
        this.poolPolicy = StaticParallelOracle.POOL_POLICY;
        return this;
    }

    @Nonnull
    public StaticParallelOracleBuilder<I, D> withPoolPolicy(PoolPolicy policy) {
        this.poolPolicy = policy;
        return this;
    }

    @Nonnull
    public StaticParallelOracleBuilder<I, D> withDefaultNumInstances() {
        this.numInstances = StaticParallelOracle.NUM_INSTANCES;
        return this;
    }

    @Nonnull
    public StaticParallelOracleBuilder<I, D> withNumInstances(@Nonnegative int numInstances) {
        this.numInstances = numInstances;
        return this;
    }

    @Nonnull
    public StaticParallelOracle<I, D> create() {
        Collection<? extends MembershipOracle<I, D>> oracleInstances;
        if (oracles != null) {
            oracleInstances = oracles;
        } else {
            List<MembershipOracle<I, D>> oracleList = new ArrayList<>(numInstances);
            for (int i = 0; i < numInstances; i++) {
                oracleList.add(oracleSupplier.get());
            }
            oracleInstances = oracleList;
        }

        return new StaticParallelOracle<>(oracleInstances, minBatchSize, poolPolicy);
    }

}
