package de.learnlib.algorithms.angluin;

import de.learnlib.algorithms.angluin.oracles.SimpleOracle;
import de.learnlib.api.LearningAlgorithm;
import de.learnlib.api.Query;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.FastAlphabet;
import net.automatalib.words.impl.Symbol;
import org.junit.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TestSimpleAutomaton {

	private Symbol zero;
	private Symbol one;

	private LearningAlgorithm<DFA, Symbol, Boolean> angluin;

	@BeforeClass
	public void setup() {
		Alphabet<Symbol> alphabet = new FastAlphabet<>();
		zero = new Symbol(0);
		one = new Symbol(1);

		alphabet.add(zero);
		alphabet.add(one);

		angluin = new Angluin<>(alphabet, new SimpleOracle());
	}

	@Test(expectedExceptions = IllegalStateException.class)
	public void testGetHypothesisBeforeLearnIteration() {
		angluin.getHypothesisModel();
	}

	@Test(expectedExceptions = IllegalStateException.class)
	public void testRefinementBeforeLearnIteration() {
		angluin.refineHypothesis(createCounterExample());
	}

	@Test(dependsOnMethods = "testGetHypothesisBeforeLearnIteration")
	public void testFirstHypothesis() {
		angluin.startLearning();
		DFA hypothesis = angluin.getHypothesisModel();
		Assert.assertEquals(hypothesis.getStates().size(), 2);
	}

	@Test(dependsOnMethods = "testFirstHypothesis", expectedExceptions = IllegalStateException.class)
	public void testDuplicateLearnInvocation() {
		angluin.startLearning();
	}

	@Test(dependsOnMethods = "testFirstHypothesis")
	public void testCounterExample() {
		angluin.refineHypothesis(createCounterExample());
		DFA hypothesis = angluin.getHypothesisModel();
		Assert.assertEquals(hypothesis.getStates().size(), 4);
	}

	private Query<Symbol, Boolean> createCounterExample() {
		Word<Symbol> counterExample = Word.epsilon();
		counterExample = counterExample.append(one).append(one).append(zero);
		Query<Symbol, Boolean> query = new Query<>(counterExample);
		query.setOutput(false);
		return query;
	}


}
