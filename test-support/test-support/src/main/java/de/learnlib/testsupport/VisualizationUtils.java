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
package de.learnlib.testsupport;

import java.util.function.BiFunction;

import de.learnlib.algorithm.LearningAlgorithm;
import de.learnlib.driver.simulator.MealySimulatorSUL;
import de.learnlib.query.DefaultQuery;
import de.learnlib.sul.SUL;
import de.learnlib.testsupport.example.mealy.ExampleCoffeeMachine;
import de.learnlib.testsupport.example.mealy.ExampleCoffeeMachine.Input;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.automaton.transducer.impl.CompactMealy;
import net.automatalib.util.automaton.Automata;
import net.automatalib.word.Word;

/**
 * Utility class for running a simple learning setup (cf. {@link ExampleCoffeeMachine}) to initialize a provided learner
 * instance.
 */
public final class VisualizationUtils {

    private VisualizationUtils() {
        // prevent initialization
    }

    public static <L extends LearningAlgorithm<? extends MealyMachine<?, Input, ?, String>, Input, Word<String>>> L runExperiment(
            BiFunction<Alphabet<Input>, SUL<Input, String>, L> builder) {
        final CompactMealy<Input, String> target = ExampleCoffeeMachine.constructMachine();
        final Alphabet<Input> alphabet = target.getInputAlphabet();
        final SUL<Input, String> sul = new MealySimulatorSUL<>(target);

        final L learner = builder.apply(alphabet, sul);
        learner.startLearning();

        MealyMachine<?, Input, ?, String> hyp = learner.getHypothesisModel();
        Word<Input> ce;

        while ((ce = Automata.findSeparatingWord(target, hyp, alphabet)) != null) {
            final DefaultQuery<Input, Word<String>> q = new DefaultQuery<>(ce, target.computeOutput(ce));
            while (learner.refineHypothesis(q)) {
                // refine exhaustively
            }
            hyp = learner.getHypothesisModel();
        }

        return learner;
    }
}
