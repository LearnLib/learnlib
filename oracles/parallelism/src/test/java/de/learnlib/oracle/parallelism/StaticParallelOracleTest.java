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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;

import de.learnlib.oracle.ParallelOracle;
import de.learnlib.oracle.parallelism.AbstractStaticParallelOracleTest.TestOutput;
import de.learnlib.query.DefaultQuery;
import org.testng.annotations.Test;

public class StaticParallelOracleTest extends AbstractStaticParallelOracleTest<TestOutput> {

    @Override
    protected StaticParallelOracleBuilder<Integer, TestOutput> getBuilder() {
        TestMembershipOracle[] oracles = getOracles();
        return ParallelOracleBuilders.newStaticParallelOracle(oracles[0],
                                                              Arrays.copyOfRange(oracles, 1, oracles.length));
    }

    @Override
    protected TestOutput extractTestOutput(TestOutput output) {
        return output;
    }

    @Test
    public void testCustomExecutorLessThreadsAvailable() {
        // this tests provides a list of 10 oracles
        ParallelOracle<Integer, TestOutput> oracle = getBuilder().withCustomExecutor(Executors.newFixedThreadPool(5)).create();
        try {
            List<DefaultQuery<Integer, TestOutput>> queries = new ArrayList<>(createQueries(100));
            // scheduling 10 batches on 5 threads should not cause any problems
            oracle.processQueries(queries);
        } finally {
            oracle.shutdown();
        }
    }
}
