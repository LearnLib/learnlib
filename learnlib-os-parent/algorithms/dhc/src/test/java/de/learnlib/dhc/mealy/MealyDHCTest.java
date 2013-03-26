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

package de.learnlib.dhc.mealy;

import static de.learnlib.examples.mealy.ExampleGrid.constructMachine;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.automata.transout.impl.FastMealy;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Symbol;

import org.testng.Assert;
import org.testng.annotations.Test;

import de.learnlib.eqtests.basic.SimulatorEQOracle;
import de.learnlib.examples.mealy.ExampleCoffeeMachine;
import de.learnlib.examples.mealy.ExampleStack;
import de.learnlib.oracles.DefaultQuery;
import de.learnlib.oracles.SimulatorOracle;

/**
 *
 * @author merten
 */
public class MealyDHCTest {
    
    @Test
    public void testMealyDHCGrid() {
        
        final int xsize = 5;
        final int ysize = 5;
        
        FastMealy<Symbol, Integer> fm = constructMachine(xsize, ysize);
        Alphabet<Symbol> alphabet = fm.getInputAlphabet();
        
        SimulatorOracle<Symbol, Word<Integer>> simoracle = new SimulatorOracle<>(fm);
        
        MealyDHC<Symbol, Integer> dhc = new MealyDHC<>(alphabet, simoracle);
        
        dhc.startLearning();
        MealyMachine<?, Symbol, ?, Integer> hypo = dhc.getHypothesisModel();
        
        Assert.assertEquals(hypo.size(), (xsize * ysize), "Mismatch in size of learned hypothesis");
        
    }
    
    @Test
    public void testMealyDHCStack() {
        
        FastMealy<Symbol, String> fm = ExampleStack.constructMachine();
        Alphabet<Symbol> alphabet = fm.getInputAlphabet();
        
        SimulatorOracle<Symbol, Word<String>> simoracle = new SimulatorOracle<>(fm);
        
        MealyDHC<Symbol, String> dhc = new MealyDHC<>(alphabet, simoracle);
        
        dhc.startLearning();
        
        MealyMachine<?, Symbol, ?, String> hypo = dhc.getHypothesisModel();
        
        // for this example the first hypothesis should have two states
        Assert.assertEquals(hypo.size(), 2, "Mismatch in size of learned hypothesis");
        
        SimulatorEQOracle<Symbol, Word<String>> eqoracle = new SimulatorEQOracle<>(fm);
        
        DefaultQuery<Symbol, Word<String>> cexQuery = eqoracle.findCounterExample(hypo, alphabet);
        
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
        
        FastMealy<Symbol, String> fm = ExampleCoffeeMachine.constructMachine();
        Alphabet<Symbol> alphabet = fm.getInputAlphabet();
        
        SimulatorOracle<Symbol, Word<String>> simoracle = new SimulatorOracle<>(fm);
        SimulatorEQOracle<Symbol, Word<String>> eqoracle = new SimulatorEQOracle<>(fm);
		
        MealyDHC<Symbol, String> dhc = new MealyDHC<>(alphabet, simoracle);
        
        int rounds = 0;
		DefaultQuery<Symbol, Word<String>> counterexample = null;
		do {
			if(counterexample == null) {
				dhc.startLearning();
			} else {
				Assert.assertTrue(dhc.refineHypothesis(counterexample), "Counterexample did not refine hypothesis");
			}
			
			counterexample = eqoracle.findCounterExample(dhc.getHypothesisModel(), alphabet);
			
			Assert.assertTrue(rounds++ < fm.size(), "Learning took more rounds than states in target model");
			
		} while(counterexample != null);
		
        Assert.assertEquals(dhc.getHypothesisModel().size(), fm.size(), "Mismatch in size of learned hypothesis and target model");
            
    }

    
}
