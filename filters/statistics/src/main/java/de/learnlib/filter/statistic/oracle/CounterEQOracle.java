/* Copyright (C) 2013-2026 TU Dortmund University
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
package de.learnlib.filter.statistic.oracle;

import java.util.Collection;

import de.learnlib.oracle.EquivalenceOracle;
import de.learnlib.query.DefaultQuery;
import de.learnlib.query.Query;
import de.learnlib.statistic.Statistics;
import de.learnlib.statistic.StatisticsKey;
import de.learnlib.statistic.StatisticsService;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Wrapper for a {@link EquivalenceOracle} that gathers various statistics on queries sent to this oracle.
 *
 * @param <A>
 *         automaton type
 * @param <I>
 *         input symbol type
 * @param <D>
 *         output domain type
 */
public class CounterEQOracle<A, I, D> implements EquivalenceOracle<A, I, D> {

    /**
     * The {@link StatisticsKey} this class uses for counting the number of
     * {@link EquivalenceOracle#findCounterExample(Object, Collection) found counterexamples} of the equivalence
     * oracle.
     */
    public static final StatisticsKey KEY_COUNT = new StatisticsKey("cex-cnt", "Number of found counterexamples");

    /**
     * The {@link StatisticsKey} this class uses for counting the number of {@link Query#length() symbols} contained in
     * the found counterexamples.
     */
    public static final StatisticsKey KEY_LEN = new StatisticsKey("cex-len", "Length (cumulated) of counterexamples");

    private final EquivalenceOracle<A, I, D> delegate;
    private final StatisticsService statistics;
    private final StatisticsKey keyCount;
    private final StatisticsKey keyLen;

    /**
     * Convenience constructor for {@link CounterEQOracle#CounterEQOracle(EquivalenceOracle, String)} which uses
     * {@code null} as {@code id}.
     *
     * @param delegate
     *         the oracle to delegate calls to
     */
    public CounterEQOracle(EquivalenceOracle<A, I, D> delegate) {
        this(delegate, null);
    }

    /**
     * Constructs a new counter oracle that writes statistical data to a {@link StatisticsService}. The provided
     * {@code id} is used to refine the supported {@link StatisticsKey}s and allows for using multiple instances of this
     * class for different purposes.
     *
     * @param delegate
     *         the oracle to delegate calls to
     * @param id
     *         the id used for specialising the statistics keys
     */
    public CounterEQOracle(EquivalenceOracle<A, I, D> delegate, @Nullable String id) {
        this.delegate = delegate;
        this.statistics = Statistics.getService();
        this.keyCount = KEY_COUNT.withId(id);
        this.keyLen = KEY_LEN.withId(id);
    }

    @Override
    public @Nullable DefaultQuery<I, D> findCounterExample(A hypothesis, Collection<? extends I> inputs) {
        final DefaultQuery<I, D> cex = this.delegate.findCounterExample(hypothesis, inputs);
        if (cex != null) {
            statistics.increaseCounter(keyLen, cex.length(), this);
            statistics.increaseCounter(keyCount, this);
        }
        return cex;
    }
}
