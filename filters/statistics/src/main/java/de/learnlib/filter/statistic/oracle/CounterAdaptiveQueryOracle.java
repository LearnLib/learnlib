/* Copyright (C) 2013-2024 TU Dortmund University
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
package de.learnlib.filter.statistic.oracle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import de.learnlib.oracle.AdaptiveMembershipOracle;
import de.learnlib.query.AdaptiveQuery;
import de.learnlib.query.AdaptiveQuery.Response;

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

    private final AdaptiveMembershipOracle<I, O> delegate;
    private final AtomicLong resetCounter = new AtomicLong();
    private final AtomicLong symbolCounter = new AtomicLong();

    public CounterAdaptiveQueryOracle(AdaptiveMembershipOracle<I, O> delegate) {
        this.delegate = delegate;
    }

    public long getResetCount() {
        return resetCounter.get();
    }

    public long getSymbolCount() {
        return symbolCounter.get();
    }

    @Override
    public void processQueries(Collection<? extends AdaptiveQuery<I, O>> queries) {
        final List<CountingQuery> wrappers = new ArrayList<>(queries.size());
        for (AdaptiveQuery<I, O> q : queries) {
            wrappers.add(new CountingQuery(q));
        }

        this.delegate.processQueries(wrappers);

    }

    private class CountingQuery implements AdaptiveQuery<I, O> {

        private final AdaptiveQuery<I, O> delegate;

        CountingQuery(AdaptiveQuery<I, O> delegate) {
            this.delegate = delegate;
        }

        @Override
        public I getInput() {
            return delegate.getInput();
        }

        @Override
        public Response processOutput(O out) {
            symbolCounter.incrementAndGet();

            final Response response = delegate.processOutput(out);

            if (response != Response.SYMBOL) {
                resetCounter.incrementAndGet();
            }

            return response;
        }
    }
}
