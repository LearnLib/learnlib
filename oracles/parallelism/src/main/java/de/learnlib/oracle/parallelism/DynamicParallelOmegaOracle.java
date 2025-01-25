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

import de.learnlib.oracle.MembershipOracle;
import de.learnlib.oracle.OmegaMembershipOracle;
import de.learnlib.oracle.ParallelOmegaOracle;
import de.learnlib.query.OmegaQuery;
import net.automatalib.word.Word;
import org.checkerframework.checker.index.qual.NonNegative;

/**
 * A specialized {@link AbstractDynamicBatchProcessor} for {@link OmegaMembershipOracle}s that implements {@link
 * ParallelOmegaOracle}.
 *
 * @param <I>
 *         input symbol type
 * @param <D>
 *         output domain type
 */
public class DynamicParallelOmegaOracle<S, I, D>
        extends AbstractDynamicBatchProcessor<OmegaQuery<I, D>, OmegaMembershipOracle<S, I, D>>
        implements ParallelOmegaOracle<S, I, D> {

    public DynamicParallelOmegaOracle(Supplier<? extends OmegaMembershipOracle<S, I, D>> oracleSupplier,
                                      @NonNegative int batchSize,
                                      ExecutorService executor) {
        super(oracleSupplier, batchSize, executor);
    }

    @Override
    public void processQueries(Collection<? extends OmegaQuery<I, D>> omegaQueries) {
        processBatch(omegaQueries);
    }

    @Override
    public MembershipOracle<I, D> getMembershipOracle() {
        return getProcessor().getMembershipOracle();
    }

    @Override
    public boolean isSameState(Word<I> w1, S s1, Word<I> w2, S s2) {
        return getProcessor().isSameState(w1, s1, w2, s2);
    }
}
