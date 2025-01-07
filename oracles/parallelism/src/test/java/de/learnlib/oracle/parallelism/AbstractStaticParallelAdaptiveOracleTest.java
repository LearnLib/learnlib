/* Copyright (C) 2013-2025 TU Dortmund University
 * This file is part of LearnLib <https://learnlib.de>.
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
package de.learnlib.oracle.parallelism;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.learnlib.oracle.AdaptiveMembershipOracle;
import de.learnlib.oracle.ThreadPool.PoolPolicy;
import de.learnlib.oracle.parallelism.AbstractDynamicParallelAdaptiveOracleTest.AnswerOnceQuery;
import de.learnlib.oracle.parallelism.Utils.Analysis;
import de.learnlib.query.AdaptiveQuery;
import org.testng.Assert;
import org.testng.annotations.Test;

public abstract class AbstractStaticParallelAdaptiveOracleTest<D> {

    private static final int NUM_ANSWERS = 1;

    @Test(dataProvider = "policies", dataProviderClass = Utils.class)
    public void testZeroQueries(PoolPolicy policy) {
        StaticParallelAdaptiveOracle<Void, D> oracle = getOracle(policy);
        oracle.processQueries(Collections.emptyList());
        Analysis ana = analyze(Collections.emptyList());
        Utils.sanityCheck(ana);
        Assert.assertEquals(ana.involvedOracles.size(), 0);
        oracle.shutdownNow();
    }

    @Test(dataProvider = "policies", dataProviderClass = Utils.class)
    public void testLessThanMin(PoolPolicy policy) {
        StaticParallelAdaptiveOracle<Void, D> oracle = getOracle(policy);
        List<AnswerOnceQuery<D>> queries = createQueries(Utils.MIN_BATCH_SIZE - 1);
        oracle.processQueries(queries);
        Analysis ana = analyze(queries);
        Utils.sanityCheck(ana);
        Assert.assertEquals(ana.involvedOracles.size(), 1);
        oracle.shutdown();
    }

    @Test(dataProvider = "policies", dataProviderClass = Utils.class)
    public void testMin(PoolPolicy policy) {
        StaticParallelAdaptiveOracle<Void, D> oracle = getOracle(policy);
        List<AnswerOnceQuery<D>> queries = createQueries(Utils.MIN_BATCH_SIZE);
        oracle.processQueries(queries);
        Analysis ana = analyze(queries);
        Utils.sanityCheck(ana);
        Assert.assertEquals(ana.involvedOracles.size(), 1);
        oracle.shutdown();
    }

    @Test(dataProvider = "policies", dataProviderClass = Utils.class)
    public void testLessThanTwoBatches(PoolPolicy policy) {
        StaticParallelAdaptiveOracle<Void, D> oracle = getOracle(policy);
        List<AnswerOnceQuery<D>> queries = createQueries(2 * Utils.MIN_BATCH_SIZE - 1);
        oracle.processQueries(queries);
        Analysis ana = analyze(queries);
        Utils.sanityCheck(ana);
        Assert.assertEquals(ana.involvedOracles.size(), 1);
        oracle.shutdown();
    }

    @Test(dataProvider = "policies", dataProviderClass = Utils.class)
    public void testLessThanSixBatches(PoolPolicy policy) {
        StaticParallelAdaptiveOracle<Void, D> oracle = getOracle(policy);
        List<AnswerOnceQuery<D>> queries = createQueries(5 * Utils.MIN_BATCH_SIZE + Utils.MIN_BATCH_SIZE / 2);
        oracle.processQueries(queries);
        Analysis ana = analyze(queries);
        Utils.sanityCheck(ana);
        Assert.assertEquals(ana.involvedOracles.size(), 5);
        oracle.shutdown();
    }

    @Test(dataProvider = "policies", dataProviderClass = Utils.class)
    public void testFullLoad(PoolPolicy policy) {
        StaticParallelAdaptiveOracle<Void, D> oracle = getOracle(policy);
        List<AnswerOnceQuery<D>> queries = createQueries(2 * Utils.NUM_ORACLES * Utils.MIN_BATCH_SIZE);
        oracle.processQueries(queries);
        Analysis ana = analyze(queries);
        Utils.sanityCheck(ana);
        Assert.assertEquals(ana.involvedOracles.size(), Utils.NUM_ORACLES);
        oracle.shutdown();
    }

    protected abstract StaticParallelAdaptiveOracleBuilder<Void, D> getBuilder();

    protected abstract TestOutput extractTestOutput(D output);

    protected TestMembershipOracle[] getOracles() {
        TestMembershipOracle[] oracles = new TestMembershipOracle[Utils.NUM_ORACLES];
        for (int i = 0; i < Utils.NUM_ORACLES; i++) {
            oracles[i] = new TestMembershipOracle(i);
        }

        return oracles;
    }

    private StaticParallelAdaptiveOracle<Void, D> getOracle(PoolPolicy poolPolicy) {
        return getBuilder().withMinBatchSize(Utils.MIN_BATCH_SIZE)
                           .withNumInstances(Utils.NUM_ORACLES)
                           .withPoolPolicy(poolPolicy)
                           .create();
    }

    private List<AnswerOnceQuery<D>> createQueries(int num) {
        List<AnswerOnceQuery<D>> result = new ArrayList<>(num);
        for (int i = 0; i < num; i++) {
            result.add(new AnswerOnceQuery<>(NUM_ANSWERS, UUID.randomUUID()));
        }
        return result;
    }

    private Analysis analyze(Collection<AnswerOnceQuery<D>> queries) {
        List<Integer> oracles = new ArrayList<>();
        Map<Integer, List<Integer>> seqIds = new HashMap<>();
        Map<Integer, Integer> incorrectAnswers = new HashMap<>();

        for (AnswerOnceQuery<D> qry : queries) {
            List<D> outputs = qry.getOutputs();
            Assert.assertEquals(outputs.size(), NUM_ANSWERS);
            D output = outputs.get(0);
            TestOutput out = extractTestOutput(output);
            Assert.assertNotNull(out);
            int oracleId = out.oracleId;
            List<Integer> seqIdList = seqIds.get(oracleId);
            if (seqIdList == null) {
                oracles.add(oracleId);
                seqIdList = new ArrayList<>();
                seqIds.put(oracleId, seqIdList);
                incorrectAnswers.put(oracleId, 0);
            }

            int seqId = out.batchSeqId;
            seqIdList.add(seqId);

            if (!qry.getId().equals(out.id)) {
                incorrectAnswers.put(oracleId, incorrectAnswers.get(oracleId) + 1);
            }
        }

        int minBatchSize = -1;
        int maxBatchSize = -1;
        for (List<Integer> batch : seqIds.values()) {
            if (minBatchSize == -1) {
                maxBatchSize = batch.size();
                minBatchSize = maxBatchSize;
            } else {
                if (batch.size() < minBatchSize) {
                    minBatchSize = batch.size();
                }
                if (batch.size() > maxBatchSize) {
                    maxBatchSize = batch.size();
                }
            }
        }

        return new Analysis(oracles, seqIds, incorrectAnswers, minBatchSize, maxBatchSize);
    }

    static final class TestOutput {

        public final int oracleId;
        public final int batchSeqId;
        public final UUID id;

        TestOutput(int oracleId, int batchSeqId, UUID id) {
            this.oracleId = oracleId;
            this.batchSeqId = batchSeqId;
            this.id = id;
        }
    }

    static final class TestMembershipOracle implements AdaptiveMembershipOracle<Void, TestOutput> {

        private final int oracleId;

        TestMembershipOracle(int oracleId) {
            this.oracleId = oracleId;
        }

        @Override
        public void processQueries(Collection<? extends AdaptiveQuery<Void, TestOutput>> queries) {
            int batchSeqId = 0;
            for (AdaptiveQuery<Void, TestOutput> q : queries) {
                for (int i = 0; i < NUM_ANSWERS; i++) {
                    q.processOutput(new TestOutput(oracleId, batchSeqId++, ((AnswerOnceQuery<TestOutput>) q).getId()));
                }
            }
        }
    }

}
