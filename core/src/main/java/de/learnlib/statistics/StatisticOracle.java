/* Copyright (C) 2013-2014 TU Dortmund
 * This file is part of LearnLib, http://www.learnlib.de/.
 *
 * LearnLib is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 3.0 as published by the Free Software Foundation.
 *
 * LearnLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with LearnLib; if not, see
 * <http://www.gnu.de/documents/lgpl.en.html>.
 */

package de.learnlib.statistics;

import javax.annotation.Nonnull;

import de.learnlib.api.Filter;

/**
 * Common interface for oracles keeping statistics.
 *
 * @param <I> input symbol class
 * @param <O> output symbol class
 *
 * @author falkhowar
 */
public interface StatisticOracle<I, O> extends Filter<I, O> {

    /**
     *
     * @return the statistical data gathered by this oracle
     */
	@Nonnull
    StatisticData getStatisticalData();
}
