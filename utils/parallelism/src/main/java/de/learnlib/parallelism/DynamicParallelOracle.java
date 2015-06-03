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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.google.common.base.Supplier;
import com.google.common.base.Throwables;

import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;
import de.learnlib.settings.LearnLibSettings;

/**
 * A parallel membership oracle that dynamically distributes queries
 * to worker threads.
 * 
 * @author Malte Isberner
 *
 * @param <I> input symbol type
 * @param <D> output domain type
 */
@ParametersAreNonnullByDefault
public class DynamicParallelOracle<I,D> implements ParallelOracle<I, D>{
	
	public static final int DEFAULT_BATCH_SIZE;
	public static final int DEFAULT_POOL_SIZE;
	public static final PoolPolicy DEFAULT_POOL_POLICY;
	
	static {
		LearnLibSettings settings = LearnLibSettings.getInstance();
		
		DEFAULT_BATCH_SIZE = settings.getInt("parallel.dynamic.batch_size", 1);
		
		int numProcessors = Runtime.getRuntime().availableProcessors();
		DEFAULT_POOL_SIZE = settings.getInt("parallel.dynamic.pool_size", numProcessors);
		
		DEFAULT_POOL_POLICY = settings.getEnumValue("parallel.static.pool_policy", PoolPolicy.class, PoolPolicy.CACHED);
	}
	
	
	
	@Nonnull
	private final ThreadLocal<MembershipOracle<I,D>> threadLocalOracle;
	@Nonnull
	private final ExecutorService executor;
	@Nonnegative
	private final int batchSize;

	
	public DynamicParallelOracle(final Supplier<? extends MembershipOracle<I,D>> oracleSupplier,
			@Nonnegative int batchSize,
			ExecutorService executor) {
		this.threadLocalOracle = new ThreadLocal<MembershipOracle<I,D>>() {
			@Override
			protected MembershipOracle<I, D> initialValue() {
				return oracleSupplier.get();
			}
		};
		this.executor = executor;
		this.batchSize = batchSize;
	}
	
	@Override
	public void shutdown() {
		executor.shutdown();
	}
	
	@Override
	public void shutdownNow() {
		executor.shutdownNow();
	}

	@Override
	public void processQueries(Collection<? extends Query<I, D>> queries) {
		if(queries.isEmpty()) {
			return;
		}
		
		
		int numQueries = queries.size();
		int numJobs = (numQueries - 1) / batchSize + 1;
		List<Query<I,D>> currentBatch = null;
		
		List<Future<?>> futures = new ArrayList<>(numJobs);
		
		for(Query<I,D> query : queries) {
			
			if(currentBatch == null) {
				currentBatch = new ArrayList<>(batchSize);
			}
			
			currentBatch.add(query);
			if(currentBatch.size() == batchSize) {
				Future<?> future = executor.submit(new DynamicQueriesJob<>(currentBatch, threadLocalOracle));
				futures.add(future);
				currentBatch = null;
			}
		}
		
		if(currentBatch != null) {
			Future<?> future = executor.submit(new DynamicQueriesJob<>(currentBatch, threadLocalOracle));
			futures.add(future);
		}
		
		try {
			// Await completion of all jobs
			for(Future<?> future : futures) {
				future.get();
			}
		}
		catch(ExecutionException e) {
			Throwables.propagateIfPossible(e.getCause());
			throw new AssertionError("Runnables must not throw checked exceptions", e);
		}
		catch (InterruptedException e) {
			Thread.interrupted();
			throw new ParallelOracleInterruptedException(e);
		}
	}

}
