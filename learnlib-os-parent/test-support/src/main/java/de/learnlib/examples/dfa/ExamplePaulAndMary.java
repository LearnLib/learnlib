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

import java.util.List;

import net.automatalib.automata.fsa.impl.FastDFA;
import net.automatalib.automata.fsa.impl.FastDFAState;
import net.automatalib.util.automata.Automata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.FastAlphabet;
import net.automatalib.words.impl.Symbol;

/**
 * This class implements a sad love story - DFA style.
 * 
 * @author Maik Merten <maikmerten@googlemail.com>
 */
public class ExamplePaulAndMary {
    
    public final static Symbol in_paul = new Symbol("Paul");
    public final static Symbol in_loves = new Symbol("loves");
    public final static Symbol in_mary = new Symbol("Mary");
    
    public final static Alphabet<Symbol> alphabet = new FastAlphabet<>(in_paul, in_loves, in_mary);
    
    
    /**
     * Construct and return a machine representation of this example
     * 
     * @return machine instance of the example
     */
    public static FastDFA<Symbol> constructMachine() {
        
        FastDFA<Symbol> dfa = new FastDFA<>(alphabet);
        
        FastDFAState s0 = dfa.addInitialState(false),
                s1 = dfa.addState(false),
                s2 = dfa.addState(false),
                s3 = dfa.addState(true),
                s4 = dfa.addState(false);

        dfa.addTransition(s0, in_paul, s1);
        dfa.addTransition(s0, in_loves, s4);
        dfa.addTransition(s0, in_mary, s4);
        
        dfa.addTransition(s1, in_paul, s4);
        dfa.addTransition(s1, in_loves, s2);
        dfa.addTransition(s1, in_mary, s4);
        
        dfa.addTransition(s2, in_paul, s4);
        dfa.addTransition(s2, in_loves, s4);
        dfa.addTransition(s2, in_mary, s3);
        
        dfa.addTransition(s3, in_paul, s4);
        dfa.addTransition(s3, in_loves, s4);
        dfa.addTransition(s3, in_mary, s4);
        
        dfa.addTransition(s4, in_paul, s4);
        dfa.addTransition(s4, in_loves, s4);
        dfa.addTransition(s4, in_mary, s4);

        return dfa;
    }

    public static void main(String[] args) {
		FastDFA<Symbol> dfa = constructMachine();
		List<Word<Symbol>> cset = Automata.characterizingSet(dfa, dfa.getInputAlphabet());
		System.err.println(cset);
	}
}
