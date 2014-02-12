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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;
import de.learnlib.oracles.DefaultQuery;

import net.automatalib.words.Word;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test
public class StaticParallelOracleTest {
	
	public static final int NUM_ORACLES = 10;
	public static final int MIN_BATCH_SIZE = 20;
	public static final int MAX_WORD_LEN = 30;
	
	private static final Random random = new Random();
	
	private static final class TestOutput {
		private final int oracleId;
		private final int batchSeqId;
		private final Word<Integer> prefix;
		private final Word<Integer> suffix;
		
		public TestOutput(int oracleId, int batchSeqId, Word<Integer> prefix, Word<Integer> suffix) {
			this.oracleId = oracleId;
			this.batchSeqId = batchSeqId;
			this.prefix = prefix;
			this.suffix = suffix;
		}
	}
	
	private static final class Analysis {
		private final List<Integer> involvedOracles;
		private final Map<Integer,List<Integer>> sequenceIds;
		private final Map<Integer,Integer> incorrectAnswers;
		private final int minBatchSize;
		private final int maxBatchSize;
		
		public Analysis(List<Integer> involvedOracles, Map<Integer,List<Integer>> sequenceIds, Map<Integer,Integer> incorrectAnswers,
				int minBatchSize, int maxBatchSize) {
			this.involvedOracles = involvedOracles;
			this.sequenceIds = sequenceIds;
			this.incorrectAnswers = incorrectAnswers;
			this.minBatchSize = minBatchSize;
			this.maxBatchSize = maxBatchSize;
		}
	}
	
	private static final class TestMembershipOracle implements MembershipOracle<Integer,TestOutput> {
		
		private final int oracleId;
		
		public TestMembershipOracle(int oracleId) {
			this.oracleId = oracleId;
		}
		
		@Override
		public void processQueries(
				Collection<? extends Query<Integer, TestOutput>> queries) {
			int batchSeqId = 0;
			for(Query<Integer,TestOutput> qry : queries) {
				qry.answer(new TestOutput(oracleId, batchSeqId++, qry.getPrefix(), qry.getSuffix()));
			}
		}
		
	}
	
	private StaticParallelOracle<Integer,TestOutput> parallelOracle;
	
	@BeforeClass
	public void setUp() {
		List<TestMembershipOracle> oracles = new ArrayList<TestMembershipOracle>(NUM_ORACLES);
		for(int i = 0; i < NUM_ORACLES; i++) {
			oracles.add(new TestMembershipOracle(i));
		}
		
		
		parallelOracle = ParallelOracleBuilders.newStaticParallelOracle(oracles).withMinBatchSize(MIN_BATCH_SIZE).create();
	}
	
	@AfterClass
	public void tearDown() {
		parallelOracle.shutdown();
	}
	
	@Test
	public void testLessThanMin() {
		List<DefaultQuery<Integer,TestOutput>> queries = createQueries(MIN_BATCH_SIZE - 1);
		parallelOracle.processQueries(queries);
		Analysis ana = analyze(queries);
		sanityCheck(ana);
		Assert.assertEquals(ana.involvedOracles.size(), 1);
	}
	
	@Test
	public void testMin() {
		List<DefaultQuery<Integer,TestOutput>> queries = createQueries(MIN_BATCH_SIZE);
		parallelOracle.processQueries(queries);
		Analysis ana = analyze(queries);
		sanityCheck(ana);
		Assert.assertEquals(ana.involvedOracles.size(), 1);
	}
	
	@Test
	public void testLessThanTwoBatches() {
		List<DefaultQuery<Integer,TestOutput>> queries = createQueries(2*MIN_BATCH_SIZE - 1);
		parallelOracle.processQueries(queries);
		Analysis ana = analyze(queries);
		sanityCheck(ana);
		Assert.assertEquals(ana.involvedOracles.size(), 1);
	}
	
