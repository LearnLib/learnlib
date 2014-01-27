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
package de.learnlib.examples.mealy;

import java.util.Arrays;
import java.util.Random;

import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.util.automata.random.RandomAutomata;
import net.automatalib.words.Alphabet;
import de.learnlib.examples.DefaultLearningExample.DefaultMealyLearningExample;

public class ExampleRandomMealy<I,O> extends DefaultMealyLearningExample<I, O> {


	@SafeVarargs
	public static <I,O>
	ExampleRandomMealy<I,O> createExample(Random random, Alphabet<I> alphabet, int size, O... outputs) {
		CompactMealy<I,O> mealy = new CompactMealy<>(alphabet, size);
		RandomAutomata.randomDeterministic(random, size, alphabet, null, Arrays.asList(outputs), mealy);
		
		return new ExampleRandomMealy<>(mealy.getInputAlphabet(), mealy);
	}


	private ExampleRandomMealy(Alphabet<I> alphabet,
			MealyMachine<?, I, ?, O> referenceAutomaton) {
		super(alphabet, referenceAutomaton);
	}

}
