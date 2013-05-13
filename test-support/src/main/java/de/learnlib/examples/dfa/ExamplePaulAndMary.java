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

package de.learnlib.examples.dfa;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.MutableDFA;
import net.automatalib.automata.fsa.impl.FastDFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.FastAlphabet;
import net.automatalib.words.impl.Symbol;

/**
 * This class implements a sad love story - DFA style.
 * 
 * @author Maik Merten <maikmerten@googlemail.com>
 */
public class ExamplePaulAndMary {
	
	private static final class InstanceHolder {
		public static final DFA<?,Symbol> INSTANCE;
		
		static {
			INSTANCE = constructMachine(new CompactDFA<>(ALPHABET));
		}
	}
    
    public static final Symbol IN_PAUL = new Symbol("Paul");
    public static final Symbol IN_LOVES = new Symbol("loves");
    public static final Symbol IN_MARY = new Symbol("Mary");
    
    private static final Alphabet<Symbol> ALPHABET = new FastAlphabet<>(IN_PAUL, IN_LOVES, IN_MARY);
    
  
    public static DFA<?,Symbol> getInstance() {
    	return InstanceHolder.INSTANCE;
    }
    
    public static Alphabet<Symbol> getAlphabet() {
    	return ALPHABET;
    }
    
    
    /**
     * Construct and return a machine representation of this example
     * 
     * @return machine instance of the example
     */
    public static FastDFA<Symbol> constructMachine() {
    	return constructMachine(new FastDFA<>(ALPHABET));
    }
     
    public static <A extends MutableDFA<S,? super Symbol>,S>
    A constructMachine(A dfa) {
    	S s0 = dfa.addInitialState(false),
          s1 = dfa.addState(false),
          s2 = dfa.addState(false),
          s3 = dfa.addState(true),
          s4 = dfa.addState(false);

        dfa.addTransition(s0, IN_PAUL, s1);
        dfa.addTransition(s0, IN_LOVES, s4);
        dfa.addTransition(s0, IN_MARY, s4);
        
        dfa.addTransition(s1, IN_PAUL, s4);
        dfa.addTransition(s1, IN_LOVES, s2);
        dfa.addTransition(s1, IN_MARY, s4);
        
        dfa.addTransition(s2, IN_PAUL, s4);
        dfa.addTransition(s2, IN_LOVES, s4);
        dfa.addTransition(s2, IN_MARY, s3);
        
        dfa.addTransition(s3, IN_PAUL, s4);
        dfa.addTransition(s3, IN_LOVES, s4);
        dfa.addTransition(s3, IN_MARY, s4);
        
        dfa.addTransition(s4, IN_PAUL, s4);
        dfa.addTransition(s4, IN_LOVES, s4);
        dfa.addTransition(s4, IN_MARY, s4);

        return dfa;
    }
}
