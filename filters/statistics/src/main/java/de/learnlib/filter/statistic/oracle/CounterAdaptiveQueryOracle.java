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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.learnlib.oracle.AdaptiveMembershipOracle;
import de.learnlib.query.AdaptiveQuery;
import de.learnlib.query.AdaptiveQuery.Response;
import de.learnlib.statistic.Statistics;
import de.learnlib.statistic.StatisticsKey;
import de.learnlib.statistic.StatisticsService;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A simple wrapper for counting the number of {@link Response#RESET resets} and {@link Response#SYMBOL symbols} of an
 * {@link AdaptiveMembershipOracle}.
 *
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbol type
 */
public class CounterAdaptiveQueryOracle<I, O> implements AdaptiveMembershipOracle<I, O> {

    /**
     * The {@link StatisticsKey} this class uses for counting the number of {@link Response#RESET reset} and
     * {@link Response#FINISHED finished} queries executed on the membership oracle.
     */
    public static final StatisticsKey KEY_RESET = new StatisticsKey("amq-reset-cnt", "Number of resets");

    /**
     * The {@link StatisticsKey} this class uses for counting the number of {@link Response#SYMBOL symbols} contained in
     * the executed queries.
     */
    public static final StatisticsKey KEY_SYMBOL = new StatisticsKey("amq-sym-cnt", "Number of symbols");

    private final AdaptiveMembershipOracle<I, O> delegate;
    private final StatisticsService statistics;
    private final StatisticsKey keyReset;
    private final StatisticsKey keySymbol;

    /**
     * Convenience constructor for
     * {@link CounterAdaptiveQueryOracle#CounterAdaptiveQueryOracle(AdaptiveMembershipOracle, String)} which uses
     * {@code null} as {@code id}.
     *
     * @param delegate
     *         the oracle to delegate calls to
     */
    public CounterAdaptiveQueryOracle(AdaptiveMembershipOracle<I, O> delegate) {
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
    public CounterAdaptiveQueryOracle(AdaptiveMembershipOracle<I, O> delegate, @Nullable String id) {
        this.delegate = delegate;
        this.statistics = Statistics.getService();
        this.keyReset = KEY_RESET.withId(id);
        this.keySymbol = KEY_SYMBOL.withId(id);
    }

    @Override
    public void processQueries(Collection<? extends AdaptiveQuery<I, O>> queries) {
        final List<CountingQuery<I, O>> wrappers = new ArrayList<>(queries.size());
        for (AdaptiveQuery<I, O> q : queries) {
            wrappers.add(new CountingQuery<>(q));
        }

        this.delegate.processQueries(wrappers);

        // aggregate locally to prevent synchronization overhead
        long numResets = 0;
        long numSymbols = 0;
        for (CountingQuery<I, O> wrapper : wrappers) {
            numResets += wrapper.resets;
            numSymbols += wrapper.symbols;
        }

        this.statistics.increaseCounter(keyReset, numResets, this);
        this.statistics.increaseCounter(keySymbol, numSymbols, this);
    }

    private static class CountingQuery<I, O> implements AdaptiveQuery<I, O> {

        private final AdaptiveQuery<I, O> delegate;
        private int symbols, resets;

        CountingQuery(AdaptiveQuery<I, O> delegate) {
            this.delegate = delegate;
        }

        @Override
        public I getInput() {
            return delegate.getInput();
        }

        @Override
        public Response processOutput(O out) {
            symbols++;

            final Response response = delegate.processOutput(out);

            if (response != Response.SYMBOL) {
                resets++;
            }

            return response;
        }
    }
}
