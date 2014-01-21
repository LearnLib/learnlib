/* Copyright (C) 2013 TU Dortmund
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

package de.learnlib.oracles;

import java.util.Collection;

import net.automatalib.words.Word;
import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;
import de.learnlib.statistics.Counter;
import de.learnlib.statistics.StatisticOracle;

/**
 * Counts queries.
 * 
 * @author falkhowar
 */
public class CounterOracle<I,O> implements StatisticOracle<I,O> {
	
	public static class DFACounterOracle<I> extends CounterOracle<I,Boolean>
			implements DFAMembershipOracle<I> {
		public DFACounterOracle(MembershipOracle<I, Boolean> nextOracle,
				String name) {
			super(nextOracle, name);
		}
	}
	
	public static class MealyCounterOracle<I,O> extends CounterOracle<I,Word<O>>
			implements MealyMembershipOracle<I,O> {
		public MealyCounterOracle(MembershipOracle<I, Word<O>> nextOracle,
				String name) {
			super(nextOracle, name);
		}
	}
    
    private final Counter counter;
    
    private MembershipOracle<I,O> nextOracle;
    
    public CounterOracle(MembershipOracle<I,O> nextOracle, String name) {        
        this.nextOracle = nextOracle;
        this.counter = new Counter(name, "queries");
    }

    @Override
    public void processQueries(Collection<? extends Query<I, O>> queries) {
        this.counter.increment(queries.size());
        nextOracle.processQueries(queries);
    }

    @Override
    public Counter getStatisticalData() {
        return this.counter;
    }
    
    public Counter getCounter() {
    	return this.counter;
    }
    
    public long getCount() {
    	return counter.getCount();
    }

    @Override
    public void setNext(MembershipOracle<I, O> next) {
        this.nextOracle = next;
    }
}
