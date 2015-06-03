/* Copyright (C) 2013-2014 TU Dortmund
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
package de.learnlib.oracles;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import de.learnlib.api.SUL;
import de.learnlib.api.SULException;
import de.learnlib.statistics.Counter;
import de.learnlib.statistics.StatisticSUL;

@ParametersAreNonnullByDefault
public class SymbolCounterSUL<I, O> implements StatisticSUL<I,O> {
	
	private final SUL<I,O> sul;
	private final Counter counter;

	public SymbolCounterSUL(String name, SUL<I,O> sul) {
		this.sul = sul;
		this.counter = new Counter(name, "symbols");
	}
	
	private SymbolCounterSUL(Counter counter, SUL<I, O> sul) {
		this.sul = sul;
		this.counter = counter;
	}

	/* (non-Javadoc)
	 * @see de.learnlib.api.SUL#pre()
	 */
	@Override
	public void pre() {
		sul.pre();
	}


	/* (non-Javadoc)
	 * @see de.learnlib.api.SUL#post()
	 */
        @Override
	public void post() {
		sul.post();
	}

	/* (non-Javadoc)
	 * @see de.learnlib.api.SUL#step(java.lang.Object)
	 */
	@Override
	@Nullable
	public O step(@Nullable I in) throws SULException {
		counter.increment();
		return sul.step(in);
	}

	/* (non-Javadoc)
	 * @see de.learnlib.statistics.StatisticSUL#getStatisticalData()
	 */
	@Override
	@Nonnull
	public Counter getStatisticalData() {
		return counter;
	}
	
	@Override
	public boolean canFork() {
		return sul.canFork();
	}

	@Override
	public SUL<I,O> fork() {
		return new SymbolCounterSUL<>(counter, sul.fork());
	}
}
