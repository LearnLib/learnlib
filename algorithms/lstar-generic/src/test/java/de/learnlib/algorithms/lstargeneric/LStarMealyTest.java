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
import net.automatalib.automata.transout.impl.FastMealy;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Symbol;

import org.testng.annotations.Test;

import de.learnlib.algorithms.lstargeneric.ce.ObservationTableCEXHandler;
import de.learnlib.algorithms.lstargeneric.closing.ClosingStrategy;
import de.learnlib.algorithms.lstargeneric.mealy.ClassicLStarMealy;
import de.learnlib.algorithms.lstargeneric.mealy.ExtensibleLStarMealy;
import de.learnlib.api.EquivalenceOracle;
import de.learnlib.api.LearningAlgorithm;
import de.learnlib.api.MembershipOracle;
import de.learnlib.eqtests.basic.SimulatorEQOracle;
import de.learnlib.eqtests.basic.mealy.SymbolEQOracleWrapper;
import de.learnlib.examples.mealy.ExampleStack;
import de.learnlib.oracles.SimulatorOracle;
import de.learnlib.oracles.mealy.SymbolOracleWrapper;

public class LStarMealyTest extends LearningTest {

	@Test
	public void testClassicLStarMealy() {
		FastMealy<Symbol,String> mealy = ExampleStack.constructMachine();
		Alphabet<Symbol> alphabet = mealy.getInputAlphabet();
		
		MembershipOracle<Symbol,Word<String>> oracle
			= new SimulatorOracle<>(mealy);

		// Empty list of suffixes => minimal compliant set
		List<Word<Symbol>> initSuffixes = Collections.emptyList();
		
		EquivalenceOracle<? super MealyMachine<?,Symbol,?,String>, Symbol, Word<String>> mealyEqOracle
					= new SimulatorEQOracle<>(mealy);
					
		EquivalenceOracle<? super MealyMachine<?,Symbol,?,String>, Symbol, String> mealySymEqOracle
			= new SymbolEQOracleWrapper<>(mealyEqOracle);
		
		for(ObservationTableCEXHandler<? super Symbol,? super String> handler : LearningTest.CEX_HANDLERS) {
			for(ClosingStrategy<? super Symbol,? super String> strategy : LearningTest.CLOSING_STRATEGIES) {
				LearningAlgorithm<MealyMachine<?,Symbol,?,String>,Symbol,String> learner
				= ClassicLStarMealy.createForWordOracle(alphabet, oracle, initSuffixes,
						handler, strategy);
				
				testLearnModel(mealy, alphabet, learner, new SymbolOracleWrapper<>(oracle), mealySymEqOracle);
			}
		}
	}
	
	@Test
	public void testOptimizedLStarMealy() {
		FastMealy<Symbol,String> mealy = ExampleStack.constructMachine();
		Alphabet<Symbol> alphabet = mealy.getInputAlphabet();
		
		MembershipOracle<Symbol,Word<String>> oracle
			= new SimulatorOracle<>(mealy);
	
		
		// Empty list of suffixes => minimal compliant set
		List<Word<Symbol>> initSuffixes = Collections.emptyList();
		
		EquivalenceOracle<? super MealyMachine<?,Symbol,?,String>, Symbol, Word<String>> mealyEqOracle
				= new SimulatorEQOracle<>(mealy);
		
		for(ObservationTableCEXHandler<? super Symbol,? super Word<String>> handler : LearningTest.CEX_HANDLERS) {
			for(ClosingStrategy<? super Symbol,? super Word<String>> strategy : LearningTest.CLOSING_STRATEGIES) {
				LearningAlgorithm<MealyMachine<?,Symbol,?,String>,Symbol,Word<String>> learner
				= new ExtensibleLStarMealy<>(alphabet, oracle, initSuffixes,
						handler, strategy);
				
				testLearnModel(mealy, alphabet, learner, oracle, mealyEqOracle);
			}
		}
	}

}
