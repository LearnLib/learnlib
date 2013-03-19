package de.learnlib.algorithms.angluin;

import de.learnlib.algorithms.angluin.oracles.SimpleOracle;
import de.learnlib.api.LearningAlgorithm;
import de.learnlib.api.Query;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.ArrayWord;
import net.automatalib.words.impl.FastAlphabet;
import net.automatalib.words.impl.Symbol;
import net.automatalib.words.util.Words;
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

	@Test
	public void testFirstHypothesis() {
		angluin.startLearning();
		DFA hypothesis = angluin.getHypothesisModel();
		Assert.assertEquals(hypothesis.getStates().size(), 2);
	}

	@Test(dependsOnMethods = "testFirstHypothesis")
	public void testCounterExample() {
		Word<Symbol> counterExample = new ArrayWord<>();
		counterExample = Words.append(counterExample, one, one, zero);
		Query<Symbol, Boolean> query = new Query<>(counterExample);
		query.setOutput(false);


		angluin.refineHypothesis(query);
		DFA hypothesis = angluin.getHypothesisModel();
		Assert.assertEquals(hypothesis.getStates().size(), 4);
	}

}
