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
