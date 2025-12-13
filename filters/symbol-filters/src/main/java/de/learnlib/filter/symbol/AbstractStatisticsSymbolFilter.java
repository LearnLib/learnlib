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
package de.learnlib.filter.symbol;

import de.learnlib.filter.FilterResponse;
import de.learnlib.filter.RefutableSymbolFilter;
import de.learnlib.filter.SymbolFilter;
import de.learnlib.statistic.Statistics;
import de.learnlib.statistic.StatisticsKey;
import de.learnlib.statistic.StatisticsService;
import net.automatalib.word.Word;

/**
 * Collects various statistics on symbol filtering, including false accepts + false ignores.
 *
 * @param <U>
 *         input symbol type of the prefix
 * @param <V>
 *         input symbol type of the transition label
 */
public abstract class AbstractStatisticsSymbolFilter<U, V> extends AbstractTruthfulSymbolFilter<U, V>
        implements RefutableSymbolFilter<U, V> {

    /**
     * A {@link StatisticsKey} for counting the number of queries that this filter has processed.
     */
    public static final StatisticsKey KEY_QUERIES = new StatisticsKey("sf-qry-cnt", "Filter: number of queries");

    /**
     * A {@link StatisticsKey} for counting the number of correctly ignored transitions (true negatives).
     */
    public static final StatisticsKey KEY_TRUE_NEGATIVES = new StatisticsKey("sf-tn-cnt", "Filter: correct ignores");

    /**
     * A {@link StatisticsKey} for counting the number of falsely ignored transitions (false negatives).
     */
    public static final StatisticsKey KEY_FALSE_NEGATIVES = new StatisticsKey("sf-fn-cnt", "Filter: false ignores");

    private final SymbolFilter<U, V> delegate;
    private final StatisticsService statistics;

    public AbstractStatisticsSymbolFilter(SymbolFilter<U, V> delegate) {
        this.delegate = delegate;
        this.statistics = Statistics.getService();
    }

    @Override
    public FilterResponse query(Word<U> prefix, V symbol) {
        statistics.increaseCounter(KEY_QUERIES, this);

        FilterResponse filterResponse = this.delegate.query(prefix, symbol);
        FilterResponse expectedResponse = this.isIgnorable(prefix, symbol);

        // Count false ignores, rejects + correct predictions:
        if (filterResponse == FilterResponse.IGNORE) {
            if (filterResponse == expectedResponse) {
                statistics.increaseCounter(KEY_TRUE_NEGATIVES, this);
            } else {
                statistics.increaseCounter(KEY_FALSE_NEGATIVES, this);
            }
        }

        return filterResponse;
    }

    @Override
    public void accept(Word<U> prefix, V symbol) {
        if (delegate instanceof RefutableSymbolFilter<U, V> rfs) {
            rfs.accept(prefix, symbol);
        } else {
            throw new UnsupportedOperationException("delegate filter does not support updates");
        }
    }
}
