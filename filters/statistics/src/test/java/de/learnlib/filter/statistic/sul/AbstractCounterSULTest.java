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
package de.learnlib.filter.statistic.sul;

import java.util.Collection;
import java.util.Collections;

import de.learnlib.filter.statistic.Counter;
import de.learnlib.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.oracle.SingleQueryOracle.SingleQueryOracleMealy;
import de.learnlib.query.Query;
import de.learnlib.statistic.StatisticSUL;
import de.learnlib.sul.SUL;
import net.automatalib.word.Word;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public abstract class AbstractCounterSULTest<S extends StatisticSUL<Integer, Character>> {

    private S statisticSUL;
    private MealyMembershipOracle<Integer, Character> asOracle;

    protected abstract S getStatisticSUL();

    protected abstract Counter getCounter(S sul);

    protected abstract int getCountIncreasePerQuery();

    protected abstract Collection<Query<Integer, Word<Character>>> createQueries(int num);

    @BeforeClass
    public void setUp() {
        this.statisticSUL = getStatisticSUL();
        this.asOracle = getSimulator(this.statisticSUL);
    }

    @Test
    public void testInitialState() {
        Assert.assertEquals(getCount(), 0L);
    }

    @Test(dependsOnMethods = "testInitialState")
    public void testFirstQueryBatch() {
        final Collection<Query<Integer, Word<Character>>> queries = createQueries(2);
        final long oldCount = getCount();

        asOracle.processQueries(queries);

        Assert.assertEquals(getCount(), oldCount + 2L * getCountIncreasePerQuery());
    }

    @Test(dependsOnMethods = "testFirstQueryBatch")
    public void testEmptyQueryBatch() {
        final Collection<Query<Integer, Word<Character>>> queries = Collections.emptySet();
        final long oldCount = getCount();

        asOracle.processQueries(queries);

        Assert.assertEquals(getCount(), oldCount);
    }

    @Test(dependsOnMethods = "testEmptyQueryBatch")
    public void testSecondQueryBatch() {
        final Collection<Query<Integer, Word<Character>>> queries = createQueries(1);
        final long oldCount = getCount();

        asOracle.processQueries(queries);

        Assert.assertEquals(getCount(), oldCount + getCountIncreasePerQuery());
    }

    @Test(dependsOnMethods = "testSecondQueryBatch")
    public void testSharedForkCounter() {
        final MealyMembershipOracle<Integer, Character> mqo1 = getSimulator(statisticSUL.fork());
        final MealyMembershipOracle<Integer, Character> mqo2 = getSimulator(statisticSUL.fork());
        final MealyMembershipOracle<Integer, Character> mqo3 = getSimulator(statisticSUL.fork());

        final Collection<Query<Integer, Word<Character>>> queries = createQueries(2);
        final long oldCount = getCount();

        mqo1.processQueries(queries);
        mqo2.processQueries(queries);
        mqo3.processQueries(queries);

        Assert.assertEquals(getCount(), oldCount + 2L * 3 * getCountIncreasePerQuery());
    }

    private long getCount() {
        return getCounter(statisticSUL).getCount();
    }

    // use custom class to prevent cyclic dependency on learnlib-membership-oracles
    private static <I, O> SingleQueryOracleMealy<I, O> getSimulator(SUL<I, O> sul) {
        return (prefix, suffix) -> {
            sul.pre();
            try {
                prefix.forEach(sul::step);
                return suffix.stream().map(sul::step).collect(Word.collector());
            } finally {
                sul.post();
            }
        };
    }
}
