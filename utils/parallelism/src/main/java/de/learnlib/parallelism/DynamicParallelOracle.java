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
