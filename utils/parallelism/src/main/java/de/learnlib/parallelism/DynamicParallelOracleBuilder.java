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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.google.common.base.Supplier;

import de.learnlib.api.MembershipOracle;
import de.learnlib.parallelism.ParallelOracle.PoolPolicy;

/**
 * Builder class for a {@link DynamicParallelOracle}.
 * 
 * @author Malte Isberner
 *
 * @param <I> input symbol type
 * @param <D> output type
 */
@ParametersAreNonnullByDefault
public class DynamicParallelOracleBuilder<I,D> {
	
	
	@Nonnull
	private final Supplier<? extends MembershipOracle<I,D>> oracleSupplier;
	@Nonnull
	private ExecutorService customExecutor;
	@Nonnegative
	private int batchSize = DynamicParallelOracle.DEFAULT_BATCH_SIZE;
	@Nonnegative
	private int poolSize = DynamicParallelOracle.DEFAULT_POOL_SIZE;
	@Nonnull
	private PoolPolicy poolPolicy = DynamicParallelOracle.DEFAULT_POOL_POLICY;
	
	
	public DynamicParallelOracleBuilder(Supplier<? extends MembershipOracle<I,D>> oracleSupplier) {
		this.oracleSupplier = oracleSupplier;
	}
	
	
	@Nonnull
	public DynamicParallelOracleBuilder<I,D> withCustomExecutor(ExecutorService executor) {
		this.customExecutor = executor;
		return this;
	}
	
	@Nonnull
	public DynamicParallelOracleBuilder<I,D> withDefaultExecutor() {
		this.customExecutor = null;
		return this;
	}
	
	@Nonnull
	public DynamicParallelOracleBuilder<I,D> withBatchSize(int batchSize) {
		this.batchSize = batchSize;
		return this;
	}
	
	@Nonnull
	public DynamicParallelOracleBuilder<I,D> withDefaultBatchSize() {
		this.batchSize = DynamicParallelOracle.DEFAULT_BATCH_SIZE;
		return this;
	}
	
	@Nonnull
	public DynamicParallelOracleBuilder<I,D> withPoolSize(@Nonnegative int poolSize) {
		this.poolSize = poolSize;
		return this;
	}
	
	@Nonnull
	public DynamicParallelOracleBuilder<I,D> withDefaultPoolSize() {
		this.poolSize = DynamicParallelOracle.DEFAULT_POOL_SIZE;
		return this;
	}
	
	@Nonnull
	public DynamicParallelOracleBuilder<I,D> withDefaultPoolPolicy() {
		this.poolPolicy = DynamicParallelOracle.DEFAULT_POOL_POLICY;
		return this;
	}
	
	@Nonnull
	public DynamicParallelOracleBuilder<I,D> withPoolPolicy(PoolPolicy policy) {
		this.poolPolicy = policy;
		return this;
	}
	
	@Nonnull
	public DynamicParallelOracle<I,D> create() {
		ExecutorService executor = customExecutor;
		if(executor == null) {
			switch(poolPolicy) {
			case FIXED:
				executor = Executors.newFixedThreadPool(poolSize);
				break;
			case CACHED:
				executor = new ThreadPoolExecutor(0, poolSize, 100L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
				break;
			default:
				throw new IllegalStateException("Unknown pool policy: " + poolPolicy);
			}
		}
		
		return new DynamicParallelOracle<>(oracleSupplier, batchSize, executor);
	}
	

}
