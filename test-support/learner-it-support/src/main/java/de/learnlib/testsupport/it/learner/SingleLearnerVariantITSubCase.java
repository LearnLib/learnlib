/* Copyright (C) 2014 TU Dortmund
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
package de.learnlib.testsupport.it.learner;

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

final class SingleLearnerVariantITSubCase<I,D,
	M extends UniversalDeterministicAutomaton<?, I, ?, ?, ?> & SuffixOutput<I,D>> implements ITest {
	
	private final LearnerVariant<? extends M, I, D> variant;
	private final LearningExample<I, D, ? extends M> example;
	
	public SingleLearnerVariantITSubCase(LearnerVariant<? extends M,I,D> variant, LearningExample<I,D,? extends M> example) {
		this.variant = variant;
		this.example = example;
	}

	@Override
	public String getTestName() {
		return variant.getLearnerName() + "[" + variant.getName() + "]/" + example.getClass().getSimpleName();
	}
	
	@Test
	public void testLearning() {
		System.out.print("Running learner integration test " + getTestName() + " ... ");
		System.out.flush();
		
		LearningAlgorithm<? extends M,I,D> learner
			= variant.getLearner();
		
		Alphabet<I> alphabet = example.getAlphabet();
		
		int maxRounds = variant.getMaxRounds();
		if(maxRounds < 0) {
			maxRounds = example.getReferenceAutomaton().size();
		}

		EquivalenceOracle<? super M, I, D> eqOracle
			= new SimulatorEQOracle<I,D>(example.getReferenceAutomaton());
		
		long start = System.nanoTime();
		
		learner.startLearning();
		
		int roundCounter = 0;
		DefaultQuery<I, D> ceQuery;
		
		while((ceQuery = eqOracle.findCounterExample(learner.getHypothesisModel(), alphabet)) != null) {
			roundCounter++;
			if(roundCounter > maxRounds) {
				Assert.fail("Learning took too many rounds (> " + maxRounds + ")");
			}
			
			boolean refined = learner.refineHypothesis(ceQuery);
			Assert.assertTrue(refined, "Real counterexample " + ceQuery.getInput() + " did not refine hypothesis");
		}
		
		Assert.assertNull(
				Automata.findSeparatingWord(example.getReferenceAutomaton(), learner.getHypothesisModel(), alphabet),
				"Final hypothesis does not match reference automaton");
		
		long duration = (System.nanoTime() - start)/1000000L;
		System.out.printf("ok [%d.%03ds]", duration/1000L, duration%1000L);
		System.out.println();
	}


}
