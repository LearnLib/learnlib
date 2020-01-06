/* Copyright (C) 2013-2020 TU Dortmund
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
package de.learnlib.oracle.parallelism;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.Query;
import de.learnlib.oracle.parallelism.DynamicParallelOracleBuilder.StaticOracleProvider;
import de.learnlib.oracle.parallelism.ParallelOracle.PoolPolicy;
import net.automatalib.words.Word;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test
public class DynamicParallelOracleTest {

    @Test
    public void testDistinctQueries() {
        ParallelOracle<Void, Void> oracle = ParallelOracleBuilders.newDynamicParallelOracle(new NullOracle())
                                                                  .withBatchSize(1)
                                                                  .withPoolSize(4)
                                                                  .withPoolPolicy(PoolPolicy.CACHED)
                                                                  .create();

        try {
            List<AnswerOnceQuery> queries = createQueries(100);

            oracle.processQueries(queries);

            for (AnswerOnceQuery query : queries) {
                Assert.assertTrue(query.answered.get());
            }
        } finally {
            oracle.shutdown();
        }
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testDuplicateQueries() {
        ParallelOracle<Void, Void> oracle = ParallelOracleBuilders.newDynamicParallelOracle(new NullOracle())
                                                                  .withBatchSize(1)
                                                                  .withPoolSize(4)
                                                                  .withPoolPolicy(PoolPolicy.CACHED)
                                                                  .create();
        try {
            List<AnswerOnceQuery> queries = new ArrayList<>(createQueries(100));
            queries.add(queries.get(0));

            oracle.processQueries(queries);
        } finally {
            oracle.shutdown();
        }
    }

    @Test(timeOut = 2000)
    public void testCachedThreadCreation() {
        // Explicitly use the supplier version, to test the cached pool creation
        testThreadCreation(oracles -> ParallelOracleBuilders.newDynamicParallelOracle(new StaticOracleProvider<>(oracles))
                                                            .withBatchSize(1)
                                                            .withPoolSize(oracles.size())
                                                            .withPoolPolicy(PoolPolicy.CACHED)
                                                            .create());
    }

    @Test(timeOut = 2000)
    public void testFixedThreadCreation() {
        // Test the fixed pool creation
        testThreadCreation(oracles -> ParallelOracleBuilders.newDynamicParallelOracle(oracles)
                                                            .withBatchSize(1)
                                                            .create());
    }

    @Test(timeOut = 2000)
    public void testCachedThreadScheduling() {
        // Explicitly use the supplier version, to test the cached pool creation
        testThreadScheduling(oracles -> ParallelOracleBuilders.newDynamicParallelOracle(new StaticOracleProvider<>(oracles))
                                                              .withBatchSize(1)
                                                              .withPoolSize(2)
                                                              .withPoolPolicy(PoolPolicy.CACHED)
                                                              .create());

    }

    @Test(timeOut = 2000)
    public void testFixedThreadScheduling() {
        // Test the fixed pool creation
        testThreadScheduling(oracles -> ParallelOracleBuilders.newDynamicParallelOracle(oracles)
                                                              .withBatchSize(1)
                                                              .create());
    }

    private void testThreadCreation(Function<Collection<NullOracle>, ParallelOracle<Void, Void>> parallelOracleFunction) {

        final List<AnswerOnceQuery> queries = createQueries(10);
        final int expectedThreads = queries.size();

        final CountDownLatch latch = new CountDownLatch(expectedThreads);
        final NullOracle[] oracles = new NullOracle[expectedThreads];

        for (int i = 0; i < expectedThreads; i++) {
            oracles[i] = new NullOracle() {

                @Override
                public void processQueries(Collection<? extends Query<Void, Void>> queries) {
                    try {
                        latch.countDown();
                        latch.await();
                    } catch (InterruptedException e) {
                        throw new IllegalStateException(e);
                    }
                    super.processQueries(queries);
                }
            };
        }

        final ParallelOracle<Void, Void> oracle = parallelOracleFunction.apply(Arrays.asList(oracles));

        try {
            // this method only returns, if 'expectedThreads' threads are spawned, which all decrease the shared latch
            oracle.processQueries(queries);
        } finally {
            oracle.shutdown();
        }
    }

    private void testThreadScheduling(Function<Collection<NullOracle>, ParallelOracle<Void, Void>> parallelOracleFunction) {

        final List<AnswerOnceQuery> queries = createQueries(10);
        final CountDownLatch latch = new CountDownLatch(queries.size() - 1);

        final NullOracle awaitingOracle = new NullOracle() {

            @Override
            public void processQueries(Collection<? extends Query<Void, Void>> queries) {
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    throw new IllegalStateException(e);
                }
                super.processQueries(queries);
            }
        };

        final NullOracle countDownOracle = new NullOracle() {

            @Override
            public void processQueries(Collection<? extends Query<Void, Void>> queries) {
                latch.countDown();
                super.processQueries(queries);
            }
        };

        final ParallelOracle<Void, Void> oracle =
                parallelOracleFunction.apply(Arrays.asList(awaitingOracle, countDownOracle));

        try {
            // this method only returns, if the countDownOracle was scheduled 9 times to unblock the awaitingOracle
            oracle.processQueries(queries);
        } finally {
            oracle.shutdown();
        }
    }

    private static List<AnswerOnceQuery> createQueries(int numQueries) {
        List<AnswerOnceQuery> queries = new ArrayList<>(numQueries);

        for (int i = 0; i < numQueries; i++) {
            queries.add(new AnswerOnceQuery());
        }

        return queries;
    }

    private static class NullOracle implements MembershipOracle<Void, Void> {

        @Override
        public void processQueries(Collection<? extends Query<Void, Void>> queries) {
            for (Query<Void, Void> q : queries) {
                q.answer(null);
            }
        }
    }

    private static final class AnswerOnceQuery extends Query<Void, Void> {

        private final AtomicBoolean answered = new AtomicBoolean(false);

        @Override
        public void answer(Void output) {
            boolean wasAnswered = answered.getAndSet(true);
            if (wasAnswered) {
                throw new IllegalStateException("Query was already answered");
            }
        }

        @Override
        public Word<Void> getPrefix() {
            return Word.epsilon();
        }

        @Override
        public Word<Void> getSuffix() {
            return Word.epsilon();
        }

    }

}
