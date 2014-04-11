package de.learnlib.algorithms.discriminationtree;

import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.words.Alphabet;

import org.testng.annotations.Test;

import de.learnlib.algorithms.discriminationtree.mealy.DTLearnerMealy;
import de.learnlib.counterexamples.LocalSuffixFinder;
import de.learnlib.examples.mealy.ExampleCoffeeMachine;
import de.learnlib.examples.mealy.ExampleShahbazGroz;
import de.learnlib.examples.mealy.ExampleStack;
import de.learnlib.oracles.SimulatorOracle.MealySimulatorOracle;

@Test
public class DTLearnerMealyTest extends AbstractDTLearnerTest {
	
	protected static <I,O> void testLearnMealy(Alphabet<I> alphabet, MealyMachine<?,I,?,O> mealy) {
		MealySimulatorOracle<I,O> oracle = new MealySimulatorOracle<>(mealy);
		
		for(LocalSuffixFinder<Object,Object> lsf : SUFFIX_FINDERS) {
			DTLearnerMealy<I,O> learner = new DTLearnerMealy<I,O>(alphabet, oracle, lsf);
		
			testLearn(learner, alphabet, mealy);
		}
	}

	@Test
	public void testExampleCoffeeMachine() {
		Alphabet<ExampleCoffeeMachine.Input> alphabet = ExampleCoffeeMachine.getInputAlphabet();
		MealyMachine<?,ExampleCoffeeMachine.Input,?,String> mealy = ExampleCoffeeMachine.getInstance();
		
		testLearnMealy(alphabet, mealy);
	}
	
	@Test
	public void testShahbazGroz() {
		Alphabet<Character> alphabet = ExampleShahbazGroz.getInputAlphabet();
		MealyMachine<?,Character,?,String> mealy = ExampleShahbazGroz.getInstance();
		
		testLearnMealy(alphabet, mealy);
	}
	
	@Test
	public void testStack() {
		Alphabet<ExampleStack.Input> alphabet = ExampleStack.getInputAlphabet();
		MealyMachine<?,ExampleStack.Input,?,ExampleStack.Output> mealy = ExampleStack.getInstance();
		
		testLearnMealy(alphabet, mealy);
	}

}
