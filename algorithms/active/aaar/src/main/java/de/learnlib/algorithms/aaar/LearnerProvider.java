package de.learnlib.algorithms.aaar;

import de.learnlib.api.algorithm.LearningAlgorithm;
import de.learnlib.api.oracle.MembershipOracle;
import net.automatalib.SupportsGrowingAlphabet;
import net.automatalib.words.Alphabet;

public interface LearnerProvider<L extends LearningAlgorithm<M, I, D> & SupportsGrowingAlphabet<I>, M, I, D> {

    L createLearner(Alphabet<I> alphabet, MembershipOracle<I, D> oracle);

}
