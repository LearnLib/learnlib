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

import java.util.List;
import java.util.Map;
import java.util.Random;

import de.learnlib.oracle.ThreadPool.PoolPolicy;
import de.learnlib.oracle.parallelism.AbstractStaticParallelOracleTest.TestOutput;
import net.automatalib.word.Word;
import org.testng.Assert;
import org.testng.annotations.DataProvider;

final class Utils {

    static final int NUM_ORACLES = 10;
    static final int MIN_BATCH_SIZE = 20;
    static final int MAX_WORD_LEN = 30;

    static final Random RANDOM = new Random();

    private Utils() {}

    @DataProvider(name = "policies")
    static Object[][] createPolicies() {
        return new Object[][] {new Object[] {PoolPolicy.CACHED}, new Object[] {PoolPolicy.FIXED}};
    }

    static Word<Integer> createWord(int minLength) {
        int length = Math.max(minLength, RANDOM.nextInt(MAX_WORD_LEN));
        Integer[] ints = new Integer[length];
        for (int i = 0; i < ints.length; i++) {
            ints[i] = RANDOM.nextInt();
        }
        return Word.fromSymbols(ints);
    }

    static void sanityCheck(Analysis analysis) {
        for (Integer oracleId : analysis.involvedOracles) {
            List<Integer> seqIds = analysis.sequenceIds.get(oracleId);
            Assert.assertNotNull(seqIds);
            boolean[] ids = new boolean[seqIds.size()];
            for (Integer seqId : seqIds) {
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

    static TestOutput extractSULOutput(Word<TestSULOutput> output) {
        assert !output.isEmpty();

        final TestSULOutput lastSym = output.lastSymbol();
        final int oracleId = lastSym.oracleId;
        final int batchSeqId = lastSym.batchSeqId;

        final Word<Integer> word = lastSym.word;
        final Word<Integer> prefix = word.prefix(word.size() - output.size());
        final Word<Integer> suffix = word.subWord(word.size() - output.size());

        return new TestOutput(oracleId, batchSeqId, prefix, suffix);
    }

    static final class Analysis {

        final List<Integer> involvedOracles;
        final Map<Integer, List<Integer>> sequenceIds;
        final Map<Integer, Integer> incorrectAnswers;
        final int minBatchSize;
        final int maxBatchSize;

        Analysis(List<Integer> involvedOracles,
                 Map<Integer, List<Integer>> sequenceIds,
                 Map<Integer, Integer> incorrectAnswers,
                 int minBatchSize,
                 int maxBatchSize) {
            this.involvedOracles = involvedOracles;
            this.sequenceIds = sequenceIds;
            this.incorrectAnswers = incorrectAnswers;
            this.minBatchSize = minBatchSize;
            this.maxBatchSize = maxBatchSize;
        }
    }

    static final class TestSULOutput {

        final int oracleId;
        final int batchSeqId;
        final Word<Integer> word;

        TestSULOutput(int oracleId, int batchSeqId, Word<Integer> word) {
            this.oracleId = oracleId;
            this.batchSeqId = batchSeqId;
            this.word = word;
        }
    }
}
