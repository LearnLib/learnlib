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
package de.learnlib.algorithm.adt.learner;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import de.learnlib.oracle.membership.SULAdaptiveOracle;
import de.learnlib.testsupport.VisualizationUtils;
import de.learnlib.testsupport.example.mealy.ExampleCoffeeMachine.Input;
import net.automatalib.common.util.IOUtil;
import net.automatalib.serialization.dot.GraphDOT;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ADTVisualizationTest {

    private final ADTLearner<Input, String> learner;

    public ADTVisualizationTest() {
        this.learner =
                VisualizationUtils.runExperiment((alphabet, sul) -> new ADTLearnerBuilder<Input, String>().withAlphabet(
                        alphabet).withOracle(new SULAdaptiveOracle<>(sul)).create());
    }

    @Test
    public void testVisualization() throws IOException {
        try (InputStream is = ADTVisualizationTest.class.getResourceAsStream("/adt.dot")) {
            final String expectedADT = IOUtil.toString(IOUtil.asBufferedUTF8Reader(is));
            final StringWriter actualADT = new StringWriter();

            GraphDOT.write(this.learner.getADT().getRoot(), actualADT);

            Assert.assertEquals(actualADT.toString(), expectedADT);
        }
    }
}
