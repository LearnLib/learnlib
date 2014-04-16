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
 * A queries job that maintains a thread-local reference to a membership oracle,
 * and dynamically selects that oracle depending on the executing thread.
 * <p>
 * Note: This class assumes that the respective {@link ThreadLocal#get()} methods
 * never returns a {@code null} reference.
 * 
 * @author Malte Isberner
 *
 * @param <I> input symbol type
 * @param <O> output type
 */
@ParametersAreNonnullByDefault
final class DynamicQueriesJob<I, O> extends AbstractQueriesJob<I, O> {
	
	@Nonnull
	private final ThreadLocal<? extends MembershipOracle<I,O>> threadLocalOracle;

	public DynamicQueriesJob(Collection<? extends Query<I, O>> queries,
			ThreadLocal<? extends MembershipOracle<I,O>> threadLocalOracle) {
		super(queries);
		this.threadLocalOracle = threadLocalOracle;
	}

	@Override
	protected MembershipOracle<I, O> getOracle() {
		return threadLocalOracle.get();
	}

}
