/* Copyright (C) 2013-2026 TU Dortmund University
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

import de.learnlib.oracle.ThreadPool.PoolPolicy;
import de.learnlib.oracle.TimedQueryOracle;
import de.learnlib.oracle.parallelism.Utils.Analysis;
import de.learnlib.query.DefaultQuery;
import de.learnlib.query.Query;
import de.learnlib.sul.TimedSUL;
import net.automatalib.symbol.time.InputSymbol;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.symbol.time.TimedOutput;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.testng.Assert;
import org.testng.annotations.Test;

public abstract class AbstractStaticParallelTimedQueryOracleTest<O> {

    @Test(dataProvider = "policies", dataProviderClass = Utils.class)
    public void testZeroQueries(PoolPolicy policy) {
        StaticParallelTimedQueryOracle<Integer, O> oracle = getOracle(policy);
        oracle.processQueries(Collections.emptyList());
        Analysis ana = analyze(Collections.emptyList());
        Utils.sanityCheck(ana);
        Assert.assertEquals(ana.involvedOracles.size(), 0);
        oracle.shutdownNow();
    }

    @Test(dataProvider = "policies", dataProviderClass = Utils.class)
    public void testLessThanMin(PoolPolicy policy) {
        StaticParallelTimedQueryOracle<Integer, O> oracle = getOracle(policy);
        List<DefaultQuery<TimedInput<Integer>, Word<TimedOutput<O>>>> queries = createQueries(Utils.MIN_BATCH_SIZE - 1);
        oracle.processQueries(queries);
        Analysis ana = analyze(queries);
        Utils.sanityCheck(ana);
        Assert.assertEquals(ana.involvedOracles.size(), 1);
        oracle.shutdown();
    }

    @Test(dataProvider = "policies", dataProviderClass = Utils.class)
    public void testMin(PoolPolicy policy) {
        StaticParallelTimedQueryOracle<Integer, O> oracle = getOracle(policy);
        List<DefaultQuery<TimedInput<Integer>, Word<TimedOutput<O>>>> queries = createQueries(Utils.MIN_BATCH_SIZE);
        oracle.processQueries(queries);
        Analysis ana = analyze(queries);
        Utils.sanityCheck(ana);
        Assert.assertEquals(ana.involvedOracles.size(), 1);
        oracle.shutdown();
    }

    @Test(dataProvider = "policies", dataProviderClass = Utils.class)
    public void testLessThanTwoBatches(PoolPolicy policy) {
        StaticParallelTimedQueryOracle<Integer, O> oracle = getOracle(policy);
        List<DefaultQuery<TimedInput<Integer>, Word<TimedOutput<O>>>> queries =
                createQueries(2 * Utils.MIN_BATCH_SIZE - 1);
        oracle.processQueries(queries);
        Analysis ana = analyze(queries);
        Utils.sanityCheck(ana);
        Assert.assertEquals(ana.involvedOracles.size(), 1);
        oracle.shutdown();
    }

    @Test(dataProvider = "policies", dataProviderClass = Utils.class)
    public void testLessThanSixBatches(PoolPolicy policy) {
        StaticParallelTimedQueryOracle<Integer, O> oracle = getOracle(policy);
        List<DefaultQuery<TimedInput<Integer>, Word<TimedOutput<O>>>> queries =
                createQueries(5 * Utils.MIN_BATCH_SIZE + Utils.MIN_BATCH_SIZE / 2);
        oracle.processQueries(queries);
        Analysis ana = analyze(queries);
        Utils.sanityCheck(ana);
        Assert.assertEquals(ana.involvedOracles.size(), 5);
        oracle.shutdown();
    }

    @Test(dataProvider = "policies", dataProviderClass = Utils.class)
    public void testFullLoad(PoolPolicy policy) {
        StaticParallelTimedQueryOracle<Integer, O> oracle = getOracle(policy);
        List<DefaultQuery<TimedInput<Integer>, Word<TimedOutput<O>>>> queries =
                createQueries(2 * Utils.NUM_ORACLES * Utils.MIN_BATCH_SIZE);
        oracle.processQueries(queries);
        Analysis ana = analyze(queries);
        Utils.sanityCheck(ana);
        Assert.assertEquals(ana.involvedOracles.size(), Utils.NUM_ORACLES);
        oracle.shutdown();
    }

    protected abstract StaticParallelTimedQueryOracleBuilder<Integer, O> getBuilder();

    protected abstract TestOutput extractTestOutput(Word<TimedOutput<O>> output);

    protected TestMembershipOracle[] getOracles() {
        TestMembershipOracle[] oracles = new TestMembershipOracle[Utils.NUM_ORACLES];
        for (int i = 0; i < Utils.NUM_ORACLES; i++) {
            oracles[i] = new TestMembershipOracle(i);
        }

        return oracles;
    }

    private StaticParallelTimedQueryOracle<Integer, O> getOracle(PoolPolicy poolPolicy) {
        return getBuilder().withMinBatchSize(Utils.MIN_BATCH_SIZE)
                           .withNumInstances(Utils.NUM_ORACLES)
                           .withPoolPolicy(poolPolicy)
                           .create();
    }

    protected int getMinQueryLength() {
        return 0;
    }

    private List<DefaultQuery<TimedInput<Integer>, Word<TimedOutput<O>>>> createQueries(int num) {
        List<DefaultQuery<TimedInput<Integer>, Word<TimedOutput<O>>>> result = new ArrayList<>(num);
        for (int i = 0; i < num; i++) {
            Word<TimedInput<Integer>> prefix =
                    Utils.createWord(getMinQueryLength()).stream().map(TimedInput::input).collect(Word.collector());
            Word<TimedInput<Integer>> suffix =
                    Utils.createWord(getMinQueryLength()).stream().map(TimedInput::input).collect(Word.collector());
            result.add(new DefaultQuery<>(prefix, suffix));
        }
        return result;
    }

    private Analysis analyze(Collection<DefaultQuery<TimedInput<Integer>, Word<TimedOutput<O>>>> queries) {
        List<Integer> oracles = new ArrayList<>();
        Map<Integer, List<Integer>> seqIds = new HashMap<>();
        Map<Integer, Integer> incorrectAnswers = new HashMap<>();

        for (DefaultQuery<TimedInput<Integer>, Word<TimedOutput<O>>> qry : queries) {
            TestOutput out = extractTestOutput(qry.getOutput());
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

            if (!qry.getPrefix().equals(out.prefix) || !qry.getSuffix().equals(out.suffix)) {
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
        public final Word<TimedInput<Integer>> prefix;
        public final Word<TimedInput<Integer>> suffix;

        TestOutput(int oracleId, int batchSeqId, Word<TimedInput<Integer>> prefix, Word<TimedInput<Integer>> suffix) {
            this.oracleId = oracleId;
            this.batchSeqId = batchSeqId;
            this.prefix = prefix;
            this.suffix = suffix;
        }
    }

    static final class TestSULOutput {

        final int oracleId;
        final int batchSeqId;
        final Word<TimedInput<Integer>> word;

        TestSULOutput(int oracleId, int batchSeqId, Word<TimedInput<Integer>> word) {
            this.oracleId = oracleId;
            this.batchSeqId = batchSeqId;
            this.word = word;
        }
    }

    static final class TestMembershipOracle implements TimedQueryOracle<Integer, TestOutput> {

        private final int oracleId;

        TestMembershipOracle(int oracleId) {
            this.oracleId = oracleId;
        }

        @Override
        public void processQueries(Collection<? extends Query<TimedInput<Integer>, Word<TimedOutput<TestOutput>>>> queries) {
            int batchSeqId = 0;
            for (Query<TimedInput<Integer>, Word<TimedOutput<TestOutput>>> qry : queries) {
                qry.answer(Word.fromLetter(new TimedOutput<>(new TestOutput(oracleId,
                                                                            batchSeqId++,
                                                                            qry.getPrefix(),
                                                                            qry.getSuffix()))));
            }
        }

        @Override
        public TimerQueryResult<TestOutput> queryTimers(Word<TimedInput<Integer>> prefix, long maxTotalWaitingTime) {
            return new TimerQueryResult<>(false, Collections.emptyList());
        }
    }

    static final class TestSUL implements TimedSUL<Integer, TestSULOutput> {

        private final AtomicInteger atomicInteger;
        private final int oracleId;
        private int batchSeqId;

        private final WordBuilder<TimedInput<Integer>> wb;

        TestSUL(AtomicInteger atomicInteger) {
            this.atomicInteger = atomicInteger;
            this.oracleId = atomicInteger.getAndIncrement();
            this.batchSeqId = -1; // so that our first query starts at 0

            this.wb = new WordBuilder<>();
        }

        @Override
        public void pre() {
            batchSeqId++;
        }

        @Override
        public void post() {
            this.wb.clear();
        }

        @Override
        public TimedOutput<TestSULOutput> step(InputSymbol<Integer> in) {
            this.wb.append(in);
            return new TimedOutput<>(new TestSULOutput(oracleId, batchSeqId, wb.toWord()));
        }

        @Override
        public @Nullable TimedOutput<TestSULOutput> timeoutStep(long maxTime) {
            return null;
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

}
