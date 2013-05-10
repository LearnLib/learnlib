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
 * This class provides the example used in the paper ''Learning Regular Sets
 * from Queries and Counterexamples'' by Dana Angluin that consists of an
 * automaton that accepts ''all strings over {0,1} with an even number of 0's
 * and an even number of 1's.''
 * 
 * @author Oliver Bauer <oliver.bauer@tu-dortmund.de>
 */
public class ExampleAngluin {
	
	private static final class InstanceHolder {
		public static final DFA<?,Symbol> INSTANCE;
		
		static {
			INSTANCE = constructMachine(new CompactDFA<>(ALPHABET));
		}
	}

	public static final Symbol IN_0 = new Symbol(0);
	public static final Symbol IN_1 = new Symbol(1);
	
	private static final Alphabet<Symbol> ALPHABET = new FastAlphabet<Symbol>(IN_0, IN_1);

	
	public static Alphabet<Symbol> getAlphabet() {
		return ALPHABET;
	}
	
	public static DFA<?,Symbol> getInstance() {
		return InstanceHolder.INSTANCE;
	}
	
	
	public static <A extends MutableDFA<S, ? super Symbol>,S>
	A constructMachine(A machine) {
		S q0 = machine.addInitialState(true);
		S q1 = machine.addState(false), q2 = machine.addState(false), q3 = machine.addState(false);
		
		machine.addTransition(q0, IN_0, q1);
		machine.addTransition(q0, IN_1, q2);
		
		machine.addTransition(q1, IN_0, q0);
		machine.addTransition(q1, IN_1, q3);
		
		machine.addTransition(q2, IN_0, q3);
		machine.addTransition(q2, IN_1, q0);
		
		machine.addTransition(q3, IN_0, q2);
		machine.addTransition(q3, IN_1, q1);
		
		return machine;
	}
	
	public static FastDFA<Symbol> constructMachine() {
		return constructMachine(new FastDFA<>(ALPHABET));
	}
	
	
	
	
	
}
