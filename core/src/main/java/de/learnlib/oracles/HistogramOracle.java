/* Copyright (C) 2013-2014 TU Dortmund
 This file is part of LearnLib

 LearnLib is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License version 3.0 as published by the Free Software Foundation.

 LearnLib is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with LearnLib; if not, see
 <http://www.gnu.de/documents/lgpl.en.html>.  */
package de.learnlib.oracles;

import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;
import de.learnlib.statistics.HistogramDataSet;
import de.learnlib.statistics.StatisticOracle;

/**
 * Collects a histogram of passed query lengths.
 *
 * @param <I> input symbol class
 * @param <D> output symbol class
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
     *
     * @param next real oracle
     * @param name name of the collected data set
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
     * @param next oracle to be used
     */
    @Override
    public final void setNext(final MembershipOracle<I, D> next) {
	this.nextOracle = next;
    }
}
