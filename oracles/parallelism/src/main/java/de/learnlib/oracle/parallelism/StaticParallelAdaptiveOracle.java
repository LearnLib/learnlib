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

import de.learnlib.oracle.AdaptiveMembershipOracle;
import de.learnlib.oracle.ParallelAdaptiveOracle;
import de.learnlib.query.AdaptiveQuery;
import org.checkerframework.checker.index.qual.NonNegative;

/**
 * A specialized {@link AbstractStaticBatchProcessor} for {@link AdaptiveMembershipOracle}s that implements
 * {@link ParallelAdaptiveOracle}.
 *
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbol type
 */
public class StaticParallelAdaptiveOracle<I, O>
        extends AbstractStaticBatchProcessor<AdaptiveQuery<I, O>, AdaptiveMembershipOracle<I, O>>
        implements ParallelAdaptiveOracle<I, O> {

    public StaticParallelAdaptiveOracle(Collection<? extends AdaptiveMembershipOracle<I, O>> oracles,
                                        @NonNegative int minBatchSize,
                                        ExecutorService executorService) {
        super(oracles, minBatchSize, executorService);
    }

    @Override
    public void processQueries(Collection<? extends AdaptiveQuery<I, O>> queries) {
        processBatch(queries);
    }
}
