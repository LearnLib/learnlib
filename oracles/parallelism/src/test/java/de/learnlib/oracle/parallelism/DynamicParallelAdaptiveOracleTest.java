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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import de.learnlib.oracle.ParallelAdaptiveOracle;
import de.learnlib.oracle.ThreadPool.PoolPolicy;
import de.learnlib.query.AdaptiveQuery;
import org.testng.annotations.Test;

public class DynamicParallelAdaptiveOracleTest extends AbstractDynamicParallelAdaptiveOracleTest<Void> {

    @Override
    protected DynamicParallelAdaptiveOracleBuilder<Void, Void> getBuilder() {
        return ParallelOracleBuilders.newDynamicParallelAdaptiveOracle(Arrays.asList(new NullOracle(),
                                                                                     new NullOracle(),
                                                                                     new NullOracle()));
    }

    @Test(dataProvider = "policies", dataProviderClass = Utils.class, timeOut = 2000)
    public void testThreadCreation(PoolPolicy poolPolicy) {

        final List<AnswerOnceQuery<Void>> queries = createQueries(10);
        final int expectedThreads = queries.size();

        final CountDownLatch latch = new CountDownLatch(expectedThreads);
        final NullOracle[] oracles = new NullOracle[expectedThreads];

        for (int i = 0; i < expectedThreads; i++) {
            oracles[i] = new NullOracle() {

                @Override
                public void processQueries(Collection<? extends AdaptiveQuery<Void, Void>> queries) {
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

        final ParallelAdaptiveOracle<Void, Void> oracle = ParallelOracleBuilders.newDynamicParallelAdaptiveOracle(
                                                                                        oracles[0],
                                                                                        Arrays.copyOfRange(oracles, 1, oracles.length))
                                                                                .withBatchSize(1)
                                                                                .withPoolSize(oracles.length)
                                                                                .withPoolPolicy(poolPolicy)
                                                                                .create();

        try {
            // this method only returns, if 'expectedThreads' threads are spawned, which all decrease the shared latch
            oracle.processQueries(queries);
        } finally {
            oracle.shutdown();
        }
    }

    @Test(dataProvider = "policies", dataProviderClass = Utils.class, timeOut = 2000)
    public void testThreadScheduling(PoolPolicy poolPolicy) {

        final List<AnswerOnceQuery<Void>> queries = createQueries(10);
        final CountDownLatch latch = new CountDownLatch(queries.size() - 1);

        final NullOracle awaitingOracle = new NullOracle() {

            @Override
            public void processQueries(Collection<? extends AdaptiveQuery<Void, Void>> queries) {
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
            public void processQueries(Collection<? extends AdaptiveQuery<Void, Void>> queries) {
                latch.countDown();
                super.processQueries(queries);
            }
        };

        final ParallelAdaptiveOracle<Void, Void> oracle =
                ParallelOracleBuilders.newDynamicParallelAdaptiveOracle(awaitingOracle, countDownOracle)
                                      .withPoolSize(2)
                                      .withPoolPolicy(poolPolicy)
                                      .create();

        try {
            // this method only returns, if the countDownOracle was scheduled 9 times to unblock the awaitingOracle
            oracle.processQueries(queries);
        } finally {
            oracle.shutdown();
        }
    }
}
