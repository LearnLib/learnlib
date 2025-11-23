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
package de.learnlib.filter.statistic.oracle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.learnlib.oracle.AdaptiveMembershipOracle;
import de.learnlib.query.AdaptiveQuery;
import de.learnlib.query.AdaptiveQuery.Response;
import de.learnlib.statistic.Statistics;
import de.learnlib.statistic.StatisticsCollector;

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

    public static final String DUR_KEY = "amq-qry-dur";
    public static final String RESET_KEY = "amq-reset-cnt";
    public static final String SYMBOL_KEY = "amq-sym-cnt";

    private final AdaptiveMembershipOracle<I, O> delegate;
    private final StatisticsCollector statisticsCollector;
    private final String id;

    public CounterAdaptiveQueryOracle(AdaptiveMembershipOracle<I, O> delegate) {
        this(delegate, "");
    }

    public CounterAdaptiveQueryOracle(AdaptiveMembershipOracle<I, O> delegate, String id) {
        this.delegate = delegate;
        this.id = id;
        this.statisticsCollector = Statistics.getCollector();
    }

    @Override
    public void processQueries(Collection<? extends AdaptiveQuery<I, O>> queries) {
        final List<CountingQuery<I, O>> wrappers = new ArrayList<>(queries.size());
        for (AdaptiveQuery<I, O> q : queries) {
            wrappers.add(new CountingQuery<>(q));
        }

        statisticsCollector.startOrResumeClock(DUR_KEY + id, "Duration of queries");
        this.delegate.processQueries(wrappers);
        statisticsCollector.pauseClock(DUR_KEY + id);

        // statContainer is not thread-safe so we need to count in post-processing
        for (CountingQuery<I, O> wrapper : wrappers) {
            this.statisticsCollector.increaseCounter(RESET_KEY + id, "Number of resets", wrapper.resets);
            this.statisticsCollector.increaseCounter(SYMBOL_KEY + id, "Number of symbols", wrapper.symbols);
        }
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
