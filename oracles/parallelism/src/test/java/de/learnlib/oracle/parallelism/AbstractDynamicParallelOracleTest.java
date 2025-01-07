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
import java.util.concurrent.atomic.AtomicBoolean;

import de.learnlib.oracle.MembershipOracle;
import de.learnlib.oracle.ParallelOracle;
import de.learnlib.oracle.ThreadPool.PoolPolicy;
import de.learnlib.query.Query;
import de.learnlib.sul.StateLocalInputSUL;
import net.automatalib.word.Word;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test
public abstract class AbstractDynamicParallelOracleTest<D> {

    @Test(dataProvider = "policies", dataProviderClass = Utils.class)
    public void testEmpty(PoolPolicy poolPolicy) {
        ParallelOracle<Void, D> oracle = getBuilder().withBatchSize(3).withPoolPolicy(poolPolicy).create();

        try {
            oracle.processQueries(Collections.emptyList());
        } finally {
            oracle.shutdownNow();
        }
    }

    @Test(dataProvider = "policies", dataProviderClass = Utils.class)
    public void testDistinctQueries(PoolPolicy poolPolicy) {
        ParallelOracle<Void, D> oracle =
                getBuilder().withBatchSize(1).withPoolSize(4).withPoolPolicy(poolPolicy).create();

        try {
            List<AnswerOnceQuery<D>> queries = createQueries(100);

            oracle.processQueries(queries);

            for (AnswerOnceQuery<D> query : queries) {
                Assert.assertTrue(query.answered.get());
            }
        } finally {
            oracle.shutdown();
        }
    }

    @Test(dataProvider = "policies", dataProviderClass = Utils.class, expectedExceptions = IllegalStateException.class)
    public void testDuplicateQueries(PoolPolicy poolPolicy) {
        ParallelOracle<Void, D> oracle = getBuilder().withBatchSize(3).withPoolPolicy(poolPolicy).create();
        try {
            List<AnswerOnceQuery<D>> queries = new ArrayList<>(createQueries(100));
            queries.add(queries.get(0));

            oracle.processQueries(queries);
        } finally {
            oracle.shutdown();
        }
    }

    protected abstract DynamicParallelOracleBuilder<Void, D> getBuilder();

    protected static <D> List<AnswerOnceQuery<D>> createQueries(int numQueries) {
        List<AnswerOnceQuery<D>> queries = new ArrayList<>(numQueries);

        for (int i = 0; i < numQueries; i++) {
            queries.add(new AnswerOnceQuery<>());
        }

        return queries;
    }

    static class NullSUL implements StateLocalInputSUL<Void, Void> {

        @Override
        public Collection<Void> currentlyEnabledInputs() {
            return Collections.emptyList();
        }

        @Override
        public void pre() {}

        @Override
        public void post() {}

        @Override
        public Void step(Void in) {
            return null;
        }

        @Override
        public boolean canFork() {
            return true;
        }

        @Override
        public StateLocalInputSUL<Void, Void> fork() {
            return new NullSUL();
        }
    }

    static class NullOracle implements MembershipOracle<Void, Void> {

        @Override
        public void processQueries(Collection<? extends Query<Void, Void>> queries) {
            for (Query<Void, Void> q : queries) {
                q.answer(null);
            }
        }
    }

    static final class AnswerOnceQuery<D> extends Query<Void, D> {

        private final AtomicBoolean answered = new AtomicBoolean(false);

        @Override
        public void answer(D output) {
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
