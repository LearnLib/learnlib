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
package de.learnlib.oracle.membership;

import java.util.Arrays;
import java.util.List;

import de.learnlib.driver.simulator.ObservableMealySimulatorSUL;
import de.learnlib.oracle.OmegaMembershipOracle.MealyOmegaMembershipOracle;
import de.learnlib.query.OmegaQuery;
import de.learnlib.sul.ObservableSUL;
import de.learnlib.testsupport.example.mealy.ExampleCoffeeMachine;
import de.learnlib.testsupport.example.mealy.ExampleCoffeeMachine.Input;
import net.automatalib.automaton.transducer.impl.CompactMealy;
import net.automatalib.word.Word;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SULOmegaOracleTest {

    @Test
    public void testForkableMealySimulatorOmegaOracle() {
        final CompactMealy<Input, String> mealy = ExampleCoffeeMachine.constructMachine();
        final ObservableSUL<Integer, Input, String> sul = new ObservableMealySimulatorSUL<>(mealy);

        queryAndValidateCoffeeMachine(sul);
    }

    @Test
    public void testDeepCopyMealySimulatorOmegaOracle() {
        final CompactMealy<Input, String> mealy = ExampleCoffeeMachine.constructMachine();
        final ObservableSUL<Integer, Input, String> sul = new ObservableMealySimulatorSUL<>(mealy);
        final ObservableSUL<Integer, Input, String> mock = Mockito.spy(sul);

        Mockito.doAnswer(invocation -> true).when(mock).deepCopies();
        Mockito.doAnswer(invocation -> false).when(mock).canFork();

        queryAndValidateCoffeeMachine(mock);

        // when we cannot fork, this mock object should be invoked correctly
        Mockito.verify(mock, Mockito.times(3)).pre();
        Mockito.verify(mock, Mockito.times(3)).post();
    }

    @Test
    public void testOmegaOracleValidation() {
        final ObservableSUL<?, ?, ?> mock = Mockito.mock(ObservableSUL.class);

        Mockito.doAnswer(invocation -> false).when(mock).deepCopies();
        Mockito.doAnswer(invocation -> false).when(mock).canFork();

        Assert.assertThrows(IllegalArgumentException.class, () -> AbstractSULOmegaOracle.newOracle(mock, true));
        Assert.assertThrows(IllegalArgumentException.class, () -> AbstractSULOmegaOracle.newOracle(mock, false));
    }

    private void queryAndValidateCoffeeMachine(ObservableSUL<?, Input, String> sul) {
        final MealyOmegaMembershipOracle<?, Input, String> omq = AbstractSULOmegaOracle.newOracle(sul);

        final OmegaQuery<Input, Word<String>> infiniteCoffee = new OmegaQuery<>(Word.epsilon(),
                                                                                Word.fromSymbols(Input.POD,
                                                                                                 Input.WATER,
                                                                                                 Input.BUTTON,
                                                                                                 Input.CLEAN),
                                                                                2);
        final OmegaQuery<Input, Word<String>> broken = new OmegaQuery<>(Word.fromSymbols(Input.POD, Input.BUTTON),
                                                                        Word.fromSymbols(Input.WATER, Input.BUTTON),
                                                                        2);
        final OmegaQuery<Input, Word<String>> litterbug =
                new OmegaQuery<>(Word.epsilon(), Word.fromSymbols(Input.POD, Input.WATER, Input.BUTTON), 2);

        final List<OmegaQuery<Input, Word<String>>> queries = Arrays.asList(infiniteCoffee, broken, litterbug);
        omq.processQueries(queries);

        Assert.assertTrue(infiniteCoffee.isUltimatelyPeriodic());
        Assert.assertEquals(infiniteCoffee.getOutput(),
                            Word.fromSymbols(ExampleCoffeeMachine.OUT_OK,
                                             ExampleCoffeeMachine.OUT_OK,
                                             ExampleCoffeeMachine.OUT_COFFEE,
                                             ExampleCoffeeMachine.OUT_OK));

        Assert.assertTrue(broken.isUltimatelyPeriodic());
        Assert.assertEquals(broken.getOutput(),
                            Word.fromSymbols(ExampleCoffeeMachine.OUT_OK,
                                             ExampleCoffeeMachine.OUT_ERROR,
                                             ExampleCoffeeMachine.OUT_ERROR,
                                             ExampleCoffeeMachine.OUT_ERROR));

        Assert.assertFalse(litterbug.isUltimatelyPeriodic());
        // after two loops, we cannot observe periodicity
        Assert.assertNull(litterbug.getOutput());
    }
}
