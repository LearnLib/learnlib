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
import java.util.List;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.google.common.base.Supplier;

import de.learnlib.api.MembershipOracle;
import de.learnlib.parallelism.ParallelOracle.PoolPolicy;

/**
 * A builder for a {@link StaticParallelOracle}.
 * 
 * @author Malte Isberner
 *
 * @param <I> input symbol type
 * @param <D> output type
 */
@ParametersAreNonnullByDefault
public class StaticParallelOracleBuilder<I, D> {
	
	@Nonnegative
	private int minBatchSize = StaticParallelOracle.DEFAULT_MIN_BATCH_SIZE;
	@Nonnegative
	private int numInstances = StaticParallelOracle.DEFAULT_NUM_INSTANCES;
	@Nonnull
	private PoolPolicy poolPolicy = StaticParallelOracle.DEFAULT_POOL_POLICY;
	
	@Nonnull
	private final Collection<? extends MembershipOracle<I,D>> oracles;
	@Nonnull
	private final Supplier<? extends MembershipOracle<I,D>> oracleSupplier;
	
	
	public StaticParallelOracleBuilder(Collection<? extends MembershipOracle<I,D>> oracles) {
		this.oracles = oracles;
		this.oracleSupplier = null;
	}
	
	
	public StaticParallelOracleBuilder(Supplier<? extends MembershipOracle<I,D>> oracleSupplier) {
		this.oracles = null;
		this.oracleSupplier = oracleSupplier;
	}

	@Nonnull
	public StaticParallelOracleBuilder<I,D> withDefaultMinBatchSize() {
		this.minBatchSize = StaticParallelOracle.DEFAULT_MIN_BATCH_SIZE;
		return this;
	}
	
	@Nonnull
	public StaticParallelOracleBuilder<I,D> withMinBatchSize(@Nonnegative int minBatchSize) {
		this.minBatchSize = minBatchSize;
		return this;
	}
	
	@Nonnull
	public StaticParallelOracleBuilder<I,D> withDefaultPoolPolicy() {
		this.poolPolicy = StaticParallelOracle.DEFAULT_POOL_POLICY;
		return this;
	}
	
	@Nonnull
	public StaticParallelOracleBuilder<I,D> withPoolPolicy(PoolPolicy policy) {
		this.poolPolicy = policy;
		return this;
	}
	
	@Nonnull
	public StaticParallelOracleBuilder<I,D> withDefaultNumInstances() {
		this.numInstances = StaticParallelOracle.DEFAULT_NUM_INSTANCES;
		return this;
	}
	
	@Nonnull
	public StaticParallelOracleBuilder<I,D> withNumInstances(@Nonnegative int numInstances) {
		this.numInstances = numInstances;
		return this;
	}
	
	@Nonnull
	public StaticParallelOracle<I,D> create() {
		Collection<? extends MembershipOracle<I, D>> oracleInstances;
		if(oracles != null) {
			oracleInstances = oracles;
		}
		else {
			List<MembershipOracle<I,D>> oracleList = new ArrayList<>(numInstances);
			for(int i = 0; i < numInstances; i++) {
				oracleList.add(oracleSupplier.get());
			}
			oracleInstances = oracleList;
		}
		
		return new StaticParallelOracle<>(oracleInstances, minBatchSize, poolPolicy);
	}

}
