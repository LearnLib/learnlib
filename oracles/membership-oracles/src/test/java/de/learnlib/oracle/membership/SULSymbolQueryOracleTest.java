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

import java.util.Random;

import de.learnlib.api.SUL;
import de.learnlib.driver.util.MealySimulatorSUL;
import de.learnlib.examples.mealy.ExampleRandomMealy;
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
public class SULSymbolQueryOracleTest {

    private ExampleRandomMealy<Character, Integer> example;
    private SUL<Character, Integer> sul;

    @BeforeClass
    public void setUp() {
        final Alphabet<Character> inputs = Alphabets.characters('a', 'c');
        final Alphabet<Integer> outputs = Alphabets.integers(0, 2);
        example = ExampleRandomMealy.createExample(new Random(42), inputs, 10, outputs.toArray(new Integer[0]));
        sul = new MealySimulatorSUL<>(example.getReferenceAutomaton());
    }

    @Test
    public void testResetIdempotency() {
        final SUL<Character, Integer> mock = Mockito.spy(sul);

        final SULSymbolQueryOracle<Character, Integer> oracle = new SULSymbolQueryOracle<>(mock);

        Mockito.verify(mock, Mockito.times(0)).pre();
        Mockito.verify(mock, Mockito.times(0)).post();

        oracle.reset();
        oracle.reset();
        oracle.reset();

        Mockito.verify(mock, Mockito.times(0)).pre();
        Mockito.verify(mock, Mockito.times(0)).post();
    }

    @Test
    public void testQueriesAndCleanUp() {
        final SUL<Character, Integer> mock = Mockito.spy(sul);

        final SULSymbolQueryOracle<Character, Integer> oracle = new SULSymbolQueryOracle<>(mock);

        Mockito.verify(mock, Mockito.times(0)).pre();
        Mockito.verify(mock, Mockito.times(0)).post();

        final Word<Character> i1 = Word.fromCharSequence("abcabcabc");
        final Word<Integer> o1 = oracle.answerQuery(i1);
        oracle.reset(); // cleanup

        Assert.assertEquals(o1, example.getReferenceAutomaton().computeOutput(i1));
        Mockito.verify(mock, Mockito.times(1)).pre();
        Mockito.verify(mock, Mockito.times(1)).post();
        Mockito.verify(mock, Mockito.times(i1.size())).step(Mockito.anyChar());

        final Word<Character> i2 = Word.fromCharSequence("cba");
        final Word<Integer> o2 = oracle.answerQuery(i2);
        oracle.reset(); // cleanup
        oracle.reset(); // twice

        Assert.assertEquals(o2, example.getReferenceAutomaton().computeOutput(i2));
        Mockito.verify(mock, Mockito.times(2)).pre();
        Mockito.verify(mock, Mockito.times(2)).post();
        Mockito.verify(mock, Mockito.times(i1.size() + i2.size())).step(Mockito.anyChar());
    }
}
