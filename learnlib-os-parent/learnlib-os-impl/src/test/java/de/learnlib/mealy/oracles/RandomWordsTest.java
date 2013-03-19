package de.learnlib.mealy.oracles;

import java.util.Random;

import net.automatalib.automata.transout.impl.FastMealy;
import net.automatalib.automata.transout.impl.FastMealyState;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.FastAlphabet;
import net.automatalib.words.impl.Symbol;

import org.junit.Assert;
import org.junit.Test;

import de.learnlib.api.EquivalenceOracle;
import de.learnlib.api.MembershipOracle;

public class RandomWordsTest {

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

		Random random = new Random(1337421337);

		EquivalenceOracle<FastMealy<Symbol, String>, Symbol, Word<String>> eq = new RandomWordsEQOracle<>(
				oracle, 10, 100, 300, random);

                // no counterexample shall be found
		Assert.assertNull(eq.findCounterExample(mealy, mealy.getInputAlphabet()));
                
                // retrieve new mealy machine
                FastMealy<Symbol, String> mealy_broken = constructMachine();
                FastMealyState<String> s0 = mealy_broken.getInitialState();
                FastMealyState<String> s1 = mealy_broken.getSuccessor(s0, in_a);
                
                // deliberately introduce error
                mealy_broken.removeAllTransitions(s0, in_a);
                mealy_broken.addTransition(s0, in_a, s1, out_error);
                
                // a counterexample has to be found
                Assert.assertNotNull(eq.findCounterExample(mealy_broken, mealy.getInputAlphabet()));

        }

}
