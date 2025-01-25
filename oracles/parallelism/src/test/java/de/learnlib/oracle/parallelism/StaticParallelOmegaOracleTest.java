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

import de.learnlib.oracle.ParallelOmegaOracle;
import de.learnlib.oracle.parallelism.AbstractStaticParallelOmegaOracleTest.TestOutput;
import org.testng.Assert;
import org.testng.annotations.Test;

public class StaticParallelOmegaOracleTest extends AbstractStaticParallelOmegaOracleTest<TestOutput> {

    @Override
    protected StaticParallelOmegaOracleBuilder<?, Integer, TestOutput> getBuilder() {
        TestMembershipOracle[] oracles = getOracles();
        return ParallelOracleBuilders.newStaticParallelOmegaOracle(oracles[0],
                                                                   Arrays.copyOfRange(oracles, 1, oracles.length));
    }

    @Override
    protected TestOutput extractTestOutput(TestOutput output) {
        return output;
    }

    @Test
    public void testSingleMethods() {
        final ParallelOmegaOracle<?, Integer, TestOutput> oracle = getBuilder().create();

        Assert.assertThrows(OmegaException.class, oracle::getMembershipOracle);
        Assert.assertThrows(OmegaException.class, () -> oracle.isSameState(null, null, null, null));
    }
}
