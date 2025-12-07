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
import de.learnlib.statistic.StatisticsCollector;
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

    public static final String KEY_QUERIES = "sf-qry-cnt";
    public static final String KEY_TRUE_POSITIVES = "sf-tp-cnt";
    public static final String KEY_FALSE_POSITIVES = "sf-fp-cnt";
    public static final String KEY_TRUE_NEGATIVES = "sf-tn-cnt";
    public static final String KEY_FALSE_NEGATIVES = "sf-fn-cnt";

    private final SymbolFilter<U, V> delegate;
    private final StatisticsCollector statisticsCollector;

    public AbstractStatisticsSymbolFilter(SymbolFilter<U, V> delegate) {
        this.delegate = delegate;
        this.statisticsCollector = Statistics.getCollector();
    }

    @Override
    public FilterResponse query(Word<U> prefix, V symbol) {
        statisticsCollector.increaseCounter(KEY_QUERIES, "Filter: queries");

        FilterResponse filterResponse = this.delegate.query(prefix, symbol);
        FilterResponse expectedResponse = this.isIgnorable(prefix, symbol);

        // Count false ignores, rejects + correct predictions:
        if (filterResponse == FilterResponse.ACCEPT) {
            if (filterResponse.equals(expectedResponse)) {
                statisticsCollector.increaseCounter(KEY_TRUE_POSITIVES, "Filter: correct accepts");
            } else {
                statisticsCollector.increaseCounter(KEY_FALSE_POSITIVES, "Filter: false accepts");
            }
        } else {
            if (filterResponse == expectedResponse) {
                statisticsCollector.increaseCounter(KEY_TRUE_NEGATIVES, "Filter: correct ignores");
            } else {
                statisticsCollector.increaseCounter(KEY_FALSE_NEGATIVES, "Filter: false ignores");
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
