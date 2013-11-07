/* Copyright (C) 2013 TU Dortmund
 * This file is part of LearnLib, http://www.learnlib.de/.
 * 
 * LearnLib is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 3.0 as published by the Free Software Foundation.
 * 
 * LearnLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with LearnLib; if not, see
 * <http://www.gnu.de/documents/lgpl.en.html>.
 */
package de.learnlib.algorithms.dhc.mealy;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.util.automata.random.RandomAutomata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;

import org.testng.Assert;
import org.testng.annotations.Test;

import de.learnlib.api.MembershipOracle;
import de.learnlib.api.MembershipOracle.MealyMembershipOracle;
import de.learnlib.cache.mealy.MealyCacheOracle;
import de.learnlib.eqtests.basic.SimulatorEQOracle;
import de.learnlib.examples.mealy.ExampleCoffeeMachine;
import de.learnlib.examples.mealy.ExampleGrid;
import de.learnlib.examples.mealy.ExampleStack;
import de.learnlib.oracles.DefaultQuery;
import de.learnlib.oracles.SimulatorOracle;
import de.learnlib.oracles.SimulatorOracle.MealySimulatorOracle;

/**
 *
 * @author merten
 */
public class MealyDHCTest {
	
	@Test(expectedExceptions = Exception.class)
	public void testMealyDHCInternalSate() {
		MealyMachine<?,ExampleStack.Input,?,ExampleStack.Output> fm = ExampleStack.getInstance();
		Alphabet<ExampleStack.Input> alphabet = ExampleStack.getInputAlphabet();
		MealySimulatorOracle<ExampleStack.Input,ExampleStack.Output> simoracle = new MealySimulatorOracle<>(fm);
		MealyDHC<ExampleStack.Input, ExampleStack.Output> dhc = new MealyDHC<>(alphabet, simoracle);
		
		// nothing learned yet, this should throw an exception!
		dhc.getHypothesisModel();
	}
	

	@Test
	public void testMealyDHCGrid() {

		final int xsize = 5;
		final int ysize = 5;

		MealyMachine<?,Character,?,Integer> fm = ExampleGrid.constructMachine(xsize, ysize);
		Alphabet<Character> alphabet = ExampleGrid.getInputAlphabet();


		MealySimulatorOracle<Character,Integer> simoracle = new MealySimulatorOracle<>(fm);
		MealyMembershipOracle<Character,Integer> cache = new MealyCacheOracle<>(alphabet, null, simoracle);

		MealyDHC<Character, Integer> dhc = new MealyDHC<>(alphabet, cache);

		dhc.startLearning();
		MealyMachine<?, Character, ?, Integer> hypo = dhc.getHypothesisModel();

		Assert.assertEquals(hypo.size(), (xsize * ysize), "Mismatch in size of learned hypothesis");

	}

	@Test
	public void testMealyDHCStack() {
		MealyMachine<?,ExampleStack.Input,?,ExampleStack.Output> fm = ExampleStack.getInstance();
		Alphabet<ExampleStack.Input> alphabet = ExampleStack.getInputAlphabet();

		MealySimulatorOracle<ExampleStack.Input,ExampleStack.Output> simoracle = new MealySimulatorOracle<>(fm);
		MealyMembershipOracle<ExampleStack.Input,ExampleStack.Output> cache = new MealyCacheOracle<>(alphabet, null, simoracle);

		MealyDHC<ExampleStack.Input, ExampleStack.Output> dhc = new MealyDHC<>(alphabet, cache);

		dhc.startLearning();

		MealyMachine<?, ExampleStack.Input, ?, ExampleStack.Output> hypo = dhc.getHypothesisModel();

		// for this example the first hypothesis should have two states
		Assert.assertEquals(hypo.size(), 2, "Mismatch in size of learned hypothesis");

		SimulatorEQOracle<ExampleStack.Input, Word<ExampleStack.Output>> eqoracle = new SimulatorEQOracle<>(fm);

		DefaultQuery<ExampleStack.Input, Word<ExampleStack.Output>> cexQuery = eqoracle.findCounterExample(hypo, alphabet);

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

		MealyMachine<?,ExampleCoffeeMachine.Input,?,String> fm = ExampleCoffeeMachine.getInstance();
		Alphabet<ExampleCoffeeMachine.Input> alphabet = ExampleCoffeeMachine.getInputAlphabet();

		SimulatorOracle<ExampleCoffeeMachine.Input, Word<String>> simoracle = new SimulatorOracle<>(fm);
		SimulatorEQOracle<ExampleCoffeeMachine.Input, Word<String>> eqoracle = new SimulatorEQOracle<>(fm);

		MembershipOracle<ExampleCoffeeMachine.Input,Word<String>> cache = new MealyCacheOracle<>(alphabet, null, simoracle);
		MealyDHC<ExampleCoffeeMachine.Input, String> dhc = new MealyDHC<>(alphabet, cache);

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

		Assert.assertEquals(dhc.getHypothesisModel().size(), fm.size(), "Mismatch in size of learned hypothesis and target model");

	}
	
	
	@Test
	public void testMealyDHCRandom() {
		
		Alphabet<Character> inputs = Alphabets.characters('a', 'c');
		
		List<String> outputs = Arrays.asList("o1", "o2", "o3");

		CompactMealy<Character, String> fm = RandomAutomata.randomDeterministic(new Random(1337), 100, inputs, null, outputs, new CompactMealy<Character,String>(inputs));
		
		
		SimulatorOracle<Character, Word<String>> simoracle = new SimulatorOracle<>(fm);
		MealyCacheOracle<Character,String> cache = new MealyCacheOracle<>(inputs, null, simoracle);
		SimulatorEQOracle<Character, Word<String>> eqoracle = new SimulatorEQOracle<>(fm);

		MealyDHC<Character, String> dhc = new MealyDHC<>(inputs, cache);

		int rounds = 0;
		DefaultQuery<Character, Word<String>> counterexample = null;
		do {
			if (counterexample == null) {
				dhc.startLearning();
			} else {
				System.out.println("found counterexample: " + counterexample.getInput() + " / " + counterexample.getOutput());
				Assert.assertTrue(dhc.refineHypothesis(counterexample), "Counterexample did not refine hypothesis");
			}

			counterexample = eqoracle.findCounterExample(dhc.getHypothesisModel(), inputs);
			
			Assert.assertTrue(rounds++ < fm.size(), "Learning took more rounds than states in target model");

		} while (counterexample != null);
		
		Assert.assertEquals(dhc.getHypothesisModel().size(), fm.size(), "Mismatch in size of learned hypothesis and target model");
		System.err.println("Hypothesis has " + dhc.getHypothesisModel().size() + " states");
		System.err.println("Cache size is " + cache.getCacheSize());

	}
}
