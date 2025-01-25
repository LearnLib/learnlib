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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import de.learnlib.filter.statistic.TestQueries;
import de.learnlib.oracle.AdaptiveMembershipOracle;
import de.learnlib.query.AdaptiveQuery;
import de.learnlib.query.AdaptiveQuery.Response;
import de.learnlib.query.Query;
import de.learnlib.statistic.StatisticData;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CounterAdaptiveOracleTest {

    private final CounterAdaptiveQueryOracle<Integer, Character> oracle;

    public CounterAdaptiveOracleTest() {
        this.oracle = new CounterAdaptiveQueryOracle<>(new DummyOracle());
    }

    @Test
    public void testInitialState() {
        verifyCounts(0, 0);
    }

    @Test(dependsOnMethods = "testInitialState")
    public void testFirstQueryBatch() {
        final List<AdaptiveQuery<Integer, Character>> queries = new ArrayList<>(2);
        for (Query<Integer, Word<Character>> q : generateQueries(1, 1, TestQueries.INPUTS)) {
            queries.add(new PresetAdaptiveQuery(q));
        }
        oracle.processQueries(queries);
        verifyCounts(1, 1);
    }

    @Test(dependsOnMethods = "testFirstQueryBatch")
    public void testEmptyQueryBatch() {
        Collection<AdaptiveQuery<Integer, Character>> noQueries = Collections.emptySet();
        oracle.processQueries(noQueries);
        verifyCounts(1, 1);
    }

    @Test(dependsOnMethods = "testEmptyQueryBatch")
    public void testSecondQueryBatch() {
        final List<AdaptiveQuery<Integer, Character>> queries = new ArrayList<>(2);
        for (Query<Integer, Word<Character>> q : generateQueries(2, 5, TestQueries.INPUTS)) {
            queries.add(new PresetAdaptiveQuery(q));
        }
        oracle.processQueries(queries);
        verifyCounts(3, 11);
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
        Assert.assertEquals(oracle.getResetCounter().getCount(), queries);
        Assert.assertEquals(oracle.getSymbolCounter().getCount(), symbols);
    }

    private Collection<Query<Integer, Word<Character>>> generateQueries(int numQueries,
                                                                        int numInputs,
                                                                        Collection<Integer> inputs) {
        return TestQueries.createNoopQueries(numQueries, numInputs, inputs);
    }

    /**
     * We can't mock this implementation because queries actually need to get delegated.
     */
    private static final class DummyOracle implements AdaptiveMembershipOracle<Integer, Character> {

        @Override
        public void processQueries(Collection<? extends AdaptiveQuery<Integer, Character>> adaptiveQueries) {
            for (AdaptiveQuery<Integer, Character> q : adaptiveQueries) {
                Response response;
                do {
                    q.getInput();
                    response = q.processOutput('-');
                } while (response != Response.FINISHED);
            }
        }
    }

    /**
     * Copied from learnlib-util because a dependency would introduce a cycle.
     */
    private static class PresetAdaptiveQuery implements AdaptiveQuery<Integer, Character> {

        private final WordBuilder<Character> builder;
        private final Query<Integer, Word<Character>> query;

        private final Word<Integer> prefix;
        private final Word<Integer> suffix;

        private int prefixIdx;
        private int suffixIdx;

        PresetAdaptiveQuery(Query<Integer, Word<Character>> query) {
            this.builder = new WordBuilder<>();
            this.query = query;
            this.prefix = query.getPrefix();
            this.suffix = query.getSuffix();
            this.prefixIdx = 0;
            this.suffixIdx = 0;
        }

        @Override
        public Integer getInput() {
            if (prefixIdx < prefix.size()) {
                return prefix.getSymbol(prefixIdx);
            } else {
                return suffix.getSymbol(suffixIdx);
            }
        }

        @Override
        public Response processOutput(Character out) {
            if (prefixIdx < prefix.size()) {
                prefixIdx++;
            } else {
                suffixIdx++;
                builder.add(out);

                if (suffixIdx >= suffix.size()) {
                    query.answer(builder.toWord());
                    return Response.FINISHED;
                }
            }

            return Response.SYMBOL;
        }
    }
}
