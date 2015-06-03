/* Copyright (C) 2014 TU Dortmund
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
 * @param <D> output type
 */
@ParametersAreNonnullByDefault
final class StaticQueriesJob<I, D> extends AbstractQueriesJob<I, D> {

	@Nonnull
	private final MembershipOracle<I, D> oracle;
	
	public StaticQueriesJob(Collection<? extends Query<I, D>> queries, MembershipOracle<I, D> oracle) {
		super(queries);
		this.oracle = oracle;
	}

	@Override
	protected MembershipOracle<I, D> getOracle() {
		return oracle;
	}

}
