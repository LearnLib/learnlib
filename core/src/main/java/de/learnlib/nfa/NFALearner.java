package de.learnlib.nfa;

import net.automatalib.automata.fsa.NFA;
import de.learnlib.api.LearningAlgorithm;

public interface NFALearner<I> extends LearningAlgorithm<NFA<?,I>, I, Boolean> {

}
