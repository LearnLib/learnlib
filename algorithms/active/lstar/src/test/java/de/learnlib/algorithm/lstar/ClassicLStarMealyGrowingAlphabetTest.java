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
package de.learnlib.algorithm.lstar;

import java.util.Collections;

import de.learnlib.algorithm.lstar.ce.ObservationTableCEXHandlers;
import de.learnlib.algorithm.lstar.closing.ClosingStrategies;
import de.learnlib.algorithm.lstar.mealy.ClassicLStarMealy;
import de.learnlib.algorithm.lstar.mealy.ExtensibleLStarMealy;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.oracle.membership.MealySimulatorOracle;
import de.learnlib.query.DefaultQuery;
import de.learnlib.testsupport.example.mealy.ExampleCoffeeMachine;
import de.learnlib.testsupport.example.mealy.ExampleCoffeeMachine.Input;
import de.learnlib.util.mealy.MealyUtil;
import net.automatalib.alphabet.impl.GrowingMapAlphabet;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.word.Word;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for specific corner-cases of Mealy-based L*-style learning.
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

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testClassicLStar() {
        final ExampleCoffeeMachine example = ExampleCoffeeMachine.createExample();
        final MealyMachine<?, Input, ?, String> automaton = example.getReferenceAutomaton();

        final GrowingMapAlphabet<Input> alphabet = new GrowingMapAlphabet<>();
        alphabet.add(Input.WATER);
        final MealyMembershipOracle<Input, String> mqo = new MealySimulatorOracle<>(automaton);
        final MembershipOracle<Input, String> smqo = MealyUtil.wrapWordOracle(mqo);

        final ClassicLStarMealy<Input, String> learner = new ClassicLStarMealy<>(alphabet, smqo);

        learner.startLearning();
        learner.addAlphabetSymbol(Input.BUTTON);
        learner.addAlphabetSymbol(Input.POD);

        MealyMachine<?, Input, ?, String> hyp = learner.getHypothesisModel();

        final DefaultQuery<Input, String> ce =
                new DefaultQuery<>(Word.fromSymbols(Input.WATER, Input.POD, Input.POD, Input.BUTTON), "coffee!");

        Assert.assertNotEquals(ce.getOutput(), hyp.computeOutput(ce.getInput()).lastSymbol());

        boolean refined = learner.refineHypothesis(ce);
        Assert.assertTrue(refined);

        hyp = learner.getHypothesisModel();
        Assert.assertEquals(ce.getOutput(), hyp.computeOutput(ce.getInput()).lastSymbol());
    }
}
