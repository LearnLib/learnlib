package de.learnlib.examples.mealy;

import java.util.Arrays;
import java.util.Random;

import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.util.automata.random.RandomAutomata;
import net.automatalib.words.Alphabet;
import de.learnlib.examples.LearningExample.MealyLearningExample;

public class ExampleRandomMealy<I,O> implements MealyLearningExample<I, O> {
	
	private final CompactMealy<I,O> mealy;

	@SafeVarargs
	public ExampleRandomMealy(Random random, Alphabet<I> alphabet, int size, O... outputs) {
		this.mealy = new CompactMealy<>(alphabet, size);
		RandomAutomata.randomDeterministic(random, 100, alphabet, null, Arrays.asList(outputs), mealy);
		
	}

	@Override
	public MealyMachine<?, I, ?, O> getReferenceAutomaton() {
		return mealy;
	}

	@Override
	public Alphabet<I> getAlphabet() {
		return mealy.getInputAlphabet();
	}

}
