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
package de.learnlib.algorithm.ttt;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import de.learnlib.algorithm.ttt.mealy.TTTLearnerMealy;
import de.learnlib.algorithm.ttt.mealy.TTTLearnerMealyBuilder;
import de.learnlib.oracle.membership.SULOracle;
import de.learnlib.testsupport.VisualizationUtils;
import de.learnlib.testsupport.example.mealy.ExampleCoffeeMachine.Input;
import net.automatalib.common.util.IOUtil;
import net.automatalib.serialization.dot.GraphDOT;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TTTVisualizationTest {

    private final TTTLearnerMealy<Input, String> learner;

    public TTTVisualizationTest() {
        this.learner =
                VisualizationUtils.runExperiment((alphabet, sul) -> new TTTLearnerMealyBuilder<Input, String>().withAlphabet(
                        alphabet).withOracle(new SULOracle<>(sul)).create());
    }

    @Test
    public void testVisualizeHyp() throws IOException {
        try (InputStream is = TTTVisualizationTest.class.getResourceAsStream("/hyp.dot")) {
            final String expectedHyp = IOUtil.toString(IOUtil.asBufferedUTF8Reader(is));

            final StringWriter actualHyp = new StringWriter();
            GraphDOT.write(this.learner.getHypothesisDS(), actualHyp);

            Assert.assertEquals(actualHyp.toString(), expectedHyp);
        }
    }

    @Test
    public void testVisualizeDT() throws IOException {
        try (InputStream is = TTTVisualizationTest.class.getResourceAsStream("/dt.dot")) {
            final String expectedDT = IOUtil.toString(IOUtil.asBufferedUTF8Reader(is));

            final StringWriter actualDT = new StringWriter();
            GraphDOT.write(this.learner.getDiscriminationTree(), actualDT);

            Assert.assertEquals(actualDT.toString(), expectedDT);
        }
    }
}
