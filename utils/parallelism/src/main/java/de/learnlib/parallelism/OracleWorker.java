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
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

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
	
	private final MembershipOracle<I,O> oracle;
	private final CyclicBarrier barrier = new CyclicBarrier(2);
	private Query<I,O>[] batch;
	private CountDownLatch finishSignal;
	
	public OracleWorker(MembershipOracle<I,O> oracle) {
		this.oracle = oracle;
	}
	
	/**
	 * Offers a batch to this worker.
	 * @param batch the batch to offer
	 * @param finishSignal the latch to countDown upon finishing processing of the batch
	 */
	public void offerBatch(Query<I,O>[] batch, CountDownLatch finishSignal) {
		this.batch = batch;
		this.finishSignal = finishSignal;
		try {
			barrier.await();
		} catch (InterruptedException | BrokenBarrierException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Stops this worker.
	 */
	public void stop() {
		this.batch = null;
		try {
			barrier.await();
		} catch (InterruptedException | BrokenBarrierException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			do {
				barrier.await();
				if(batch == null)
					return;
				oracle.processQueries(Arrays.asList(batch));
				finishSignal.countDown();
			} while(true);
		}
		catch(InterruptedException | BrokenBarrierException ex) {
			ex.printStackTrace(); // TODO
		}
	}

}
