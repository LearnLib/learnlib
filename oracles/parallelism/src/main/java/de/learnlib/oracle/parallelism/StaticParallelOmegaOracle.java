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

import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.oracle.OmegaMembershipOracle;
import de.learnlib.api.oracle.parallelism.ParallelOmegaOracle;
import de.learnlib.api.query.OmegaQuery;
import net.automatalib.words.Word;
import org.checkerframework.checker.index.qual.NonNegative;

/**
 * A specialized {@link AbstractStaticBatchProcessor} for {@link OmegaMembershipOracle}s that implements {@link
 * ParallelOmegaOracle}.
 *
 * @param <I>
 *         input symbol type
 * @param <D>
 *         output domain type
 */
public class StaticParallelOmegaOracle<S, I, D>
        extends AbstractStaticBatchProcessor<OmegaQuery<I, D>, OmegaMembershipOracle<S, I, D>>
        implements ParallelOmegaOracle<S, I, D> {

    public StaticParallelOmegaOracle(Collection<? extends OmegaMembershipOracle<S, I, D>> oracles,
                                     @NonNegative int minBatchSize,
                                     PoolPolicy policy) {
        super(oracles, minBatchSize, policy);
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
