/* Copyright (C) 2013 TU Dortmund
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

import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;
import de.learnlib.statistics.HistogramDataSet;
import de.learnlib.statistics.StatisticData;
import de.learnlib.statistics.StatisticOracle;
import java.util.Collection;

/**
 * Keeps a histogram of passed query lengths.
 * 
 * @author falkhowar
 */
public class HistogramOracle<I,O> implements StatisticOracle<I,O> {

    private final HistogramDataSet dataSet;
    
    private final MembershipOracle<I,O> nextOracle;
    
    public HistogramOracle(MembershipOracle<I,O> nextOracle, String name) {
        this.nextOracle = nextOracle;
        this.dataSet = new HistogramDataSet(name, "query length");
    }

    @Override
    public void processQueries(Collection<Query<I, O>> queries) {
        for (Query<I,O> q : queries) {
            this.dataSet.addDataPoint( (long) q.getInput().size());
        }
        nextOracle.processQueries(queries);
    }
    
    public HistogramDataSet getDataSet() {
        return this.dataSet;
    }

    @Override
    public StatisticData getStatisticalData() {
        return this.dataSet;
    }    
}
