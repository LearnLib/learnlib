package de.learnlib.algorithms.aaar;

import de.learnlib.algorithms.aaar.abstraction.InitialAbstraction;
import de.learnlib.api.algorithm.LearningAlgorithm.DFALearner;
import de.learnlib.api.oracle.MembershipOracle;
import net.automatalib.SupportsGrowingAlphabet;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;

public class AAARLearnerDFA<L extends DFALearner<CI> & SupportsGrowingAlphabet<CI>, AI, CI>
        extends AbstractAAARLearner<L, DFA<?, AI>, DFA<?, CI>, AI, CI, Boolean> {

    public AAARLearnerDFA(LearnerProvider<L, DFA<?, CI>, CI, Boolean> learnerProvider,
                          InitialAbstraction<AI, CI> initial,
                          MembershipOracle<CI, Boolean> o) {
        super(learnerProvider, initial, o);
    }

    @Override
    public DFA<?, AI> getHypothesisModel() {
        DFA<?, CI> concrete = super.getConcreteHypothesisModel();
        CompactDFA<AI> result = new CompactDFA<>(super.abs);

        super.copyAbstract(concrete, result);

        return result;
    }
}

