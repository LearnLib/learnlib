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
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

@Test
public class CounterOracleTest {

    private static final String COUNTER_NAME = "testCounter";

    private final CounterOracle<Object, Object> oracle;

    @SuppressWarnings("unchecked")
    public CounterOracleTest() {
        this.oracle = new CounterOracle<>(Mockito.mock(MembershipOracle.class), COUNTER_NAME);
    }

    @Test
    public void testInitialState() {
        Assert.assertEquals(oracle.getCount(), 0L);
    }

    @Test(dependsOnMethods = "testInitialState")
    public void testFirstQueryBatch() {
        Collection<Query<Object, Object>> queries = TestQueries.createNoopQueries(2);
        long oldCount = oracle.getCount();
        oracle.processQueries(queries);
        Assert.assertEquals(oracle.getCount(), oldCount + 2L);
    }

    @Test(dependsOnMethods = "testFirstQueryBatch")
    public void testEmptyQueryBatch() {
        Collection<Query<Object, Object>> noQueries = Collections.emptySet();
        long oldCount = oracle.getCount();
        oracle.processQueries(noQueries);
        Assert.assertEquals(oracle.getCount(), oldCount);
    }

    @Test(dependsOnMethods = "testEmptyQueryBatch")
    public void testSecondQueryBatch() {
        Collection<Query<Object, Object>> queries = TestQueries.createNoopQueries(1);
        long oldCount = oracle.getCount();
        oracle.processQueries(queries);
        Assert.assertEquals(oracle.getCount(), oldCount + 1L);
    }

    @Test
    public void testGetName() {
        Assert.assertEquals(oracle.getCounter().getName(), COUNTER_NAME);
    }

    @AfterMethod
    public void testInvariants() {
        Assert.assertEquals(oracle.getCounter().getCount(), oracle.getCount());
    }

}
