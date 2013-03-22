package de.learnlib.algorithms.angluin;

import de.learnlib.api.Query;
import de.learnlib.oracles.SafeOracle;
import de.learnlib.oracles.SimulatorOracle;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.examples.dfa.ExampleAngluin;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.FastAlphabet;
import net.automatalib.words.impl.Symbol;
import org.junit.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;

public class AngluinTest {

	private Symbol zero;
	private Symbol one;

	private Angluin<Symbol> angluin;

	@BeforeClass
	public void setup() {
		zero = new Symbol("0");
		one = new Symbol("1");

		Alphabet<Symbol> alphabet = new FastAlphabet<>(zero, one);

		DFA<?, Symbol> dfa = ExampleAngluin.constructMachine();

		SimulatorOracle<Symbol, Boolean> dso = new SimulatorOracle<>(dfa);
		SafeOracle<Symbol, Boolean> oracle = new SafeOracle<>(dso);

		angluin = new Angluin<>(alphabet, oracle);
	}

	@Test(expectedExceptions = IllegalStateException.class)
	public void testGetHypothesisBeforeLearnIteration() {
		angluin.getHypothesisModel();
	}

	@Test(expectedExceptions = IllegalStateException.class)
	public void testRefinementBeforeLearnIteration() {
		angluin.refineHypothesis(createCounterExample(false, one));
	}

	@Test(dependsOnMethods = { "testGetHypothesisBeforeLearnIteration", "testRefinementBeforeLearnIteration" })
	public void testFirstHypothesis() {
		angluin.startLearning();
		DFA hypothesis = angluin.getHypothesisModel();
		Assert.assertEquals(hypothesis.getStates().size(), 2);

		String observationTableOutput = angluin.getStringRepresentationOfObservationTable();
		Assert.assertEquals(8, observationTableOutput.split("\n").length);
	}

	@Test(dependsOnMethods = "testFirstHypothesis", expectedExceptions = IllegalStateException.class)
	public void testDuplicateLearnInvocation() {
		angluin.startLearning();
	}

	@Test(dependsOnMethods = "testDuplicateLearnInvocation")
	public void testCounterExample() throws IOException {
		angluin.refineHypothesis(createCounterExample(false, one, one, zero));
		DFA hypothesis = angluin.getHypothesisModel();
		Assert.assertEquals(3, hypothesis.getStates().size());
	}

	@Test(dependsOnMethods = "testCounterExample")
	public void testSecondCounterExample() throws IOException {
		angluin.refineHypothesis(createCounterExample(false, zero, one, zero));
		DFA hypothesis = angluin.getHypothesisModel();
		Assert.assertEquals(4, hypothesis.getStates().size());

		String observationTableOutput = angluin.getStringRepresentationOfObservationTable();
		Assert.assertEquals(18, observationTableOutput.split("\n").length);
	}

	private Query<Symbol, Boolean> createCounterExample(boolean output, Symbol... symbols) {
		Word<Symbol> counterExample = Word.fromSymbols(symbols);
		Query<Symbol, Boolean> query = new Query<>(counterExample);
		query.setOutput(output);
		return query;
	}

}
