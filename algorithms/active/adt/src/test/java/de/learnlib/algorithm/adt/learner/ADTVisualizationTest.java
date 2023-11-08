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
package de.learnlib.algorithm.adt.learner;

import java.io.IOException;
import java.io.StringWriter;

import de.learnlib.example.mealy.ExampleCoffeeMachine.Input;
import de.learnlib.oracle.membership.SULSymbolQueryOracle;
import de.learnlib.sul.SUL;
import de.learnlib.testsupport.AbstractVisualizationTest;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.serialization.dot.GraphDOT;
import org.checkerframework.checker.initialization.qual.UnderInitialization;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ADTVisualizationTest extends AbstractVisualizationTest<ADTLearner<Input, String>> {

    @Override
    protected ADTLearner<Input, String> getLearnerBuilder(@UnderInitialization ADTVisualizationTest this,
                                                          Alphabet<Input> alphabet,
                                                          SUL<Input, String> sul) {
        return new ADTLearnerBuilder<Input, String>().withAlphabet(alphabet)
                                                     .withOracle(new SULSymbolQueryOracle<>(sul))
                                                     .create();
    }

    @Test
    public void testVisualization() throws IOException {
        final String expectedADT = resourceAsString("/adt.dot");

        final StringWriter actualADT = new StringWriter();
        GraphDOT.write(super.learner.getADT().getRoot(), actualADT);

        Assert.assertEquals(actualADT.toString(), expectedADT);
    }
}
