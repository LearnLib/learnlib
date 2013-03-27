/* Copyright (C) 2013 TU Dortmund
 * This file is part of AutomataLib, http://www.automatalib.net/.
 * 
 * AutomataLib is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 3.0 as published by the Free Software Foundation.
 * 
 * AutomataLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with AutomataLib; if not, see
 * <http://www.gnu.de/documents/lgpl.en.html>.
 */

package de.learnlib.examples.mealy;

import net.automatalib.automata.transout.impl.FastMealy;
import net.automatalib.automata.transout.impl.FastMealyState;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.FastAlphabet;
import net.automatalib.words.impl.Symbol;

/**
 * This class provides the example used in the paper ''Inferring Mealy Machines'' 
 * by Muzammil Shahbaz and Roland Groz (see Figure 1).
 * 
 * @author Oliver Bauer <oliver.bauer@tu-dortmund.de>
 */
public class ExampleShahbazGroz {
	private final static Symbol in_a = new Symbol("a");
    private final static Symbol in_b = new Symbol("b");
    private final static Alphabet<Symbol> alphabet = new FastAlphabet<>(in_a, in_b);
    
    private final static String out_x = "x";
    private final static String out_y = "y";
    
    /**
     * Construct and return a machine representation of this example
     * 
     * @return machine instance of the example
     */
    public static FastMealy<Symbol, String> constructMachine() {
        
        FastMealy<Symbol, String> fm = new FastMealy<>(alphabet);
        
        FastMealyState<String> q0 = fm.addInitialState();
        FastMealyState<String> q1 = fm.addState();
        FastMealyState<String> q2 = fm.addState();
        FastMealyState<String> q3 = fm.addState();
        
        fm.addTransition(q0, in_a, q1, out_x);
        fm.addTransition(q0, in_b, q3, out_x);
        
        fm.addTransition(q1, in_a, q1, out_y);
        fm.addTransition(q1, in_b, q2, out_x);
        
        fm.addTransition(q2, in_a, q3, out_x);
        fm.addTransition(q2, in_b, q3, out_x);
        
        fm.addTransition(q3, in_a, q0, out_x);
        fm.addTransition(q3, in_b, q0, out_x);
        
        /*
         * In the paper the authors use the following counterexample
         * to refine the first conjecture from an angluin for mealy machines:
         * in_a in_b in_a in_b in_b in_a in_a
         */
        
        return fm;
    }
}
