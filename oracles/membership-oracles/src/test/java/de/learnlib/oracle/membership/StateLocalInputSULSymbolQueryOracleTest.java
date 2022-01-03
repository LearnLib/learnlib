/* Copyright (C) 2013-2022 TU Dortmund
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
package de.learnlib.oracle.membership;

import java.util.Collections;
import java.util.Random;

import de.learnlib.api.StateLocalInputSUL;
import de.learnlib.driver.util.StateLocalInputMealySimulatorSUL;
import de.learnlib.examples.mealy.ExampleRandomStateLocalInputMealy;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author frohme
 */
public class StateLocalInputSULSymbolQueryOracleTest {

    private ExampleRandomStateLocalInputMealy<Character, Integer> example;
    private StateLocalInputSUL<Character, Integer> sul;

    @BeforeClass
    public void setUp() {
        final Alphabet<Character> inputs = Alphabets.characters('a', 'c');
        final Alphabet<Integer> outputs = Alphabets.integers(0, 2);
        example = ExampleRandomStateLocalInputMealy.createExample(new Random(42),
                                                                  inputs,
                                                                  10,
                                                                  -1,
                                                                  outputs.toArray(new Integer[0]));
        sul = new StateLocalInputMealySimulatorSUL<>(example.getReferenceAutomaton());
    }

    @Test
    public void testResetIdempotency() {
        final StateLocalInputSUL<Character, Integer> mock = Mockito.spy(sul);
        Mockito.doAnswer(invocation -> Collections.singleton('a')).when(mock).currentlyEnabledInputs();

        final SULSymbolQueryOracle<Character, Integer> oracle =
                new StateLocalInputSULSymbolQueryOracle<>(mock, example.getUndefinedOutput());

        Mockito.verify(mock, Mockito.times(0)).pre();
        Mockito.verify(mock, Mockito.times(0)).post();
        Mockito.verify(mock, Mockito.times(0)).currentlyEnabledInputs();

        oracle.reset();
        oracle.reset();
        oracle.reset();

        Mockito.verify(mock, Mockito.times(0)).pre();
        Mockito.verify(mock, Mockito.times(0)).post();
        Mockito.verify(mock, Mockito.times(0)).currentlyEnabledInputs();
    }

    @Test
    public void testQueriesAndCleanUp() {
        final StateLocalInputSUL<Character, Integer> mock = Mockito.spy(sul);
        Mockito.doAnswer(invocation -> Collections.singleton('a')).when(mock).currentlyEnabledInputs();

        final SULSymbolQueryOracle<Character, Integer> oracle =
                new StateLocalInputSULSymbolQueryOracle<>(mock, example.getUndefinedOutput());

        Mockito.verify(mock, Mockito.times(0)).pre();
        Mockito.verify(mock, Mockito.times(0)).post();

        final Word<Character> i1 = Word.fromCharSequence("abcabcabc");
        final Word<Integer> o1 = oracle.answerQuery(i1);
        oracle.reset(); // cleanup

        Assert.assertEquals(o1.firstSymbol(), example.getReferenceAutomaton().computeOutput(i1).firstSymbol());
        Assert.assertEquals(o1.subWord(1),
                            Word.fromList(Collections.nCopies(i1.size() - 1, example.getUndefinedOutput())));
        Mockito.verify(mock, Mockito.times(1)).pre();
        Mockito.verify(mock, Mockito.times(1)).post();
        Mockito.verify(mock, Mockito.times(2)).currentlyEnabledInputs();
        Mockito.verify(mock, Mockito.times(1)).step(Mockito.anyChar());

        final Word<Character> i2 = Word.fromCharSequence("aaaaa");
        final Word<Integer> o2 = oracle.answerQuery(i2);
        oracle.reset(); // cleanup
        oracle.reset(); // twice

        Assert.assertEquals(o2, example.getReferenceAutomaton().computeOutput(i2));
        Mockito.verify(mock, Mockito.times(2)).pre();
        Mockito.verify(mock, Mockito.times(2)).post();
        Mockito.verify(mock, Mockito.times(2 + i2.size())).currentlyEnabledInputs();
        Mockito.verify(mock, Mockito.times(1 + i2.size())).step(Mockito.anyChar());
    }
}
