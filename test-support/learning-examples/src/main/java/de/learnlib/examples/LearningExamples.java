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

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;
import de.learnlib.examples.LearningExample.DFALearningExample;
import de.learnlib.examples.LearningExample.MealyLearningExample;
import de.learnlib.examples.dfa.ExampleAngluin;
import de.learnlib.examples.dfa.ExamplePaulAndMary;
import de.learnlib.examples.mealy.ExampleCoffeeMachine;
import de.learnlib.examples.mealy.ExampleGrid;
import de.learnlib.examples.mealy.ExampleRandomMealy;
import de.learnlib.examples.mealy.ExampleShahbazGroz;
import de.learnlib.examples.mealy.ExampleStack;

public abstract class LearningExamples {
	
	private static long RANDOM_SEED = 1337L;
	private static final Alphabet<Character> RANDOM_ALPHABET = Alphabets.characters('a', 'c');
	
	private static final int RANDOM_SIZE = 100;

	private static final int GRID_XSIZE = 5;
	private static final int GRID_YSIZE = 5;
	
	private static final String[] RANDOM_MEALY_OUTPUTS = { "o1", "o2", "o3" };
	

	private LearningExamples() {
		throw new AssertionError("Constructor should not be invoked");
	}
	
	public static List<? extends DFALearningExample<?>> createDFAExamples() {
		return Arrays.asList(
				ExampleAngluin.createExample(),
				ExamplePaulAndMary.createExample()
				);
	}
	
	public static List<? extends MealyLearningExample<?,?>> createMealyExamples() {
		return Arrays.asList(
				ExampleCoffeeMachine.createExample(),
				ExampleGrid.createExample(GRID_XSIZE, GRID_YSIZE),
				ExampleShahbazGroz.createExample(),
				ExampleStack.createExample(),
				ExampleRandomMealy.createExample(new Random(RANDOM_SEED), RANDOM_ALPHABET, RANDOM_SIZE, RANDOM_MEALY_OUTPUTS)
				);
	}

}
