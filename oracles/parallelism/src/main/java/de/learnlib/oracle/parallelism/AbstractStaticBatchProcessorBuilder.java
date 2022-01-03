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
import java.util.function.Supplier;

import com.google.common.base.Preconditions;
import de.learnlib.api.oracle.parallelism.BatchProcessor;
import de.learnlib.api.oracle.parallelism.ThreadPool.PoolPolicy;
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
 *
 * @author Malte Isberner
 */
public abstract class AbstractStaticBatchProcessorBuilder<Q, P extends BatchProcessor<Q>, OR> {

    private final @Nullable Collection<? extends P> oracles;
    private final @Nullable Supplier<? extends P> oracleSupplier;
    private @NonNegative int minBatchSize = AbstractStaticBatchProcessor.MIN_BATCH_SIZE;
    private @NonNegative int numInstances = AbstractStaticBatchProcessor.NUM_INSTANCES;
    private PoolPolicy poolPolicy = AbstractStaticBatchProcessor.POOL_POLICY;

    public AbstractStaticBatchProcessorBuilder(Collection<? extends P> oracles) {
        Preconditions.checkArgument(!oracles.isEmpty(), "No oracles specified");
        this.oracles = oracles;
        this.oracleSupplier = null;
    }

    public AbstractStaticBatchProcessorBuilder(Supplier<? extends P> oracleSupplier) {
        this.oracles = null;
        this.oracleSupplier = oracleSupplier;
    }

    public AbstractStaticBatchProcessorBuilder<Q, P, OR> withMinBatchSize(@NonNegative int minBatchSize) {
        this.minBatchSize = minBatchSize;
        return this;
    }

    public AbstractStaticBatchProcessorBuilder<Q, P, OR> withPoolPolicy(PoolPolicy policy) {
        this.poolPolicy = policy;
        return this;
    }

    public AbstractStaticBatchProcessorBuilder<Q, P, OR> withNumInstances(@NonNegative int numInstances) {
        this.numInstances = numInstances;
        return this;
    }

    @SuppressWarnings("nullness") // the constructors guarantee that oracles and oracleSupplier are null exclusively
    public OR create() {
        Collection<? extends P> oracleInstances;
        if (oracles != null) {
            oracleInstances = oracles;
        } else {
            List<P> oracleList = new ArrayList<>(numInstances);
            for (int i = 0; i < numInstances; i++) {
                oracleList.add(oracleSupplier.get());
            }
            oracleInstances = oracleList;
        }

        return buildOracle(oracleInstances, minBatchSize, poolPolicy);
    }

    protected abstract OR buildOracle(Collection<? extends P> oracleInstances, int minBatchSize, PoolPolicy poolPolicy);

}
