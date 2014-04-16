/* Copyright (C) 2014 TU Dortmund
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
package de.learnlib.examples;

import net.automatalib.automata.UniversalAutomaton;
import net.automatalib.automata.concepts.SuffixOutput;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

public interface LearningExample<I,O,A extends UniversalAutomaton<?, I, ?, ?, ?> & SuffixOutput<I,O>> {
	
	public static interface DFALearningExample<I> extends LearningExample<I,Boolean,DFA<?,I>> {
	}
	
	public static interface MealyLearningExample<I,O> extends LearningExample<I,Word<O>,MealyMachine<?,I,?,O>> {
	}
	
	public A getReferenceAutomaton();
	
	public Alphabet<I> getAlphabet();

}
