package de.learnlib.eqtests.basic;

import de.learnlib.api.EquivalenceOracle;
import de.learnlib.api.MembershipOracle;
import de.learnlib.examples.mealy.ExampleStack;
import de.learnlib.oracles.DefaultQuery;
import de.learnlib.oracles.SimulatorOracle;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Random;

public class RandomWordsEQOracleTest {

	@Test
	public void testEmptyAutomaton() {
		ExampleStack stackExample = ExampleStack.createExample();
		MealyMachine<?,ExampleStack.Input,?,ExampleStack.Output> mealy = stackExample.getReferenceAutomaton();
		Alphabet<ExampleStack.Input> alphabet = stackExample.getAlphabet();

		MembershipOracle<ExampleStack.Input,Word<ExampleStack.Output>> oracle = new SimulatorOracle<>(mealy);

		EquivalenceOracle.MealyEquivalenceOracle<ExampleStack.Input, ExampleStack.Output> eqTest =
				new RandomWordsEQOracle.MealyRandomWordsEQOracle<>(oracle, 5, 15, 100, new Random(1));

		final CompactMealy<ExampleStack.Input, ExampleStack.Output> emptyHypothesis = new CompactMealy<>(alphabet);

		DefaultQuery<ExampleStack.Input,Word<ExampleStack.Output>> counterExample =
				eqTest.findCounterExample(emptyHypothesis, alphabet);

		Assert.assertNotNull(counterExample);
	}

}
