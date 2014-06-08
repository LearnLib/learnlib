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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

import de.learnlib.api.MembershipOracle;

/**
 * Builders for (static and dynamic) parallel oracles.
 * <p>
 * Using the methods defined in this class is the preferred way of instantiating parallel oracles.
 * <p>
 * <b>Usage examples</b>
 * <p>
 * Creating a static parallel oracle with a minimum batch size of 20 and a fixed thread pool, using
 * a membership oracle shared by 4 threads:
 * <pre>
 * ParallelOracleBuilders.newStaticParallelOracle(membershipOracle)
 * 		.withMinBatchSize(20)
 * 		.withNumInstances(4)
 * 		.withPoolPolicy(PoolPolicy.FIXED)
 * 		.create();
 * </pre>
 * <p>
 * Creating a dynamic parallel oracle with a custom executor, and a batch size of 5, using a shared membership oracle:
 * <pre>
 * ParallelOracleBuilders.newDynamicParallelOracle(membershipOracle)
 * 		.withBatchSize(5)
 * 		.withCustomExecutor(myExecutor)
 * 		.create();
 * </pre>
 * <p>
 * Creating a dynamic parallel oracle with a cached thread pool of maximum size 4, a batch size of 5, using an
 * oracle supplier:
 * <pre>
 * ParallelOracleBuilders.newDynamicParallelOracle(oracleSupplier)
 * 		.withBatchSize(5)
 * 		.withPoolSize(4)
 * 		.withPoolPolicy(PoolPolicy.CACHED)
 * 		.create();
 * </pre>
 * 
 * @author Malte Isberner
 *
 */
@ParametersAreNonnullByDefault
public abstract class ParallelOracleBuilders {
	
	@Nonnull
	public static <I,D>
	DynamicParallelOracleBuilder<I, D> newDynamicParallelOracle(MembershipOracle<I,D> sharedOracle) {
		return newDynamicParallelOracle(Suppliers.ofInstance(sharedOracle));
	}
	
	@Nonnull
	public static <I,D>
	DynamicParallelOracleBuilder<I,D> newDynamicParallelOracle(Supplier<? extends MembershipOracle<I,D>> oracleSupplier) {
		return new DynamicParallelOracleBuilder<>(oracleSupplier);
	}
	
	@Nonnull
	public static <I,D>
	StaticParallelOracleBuilder<I, D> newStaticParallelOracle(MembershipOracle<I,D> sharedOracle) {
		return newStaticParallelOracle(Suppliers.ofInstance(sharedOracle));
	}
	
	@Nonnull
	public static <I,D>
	StaticParallelOracleBuilder<I,D> newStaticParallelOracle(Supplier<? extends MembershipOracle<I,D>> oracleSupplier) {
		return new StaticParallelOracleBuilder<>(oracleSupplier);
	}
	
	@Nonnull
	@SafeVarargs
	public static <I,D>
	StaticParallelOracleBuilder<I,D> newStaticParallelOracle(
			MembershipOracle<I,D> firstOracle,
			MembershipOracle<I,D>... otherOracles) {
		List<MembershipOracle<I,D>> oracles = new ArrayList<>(otherOracles.length + 1);
		oracles.add(firstOracle);
		Collections.addAll(oracles, otherOracles);
		return newStaticParallelOracle(oracles);
	}
	
	@Nonnull
	public static <I,D>
	StaticParallelOracleBuilder<I,D> newStaticParallelOracle(Collection<? extends MembershipOracle<I,D>> oracles) {
		return new StaticParallelOracleBuilder<>(oracles);
	}
	
	private ParallelOracleBuilders() {
		throw new AssertionError("Constructor should not be invoked");
	}
}
