package de.learnlib.algorithms.angluin;

import de.learnlib.algorithms.angluin.oracles.SimpleOracle;
import de.learnlib.api.LearningAlgorithm;
import de.ls5.automata.fsa.DFA;
import de.ls5.words.Alphabet;
import de.ls5.words.Word;
import de.ls5.words.impl.ArrayWord;
import de.ls5.words.impl.FastAlphabet;
import de.ls5.words.impl.Symbol;
import de.ls5.words.util.Words;
import org.junit.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TestSimpleAutomaton {

	private Symbol zero;
	private Symbol one;

	private LearningAlgorithm<DFA, Symbol, Boolean> angluin;

	@BeforeClass
	public void setup() {
		Alphabet<Symbol> alphabet = new FastAlphabet<Symbol>();
		zero = new Symbol(0);
		one = new Symbol(1);

		alphabet.add(zero);
		alphabet.add(one);

		angluin = new Angluin<Symbol>(alphabet, new SimpleOracle());
	}

	@Test
	public void testFirstHypothesis() {
		DFA hypothesis = angluin.createHypothesis();
		Assert.assertEquals(hypothesis.getStates().size(), 2);
	}

	@Test(dependsOnMethods = "testFirstHypothesis")
	public void testCounterExample() {
		Word<Symbol> counterExample = new ArrayWord<Symbol>();
		counterExample = Words.append(counterExample, one, one, zero);

		DFA hypothesis = angluin.refineHypothesis(counterExample, false);
		Assert.assertEquals(hypothesis.getStates().size(), 4);
	}

}
