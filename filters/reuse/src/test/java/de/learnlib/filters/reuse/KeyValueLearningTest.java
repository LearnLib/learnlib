package de.learnlib.filters.reuse;

import static de.learnlib.algorithms.lstargeneric.ce.ObservationTableCEXHandlers.SHAHBAZ;
import static de.learnlib.algorithms.lstargeneric.closing.ClosingStrategies.CLOSE_FIRST;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.SimpleAlphabet;

import org.testng.Assert;
import org.testng.annotations.Test;

import de.learnlib.algorithms.lstargeneric.ce.ObservationTableCEXHandler;
import de.learnlib.algorithms.lstargeneric.ce.ObservationTableCEXHandlers;
import de.learnlib.algorithms.lstargeneric.closing.ClosingStrategy;
import de.learnlib.algorithms.lstargeneric.mealy.ExtensibleLStarMealy;
import de.learnlib.api.MembershipOracle.MealyMembershipOracle;
import de.learnlib.filters.reuse.api.InjectableSystemStateRef;
import de.learnlib.filters.reuse.api.SystemStateRef;
import de.learnlib.filters.reuse.symbols.KeyValueResetSymbol;
import de.learnlib.filters.reuse.symbols.KeyValueSymbol;

/**
 * Test with a key value mapping where the SUL is able to put/clear an arbitrary
 * object to a given integer position (in a map).
 * 
 * All tests use four symbols (put 1, put 2, clear 1, clear 2) and the tests are
 * successful if the first hypothesis has four states and some typical reuse
 * results are fulfilled. For the output alphabet have a look at {@link KeyValueSymbol}.
 * 
 * @author Oliver Bauer <oliver.bauer@tu-dortmund.de>
 */
public class KeyValueLearningTest {
	private ReuseOracleImpl<SystemStateRef<Map<Integer, Object>, KeyValueSymbol, String>, KeyValueSymbol, String> createOracle() {

		InjectableSystemStateRef<SystemStateRef<Map<Integer, Object>, KeyValueSymbol, String>, KeyValueSymbol, String> reset = null;
		reset = new KeyValueResetSymbol();

		ExecutableOracleImpl<SystemStateRef<Map<Integer, Object>, KeyValueSymbol, String>, KeyValueSymbol, String> executableOracle = null;
		executableOracle = new ExecutableOracleImpl<>(reset);

		ReuseOracleImpl<SystemStateRef<Map<Integer, Object>, KeyValueSymbol, String>, KeyValueSymbol, String> reuseOracle = null;
		reuseOracle = new ReuseOracleImpl<>(executableOracle);
		return reuseOracle;
	}

	@Test
	public void testOnlyReusage() {
		Alphabet<KeyValueSymbol> sigma = getSigma();
		ReuseOracleImpl<SystemStateRef<Map<Integer, Object>, KeyValueSymbol, String>, KeyValueSymbol, String> reuseOracle = createOracle();

		Assert.assertTrue(reuseOracle.getAnswers() == 0, "answers not 0: " + reuseOracle.getAnswers());
		Assert.assertTrue(reuseOracle.getFull()    == 0, "full queries not 0: " + reuseOracle.getFull());
		Assert.assertTrue(reuseOracle.getReuse()   == 0, "reused queries not 0: " + reuseOracle.getReuse());

		reuseOracle.getReuseTree().useFailureOutputKnowledge(false);
		reuseOracle.getReuseTree().useModelInvariantSymbols(false);

		ExtensibleLStarMealy<KeyValueSymbol, String> learner = createLearner(
				sigma, reuseOracle);
		learner.startLearning();
		int pumped = reuseOracle.getAnswers();

		Assert.assertTrue(learner.getHypothesisModel().size() == 4);
		// TODO https://github.com/LearnLib/learnlib/issues/5 so fetched answers before hyp. constr.
		Assert.assertTrue(pumped == 0, "answers: " + pumped);
		/* The automaton has |Q|=4 states, |D|=4 => |Q|*|D| reuses; upper part of the table. */
		Assert.assertTrue(reuseOracle.getReuse() == 16);
		/* Full queries are exactly |long prefixes|*|D|; lower part of the table. */
		Assert.assertTrue(reuseOracle.getFull() == 52);
	}

	@Test
	public void testReusageAndDomainKnowledge() {
		Alphabet<KeyValueSymbol> sigma = getSigma();
		ReuseOracleImpl<SystemStateRef<Map<Integer, Object>, KeyValueSymbol, String>, KeyValueSymbol, String> reuseOracle = createOracle();

		Assert.assertTrue(reuseOracle.getAnswers() == 0, "answers not 0: " + reuseOracle.getAnswers());
		Assert.assertTrue(reuseOracle.getFull()    == 0, "full queries not 0: " + reuseOracle.getFull());
		Assert.assertTrue(reuseOracle.getReuse()   == 0, "reused queries not 0: " + reuseOracle.getReuse());
		
		reuseOracle.getReuseTree().addFailureOutputSymbol("alreadyput");
		reuseOracle.getReuseTree().addFailureOutputSymbol("alreadycleared");
		reuseOracle.getReuseTree().useFailureOutputKnowledge(true);

		ExtensibleLStarMealy<KeyValueSymbol, String> learner = createLearner(
				sigma, reuseOracle);
		learner.startLearning();
		// First hypothesis will find outputs ''alreadyput'' and ''alreadycleared'', so
		// the long prefixes will be pumped
		Assert.assertTrue(reuseOracle.getAnswers() > 0,"There must be pumped queries in this example!");
		Assert.assertTrue(learner.getHypothesisModel().size() == 4);

//		// correct values are not checked:
//		Assert.assertTrue(reuseOracle.getFull()    == 12, "full queries not 12: " + reuseOracle.getFull());
//		Assert.assertTrue(reuseOracle.getReuse()   == 24, "reused queries not 24: " + reuseOracle.getReuse());
//		Assert.assertTrue(reuseOracle.getAnswers() == 32, "answers not 32: " + reuseOracle.getAnswers());
	}

	private Alphabet<KeyValueSymbol> getSigma() {
		Alphabet<KeyValueSymbol> sigma = new SimpleAlphabet<>();
		sigma.add(new KeyValueSymbol(new Integer(1), true));
		sigma.add(new KeyValueSymbol(new Integer(1), false));
		sigma.add(new KeyValueSymbol(new Integer(2), true));
		sigma.add(new KeyValueSymbol(new Integer(2), false));
		return sigma;
	}

	private ExtensibleLStarMealy<KeyValueSymbol, String> createLearner(
			Alphabet<KeyValueSymbol> sigma,
			MealyMembershipOracle<KeyValueSymbol, String> oracle) {
		final ExtensibleLStarMealy<KeyValueSymbol, String> learner;

		ObservationTableCEXHandler<Object, Object> cex = SHAHBAZ;
		cex = ObservationTableCEXHandlers.RIVEST_SCHAPIRE;
		cex = ObservationTableCEXHandlers.SUFFIX1BY1;

		ClosingStrategy<Object, Object> cs = CLOSE_FIRST;

		List<Word<KeyValueSymbol>> suffixes = new ArrayList<>();// Collections.emptyList();
		for (int i = 0; i <= sigma.size() - 1; i++) {
			Word<KeyValueSymbol> w = Word.fromLetter(sigma.getSymbol(i));
			suffixes.add(w);
		}

		learner = new ExtensibleLStarMealy<>(sigma, oracle, suffixes, cex, cs);

		return learner;
	}
}
