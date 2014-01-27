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

/**
 * A {@link LearningExample learning example} that directly stores the alphabet and the reference automaton
 * in its fields.
 * 
 * @author Malte Isberner
 *
 * @param <I> input symbol type
 * @param <O> output type
 * @param <A> automaton type
 */
public class DefaultLearningExample<I, O, A extends UniversalAutomaton<?, I, ?, ?, ?> & SuffixOutput<I, O>>
		implements LearningExample<I, O, A> {
	
	public static class DefaultDFALearningExample<I> extends DefaultLearningExample<I,Boolean,DFA<?,I>>
		implements DFALearningExample<I> {
		public DefaultDFALearningExample(Alphabet<I> alphabet,
				DFA<?, I> referenceAutomaton) {
			super(alphabet, referenceAutomaton);
		}
	}
	
	public static class DefaultMealyLearningExample<I,O> extends DefaultLearningExample<I,Word<O>,MealyMachine<?,I,?,O>>
		implements MealyLearningExample<I,O> {
		public DefaultMealyLearningExample(Alphabet<I> alphabet,
				MealyMachine<?, I, ?, O> referenceAutomaton) {
			super(alphabet, referenceAutomaton);
		}
	}
	
	private final Alphabet<I> alphabet;
	private final A referenceAutomaton;

	public DefaultLearningExample(Alphabet<I> alphabet, A referenceAutomaton) {
		this.alphabet = alphabet;
		this.referenceAutomaton = referenceAutomaton;
	}

	@Override
	public A getReferenceAutomaton() {
		return referenceAutomaton;
	}

	@Override
	public Alphabet<I> getAlphabet() {
		return alphabet;
	}

}
