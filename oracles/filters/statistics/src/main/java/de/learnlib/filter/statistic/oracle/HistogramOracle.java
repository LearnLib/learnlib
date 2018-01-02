/* Copyright (C) 2013-2018 TU Dortmund
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

import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.Query;
import de.learnlib.api.statistic.StatisticOracle;
import de.learnlib.filter.statistic.HistogramDataSet;

/**
 * Collects a histogram of passed query lengths.
 *
 * @param <I>
 *         input symbol class
 * @param <D>
 *         output symbol class
 *
 * @author falkhowar
 */
@ParametersAreNonnullByDefault
public class HistogramOracle<I, D> implements StatisticOracle<I, D> {

    /**
     * dataset to be collected.
     */
    private final HistogramDataSet dataSet;

    /**
     * oracle used to answer queries.
     */
    private MembershipOracle<I, D> nextOracle;

    /**
     * @param next
     *         real oracle
     * @param name
     *         name of the collected data set
     */
    public HistogramOracle(MembershipOracle<I, D> next, String name) {
        this.nextOracle = next;
        this.dataSet = new HistogramDataSet(name, "query length");
    }

    @Override
    public final void processQueries(Collection<? extends Query<I, D>> queries) {
        for (Query<I, D> q : queries) {
            this.dataSet.addDataPoint((long) q.getInput().size());
        }
        nextOracle.processQueries(queries);
    }

    /**
     * @return the data set collected by this oracle.
     */
    @Override
    @Nonnull
    public final HistogramDataSet getStatisticalData() {
        return this.dataSet;
    }

    /**
     * set used oracle.
     *
     * @param next
     *         oracle to be used
     */
    @Override
    public final void setNext(final MembershipOracle<I, D> next) {
        this.nextOracle = next;
    }
}
