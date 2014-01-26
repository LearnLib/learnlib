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
 * http://www.gnu.de/documents/lgpl.en.html.
 */
package de.learnlib.oracles;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import de.learnlib.api.SUL;
import de.learnlib.statistics.Counter;
import de.learnlib.statistics.StatisticSUL;

@ParametersAreNonnullByDefault
public class ResetCounterSUL<I, O> implements StatisticSUL<I, O> {
	
	private final SUL<I,O> sul;
	private final Counter counter;

	public ResetCounterSUL(String name, SUL<I,O> sul) {
		this.sul = sul;
		this.counter = new Counter(name, "resets");
	}

            @Override
            public void pre() {
                    counter.increment();
                    sul.pre();
            }

            @Override
            public void post() {
                    sul.post();
            }
        
	@Override
	@Nullable
	public O step(@Nullable I in) {
		return sul.step(in);
	}

	@Override
	@Nonnull
	public Counter getStatisticalData() {
		return counter;
	}

}
