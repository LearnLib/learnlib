package de.learnlib.algorithms.ttt;

import de.learnlib.acex.analyzers.AcexAnalyzers;
import de.learnlib.algorithms.ttt.mealy.TTTLearnerMealy;
import de.learnlib.api.MembershipOracle;
import de.learnlib.testsupport.AbstractGrowingAlphabetMealyTest;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

/**
 * @author frohme
 */
public class TTTLearnerMealyGrowingAlphabetTest
		extends AbstractGrowingAlphabetMealyTest<TTTLearnerMealy<Integer, Character>> {

	@Override
	protected TTTLearnerMealy<Integer, Character> getLearner(MembershipOracle<Integer, Word<Character>> oracle,
															 Alphabet<Integer> alphabet) {
		return new TTTLearnerMealy<>(alphabet, oracle, AcexAnalyzers.LINEAR_FWD);
	}
}
