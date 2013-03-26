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
 * This class provides the example used in the paper ''Learning Regular Sets
 * from Queries and Counterexamples'' by Dana Angluin that consists of an
 * automaton that accepts ''all strings over {0,1} with an even number of 0's
 * and an even number of 1's.''
 * 
 * @author Oliver Bauer <oliver.bauer@tu-dortmund.de>
 */
public class ExampleAngluin {

	private final static Symbol in_0 = new Symbol("0");
	private final static Symbol in_1 = new Symbol("1");
	
	private final static Alphabet<Symbol> alphabet = new FastAlphabet<Symbol>(in_0, in_1);

	public static FastDFA<Symbol> constructMachine() {
		

		FastDFA<Symbol> dfa = new FastDFA<>(alphabet);

		FastDFAState q0 = dfa.addInitialState(true);
		FastDFAState q1 = dfa.addState(false);
		FastDFAState q2 = dfa.addState(false);
		FastDFAState q3 = dfa.addState(false);

		// see figure 10 (page 15) in the paper:
		dfa.addTransition(q0, in_0, q1);
		dfa.addTransition(q0, in_1, q2);

		dfa.addTransition(q1, in_0, q0);
		dfa.addTransition(q1, in_1, q3);

		dfa.addTransition(q2, in_0, q3);
		dfa.addTransition(q2, in_1, q0);

		dfa.addTransition(q3, in_0, q2);
		dfa.addTransition(q3, in_1, q1);

		return dfa;
	}
	
	public static void main(String[] args) {
		FastDFA<Symbol> dfa = constructMachine();
		List<Word<Symbol>> cset = Automata.characterizingSet(dfa, dfa.getInputAlphabet());
		System.err.println(cset);
	}
	
}
