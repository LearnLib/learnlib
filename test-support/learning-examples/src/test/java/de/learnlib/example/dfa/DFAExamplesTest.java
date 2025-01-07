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
package de.learnlib.example.dfa;

import de.learnlib.testsupport.example.dfa.DFABenchmarks;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DFAExamplesTest {

    @Test
    public void testPots2() {
        Assert.assertNotNull(DFABenchmarks.loadPots2());
    }

    @Test
    public void testPots3() {
        Assert.assertNotNull(DFABenchmarks.loadPots3());
    }

    @Test
    public void testPeterson2() {
        Assert.assertNotNull(DFABenchmarks.loadPeterson2());
    }

    @Test
    public void testPeterson3() {
        Assert.assertNotNull(DFABenchmarks.loadPeterson3());
    }
}
