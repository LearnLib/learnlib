/* Copyright (C) 2013-2014 TU Dortmund
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

import net.automatalib.automata.fsa.MutableDFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.util.automata.builders.AutomatonBuilders;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.FastAlphabet;
import net.automatalib.words.impl.Symbol;
import de.learnlib.examples.DefaultLearningExample.DefaultDFALearningExample;

/**
 * This class implements a sad love story - DFA style.
 * 
 * @author Maik Merten 
 */
public class ExamplePaulAndMary extends DefaultDFALearningExample<Symbol> {
	

	public static final Symbol IN_PAUL = new Symbol("Paul");
    public static final Symbol IN_LOVES = new Symbol("loves");
    public static final Symbol IN_MARY = new Symbol("Mary");
    
 
    
    public static Alphabet<Symbol> createInputAlphabet() {
    	return new FastAlphabet<>(IN_PAUL, IN_LOVES, IN_MARY);
    }
  
    
    /**
     * Construct and return a machine representation of this example
     * 
     * @return machine instance of the example
     */
    public static CompactDFA<Symbol> constructMachine() {
    	return constructMachine(new CompactDFA<>(createInputAlphabet()));
    }
     
    public static <A extends MutableDFA<S,? super Symbol>,S>
    A constructMachine(A dfa) {

    	/*
		S s0 = dfa.addInitialState(false), s1 = dfa.addState(false), s2 = dfa
				.addState(false), s3 = dfa.addState(true), s4 = dfa
				.addState(false);

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
		*/

    	dfa = AutomatonBuilders.forDFA(dfa)
    			.withInitial("s0")
    			.from("s0")
    				.on(IN_PAUL).to("s1")
    				.on(IN_LOVES, IN_MARY).to("s4")
    			.from("s1")
    				.on(IN_LOVES).to("s2")
    				.on(IN_PAUL, IN_MARY).to("s4")
    			.from("s2")
    				.on(IN_MARY).to("s3")
    				.on(IN_PAUL, IN_LOVES).to("s4")
    			.from("s3")
    				.on(IN_PAUL, IN_LOVES, IN_MARY).to("s4")
    			.from("s4")
    				.on(IN_PAUL, IN_LOVES, IN_MARY).loop()
    			.withAccepting("s3")
    		.create();
    	
    	return dfa;
    }
    
    public static ExamplePaulAndMary createExample() {
    	return new ExamplePaulAndMary();
    }
    
    public ExamplePaulAndMary() {
		super(constructMachine());
	}

}
