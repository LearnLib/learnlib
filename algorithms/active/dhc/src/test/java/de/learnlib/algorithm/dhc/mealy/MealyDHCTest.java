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
package de.learnlib.algorithm.dhc.mealy;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import de.learnlib.example.mealy.ExampleCoffeeMachine;
import de.learnlib.example.mealy.ExampleCoffeeMachine.Input;
import de.learnlib.example.mealy.ExampleGrid;
import de.learnlib.example.mealy.ExampleStack;
import de.learnlib.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.oracle.equivalence.MealySimulatorEQOracle;
import de.learnlib.oracle.membership.MealySimulatorOracle;
import de.learnlib.query.DefaultQuery;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.Alphabets;
import net.automatalib.automaton.transducer.CompactMealy;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.util.automaton.random.RandomAutomata;
import net.automatalib.word.Word;
import org.testng.Assert;
import org.testng.annotations.Test;

public class MealyDHCTest {

    @Test(expectedExceptions = Exception.class)
    public void testMealyDHCInternalSate() {
        ExampleStack stackExample = ExampleStack.createExample();
        MealyMachine<?, ExampleStack.Input, ?, ExampleStack.Output> fm = stackExample.getReferenceAutomaton();
        Alphabet<ExampleStack.Input> alphabet = stackExample.getAlphabet();

        MealySimulatorOracle<ExampleStack.Input, ExampleStack.Output> simoracle = new MealySimulatorOracle<>(fm);
        MealyDHC<ExampleStack.Input, ExampleStack.Output> dhc = new MealyDHC<>(alphabet, simoracle);

        // nothing learned yet, this should throw an exception!
        dhc.getHypothesisModel();
    }

    @Test
    public void testMealyDHCGrid() {

        final int xsize = 5;
        final int ysize = 5;

        ExampleGrid gridExample = ExampleGrid.createExample(xsize, ysize);
        MealyMachine<?, Character, ?, Integer> fm = gridExample.getReferenceAutomaton();
        Alphabet<Character> alphabet = gridExample.getAlphabet();

        MealySimulatorOracle<Character, Integer> simoracle = new MealySimulatorOracle<>(fm);

        MealyDHC<Character, Integer> dhc = new MealyDHC<>(alphabet, simoracle);

        dhc.startLearning();
        MealyMachine<?, Character, ?, Integer> hypo = dhc.getHypothesisModel();

        Assert.assertEquals(hypo.size(), (xsize * ysize), "Mismatch in size of learned hypothesis");

    }

    @Test
    public void testMealyDHCStack() {
        ExampleStack stackExample = ExampleStack.createExample();
        MealyMachine<?, ExampleStack.Input, ?, ExampleStack.Output> fm = stackExample.getReferenceAutomaton();
        Alphabet<ExampleStack.Input> alphabet = stackExample.getAlphabet();

        MealySimulatorOracle<ExampleStack.Input, ExampleStack.Output> simoracle = new MealySimulatorOracle<>(fm);

        MealyDHC<ExampleStack.Input, ExampleStack.Output> dhc = new MealyDHC<>(alphabet, simoracle);

        dhc.startLearning();

        MealyMachine<?, ExampleStack.Input, ?, ExampleStack.Output> hypo = dhc.getHypothesisModel();

        // for this example the first hypothesis should have two states
        Assert.assertEquals(hypo.size(), 2, "Mismatch in size of learned hypothesis");

        MealySimulatorEQOracle<ExampleStack.Input, ExampleStack.Output> eqoracle = new MealySimulatorEQOracle<>(fm);

        DefaultQuery<ExampleStack.Input, Word<ExampleStack.Output>> cexQuery =
                eqoracle.findCounterExample(hypo, alphabet);

        // a counterexample has to be found
        Assert.assertNotNull(cexQuery, "No counterexample found for incomplete hypothesis");

        boolean refined = dhc.refineHypothesis(cexQuery);

        // the counterexample has to lead to a refinement
        Assert.assertTrue(refined, "No refinement reported by learning algorithm");

        hypo = dhc.getHypothesisModel();

        // the refined hypothesis should now have the correct size
        Assert.assertEquals(hypo.size(), fm.size(), "Refined hypothesis does not have correct size");

        // no counterexample shall be found now
        cexQuery = eqoracle.findCounterExample(hypo, alphabet);
        Assert.assertNull(cexQuery, "Counterexample found despite correct model size");

    }

    @Test
    public void testMealyDHCCoffee() {

        ExampleCoffeeMachine cmExample = ExampleCoffeeMachine.createExample();

        MealyMachine<?, ExampleCoffeeMachine.Input, ?, String> fm = cmExample.getReferenceAutomaton();
        Alphabet<ExampleCoffeeMachine.Input> alphabet = cmExample.getAlphabet();

        MealyMembershipOracle<Input, String> simoracle = new MealySimulatorOracle<>(fm);
        MealySimulatorEQOracle<ExampleCoffeeMachine.Input, String> eqoracle = new MealySimulatorEQOracle<>(fm);

        MealyDHC<ExampleCoffeeMachine.Input, String> dhc = new MealyDHC<>(alphabet, simoracle);

        int rounds = 0;
        DefaultQuery<ExampleCoffeeMachine.Input, Word<String>> counterexample = null;
        do {
            if (counterexample == null) {
                dhc.startLearning();
            } else {
                Assert.assertTrue(dhc.refineHypothesis(counterexample), "Counterexample did not refine hypothesis");
            }

            counterexample = eqoracle.findCounterExample(dhc.getHypothesisModel(), alphabet);

            Assert.assertTrue(rounds++ < fm.size(), "Learning took more rounds than states in target model");

        } while (counterexample != null);

        Assert.assertEquals(dhc.getHypothesisModel().size(),
                            fm.size(),
                            "Mismatch in size of learned hypothesis and target model");

    }

    @Test
    public void testMealyDHCRandom() {

        Alphabet<Character> inputs = Alphabets.characters('a', 'c');

        List<String> outputs = Arrays.asList("o1", "o2", "o3");

        CompactMealy<Character, String> fm = RandomAutomata.randomDeterministic(new Random(1337),
                                                                                100,
                                                                                inputs,
                                                                                Collections.emptyList(),
                                                                                outputs,
                                                                                new CompactMealy<>(inputs));

        MealySimulatorOracle<Character, String> simoracle = new MealySimulatorOracle<>(fm);
        MealySimulatorEQOracle<Character, String> eqoracle = new MealySimulatorEQOracle<>(fm);

        MealyDHC<Character, String> dhc = new MealyDHC<>(inputs, simoracle);

        int rounds = 0;
        DefaultQuery<Character, Word<String>> counterexample = null;
        do {
            if (counterexample == null) {
                dhc.startLearning();
            } else {
                Assert.assertTrue(dhc.refineHypothesis(counterexample), "Counterexample did not refine hypothesis");
            }

            counterexample = eqoracle.findCounterExample(dhc.getHypothesisModel(), inputs);

            Assert.assertTrue(rounds++ < fm.size(), "Learning took more rounds than states in target model");

        } while (counterexample != null);

        Assert.assertEquals(dhc.getHypothesisModel().size(),
                            fm.size(),
                            "Mismatch in size of learned hypothesis and target model");

    }
}
