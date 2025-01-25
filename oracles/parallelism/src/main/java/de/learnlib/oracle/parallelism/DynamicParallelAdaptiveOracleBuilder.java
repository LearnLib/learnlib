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

import de.learnlib.oracle.AdaptiveMembershipOracle;
import de.learnlib.query.AdaptiveQuery;

/**
 * A specialized {@link AbstractDynamicBatchProcessorBuilder} for {@link AdaptiveMembershipOracle}s.
 *
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbol type
 */
public class DynamicParallelAdaptiveOracleBuilder<I, O>
        extends AbstractDynamicBatchProcessorBuilder<AdaptiveQuery<I, O>, AdaptiveMembershipOracle<I, O>, DynamicParallelAdaptiveOracle<I, O>> {

    public DynamicParallelAdaptiveOracleBuilder(Supplier<? extends AdaptiveMembershipOracle<I, O>> oracleSupplier) {
        super(oracleSupplier);
    }

    public DynamicParallelAdaptiveOracleBuilder(Collection<? extends AdaptiveMembershipOracle<I, O>> oracles) {
        super(oracles);
    }

    @Override
    protected DynamicParallelAdaptiveOracle<I, O> buildOracle(Supplier<? extends AdaptiveMembershipOracle<I, O>> supplier,
                                                              int batchSize,
                                                              ExecutorService executorService) {
        return new DynamicParallelAdaptiveOracle<>(supplier, batchSize, executorService);
    }
}
