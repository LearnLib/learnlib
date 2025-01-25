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

import java.util.concurrent.atomic.AtomicInteger;

import de.learnlib.oracle.parallelism.Utils.TestSULOutput;
import net.automatalib.word.Word;

public class StaticParallelSULTest extends AbstractStaticParallelOracleTest<Word<TestSULOutput>> {

    @Override
    protected StaticParallelOracleBuilder<Integer, Word<TestSULOutput>> getBuilder() {
        // since we fork our initial SUL, start at -1
        return ParallelOracleBuilders.newStaticParallelOracle(new TestSUL(new AtomicInteger(-1)));
    }

    @Override
    protected TestOutput extractTestOutput(Word<TestSULOutput> output) {
        return Utils.extractSULOutput(output);
    }

    @Override
    protected int getMinQueryLength() {
        return 1;
    }
}
