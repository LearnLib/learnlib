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
package de.learnlib.testsupport;

import java.io.IOException;
import java.io.InputStream;

import com.google.common.io.CharStreams;
import de.learnlib.algorithm.LearningAlgorithm;
import de.learnlib.driver.simulator.MealySimulatorSUL;
import de.learnlib.example.mealy.ExampleCoffeeMachine;
import de.learnlib.example.mealy.ExampleCoffeeMachine.Input;
import de.learnlib.oracle.equivalence.SimulatorEQOracle;
import de.learnlib.sul.SUL;
import de.learnlib.util.Experiment;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.transducer.CompactMealy;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.common.util.IOUtil;
import net.automatalib.word.Word;
import org.checkerframework.checker.initialization.qual.UnderInitialization;

/**
 * Abstract class for tests that check the visualization of hypotheses or other internal data structure. This class'
 * constructor runs a simple learning setup (cf. {@link ExampleCoffeeMachine}) to initialize the provided learner
 * instance.
 *
 * @param <L>
 *         type of the learner
 */
public abstract class AbstractVisualizationTest<L extends LearningAlgorithm<? extends MealyMachine<?, Input, ?, String>, Input, Word<String>>> {

    protected final L learner;

    public AbstractVisualizationTest() {
        final CompactMealy<Input, String> target = ExampleCoffeeMachine.constructMachine();
        final Alphabet<Input> alphabet = target.getInputAlphabet();
        final SUL<Input, String> sul = new MealySimulatorSUL<>(target);

        this.learner = getLearnerBuilder(alphabet, sul);

        final Experiment<?> experiment = new Experiment<>(learner, new SimulatorEQOracle<>(target), alphabet);

        experiment.run();
    }

    protected String resourceAsString(String resourceName) throws IOException {
        try (InputStream is = getClass().getResourceAsStream(resourceName)) {
            assert is != null;
            return CharStreams.toString(IOUtil.asBufferedUTF8Reader(is));
        }
    }

    protected abstract L getLearnerBuilder(@UnderInitialization AbstractVisualizationTest<L> this,
                                           Alphabet<Input> alphabet,
                                           SUL<Input, String> sul);
}
