/* Copyright (C) 2013-2023 TU Dortmund
 * This file is part of LearnLib, http://www.learnlib.de/.
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
package de.learnlib.algorithm.observationpack;

import java.io.IOException;
import java.io.StringWriter;

import de.learnlib.algorithm.observationpack.mealy.OPLearnerMealy;
import de.learnlib.algorithm.observationpack.mealy.OPLearnerMealyBuilder;
import de.learnlib.example.mealy.ExampleCoffeeMachine.Input;
import de.learnlib.oracle.membership.SULOracle;
import de.learnlib.sul.SUL;
import de.learnlib.testsupport.AbstractVisualizationTest;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.serialization.dot.GraphDOT;
import org.checkerframework.checker.initialization.qual.UnderInitialization;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DTVisualizationTest extends AbstractVisualizationTest<OPLearnerMealy<Input, String>> {

    @Override
    protected OPLearnerMealy<Input, String> getLearnerBuilder(@UnderInitialization DTVisualizationTest this,
                                                              Alphabet<Input> alphabet,
                                                              SUL<Input, String> sul) {
        return new OPLearnerMealyBuilder<Input, String>().withAlphabet(alphabet)
                                                         .withOracle(new SULOracle<>(sul))
                                                         .create();
    }

    @Test
    public void testVisualizeHyp() throws IOException {
        final String expectedHyp = resourceAsString("/hyp.dot");

        final StringWriter actualHyp = new StringWriter();
        GraphDOT.write(super.learner.getHypothesisDS(), actualHyp);

        Assert.assertEquals(actualHyp.toString(), expectedHyp);
    }

    @Test
    public void testVisualizeDT() throws IOException {
        final String expectedDT = resourceAsString("/dt.dot");

        final StringWriter actualDT = new StringWriter();
        GraphDOT.write(super.learner.getDiscriminationTree(), actualDT);

        Assert.assertEquals(actualDT.toString(), expectedDT);
    }
}
