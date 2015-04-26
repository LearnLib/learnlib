package de.learnlib.examples.dfa;

import java.util.Random;

import net.automatalib.util.automata.random.RandomAutomata;
import net.automatalib.words.impl.Alphabets;
import de.learnlib.examples.DefaultLearningExample.DefaultDFALearningExample;

public class ExampleRandomDFA extends DefaultDFALearningExample<Integer> {

	public ExampleRandomDFA(Random rand, int numInputs, int size) {
		super(RandomAutomata.randomDFA(rand, size, Alphabets.integers(0, numInputs - 1)));
	}
	
	public ExampleRandomDFA(int numInputs, int size) {
		this(new Random(), numInputs, size);
	}

}
