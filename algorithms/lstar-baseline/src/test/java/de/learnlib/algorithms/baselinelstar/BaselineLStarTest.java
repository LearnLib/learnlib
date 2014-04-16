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
package de.learnlib.algorithms.baselinelstar;

import java.io.IOException;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import de.learnlib.examples.dfa.ExampleAngluin;
import de.learnlib.oracles.DefaultQuery;
import de.learnlib.oracles.SafeOracle;
import de.learnlib.oracles.SimulatorOracle;

public class BaselineLStarTest {

	private BaselineLStar<Integer> angluin;

	@BeforeClass
	public void setup() {
		ExampleAngluin angluinExample = ExampleAngluin.createExample();
		
		DFA<?, Integer> dfa = angluinExample.getReferenceAutomaton();
		Alphabet<Integer> alphabet = angluinExample.getAlphabet();

		SimulatorOracle<Integer, Boolean> dso = new SimulatorOracle<>(dfa);
		SafeOracle<Integer, Boolean> oracle = new SafeOracle<>(dso);

		angluin = new BaselineLStar<>(alphabet, oracle);
	}

	@Test(expectedExceptions = IllegalStateException.class)
	public void testGetHypothesisBeforeLearnIteration() {
		angluin.getHypothesisModel();
	}

	@Test(expectedExceptions = IllegalStateException.class)
	public void testRefinementBeforeLearnIteration() {
		angluin.refineHypothesis(createCounterExample(false, 1));
	}

	@Test(dependsOnMethods = { "testGetHypothesisBeforeLearnIteration", "testRefinementBeforeLearnIteration" })
	public void testFirstHypothesis() {
		angluin.startLearning();
		DFA<?,Integer> hypothesis = angluin.getHypothesisModel();
		Assert.assertEquals(hypothesis.getStates().size(), 2);

		String observationTableOutput = angluin.getStringRepresentationOfObservationTable();
		Assert.assertEquals(8, observationTableOutput.split("\n").length);
	}

	@Test(dependsOnMethods = "testFirstHypothesis", expectedExceptions = IllegalStateException.class)
	public void testDuplicateLearnInvocation() {
		angluin.startLearning();
	}

	@Test(dependsOnMethods = "testDuplicateLearnInvocation")
	public void testCounterExample() throws IOException {
		angluin.refineHypothesis(createCounterExample(false, 1, 1, 0));
		DFA<?,Integer> hypothesis = angluin.getHypothesisModel();
		Assert.assertEquals(3, hypothesis.getStates().size());
	}

	@Test(dependsOnMethods = "testCounterExample")
	public void testSecondCounterExample() throws IOException {
		angluin.refineHypothesis(createCounterExample(false, 0, 1, 0));
		DFA<?,Integer> hypothesis = angluin.getHypothesisModel();
		Assert.assertEquals(4, hypothesis.getStates().size());
		

		String observationTableOutput = angluin.getStringRepresentationOfObservationTable();
		Assert.assertEquals(18, observationTableOutput.split("\n").length);
	}

	private static DefaultQuery<Integer, Boolean> createCounterExample(boolean output, Integer... symbols) {
		Word<Integer> counterExample = Word.fromSymbols(symbols);
		DefaultQuery<Integer, Boolean> query = new DefaultQuery<>(counterExample);
		query.answer(output);
		return query;
	}

}
