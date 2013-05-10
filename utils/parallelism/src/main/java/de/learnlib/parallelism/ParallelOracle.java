/* Copyright (C) 2013 TU Dortmund
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

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;


/**
 * A membership oracle that distributes a set of queries among several threads.
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 * @param <I> input symbol class
 * @param <O> output class
 */
public class ParallelOracle<I, O> implements MembershipOracle<I, O> {
	
	// TODO Does this number make sense?
	public static int DEFAULT_MIN_BATCH_SIZE = 10;
	
	private final int minBatchSize;
	private final OracleWorker<I, O>[] workers;
	private final Thread[] workerThreads;
	private final MembershipOracle<I,O> thisThreadOracle;

	/**
	 * Constructor for using (potentially) <i>separate</i> oracles for each worker thread,
	 * with a default minimum batch size.
	 * @param oracles the oracles to use for answering the queries. The cardinality of this
	 * list coincides with the number of threads created.
	 */
	public ParallelOracle(List<? extends MembershipOracle<I,O>> oracles) {
		this(oracles, DEFAULT_MIN_BATCH_SIZE);
	}
	
	/**
	 * Constructor for using (potentially) <i>separate</i> oracles for each worker thread.
	 * @param oracles the oracles to use for answering the queries. The cardinality of this
	 * list coincides with the number of threads created.
	 * @param minBatchSize the minimum batch size
	 */
	@SuppressWarnings("unchecked")
	public ParallelOracle(List<? extends MembershipOracle<I,O>> oracles, int minBatchSize) {
		int numOracles = oracles.size();
		if(numOracles <= 0)
			throw new IllegalArgumentException("Must provide at least one oracle");
		workers = new OracleWorker[numOracles - 1];
		workerThreads = new Thread[numOracles - 1];
		Iterator<? extends MembershipOracle<I,O>> it = oracles.iterator();
		thisThreadOracle = it.next();
		int i = 0;
		while(it.hasNext()) {
			MembershipOracle<I,O> oracle = it.next();
			OracleWorker<I,O> worker = new OracleWorker<>(oracle);
			workers[i++] = worker;
		}
		this.minBatchSize = minBatchSize;
	}
	
	/**
	 * Constructor for using one <i>shared</i> oracle for all worker threads,
	 * with the default minimum batch size.
	 * @param sharedOracle the shared oracle
	 * @param numInstances the number of threads to create
	 */
	public ParallelOracle(MembershipOracle<I,O> sharedOracle, int numInstances) {
		this(sharedOracle, numInstances, DEFAULT_MIN_BATCH_SIZE);
	}
	
	/**
	 * Constructor for using one <i>shared</i> oracle for all worker threads,
	 * with a custom minimum batch size.
	 * @param sharedOracle the shared oracle
	 * @param numInstances the number of threads to create
	 * @param minBatchSize the minimum batch size
	 */
	@SuppressWarnings("unchecked")
	public ParallelOracle(MembershipOracle<I,O> sharedOracle, int numInstances, int minBatchSize) {
		if(numInstances <= 0)
			throw new IllegalArgumentException("Must have at least one oracle instance");
		numInstances--;
		workers = new OracleWorker[numInstances];
		workerThreads = new Thread[numInstances];
		thisThreadOracle = sharedOracle;
		for(int i = 0; i < numInstances; i++) {
			OracleWorker<I,O> worker = new OracleWorker<>(sharedOracle);
			workers[i] = worker;
		}
		this.minBatchSize = minBatchSize;
	}
	
	/**
	 * Starts all worker threads.
	 */
	public void start() {
		if(workerThreads.length > 0 && workerThreads[0] != null)
			throw new IllegalStateException("ParallelOracle already started");
		
		for(int i = 0; i < workers.length; i++) {
			Thread t = new Thread(workers[i]);
			workerThreads[i] = t;
			t.start();
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.learnlib.api.MembershipOracle#processQueries(java.util.Collection)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void processQueries(Collection<? extends Query<I, O>> queries) {
		if(workerThreads.length > 0 && workerThreads[0] == null)
			throw new IllegalStateException("ParallelOracle was not started");
		
		int num = queries.size();
		if(num <= 0)
			return;
		
		int numBatches = (num - minBatchSize)/minBatchSize + 1;
		if(numBatches > workers.length + 1)
			numBatches = workers.length + 1;
		
		// Calculate the number of full and non-full batches. The difference in size
		// will never exceed one (cf. pidgeonhole principle)
		int fullBatchSize = (num - 1)/numBatches + 1;
		int nonFullBatches = fullBatchSize*numBatches - num;
		
		Iterator<? extends Query<I,O>> queryIt = queries.iterator();
		
		// One batch is always executed in the local thread. This saves the thread creation
		// overhead for the common case where the batch size is quite small.
		int externalBatches = numBatches - 1;
		
		// If we decide not to need any external threads, we can save initializing synchronization
		// measures.
		CountDownLatch finishSignal = (externalBatches > 0) ? new CountDownLatch(externalBatches) : null;
		
		// Start the threads for the external batches
		for(int i = 0; i < externalBatches; i++) {
			int bs = fullBatchSize;
			if(i < nonFullBatches)
				bs--;
			Query<I,O>[] batch = new Query[bs];
			for(int j = 0; j < bs; j++)
				batch[j] = queryIt.next();
			
			workers[i].offerBatch(batch, finishSignal);
		}
		
		// Finally, prepare and process the batch for the oracle executed in this thread.
		Query<I,O>[] batch = new Query[fullBatchSize];
		for(int j = 0; j < fullBatchSize; j++)
			batch[j] = queryIt.next();
		
		thisThreadOracle.processQueries(Arrays.asList(batch));
		
		// FIXME: Needs deadlock prevention
		if(finishSignal != null) {
			try {
				finishSignal.await();
			} catch (InterruptedException e) {
				throw new IllegalStateException(e);
			}
		}
	}
	
	/**
	 * Stop all worker threads. After this method has been called, invoking {@link #processQueries(Collection)}
	 * will result in an {@link IllegalStateException} until the next call to {@link #start()}
	 * is made. 
	 */
	public void stop() {
		if(workerThreads.length > 0 && workerThreads[0] == null)
			throw new IllegalStateException("Parallel oracle was not started");
		
		for(int i = 0; i < workers.length; i++) {
			workers[i].stop();
		}
		for(int i = 0; i < workerThreads.length; i++) {
			try {
				workerThreads[i].join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			workerThreads[i] = null;
		}
	}

}
