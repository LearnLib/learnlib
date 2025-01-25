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

import java.util.Collections;
import java.util.List;
import java.util.Random;

import de.learnlib.driver.simulator.StateLocalInputMealySimulatorSUL;
import de.learnlib.sul.StateLocalInputSUL;
import de.learnlib.testsupport.example.mealy.ExampleRandomStateLocalInputMealy;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class StateLocalInputSULAdaptiveOracleTest {

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
    public void testNoopIdempotency() {
        final StateLocalInputSUL<Character, Integer> mock = Mockito.spy(sul);
        Mockito.doAnswer(invocation -> Collections.singleton('a')).when(mock).currentlyEnabledInputs();

        final SULAdaptiveOracle<Character, Integer> oracle =
                new StateLocalInputSULAdaptiveOracle<>(mock, example.getUndefinedOutput());

        Mockito.verify(mock, Mockito.times(0)).pre();
        Mockito.verify(mock, Mockito.times(0)).post();
        Mockito.verify(mock, Mockito.times(0)).step(Mockito.anyChar());
        Mockito.verify(mock, Mockito.times(0)).currentlyEnabledInputs();

        oracle.processQueries(Collections.emptyList());
        oracle.processQueries(Collections.emptyList());
        oracle.processQueries(Collections.emptyList());

        Mockito.verify(mock, Mockito.times(0)).pre();
        Mockito.verify(mock, Mockito.times(0)).post();
        Mockito.verify(mock, Mockito.times(0)).step(Mockito.anyChar());
        Mockito.verify(mock, Mockito.times(0)).currentlyEnabledInputs();
    }

    @Test
    public void testQueriesAndCleanUp() {
        final StateLocalInputSUL<Character, Integer> mock = Mockito.spy(sul);
        Mockito.doAnswer(invocation -> Collections.singleton('a')).when(mock).currentlyEnabledInputs();

        final SULAdaptiveOracle<Character, Integer> oracle =
                new StateLocalInputSULAdaptiveOracle<>(mock, example.getUndefinedOutput());

        Mockito.verify(mock, Mockito.times(0)).pre();
        Mockito.verify(mock, Mockito.times(0)).post();
        Mockito.verify(mock, Mockito.times(0)).step(Mockito.anyChar());
        Mockito.verify(mock, Mockito.times(0)).currentlyEnabledInputs();

        final Word<Character> i1 = Word.fromString("abcabcabc");
        final Word<Character> i2 = Word.fromString("aaaaa");
        final AdaptiveTestQuery<Character, Integer> query = new AdaptiveTestQuery<>(i1, i2);

        oracle.processQuery(query);

        final List<WordBuilder<Integer>> outputs = query.getOutputs();
        Assert.assertEquals(outputs.size(), 2);

        final Word<Integer> o1 = outputs.get(0).toWord();
        final Word<Integer> o2 = outputs.get(1).toWord();

        Assert.assertEquals(o1.firstSymbol(), example.getReferenceAutomaton().computeOutput(i1).firstSymbol());
        Assert.assertEquals(o1.subWord(1),
                            Word.fromList(Collections.nCopies(i1.size() - 1, example.getUndefinedOutput())));
        Assert.assertEquals(o2, example.getReferenceAutomaton().computeOutput(i2));

        Mockito.verify(mock, Mockito.times(2)).pre();
        Mockito.verify(mock, Mockito.times(2)).post();
        Mockito.verify(mock, Mockito.times(1 + i2.size())).step(Mockito.anyChar());
        Mockito.verify(mock, Mockito.times(2 + i2.size())).currentlyEnabledInputs();
    }
}
