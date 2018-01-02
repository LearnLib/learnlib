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

package de.learnlib.api.statistic;

import javax.annotation.Nonnull;

import de.learnlib.api.oracle.Filter;

/**
 * Common interface for oracles keeping statistics.
 *
 * @param <I>
 *         input symbol class
 * @param <O>
 *         output symbol class
 *
 * @author falkhowar
 */
public interface StatisticOracle<I, O> extends Filter<I, O> {

    /**
     * @return the statistical data gathered by this oracle
     */
    @Nonnull
    StatisticData getStatisticalData();
}
