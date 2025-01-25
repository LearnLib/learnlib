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

import java.util.Collection;
import java.util.Collections;

import de.learnlib.filter.statistic.TestQueries;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.query.Query;
import de.learnlib.statistic.StatisticData;
import net.automatalib.word.Word;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CounterOracleTest {

    private final CounterOracle<Integer, Word<Character>> oracle;

    @SuppressWarnings("unchecked")
    public CounterOracleTest() {
        this.oracle = new CounterOracle<Integer, Word<Character>>(Mockito.mock(MembershipOracle.class));
    }

    @Test
    public void testInitialState() {
        verifyCounts(0, 0);
    }

    @Test(dependsOnMethods = "testInitialState")
    public void testFirstQueryBatch() {
        Collection<Query<Integer, Word<Character>>> queries = TestQueries.createNoopQueries(2);
        oracle.processQueries(queries);
        verifyCounts(2, 0);
    }

    @Test(dependsOnMethods = "testFirstQueryBatch")
    public void testEmptyQueryBatch() {
        Collection<Query<Integer, Word<Character>>> noQueries = Collections.emptySet();
        oracle.processQueries(noQueries);
        verifyCounts(2, 0);
    }

    @Test(dependsOnMethods = "testEmptyQueryBatch")
    public void testSecondQueryBatch() {
        Collection<Query<Integer, Word<Character>>> queries = TestQueries.createNoopQueries(2, 5, TestQueries.INPUTS);
        oracle.processQueries(queries);
        verifyCounts(4, 10);
    }

    @Test
    public void testStatistics() {
        final StatisticData statisticalData = oracle.getStatisticalData();
        Assert.assertTrue(statisticalData.getName().contains("\n"));
        Assert.assertTrue(statisticalData.getUnit().contains("\n"));
        Assert.assertTrue(statisticalData.getSummary().contains("\n"));
        Assert.assertTrue(statisticalData.getDetails().contains("\n"));
    }

    private void verifyCounts(long queries, long symbols) {
        Assert.assertEquals(oracle.getQueryCounter().getCount(), queries);
        Assert.assertEquals(oracle.getSymbolCounter().getCount(), symbols);
    }

}
