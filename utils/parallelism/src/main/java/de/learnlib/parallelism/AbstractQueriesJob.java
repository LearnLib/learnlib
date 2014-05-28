/* Copyright (C) 2014 TU Dortmund
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
package de.learnlib.parallelism;

import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;

/**
 * Abstract base class for jobs (i.e., {@link Runnable}s) that process queries.
 * <p>
 * Subclasses specify how the delegate oracle is obtained.
 * 
 * @author Malte Isberner
 *
 * @param <I> input symbol type
 * @param <D> output domain type
 */
@ParametersAreNonnullByDefault
abstract class AbstractQueriesJob<I, D> implements Runnable {
	
	private final Collection<? extends Query<I,D>> queries;
	
	@Nonnull
	protected abstract MembershipOracle<I,D> getOracle();
	
	public AbstractQueriesJob(Collection<? extends Query<I,D>> queries) {
		this.queries = queries;
	}
	
	@Override
	public void run() {
		MembershipOracle<I, D> oracle = getOracle();
		
		oracle.processQueries(queries);
	}
}
