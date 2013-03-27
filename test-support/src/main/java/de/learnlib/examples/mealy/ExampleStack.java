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
 * This example encodes a small stack with a capacity of three elements
 * and "push" and "pop" operations as Mealy machine. Outputs are
 * "ok", "empty" and "full".
 * 
 * @author Maik Merten <maikmerten@googlemail.com>
 */
public class ExampleStack {
    private final static Symbol in_push = new Symbol("push");
    private final static Symbol in_pop = new Symbol("pop");
    private final static Alphabet<Symbol> alphabet = new FastAlphabet<>(in_push, in_pop); 
    
    private final static String out_ok = "ok";
    private final static String out_empty = "empty";
    private final static String out_full = "full";
    
    
    /**
     * Construct and return a machine representation of this example
     * 
     * @return machine instance of the example
     */
    public static FastMealy<Symbol, String> constructMachine() {

        FastMealy<Symbol, String> fm = new FastMealy<>(alphabet);
        
        FastMealyState<String> s0 = fm.addInitialState(),
                s1 = fm.addState(),
                s2 = fm.addState(),
                s3 = fm.addState();
        
        fm.addTransition(s0, in_push, s1, out_ok);
        fm.addTransition(s0, in_pop, s0, out_empty);
        
        fm.addTransition(s1, in_push, s2, out_ok);
        fm.addTransition(s1, in_pop, s0, out_ok);
        
        fm.addTransition(s2, in_push, s3, out_ok);
        fm.addTransition(s2, in_pop, s1, out_ok);
        
        fm.addTransition(s3, in_push, s3, out_full);
        fm.addTransition(s3, in_pop, s2, out_ok);
        
        return fm;
    }
    
}
