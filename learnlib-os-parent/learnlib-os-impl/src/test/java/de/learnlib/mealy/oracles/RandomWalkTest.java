package de.learnlib.mealy.oracles;

import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import de.learnlib.api.EquivalenceOracle;
import de.learnlib.api.MembershipOracle;
import de.ls5.automata.transout.impl.FastMealy;
import de.ls5.automata.transout.impl.FastMealyState;
import de.ls5.words.Alphabet;
import de.ls5.words.Word;
import de.ls5.words.impl.FastAlphabet;
import de.ls5.words.impl.Symbol;

public class RandomWalkTest {

	private final static Symbol in_a = new Symbol("a");
	private final static Symbol in_b = new Symbol("b");

	private final static String out_ok = "ok";
	private final static String out_error = "error";

	private FastMealy<Symbol, String> constructMachine() {
		Alphabet<Symbol> alpha = new FastAlphabet<>();
		alpha.add(in_a);
		alpha.add(in_b);

		FastMealy<Symbol, String> fm = new FastMealy<>(alpha);

		FastMealyState<String> s0 = fm.addInitialState(), s1 = fm.addState(), s2 = fm
				.addState();

		fm.addTransition(s0, in_a, s1, out_ok);
		fm.addTransition(s0, in_b, s0, out_error);

		fm.addTransition(s1, in_a, s2, out_ok);
		fm.addTransition(s1, in_b, s0, out_ok);

		fm.addTransition(s2, in_a, s2, out_error);
		fm.addTransition(s2, in_b, s1, out_ok);

		return fm;
	}

	@Test
	public void testRandomWalk() {
		FastMealy<Symbol, String> mealy = constructMachine();

		MembershipOracle<Symbol, Word<String>> oracle = new MealySimulatorOracle<>(
				mealy);

		Random random = new Random();

		EquivalenceOracle<FastMealy<Symbol, String>, Symbol, Word<String>> eq = new RandomWalkEQOracle<>(
				oracle, 10, 100, 300, random);

		Assert.assertNull(eq.findCounterExample(mealy));
	}

}
