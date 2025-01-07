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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import de.learnlib.oracle.AdaptiveMembershipOracle;
import de.learnlib.oracle.ParallelAdaptiveOracle;
import de.learnlib.oracle.ThreadPool.PoolPolicy;
import de.learnlib.query.AdaptiveQuery;
import de.learnlib.query.AdaptiveQuery.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test
public abstract class AbstractDynamicParallelAdaptiveOracleTest<D> {

    @Test(dataProvider = "policies", dataProviderClass = Utils.class)
    public void testEmpty(PoolPolicy poolPolicy) {
        ParallelAdaptiveOracle<Void, D> oracle = getBuilder().withPoolPolicy(poolPolicy).create();

        try {
            oracle.processQueries(Collections.emptyList());
        } finally {
            oracle.shutdownNow();
        }
    }

    @Test(dataProvider = "policies", dataProviderClass = Utils.class)
    public void testDistinctQueries(PoolPolicy poolPolicy) {
        ParallelAdaptiveOracle<Void, D> oracle =
                getBuilder().withBatchSize(1).withPoolSize(4).withPoolPolicy(poolPolicy).create();

        try {
            List<AnswerOnceQuery<D>> queries = createQueries(100);

            oracle.processQueries(queries);

            for (AnswerOnceQuery<D> query : queries) {
                Assert.assertEquals(query.counter.get(), 0);
            }
        } finally {
            oracle.shutdown();
        }
    }

    @Test(dataProvider = "policies", dataProviderClass = Utils.class, expectedExceptions = IllegalStateException.class)
    public void testDuplicateQueries(PoolPolicy poolPolicy) {
        ParallelAdaptiveOracle<Void, D> oracle =
                getBuilder().withBatchSize(1).withPoolSize(4).withPoolPolicy(poolPolicy).create();
        try {
            List<AnswerOnceQuery<D>> queries = new ArrayList<>(createQueries(100));
            queries.add(queries.get(0));

            oracle.processQueries(queries);
        } finally {
            oracle.shutdown();
        }
    }

    protected abstract DynamicParallelAdaptiveOracleBuilder<Void, D> getBuilder();

    protected static <D> List<AnswerOnceQuery<D>> createQueries(int numQueries) {
        List<AnswerOnceQuery<D>> queries = new ArrayList<>(numQueries);

        for (int i = 0; i < numQueries; i++) {
            queries.add(new AnswerOnceQuery<>(3));
        }

        return queries;
    }

    static class NullOracle implements AdaptiveMembershipOracle<Void, Void> {

        @Override
        public void processQueries(Collection<? extends AdaptiveQuery<Void, Void>> adaptiveQueries) {
            for (AdaptiveQuery<Void, Void> q : adaptiveQueries) {
                Response response;
                do {
                    response = q.processOutput(null);
                } while (response != Response.FINISHED);
            }
        }
    }

    static final class AnswerOnceQuery<D> implements AdaptiveQuery<Void, D> {

        private final AtomicInteger counter;
        private final List<D> outputs;
        private final UUID id;

        AnswerOnceQuery(int count) {
            this(count, null);
        }

        AnswerOnceQuery(int count, UUID id) {
            this.counter = new AtomicInteger(count);
            this.id = id;
            this.outputs = Collections.synchronizedList(new ArrayList<>(count));
        }

        @Override
        public Void getInput() {
            return null;
        }

        @Override
        public Response processOutput(D out) {
            this.outputs.add(out);
            final int i = counter.decrementAndGet();

            if (i == 0) {
                return Response.FINISHED;
            } else if (i > 0) {
                return Response.SYMBOL;
            } else {
                throw new IllegalStateException("Query was answered more often than it should have been");
            }
        }

        public List<D> getOutputs() {
            return outputs;
        }

        public UUID getId() {
            return id;
        }
    }

}
