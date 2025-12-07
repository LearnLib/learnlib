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

import de.learnlib.oracle.TimedQueryOracle;
import de.learnlib.query.Query;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.symbol.time.TimedOutput;
import net.automatalib.word.Word;

/**
 * A specialized {@link AbstractStaticBatchProcessorBuilder} for {@link TimedQueryOracle}s.
 *
 * @param <I>
 *         input symbol type (of non-delaying inputs)
 * @param <O>
 *         output symbol type
 */
public class StaticParallelTimedQueryOracleBuilder<I, O>
        extends AbstractStaticBatchProcessorBuilder<Query<TimedInput<I>, Word<TimedOutput<O>>>, TimedQueryOracle<I, O>, StaticParallelTimedQueryOracle<I, O>> {

    public StaticParallelTimedQueryOracleBuilder(Supplier<? extends TimedQueryOracle<I, O>> oracleSupplier) {
        super(oracleSupplier);
    }

    public StaticParallelTimedQueryOracleBuilder(Collection<? extends TimedQueryOracle<I, O>> oracles) {
        super(oracles);
    }

    @Override
    protected StaticParallelTimedQueryOracle<I, O> buildOracle(Collection<? extends TimedQueryOracle<I, O>> oracleInstances,
                                                               int minBatchSize,
                                                               ExecutorService executor) {
        return new StaticParallelTimedQueryOracle<>(oracleInstances, minBatchSize, executor);
    }
}
