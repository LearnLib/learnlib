package de.learnlib.algorithms.ttt;

import de.learnlib.acex.analyzers.AcexAnalyzers;
import de.learnlib.algorithms.ttt.dfa.TTTLearnerDFA;
import de.learnlib.api.MembershipOracle;
import de.learnlib.testsupport.AbstractGrowingAlphabetDFATest;
import net.automatalib.words.Alphabet;

/**
 * @author frohme
 */
public class TTTDFAGrowingAlphabetTest extends AbstractGrowingAlphabetDFATest<TTTLearnerDFA<Integer>> {

	@Override
	protected TTTLearnerDFA<Integer> getLearner(MembershipOracle<Integer, Boolean> oracle, Alphabet<Integer> alphabet) {
		return new TTTLearnerDFA<>(alphabet, oracle, AcexAnalyzers.LINEAR_FWD);
	}

}
