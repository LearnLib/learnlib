/* Copyright (C) 2013-2014 TU Dortmund
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
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;

/**
 * Worker class, receiving batches of exclusive queries and processing them.
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 * @param <I> input symbol class
 * @param <O> output class
 */
final class OracleWorker<I,O> implements Runnable {
	
	
	private static final Logger LOGGER = Logger.getLogger(OracleWorker.class.getName());
	
	private final MembershipOracle<I,O> oracle;
	private Query<I,O>[] batch;
	private CountDownLatch finishSignal;
	private boolean stop = false;
	
	public OracleWorker(MembershipOracle<I,O> oracle) {
		this.oracle = oracle;
	}
	
	/**
	 * Offers a batch to this worker.
	 * @param batch the batch to offer
	 * @param finishSignal the latch to countDown upon finishing processing of the batch
	 */
	public synchronized void offerBatch(Query<I,O>[] batch, CountDownLatch finishSignal) {
		this.batch = batch;
		this.finishSignal = finishSignal;
		notify();
	}
	
	/**
	 * Stops this worker.
	 */
	public synchronized void stop() {
		this.stop = true;
		notify();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			do {
				synchronized(this) {
					if(stop) {
						return;
					}
					if(batch == null) {
						wait();
						if(stop) {
							return;
						}
						if(batch == null) {
							LOGGER.warning("Worker thread of ParallelOracle was notified, but no query batch was provided.");
							continue;
						}
					}
					oracle.processQueries(Arrays.asList(batch));
					finishSignal.countDown();
				}
			} while(true);
		}
		catch(InterruptedException ex) {
			LOGGER.severe("Worker thread of ParallelOracle interrupted: " + ex.getMessage());
			LOGGER.severe("Exiting worker thread ...");
		}
	}

}
