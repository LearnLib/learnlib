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

import de.learnlib.oracles.SimulatorOracle;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.automata.transout.impl.FastMealy;
import org.junit.Test;

import static net.automatalib.examples.mealy.ExampleGrid.*;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Symbol;

/**
 *
 * @author merten
 */
public class MealyDHCTest {
    
    @Test
    public void testMealyDHC() {
        
        FastMealy<Symbol, Integer> fm = constructMachine(1, 1);
        Alphabet<Symbol> alphabet = fm.getInputAlphabet();
        
        SimulatorOracle<Symbol, Word<Integer>> simoracle = new SimulatorOracle<>(fm);
        
        MealyDHC dhc = new MealyDHC(alphabet, simoracle);
        
        dhc.startLearning();
        MealyMachine<?, Symbol, ?, Integer> hypo = dhc.getHypothesisModel();
        
        
        
    }
    
}
