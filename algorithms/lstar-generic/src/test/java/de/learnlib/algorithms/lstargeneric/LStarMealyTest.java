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
package de.learnlib.algorithms.lstargeneric;

import java.util.Collections;
import java.util.List;

import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

import org.testng.annotations.Test;

import de.learnlib.algorithms.lstargeneric.ce.ObservationTableCEXHandler;
import de.learnlib.algorithms.lstargeneric.closing.ClosingStrategy;
import de.learnlib.algorithms.lstargeneric.mealy.ClassicLStarMealy;
import de.learnlib.algorithms.lstargeneric.mealy.ExtensibleLStarMealy;
import de.learnlib.api.EquivalenceOracle;
import de.learnlib.api.LearningAlgorithm;
import de.learnlib.api.MembershipOracle;
import de.learnlib.api.MembershipOracle.MealyMembershipOracle;
import de.learnlib.eqtests.basic.SimulatorEQOracle;
import de.learnlib.eqtests.basic.mealy.SymbolEQOracleWrapper;
import de.learnlib.examples.mealy.ExampleStack;
import de.learnlib.mealy.MealyUtil;
import de.learnlib.oracles.SimulatorOracle;
import de.learnlib.oracles.SimulatorOracle.MealySimulatorOracle;

@Test
public class LStarMealyTest extends LearningTest {

	@Test
	public void testClassicLStarMealy() {
		ExampleStack stackExample = ExampleStack.createExample();
		
		MealyMachine<?,ExampleStack.Input,?,ExampleStack.Output> mealy = stackExample.getReferenceAutomaton();
		Alphabet<ExampleStack.Input> alphabet = stackExample.getAlphabet();
		
		MealyMembershipOracle<ExampleStack.Input,ExampleStack.Output> oracle
			= new MealySimulatorOracle<>(mealy);

		// Empty list of suffixes => minimal compliant set
		List<Word<ExampleStack.Input>> initSuffixes = Collections.emptyList();
		
		EquivalenceOracle<? super MealyMachine<?,ExampleStack.Input,?,ExampleStack.Output>, ExampleStack.Input, Word<ExampleStack.Output>> mealyEqOracle
					= new SimulatorEQOracle<>(mealy);
					
		EquivalenceOracle<? super MealyMachine<?,ExampleStack.Input,?,ExampleStack.Output>, ExampleStack.Input, ExampleStack.Output> mealySymEqOracle
			= new SymbolEQOracleWrapper<MealyMachine<?,ExampleStack.Input,?,ExampleStack.Output>,ExampleStack.Input,ExampleStack.Output>(mealyEqOracle);
		
		for(ObservationTableCEXHandler<? super ExampleStack.Input,? super ExampleStack.Output> handler : LearningTest.CEX_HANDLERS) {
			for(ClosingStrategy<? super ExampleStack.Input,? super ExampleStack.Output> strategy : LearningTest.CLOSING_STRATEGIES) {
				LearningAlgorithm<MealyMachine<?,ExampleStack.Input,?,ExampleStack.Output>,ExampleStack.Input,ExampleStack.Output> learner
				= ClassicLStarMealy.createForWordOracle(alphabet, oracle, initSuffixes,
						handler, strategy);
				
				testLearnModel(mealy, alphabet, learner, MealyUtil.wrapWordOracle(oracle), mealySymEqOracle);
			}
		}
	}
	
	@Test
	public void testOptimizedLStarMealy() {
		ExampleStack stackExample = ExampleStack.createExample();
		MealyMachine<?,ExampleStack.Input,?,ExampleStack.Output> mealy = stackExample.getReferenceAutomaton();
		Alphabet<ExampleStack.Input> alphabet = stackExample.getAlphabet();
		
		MembershipOracle<ExampleStack.Input,Word<ExampleStack.Output>> oracle
			= new SimulatorOracle<>(mealy);
	
		
		// Empty list of suffixes => minimal compliant set
		List<Word<ExampleStack.Input>> initSuffixes = Collections.emptyList();
		
		EquivalenceOracle<? super MealyMachine<?,ExampleStack.Input,?,ExampleStack.Output>, ExampleStack.Input, Word<ExampleStack.Output>> mealyEqOracle
				= new SimulatorEQOracle<>(mealy);
		
		for(ObservationTableCEXHandler<? super ExampleStack.Input,? super Word<ExampleStack.Output>> handler : LearningTest.CEX_HANDLERS) {
			for(ClosingStrategy<? super ExampleStack.Input,? super Word<ExampleStack.Output>> strategy : LearningTest.CLOSING_STRATEGIES) {
				LearningAlgorithm<MealyMachine<?,ExampleStack.Input,?,ExampleStack.Output>,ExampleStack.Input,Word<ExampleStack.Output>> learner
				= new ExtensibleLStarMealy<>(alphabet, oracle, initSuffixes,
						handler, strategy);
				
				testLearnModel(mealy, alphabet, learner, oracle, mealyEqOracle);
			}
		}
	}

}
