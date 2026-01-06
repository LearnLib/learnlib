/* Copyright (C) 2013-2026 TU Dortmund University
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

import de.learnlib.oracle.ParallelTimedQueryOracle;
import de.learnlib.oracle.ThreadPool.PoolPolicy;
import de.learnlib.oracle.TimedQueryOracle;
import de.learnlib.query.Query;
import de.learnlib.sul.TimedSUL;
import net.automatalib.symbol.time.InputSymbol;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.symbol.time.TimedOutput;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test
public abstract class AbstractDynamicParallelTimedQueryOracleTest<D> {

    @Test(dataProvider = "policies", dataProviderClass = Utils.class)
    public void testEmpty(PoolPolicy poolPolicy) {
        ParallelTimedQueryOracle<Void, D> oracle = getBuilder().withPoolPolicy(poolPolicy).create();

        try {
            oracle.processQueries(Collections.emptyList());
        } finally {
            oracle.shutdownNow();
        }
    }

    @Test(dataProvider = "policies", dataProviderClass = Utils.class)
    public void testDistinctQueries(PoolPolicy poolPolicy) {
        ParallelTimedQueryOracle<Void, D> oracle =
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
        ParallelTimedQueryOracle<Void, D> oracle =
                getBuilder().withBatchSize(1).withPoolSize(4).withPoolPolicy(poolPolicy).create();
        try {
            List<AnswerOnceQuery<D>> queries = new ArrayList<>(createQueries(100));
            queries.add(queries.get(0));

            oracle.processQueries(queries);
        } finally {
            oracle.shutdown();
        }
    }

    protected abstract DynamicParallelTimedQueryOracleBuilder<Void, D> getBuilder();

    protected static <D> List<AnswerOnceQuery<D>> createQueries(int numQueries) {
        List<AnswerOnceQuery<D>> queries = new ArrayList<>(numQueries);

        for (int i = 0; i < numQueries; i++) {
            queries.add(new AnswerOnceQuery<>());
        }

        return queries;
    }

    static class NullSUL implements TimedSUL<Void, Void> {

        @Override
        public void pre() {}

        @Override
        public void post() {}

        @Override
        public TimedOutput<Void> step(InputSymbol<Void> in) {
            return new TimedOutput<>(null);
        }

        @Override
        public @Nullable TimedOutput<Void> timeoutStep(long maxTime) {
            return null;
        }

        @Override
        public boolean canFork() {
            return true;
        }

        @Override
        public TimedSUL<Void, Void> fork() {
            return new NullSUL();
        }
    }

    static class NullOracle implements TimedQueryOracle<Void, Void> {

        @Override
        public TimerQueryResult<Void> queryTimers(Word<TimedInput<Void>> prefix, long maxTotalWaitingTime) {
            return new TimerQueryResult<>(false, Collections.emptyList());
        }

        @Override
        public void processQueries(Collection<? extends Query<TimedInput<Void>, Word<TimedOutput<Void>>>> queries) {
            for (Query<?, ?> q : queries) {
                q.answer(null);
            }

        }
    }

    static final class AnswerOnceQuery<O> extends Query<TimedInput<Void>, Word<TimedOutput<O>>> {

        private final AtomicBoolean answered = new AtomicBoolean(false);

        @Override
        public void answer(Word<TimedOutput<O>> output) {
            boolean wasAnswered = answered.getAndSet(true);
            if (wasAnswered) {
                throw new IllegalStateException("Query was already answered");
            }
        }

        @Override
        public Word<TimedInput<Void>> getPrefix() {
            return Word.epsilon();
        }

        @Override
        public Word<TimedInput<Void>> getSuffix() {
            return Word.epsilon();
        }
    }

}
