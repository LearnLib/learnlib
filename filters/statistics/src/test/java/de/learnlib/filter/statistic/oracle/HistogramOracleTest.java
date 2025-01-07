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
package de.learnlib.filter.statistic.oracle;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;

import de.learnlib.filter.statistic.TestQueries;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.query.Query;
import net.automatalib.common.util.IOUtil;
import net.automatalib.word.Word;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

public class HistogramOracleTest {

    private static final String COUNTER_NAME = "testCounter";

    private final HistogramOracle<Integer, Word<Character>> oracle;

    @SuppressWarnings("unchecked")
    public HistogramOracleTest() {
        this.oracle = new HistogramOracle<Integer, Word<Character>>(Mockito.mock(MembershipOracle.class), COUNTER_NAME);
    }

    @Test
    public void testInitialState() {
        verifyCounts(0, 0, 0, 0);
    }

    @Test(dependsOnMethods = "testInitialState")
    public void testFirstQueryBatch() {
        Collection<Query<Integer, Word<Character>>> queries = TestQueries.createNoopQueries(2);
        oracle.processQueries(queries);
        verifyCounts(2, 0, 0, 0);
    }

    @Test(dependsOnMethods = "testFirstQueryBatch")
    public void testEmptyQueryBatch() {
        Collection<Query<Integer, Word<Character>>> noQueries = Collections.emptySet();
        oracle.processQueries(noQueries);
        verifyCounts(2, 0, 0, 0);
    }

    @Test(dependsOnMethods = "testEmptyQueryBatch")
    public void testSecondQueryBatch() {
        Collection<Query<Integer, Word<Character>>> queries = TestQueries.createNoopQueries(2, 5, TestQueries.INPUTS);
        oracle.processQueries(queries);
        verifyCounts(4, 10, 2.5, 0);
    }

    @Test(dependsOnMethods = "testSecondQueryBatch")
    public void testSummary() throws IOException {

        final String details = oracle.getStatisticalData().getDetails();
        final String summary = oracle.getStatisticalData().getSummary();

        try (InputStream detailStream = HistogramOracleTest.class.getResourceAsStream("/histogram_details.txt");
             InputStream summaryStream = HistogramOracleTest.class.getResourceAsStream("/histogram_summary.txt")) {

            final String expectedDetail = IOUtil.toString(IOUtil.asBufferedUTF8Reader(detailStream));
            final String expectedSummary = IOUtil.toString(IOUtil.asBufferedUTF8Reader(summaryStream));

            Assert.assertEquals(details, expectedDetail);
            Assert.assertEquals(summary, expectedSummary);
        }
    }

    @Test
    public void testGetName() {
        Assert.assertEquals(oracle.getStatisticalData().getName(), COUNTER_NAME);
    }

    private void verifyCounts(long size, long sum, double mean, long median) {
        Assert.assertEquals(oracle.getStatisticalData().getSize(), size);
        Assert.assertEquals(oracle.getStatisticalData().getSum(), sum);
        Assert.assertEquals(oracle.getStatisticalData().getMean(), mean);
        Assert.assertEquals(oracle.getStatisticalData().getMedian(), median);
    }
}
