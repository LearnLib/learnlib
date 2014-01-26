/* Copyright (C) 2014 TU Dortmund
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
package de.learner.testsupport.it.learner.internal;

import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.concepts.SuffixOutput;
import net.automatalib.util.automata.Automata;
import net.automatalib.words.Alphabet;

import org.testng.Assert;
import org.testng.ITest;
import org.testng.annotations.Test;

import de.learnlib.api.EquivalenceOracle;
import de.learnlib.api.LearningAlgorithm;
import de.learnlib.eqtests.basic.SimulatorEQOracle;
import de.learnlib.examples.LearningExample;
import de.learnlib.oracles.DefaultQuery;

public final class SingleLearnerVariantITSubCase<I,O,
	M extends UniversalDeterministicAutomaton<?, I, ?, ?, ?> & SuffixOutput<I,O>> implements ITest {
	
	private final LearnerVariant<? extends M, I, O> variant;
	private final LearningExample<I, O, ? extends M> example;
	
	public SingleLearnerVariantITSubCase(LearnerVariant<? extends M,I,O> variant, LearningExample<I,O,? extends M> example) {
		this.variant = variant;
		this.example = example;
	}

	@Override
	public String getTestName() {
		return variant.getLearner().getClass().getSimpleName() + "[" + variant.getName() + "]/" + example.getClass().getSimpleName();
	}
	
	@Test
	public void testLearning() {
		System.out.println("Running learner integration test " + getTestName());
		LearningAlgorithm<? extends M,I,O> learner
			= variant.getLearner();
		
		Alphabet<I> alphabet = example.getAlphabet();
		
		int maxRounds = variant.getMaxRounds();
		if(maxRounds < 0) {
			maxRounds = example.getReferenceAutomaton().size();
		}

		EquivalenceOracle<? super M, I, O> eqOracle
			= new SimulatorEQOracle<I,O>(example.getReferenceAutomaton());
		
		learner.startLearning();
		
		int roundCounter = 0;
		DefaultQuery<I, O> ceQuery;
		
		while((ceQuery = eqOracle.findCounterExample(learner.getHypothesisModel(), alphabet)) != null) {
			roundCounter++;
			if(roundCounter > maxRounds) {
				Assert.fail("Learning took too many rounds (> " + maxRounds + ")");
			}
			
			boolean refined = learner.refineHypothesis(ceQuery);
			Assert.assertTrue(refined, "Real counterexample did not refine hypothesis");
		}
		
		Assert.assertNull(
				Automata.findSeparatingWord(example.getReferenceAutomaton(), learner.getHypothesisModel(), alphabet),
				"Final hypothesis does not match reference automaton");
	}


}
