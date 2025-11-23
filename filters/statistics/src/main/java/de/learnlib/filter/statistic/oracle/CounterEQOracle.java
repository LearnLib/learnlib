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

import java.util.Collection;

import de.learnlib.oracle.EquivalenceOracle;
import de.learnlib.query.DefaultQuery;
import de.learnlib.statistic.Statistics;
import de.learnlib.statistic.StatisticsCollector;
import org.checkerframework.checker.nullness.qual.Nullable;

public class CounterEQOracle<A, I, D> implements EquivalenceOracle<A, I, D> {

    public static final String KEY_CEX_CNT = "cex-cnt";
    public static final String KEY_CEX_DUR = "cex-dur";

    private final EquivalenceOracle<A, I, D> delegate;
    private final StatisticsCollector statisticsCollector;
    private final String id;

    public CounterEQOracle(EquivalenceOracle<A, I, D> delegate) {
        this(delegate, "");
    }

    public CounterEQOracle(EquivalenceOracle<A, I, D> delegate, String id) {
        this.delegate = delegate;
        this.id = id;
        this.statisticsCollector = Statistics.getCollector();
    }

    @Override
    public @Nullable DefaultQuery<I, D> findCounterExample(A hypothesis, Collection<? extends I> inputs) {
        final String suffix = id.isEmpty() ? "" : " from '" + id + '\'';

        statisticsCollector.startOrResumeClock(KEY_CEX_DUR + id, "Duration of CEX search" + suffix);
        final DefaultQuery<I, D> cex = this.delegate.findCounterExample(hypothesis, inputs);
        statisticsCollector.pauseClock(KEY_CEX_DUR + id);
        if (cex != null) {
            statisticsCollector.increaseCounter(KEY_CEX_CNT + id, "Found CEX" + suffix);
        }
        return cex;
    }
}
