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
import java.util.concurrent.atomic.AtomicInteger;

import de.learnlib.oracle.AdaptiveMembershipOracle;
import de.learnlib.oracle.ThreadPool.PoolPolicy;
import de.learnlib.oracle.parallelism.AbstractDynamicParallelAdaptiveOracleTest.AbstractAdaptiveQuery;
import de.learnlib.oracle.parallelism.Utils.Analysis;
import de.learnlib.oracle.parallelism.Utils.TestSULOutput;
import de.learnlib.query.AdaptiveQuery;
import de.learnlib.sul.SUL;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

public abstract class AbstractStaticParallelAdaptiveOracleTest<D> {

    private static final int NUM_ANSWERS = 3;

    @Test(dataProvider = "policies", dataProviderClass = Utils.class)
    public void testZeroQueries(PoolPolicy policy) {
        StaticParallelAdaptiveOracle<Integer, D> oracle = getOracle(policy);
        oracle.processQueries(Collections.emptyList());
        Analysis ana = analyze(Collections.emptyList());
        Utils.sanityCheck(ana);
        Assert.assertEquals(ana.involvedOracles.size(), 0);
        oracle.shutdownNow();
    }

    @Test(dataProvider = "policies", dataProviderClass = Utils.class)
    public void testLessThanMin(PoolPolicy policy) {
        StaticParallelAdaptiveOracle<Integer, D> oracle = getOracle(policy);
        List<AnswerOnceQuery<D>> queries = createQueries(Utils.MIN_BATCH_SIZE - 1);
        oracle.processQueries(queries);
        Analysis ana = analyze(queries);
        Utils.sanityCheck(ana);
        Assert.assertEquals(ana.involvedOracles.size(), 1);
        oracle.shutdown();
    }

    @Test(dataProvider = "policies", dataProviderClass = Utils.class)
    public void testMin(PoolPolicy policy) {
        StaticParallelAdaptiveOracle<Integer, D> oracle = getOracle(policy);
        List<AnswerOnceQuery<D>> queries = createQueries(Utils.MIN_BATCH_SIZE);
        oracle.processQueries(queries);
        Analysis ana = analyze(queries);
        Utils.sanityCheck(ana);
        Assert.assertEquals(ana.involvedOracles.size(), 1);
        oracle.shutdown();
    }

    @Test(dataProvider = "policies", dataProviderClass = Utils.class)
    public void testLessThanTwoBatches(PoolPolicy policy) {
        StaticParallelAdaptiveOracle<Integer, D> oracle = getOracle(policy);
        List<AnswerOnceQuery<D>> queries = createQueries(2 * Utils.MIN_BATCH_SIZE - 1);
        oracle.processQueries(queries);
        Analysis ana = analyze(queries);
        Utils.sanityCheck(ana);
        Assert.assertEquals(ana.involvedOracles.size(), 1);
        oracle.shutdown();
    }

    @Test(dataProvider = "policies", dataProviderClass = Utils.class)
    public void testLessThanSixBatches(PoolPolicy policy) {
        StaticParallelAdaptiveOracle<Integer, D> oracle = getOracle(policy);
        List<AnswerOnceQuery<D>> queries = createQueries(5 * Utils.MIN_BATCH_SIZE + Utils.MIN_BATCH_SIZE / 2);
        oracle.processQueries(queries);
        Analysis ana = analyze(queries);
        Utils.sanityCheck(ana);
        Assert.assertEquals(ana.involvedOracles.size(), 5);
        oracle.shutdown();
    }

    @Test(dataProvider = "policies", dataProviderClass = Utils.class)
    public void testFullLoad(PoolPolicy policy) {
        StaticParallelAdaptiveOracle<Integer, D> oracle = getOracle(policy);
        List<AnswerOnceQuery<D>> queries = createQueries(2 * Utils.NUM_ORACLES * Utils.MIN_BATCH_SIZE);
        oracle.processQueries(queries);
        Analysis ana = analyze(queries);
        Utils.sanityCheck(ana);
        Assert.assertEquals(ana.involvedOracles.size(), Utils.NUM_ORACLES);
        oracle.shutdown();
    }

