package de.learnlib.algorithms.angluin;

import de.learnlib.algorithms.angluin.oracles.TwoStateOracle;
import de.learnlib.api.LearningAlgorithm;
import de.ls5.automata.Automaton;
import de.ls5.words.Alphabet;
import de.ls5.words.impl.FastAlphabet;
import de.ls5.words.impl.Symbol;
import org.junit.Assert;
import org.testng.annotations.Test;

public class TestTwoStateAutomaton {

	@Test
	public void test() {
		Alphabet<Symbol> alphabet = new FastAlphabet<Symbol>();
		alphabet.add(new Symbol(0));
		alphabet.add(new Symbol(1));

		LearningAlgorithm<Automaton, Symbol, Boolean> angluin = new Angluin<Symbol>(alphabet, new TwoStateOracle());
		Automaton hypothesis = angluin.createHypothesis();

		Assert.assertEquals(hypothesis.getStates().size(), 2);
	}

}
