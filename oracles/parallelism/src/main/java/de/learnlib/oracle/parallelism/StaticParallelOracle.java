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

import de.learnlib.oracle.MembershipOracle;
import de.learnlib.oracle.ParallelOracle;
import de.learnlib.query.Query;
import org.checkerframework.checker.index.qual.NonNegative;

/**
 * A specialized {@link AbstractStaticBatchProcessor} for {@link MembershipOracle}s that implements {@link
 * ParallelOracle}.
 *
 * @param <I>
 *         input symbol type
 * @param <D>
 *         output domain type
 */
public class StaticParallelOracle<I, D> extends AbstractStaticBatchProcessor<Query<I, D>, MembershipOracle<I, D>>
        implements ParallelOracle<I, D> {

    public StaticParallelOracle(Collection<? extends MembershipOracle<I, D>> oracles,
                                @NonNegative int minBatchSize,
                                ExecutorService executorService) {
        super(oracles, minBatchSize, executorService);
    }

    @Override
    public void processQueries(Collection<? extends Query<I, D>> queries) {
        processBatch(queries);
    }
}
