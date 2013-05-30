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

import de.learnlib.algorithms.baselinelstar.BaselineLStar;
import de.learnlib.examples.dfa.ExampleAngluin;
import de.learnlib.oracles.DefaultQuery;
import de.learnlib.oracles.SafeOracle;
import de.learnlib.oracles.SimulatorOracle;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.FastAlphabet;
import net.automatalib.words.impl.Symbol;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;

public class BaselineLStarTest {

	private Symbol zero;
	private Symbol one;

	private BaselineLStar<Symbol> angluin;

	@BeforeClass
	public void setup() {
		zero = new Symbol("0");
		one = new Symbol("1");

		Alphabet<Symbol> alphabet = new FastAlphabet<>(zero, one);

		DFA<?, Symbol> dfa = ExampleAngluin.constructMachine();

		SimulatorOracle<Symbol, Boolean> dso = new SimulatorOracle<>(dfa);
		SafeOracle<Symbol, Boolean> oracle = new SafeOracle<>(dso);

		angluin = new BaselineLStar<>(alphabet, oracle);
	}

	@Test(expectedExceptions = IllegalStateException.class)
	public void testGetHypothesisBeforeLearnIteration() {
		angluin.getHypothesisModel();
	}

	@Test(expectedExceptions = IllegalStateException.class)
	public void testRefinementBeforeLearnIteration() {
		angluin.refineHypothesis(createCounterExample(false, one));
	}

	@Test(dependsOnMethods = { "testGetHypothesisBeforeLearnIteration", "testRefinementBeforeLearnIteration" })
	public void testFirstHypothesis() {
		angluin.startLearning();
		DFA<?,Symbol> hypothesis = angluin.getHypothesisModel();
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
		angluin.refineHypothesis(createCounterExample(false, one, one, zero));
		DFA<?,Symbol> hypothesis = angluin.getHypothesisModel();
		Assert.assertEquals(3, hypothesis.getStates().size());
	}

	@Test(dependsOnMethods = "testCounterExample")
	public void testSecondCounterExample() throws IOException {
		angluin.refineHypothesis(createCounterExample(false, zero, one, zero));
		DFA<?,Symbol> hypothesis = angluin.getHypothesisModel();
		Assert.assertEquals(4, hypothesis.getStates().size());
		

		String observationTableOutput = angluin.getStringRepresentationOfObservationTable();
		Assert.assertEquals(18, observationTableOutput.split("\n").length);
	}

	private static DefaultQuery<Symbol, Boolean> createCounterExample(boolean output, Symbol... symbols) {
		Word<Symbol> counterExample = Word.fromSymbols(symbols);
		DefaultQuery<Symbol, Boolean> query = new DefaultQuery<>(counterExample);
		query.answer(output);
		return query;
	}

}
