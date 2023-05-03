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
import java.util.function.Supplier;

import de.learnlib.api.oracle.OmegaMembershipOracle;
import de.learnlib.api.oracle.parallelism.ThreadPool.PoolPolicy;
import de.learnlib.api.query.OmegaQuery;

/**
 * A specialized {@link AbstractStaticBatchProcessorBuilder} for {@link OmegaMembershipOracle}s.
 *
 * @param <I>
 *         input symbol type
 * @param <D>
 *         output domain type
 */
public class StaticParallelOmegaOracleBuilder<S, I, D>
        extends AbstractStaticBatchProcessorBuilder<OmegaQuery<I, D>, OmegaMembershipOracle<S, I, D>, StaticParallelOmegaOracle<S, I, D>> {

    public StaticParallelOmegaOracleBuilder(Supplier<? extends OmegaMembershipOracle<S, I, D>> oracleSupplier) {
        super(oracleSupplier);
    }

    public StaticParallelOmegaOracleBuilder(Collection<? extends OmegaMembershipOracle<S, I, D>> oracles) {
        super(oracles);
    }

    @Override
    protected StaticParallelOmegaOracle<S, I, D> buildOracle(Collection<? extends OmegaMembershipOracle<S, I, D>> oracleInstances,
                                                             int minBatchSize,
                                                             PoolPolicy poolPolicy) {
        return new StaticParallelOmegaOracle<>(oracleInstances, minBatchSize, poolPolicy);
    }
}