    protected abstract StaticParallelAdaptiveOracleBuilder<Integer, D> getBuilder();

    protected abstract TestSULOutput extractTestOutput(D output);

    protected TestMembershipOracle[] getOracles() {
        TestMembershipOracle[] oracles = new TestMembershipOracle[Utils.NUM_ORACLES];
        for (int i = 0; i < Utils.NUM_ORACLES; i++) {
            oracles[i] = new TestMembershipOracle(i);
        }

        return oracles;
    }

    private StaticParallelAdaptiveOracle<Integer, D> getOracle(PoolPolicy poolPolicy) {
        return getBuilder().withMinBatchSize(Utils.MIN_BATCH_SIZE)
                           .withNumInstances(Utils.NUM_ORACLES)
                           .withPoolPolicy(poolPolicy)
                           .create();
    }

    private List<AnswerOnceQuery<D>> createQueries(int num) {
        List<AnswerOnceQuery<D>> result = new ArrayList<>(num);
        for (int i = 0; i < num; i++) {
            result.add(new AnswerOnceQuery<>(NUM_ANSWERS));
        }
        return result;
    }

    private Analysis analyze(Collection<AnswerOnceQuery<D>> queries) {
        List<Integer> oracles = new ArrayList<>();
        Map<Integer, List<Integer>> seqIds = new HashMap<>();
        Map<Integer, Integer> incorrectAnswers = new HashMap<>();

        for (AnswerOnceQuery<D> qry : queries) {
            List<D> outputs = qry.getOutputs();
            Word<Integer> inputs = qry.getInputs();
            Assert.assertEquals(outputs.size(), NUM_ANSWERS);
            Assert.assertEquals(inputs.size(), NUM_ANSWERS);

            for (int i = 0; i < outputs.size(); i++) {
                D output = outputs.get(i);
                TestSULOutput out = extractTestOutput(output);
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

                Assert.assertEquals(out.word.size(), 1);
                if (!inputs.getSymbol(i).equals(out.word.firstSymbol())) {
                    incorrectAnswers.put(oracleId, incorrectAnswers.get(oracleId) + 1);
                }
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

    static final class TestMembershipOracle implements AdaptiveMembershipOracle<Integer, TestSULOutput> {

        private final int oracleId;

        TestMembershipOracle(int oracleId) {
            this.oracleId = oracleId;
        }

        @Override
        public void processQueries(Collection<? extends AdaptiveQuery<Integer, TestSULOutput>> queries) {
            int batchSeqId = 0;
            for (AdaptiveQuery<Integer, TestSULOutput> q : queries) {
                for (int i = 0; i < NUM_ANSWERS; i++) {
                    q.processOutput(new TestSULOutput(oracleId, batchSeqId++, Word.fromLetter(q.getInput())));
                }
            }
        }
    }

    static final class TestSUL implements SUL<Integer, TestSULOutput> {

        private final AtomicInteger atomicInteger;
        private final int oracleId;
        private int batchSeqId;

        TestSUL(AtomicInteger atomicInteger) {
            this.atomicInteger = atomicInteger;
            this.oracleId = atomicInteger.getAndIncrement();
        }

        @Override
        public void pre() {}

        @Override
        public void post() {}

        @Override
        public TestSULOutput step(Integer in) {
            return new TestSULOutput(oracleId, batchSeqId++, Word.fromLetter(in));
        }

        @Override
        public boolean canFork() {
            return true;
        }

        @Override
        public TestSUL fork() {
            return new TestSUL(this.atomicInteger);
        }
    }

    static final class AnswerOnceQuery<D> extends AbstractAdaptiveQuery<Integer, D> {

        final WordBuilder<Integer> wb;

        AnswerOnceQuery(int count) {
            super(count);
            this.wb = new WordBuilder<>(count);
        }

        @Override
        public Integer getInput() {
            this.wb.append(super.counter);
            return super.counter;
        }

        public Word<Integer> getInputs() {
            return wb.toWord();
        }
    }
}
