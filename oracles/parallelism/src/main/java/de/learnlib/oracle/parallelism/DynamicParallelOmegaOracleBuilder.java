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
import java.util.function.Supplier;

import de.learnlib.oracle.OmegaMembershipOracle;
import de.learnlib.query.OmegaQuery;

/**
 * A specialized {@link AbstractDynamicBatchProcessorBuilder} for {@link OmegaMembershipOracle}s.
 *
 * @param <S>
 *         oracle state type
 * @param <I>
 *         input symbol type
 * @param <D>
 *         output domain type
 */
public class DynamicParallelOmegaOracleBuilder<S, I, D>
        extends AbstractDynamicBatchProcessorBuilder<OmegaQuery<I, D>, OmegaMembershipOracle<S, I, D>, DynamicParallelOmegaOracle<S, I, D>> {

    public DynamicParallelOmegaOracleBuilder(Supplier<? extends OmegaMembershipOracle<S, I, D>> oracleSupplier) {
        super(oracleSupplier);
    }

    public DynamicParallelOmegaOracleBuilder(Collection<? extends OmegaMembershipOracle<S, I, D>> oracles) {
        super(oracles);
    }

    @Override
    protected DynamicParallelOmegaOracle<S, I, D> buildOracle(Supplier<? extends OmegaMembershipOracle<S, I, D>> supplier,
                                                              int batchSize,
                                                              ExecutorService executorService) {
        return new DynamicParallelOmegaOracle<>(supplier, batchSize, executorService);
    }
}
