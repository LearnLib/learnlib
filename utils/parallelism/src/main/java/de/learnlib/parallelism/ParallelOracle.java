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
		workers = new OracleWorker[numOracles];
		workerThreads = new Thread[numOracles];
		int i = 0;
		for(MembershipOracle<I,O> oracle : oracles) {
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
		workers = new OracleWorker[numInstances];
		workerThreads = new Thread[numInstances];
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
		if(workerThreads[0] != null)
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
		if(workerThreads[0] == null)
			throw new IllegalStateException("ParallelOracle was not started");
		
		int num = queries.size();
		if(num <= 0)
			return;
		
		int numBatches = (num - minBatchSize)/minBatchSize + 1;
		if(numBatches > workers.length)
			numBatches = workers.length;
		
		int fullBatchSize = (num - 1)/numBatches + 1;
		int nonFullBatches = fullBatchSize*numBatches - num;
		
		Iterator<? extends Query<I,O>> queryIt = queries.iterator();
		
		CountDownLatch finishSignal = new CountDownLatch(numBatches);
		
		
		for(int i = 0; i < numBatches; i++) {
			int bs = fullBatchSize;
			if(i < nonFullBatches)
				bs--;
			Query<I,O>[] batch = new Query[bs];
			for(int j = 0; j < bs; j++)
				batch[j] = queryIt.next();
			
			workers[i].offerBatch(batch, finishSignal);
		}
		
		try {
			finishSignal.await();
		} catch (InterruptedException e) {
			throw new IllegalStateException(e);
		}
	}
	
	/**
	 * Stop all worker threads. After this method has been called, invoking {@link #processQueries(Collection)}
	 * will result in an {@link IllegalStateException} until the next call to {@link #start()}
	 * is made. 
	 */
	public void stop() {
		if(workerThreads[0] == null)
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
