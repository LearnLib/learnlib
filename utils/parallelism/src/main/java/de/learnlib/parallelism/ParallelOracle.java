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

import de.learnlib.api.MembershipOracle;

/**
 * Basic interface for {@link MembershipOracle}s that can process queries
 * in parallel.
 * <p>
 * Parallel oracles usually use one or more dedicated worker threads in which the
 * processing of queries is performed. Since these do not have a defined life span,
 * they must be terminated explicitly using {@link #shutdown()} or {@link #shutdownNow()}. 
 * 
 * @author Malte Isberner
 *
 * @param <I> input symbol type
 * @param <D> output domain type
 */
public interface ParallelOracle<I, D> extends MembershipOracle<I, D> {
	
	/**
	 * The policy for dealing with thread pools.
	 * 
	 * @author Malte Isberner
	 *
	 */
	public enum PoolPolicy {
		/**
		 * Maintain a fixed thread pool. The threads will be started immediately,
		 * and will terminate only if {@link ParallelOracle#shutdown()} or
		 * {@link ParallelOracle#shutdownNow()} are called.
		 * @see Executors#newFixedThreadPool(int)
		 */
		FIXED,
		/**
		 * Maintain a "cached" thread pool. Threads will be created on-demand,
		 * but will be kept alive for re-use when all jobs are processed. However,
		 * they will be terminated when they have been idle for 100 seconds.
		 * <p>
		 * Note that as opposed to {@link Executors#newCachedThreadPool()}, the
		 * specified pool size will never be exceeded.
		 * 
		 * @see Executors#newCachedThreadPool()
		 */
		CACHED
	}
	
	/**
	 * Shuts down all worker threads, but waits for any queued queries
	 * to be processed.
	 * @see ExecutorService#shutdown()
	 */
	public void shutdown();
	
	/**
	 * Shuts down all worker threads, and attempts to abort any query
	 * processing currently taking place.
	 * @see ExecutorService#shutdownNow()
	 */
	public void shutdownNow();
}
