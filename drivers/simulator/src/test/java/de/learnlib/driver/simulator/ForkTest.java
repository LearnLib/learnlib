/* Copyright (C) 2013-2026 TU Dortmund University
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
package de.learnlib.driver.simulator;

import de.learnlib.testsupport.example.mealy.ExampleCoffeeMachine;
import de.learnlib.testsupport.example.mealy.ExampleCoffeeMachine.Input;
import de.learnlib.testsupport.example.mmlt.MMLTExamples;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.symbol.time.TimedOutput;
import net.automatalib.word.Word;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ForkTest {

    @Test
    public void testMealy() {
        var sul = new MealySimulatorSUL<>(ExampleCoffeeMachine.constructMachine());

        Assert.assertTrue(sul.canFork());

        // check delegation
        var spy = Mockito.spy(sul);
        var fork = spy.fork();

        Assert.assertNotNull(fork);

        fork.pre();
        fork.step(Input.CLEAN);
        fork.post();
        fork.canFork();
        fork.fork();

        Mockito.verify(spy, Mockito.only()).fork();

        // check independence
        spy.pre();
        spy.step(Input.WATER);
        spy.step(Input.POD);

        fork.pre();
        fork.post();

        var out = spy.step(Input.BUTTON);
        Assert.assertEquals(out, ExampleCoffeeMachine.OUT_COFFEE);

        spy.post();
    }

    @Test
    public void testMMLT() {
        var mmlt = MMLTExamples.sensorCollector().getReferenceAutomaton();
        var alphabet = mmlt.getInputAlphabet();
        var sul = new MMLTSimulatorSUL<>(mmlt);
        var input = TimedInput.input(alphabet.getSymbol(0));

        Assert.assertTrue(sul.canFork());

        // check delegation
        var spy = Mockito.spy(sul);
        var fork = spy.fork();

        Assert.assertNotNull(fork);

        fork.pre();
        fork.step(input);
        fork.follow(Word.fromLetter(input));
        fork.timeoutStep(2);
        fork.post();
        fork.canFork();
        fork.fork();

        Mockito.verify(spy, Mockito.only()).fork();

        // check independence
        spy.pre();
        spy.step(input);

        fork.pre();
        fork.post();

        var out = spy.timeoutStep(3);
        Assert.assertEquals(out, new TimedOutput<>("part", 3));

        spy.post();
    }

    @Test
    public void testObservable() {
        var sul = new ObservableMealySimulatorSUL<>(ExampleCoffeeMachine.constructMachine());

        Assert.assertTrue(sul.canFork());

        // check delegation
        var spy = Mockito.spy(sul);
        var fork = spy.fork();

        Assert.assertNotNull(fork);

        fork.pre();
        fork.step(Input.CLEAN);
        fork.deepCopies();
        fork.getState();
        fork.post();
        fork.canFork();
        fork.fork();

        Mockito.verify(spy, Mockito.only()).fork();

        // check independence
        spy.pre();
        spy.step(Input.WATER);
        spy.step(Input.POD);

        fork.pre();
        fork.post();

        var out = spy.step(Input.BUTTON);
        Assert.assertEquals(out, ExampleCoffeeMachine.OUT_COFFEE);

        spy.post();
    }

    @Test
    public void testSLI() {
        var sul = new StateLocalInputMealySimulatorSUL<>(ExampleCoffeeMachine.constructMachine());

        Assert.assertTrue(sul.canFork());

        // check delegation
        var spy = Mockito.spy(sul);
        var fork = spy.fork();

        Assert.assertNotNull(fork);

        fork.pre();
        fork.step(Input.CLEAN);
        fork.currentlyEnabledInputs();
        fork.post();
        fork.canFork();
        fork.fork();

        Mockito.verify(spy, Mockito.only()).fork();

        // check independence
        spy.pre();
        spy.step(Input.WATER);
        spy.step(Input.POD);

        fork.pre();
        fork.post();

        var out = spy.step(Input.BUTTON);
        Assert.assertEquals(out, ExampleCoffeeMachine.OUT_COFFEE);

        spy.post();
    }
}


