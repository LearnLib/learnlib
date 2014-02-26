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
 * A queries job that maintains a fixed reference to a membership oracle,
 * executes queries using this oracle regardless of the executing thread.
 * 
 * @author Malte Isberner
 *
 * @param <I> input symbol type
 * @param <O> output type
 */
@ParametersAreNonnullByDefault
final class StaticQueriesJob<I, O> extends AbstractQueriesJob<I, O> {

	@Nonnull
	private final MembershipOracle<I, O> oracle;
	
	public StaticQueriesJob(Collection<? extends Query<I, O>> queries, MembershipOracle<I, O> oracle) {
		super(queries);
		this.oracle = oracle;
	}

	@Override
	protected MembershipOracle<I, O> getOracle() {
		return oracle;
	}

}