	@Test
	public void testLessThanSixBatches() {
		List<DefaultQuery<Integer,TestOutput>> queries = createQueries(5*MIN_BATCH_SIZE + MIN_BATCH_SIZE/2);
		parallelOracle.processQueries(queries);
		Analysis ana = analyze(queries);
		sanityCheck(ana);
		Assert.assertEquals(ana.involvedOracles.size(), 5);
	}
	
	@Test
	public void testFullLoad() {
		List<DefaultQuery<Integer,TestOutput>> queries = createQueries(2*NUM_ORACLES*MIN_BATCH_SIZE);
		parallelOracle.processQueries(queries);
		Analysis ana = analyze(queries);
		sanityCheck(ana);
		Assert.assertEquals(ana.involvedOracles.size(), NUM_ORACLES);
	}
	
	
	
	
	
	private static Analysis analyze(Collection<DefaultQuery<Integer,TestOutput>> queries) {
		List<Integer> oracles = new ArrayList<>();
		Map<Integer,List<Integer>> seqIds = new HashMap<>();
		Map<Integer,Integer> incorrectAnswers = new HashMap<>();
		
		for(DefaultQuery<Integer,TestOutput> qry : queries) {
			TestOutput out = qry.getOutput();
			Assert.assertNotNull(out);
			int oracleId = out.oracleId;
			List<Integer> seqIdList = seqIds.get(oracleId);
			if(seqIdList == null) {
				oracles.add(oracleId);
				seqIdList = new ArrayList<>();
				seqIds.put(oracleId, seqIdList);
				incorrectAnswers.put(oracleId, 0);
			}
			
			int seqId = out.batchSeqId;
			seqIdList.add(seqId);
			
			if(!qry.getPrefix().equals(out.prefix) || !qry.getSuffix().equals(out.suffix))
				incorrectAnswers.put(oracleId, incorrectAnswers.get(oracleId)+1);
		}
		
		int minBatchSize = -1;
		int maxBatchSize = -1;
		for(List<Integer> batch : seqIds.values()) {
			if(minBatchSize == -1)
				minBatchSize = maxBatchSize = batch.size();
			else {
				if(batch.size() < minBatchSize)
					minBatchSize = batch.size();
				if(batch.size() > maxBatchSize)
					maxBatchSize = batch.size();
			}
		}
		
		return new Analysis(oracles, seqIds, incorrectAnswers, minBatchSize, maxBatchSize);
	}
	
	private static void sanityCheck(Analysis analysis) {
		for(Integer oracleId : analysis.involvedOracles) {
			List<Integer> seqIds = analysis.sequenceIds.get(oracleId);
			Assert.assertNotNull(seqIds);
			boolean[] ids = new boolean[seqIds.size()];
			for(Integer seqId : seqIds) {
				Assert.assertNotNull(seqId);
				Assert.assertTrue(seqId >= 0);
				Assert.assertTrue(seqId < ids.length);
				Assert.assertFalse(ids[seqId]);
				ids[seqId] = true;
			}
			Integer incAnswers = analysis.incorrectAnswers.get(oracleId);
			Assert.assertEquals(incAnswers.intValue(), 0);
		}
		Assert.assertTrue((analysis.maxBatchSize - analysis.minBatchSize) <= 1);
		
		Assert.assertTrue(analysis.involvedOracles.size() <= 1 || analysis.minBatchSize >= MIN_BATCH_SIZE);
	}
	
	private static Word<Integer> createWord() {
		int length = random.nextInt(MAX_WORD_LEN);
		Integer[] ints = new Integer[length];
		for(int i = 0; i < ints.length; i++)
			ints[i] = random.nextInt();
		return Word.fromSymbols(ints);
	}
	
	private static List<DefaultQuery<Integer,TestOutput>> createQueries(int num) {
		List<DefaultQuery<Integer,TestOutput>> result = new ArrayList<>(num);
		for(int i = 0; i < num; i++) {
			DefaultQuery<Integer,TestOutput> qry = new DefaultQuery<>(createWord(), createWord());
			result.add(qry);
		}
		return result;
	}

}
