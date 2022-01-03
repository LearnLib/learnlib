/* Copyright (C) 2013-2022 TU Dortmund
 * This file is part of LearnLib, http://www.learnlib.de/.
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

import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.Query;
import de.learnlib.filter.statistic.TestQueries;
import net.automatalib.words.Word;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

public class JointCounterOracleTest {

    private final JointCounterOracle<Integer, Word<Character>> oracle;

    @SuppressWarnings("unchecked")
    public JointCounterOracleTest() {
        this.oracle = new JointCounterOracle<>(Mockito.mock(MembershipOracle.class));
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

    private void verifyCounts(long queries, long symbols) {
        Assert.assertEquals(oracle.getQueryCount(), queries);
        Assert.assertEquals(oracle.getSymbolCount(), symbols);
    }

}
