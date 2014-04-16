/* Copyright (C) 2014 TU Dortmund
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
package de.learnlib.examples.dfa;

import de.learnlib.examples.DefaultLearningExample.DefaultDFALearningExample;

import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;


public class ExampleKeylock extends DefaultDFALearningExample<Integer> {

	private final boolean cyclical;
	
	public ExampleKeylock(int size, boolean cyclical, int additionalSymbols) {
		super(createDFA(size, cyclical, additionalSymbols));
		this.cyclical = cyclical;
	}

	@Override
	public String toString() {
		return "Keylock[size=" + getReferenceAutomaton().size() + ",alphabetSize=" + getAlphabet().size()
				+ ",cyclical=" + cyclical + "]";
	}
	
	public static CompactDFA<Integer> createDFA(int size, boolean cyclical, int additionalSymbols) {
		if(size < 2) {
			throw new IllegalArgumentException("Minimum keylock DFA size is 2");
		}
		
		Alphabet<Integer> alphabet = Alphabets.integers(0, additionalSymbols);
		CompactDFA<Integer> result = new CompactDFA<>(alphabet, size);
		
		int init = result.addIntInitialState(false);
		for(int sym = 1; sym <= additionalSymbols; sym++) {
			result.setTransition(init, sym, init);
		}
		
		int prev = init;
		for(int i = 2; i < size; i++) {
			int curr = result.addIntState(false);
			for(int sym = 1; sym <= additionalSymbols; sym++) {
				result.setTransition(curr, sym, curr);
			}
			
			result.setTransition(prev, 0, curr);
			prev = curr;
		}
		
		int end = result.addIntState(true);
		result.setTransition(prev, 0, end);
		
		if(cyclical) {
			result.setTransition(end, 0, init);
		}
		for(int sym = cyclical ? 1 : 0; sym <= additionalSymbols; sym++) {
			result.setTransition(end, sym, end);
		}
		
		return result;
	}
	
	public static ExampleKeylock createExample(int size, boolean cyclical) {
		return createExample(size, cyclical, 0);
	}
	
	public static ExampleKeylock createExample(int size, boolean cyclical, int additionalSymbols) {
		return new ExampleKeylock(size, cyclical, additionalSymbols);
	}

}
