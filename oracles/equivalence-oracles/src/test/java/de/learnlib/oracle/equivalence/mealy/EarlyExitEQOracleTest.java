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
package de.learnlib.oracle.equivalence.mealy;

import java.util.stream.Stream;

import de.learnlib.TestWordGenerator;
import de.learnlib.driver.simulator.MealySimulatorSUL;
import de.learnlib.oracle.membership.SULAdaptiveOracle;
import de.learnlib.query.DefaultQuery;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.automaton.transducer.impl.CompactMealy;
import net.automatalib.util.automaton.builder.AutomatonBuilders;
import net.automatalib.util.automaton.transducer.MutableMealyMachines;
import net.automatalib.word.Word;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test
public class EarlyExitEQOracleTest {

    @Test
    public void testEarlyExit() {
        final Alphabet<Character> alphabet = Alphabets.characters('a', 'c');

        // @formatter:off
        final CompactMealy<Character, Character> sul = AutomatonBuilders.<Character, Character>newMealy(alphabet)
                                                                        .from("s0").on('a').withOutput('0').to("s1")
                                                                        .from("s0").on('b').withOutput('1').loop()
                                                                        .from("s1").on('a').withOutput('0').to("s2")
                                                                        .from("s1").on('b').withOutput('1').loop()
                                                                        .from("s2").on('a').withOutput('2').to("s0")
                                                                        .from("s2").on('b').withOutput('1').loop()
                                                                        .withInitial("s0")
                                                                        .create();

        final CompactMealy<Character, Character> hyp = AutomatonBuilders.<Character, Character>newMealy(alphabet)
                                                                        .from("s0").on('a').withOutput('0').to("s1")
                                                                        .from("s0").on('b').withOutput('1').loop()
                                                                        .from("s1").on('a').withOutput('0').to("s2")
                                                                        .from("s1").on('b').withOutput('1').loop()
                                                                        .from("s2").on('a').withOutput('1').to("s0")
                                                                        .from("s2").on('b').withOutput('1').loop()
                                                                        .withInitial("s0")
                                                                        .create();
        // @formatter:on

        MutableMealyMachines.complete(sul, alphabet, '3');

        final TestWordGenerator<MealyMachine<?, Character, ?, Character>, Character> eqo =
                (hypothesis, inputs) -> Stream.of(Word.fromString("aa"),
                                                  Word.fromString("ab"),
                                                  Word.fromString("aabaabaab"),
                                                  Word.fromString("aabbac"));

        final MealySimulatorSUL<Character, Character> spy = Mockito.spy(new MealySimulatorSUL<>(sul));
        final SULAdaptiveOracle<Character, Character> aqo = new SULAdaptiveOracle<>(spy);
        final EarlyExitEQOracle<Character, Character> eeeqo = new EarlyExitEQOracle<>(aqo, eqo);

        // mismatch on third 'a'
        DefaultQuery<Character, Word<Character>> ce = eeeqo.findCounterExample(hyp, alphabet);

        Assert.assertNotNull(ce);
        Assert.assertEquals(ce.getPrefix(), Word.fromString("aab"));
        Assert.assertEquals(ce.getSuffix(), Word.fromLetter('a'));
        Assert.assertEquals(ce.getOutput(), Word.fromLetter('2'));
        Mockito.verify(spy, Mockito.times(8)).step(Mockito.anyChar());

        Character i = 'a';
        Integer s1 = hyp.getState(Word.fromString("aab"));
        Integer s2 = hyp.getSuccessor(s1, i);

        hyp.setTransition(s1, i, s2, (Character) '2');

        // mismatch on undefined 'c' transition
        ce = eeeqo.findCounterExample(hyp, alphabet);

        Assert.assertNotNull(ce);
        Assert.assertEquals(ce.getPrefix(), Word.fromString("aabba"));
        Assert.assertEquals(ce.getSuffix(), Word.fromLetter('c'));
        Assert.assertEquals(ce.getOutput(), Word.fromLetter('3'));
        Mockito.verify(spy, Mockito.times(8 + 19)).step(Mockito.anyChar());

        MutableMealyMachines.complete(hyp, alphabet, '3');

        // no more mismatch
        ce = eeeqo.findCounterExample(hyp, alphabet);

        Assert.assertNull(ce);
        Mockito.verify(spy, Mockito.times(8 + 19 + 19)).step(Mockito.anyChar());
    }

    @Test
    public void testEmptyAutomaton() {

        final Alphabet<Character> alphabet = Alphabets.singleton('a');

        // @formatter:off
        final CompactMealy<Character, Character> sul = AutomatonBuilders.<Character, Character>newMealy(alphabet)
                                                                        .from("s0").on('a').withOutput('0').loop()
                                                                        .withInitial("s0")
                                                                        .create();
        // @formatter:on

        final CompactMealy<Character, Character> hyp = new CompactMealy<>(alphabet);

        final TestWordGenerator<MealyMachine<?, Character, ?, Character>, Character> eqo =
                (hypothesis, inputs) -> Stream.of(Word.fromString("aa"));

        final MealySimulatorSUL<Character, Character> spy = Mockito.spy(new MealySimulatorSUL<>(sul));
        final SULAdaptiveOracle<Character, Character> aqo = new SULAdaptiveOracle<>(spy);
        final EarlyExitEQOracle<Character, Character> eeeqo = new EarlyExitEQOracle<>(aqo, eqo);

        // mismatch on third 'a'
        DefaultQuery<Character, Word<Character>> ce = eeeqo.findCounterExample(hyp, alphabet);

        Assert.assertNotNull(ce);
        Assert.assertEquals(ce.getPrefix(), Word.epsilon());
        Assert.assertEquals(ce.getSuffix(), Word.fromLetter('a'));
        Assert.assertEquals(ce.getOutput(), Word.fromLetter('0'));
        Mockito.verify(spy, Mockito.times(1)).step(Mockito.anyChar());
    }
}
