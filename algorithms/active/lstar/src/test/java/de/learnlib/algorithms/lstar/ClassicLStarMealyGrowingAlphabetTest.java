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
package de.learnlib.algorithms.lstar;

import java.util.Collections;

import de.learnlib.algorithms.lstar.ce.ObservationTableCEXHandlers;
import de.learnlib.algorithms.lstar.closing.ClosingStrategies;
import de.learnlib.algorithms.lstar.mealy.ExtensibleLStarMealy;
import de.learnlib.api.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.examples.mealy.ExampleCoffeeMachine;
import de.learnlib.examples.mealy.ExampleCoffeeMachine.Input;
import de.learnlib.oracle.membership.SimulatorOracle.MealySimulatorOracle;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.words.Word;
import net.automatalib.words.impl.GrowingMapAlphabet;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for specific corner-cases of Mealy-based L*-style learning.
 *
 * @author frohme
 */
public class ClassicLStarMealyGrowingAlphabetTest {

    @Test
    public void testConsistencyCheck() {
        final ExampleCoffeeMachine example = ExampleCoffeeMachine.createExample();
        final MealyMachine<?, Input, ?, String> automaton = example.getReferenceAutomaton();

        final GrowingMapAlphabet<Input> alphabet = new GrowingMapAlphabet<>();
        alphabet.add(Input.WATER);

        final MealyMembershipOracle<Input, String> mqo = new MealySimulatorOracle<>(automaton);
        final ExtensibleLStarMealy<Input, String> learner = new ExtensibleLStarMealy<>(alphabet,
                                                                                       mqo,
                                                                                       Collections.emptyList(),
                                                                                       ObservationTableCEXHandlers.CLASSIC_LSTAR,
                                                                                       ClosingStrategies.CLOSE_FIRST);

        learner.startLearning();
        learner.addAlphabetSymbol(Input.BUTTON);
        learner.addAlphabetSymbol(Input.POD);

        MealyMachine<?, Input, ?, String> hyp = learner.getHypothesisModel();

        final DefaultQuery<Input, Word<String>> ce = new DefaultQuery<>(Word.epsilon(),
                                                                        Word.fromSymbols(Input.WATER,
                                                                                         Input.POD,
                                                                                         Input.POD,
                                                                                         Input.BUTTON),
                                                                        Word.fromSymbols("ok", "ok", "ok", "coffee!"));

        Assert.assertNotEquals(ce.getOutput(), hyp.computeOutput(ce.getInput()));

        final boolean refined = learner.refineHypothesis(ce);
        Assert.assertTrue(refined);

        hyp = learner.getHypothesisModel();
        Assert.assertEquals(ce.getOutput(), hyp.computeOutput(ce.getInput()));
    }
}
