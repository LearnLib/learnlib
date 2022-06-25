package de.learnlib.algorithms.aaar;

import java.util.function.Function;

import de.learnlib.api.algorithm.LearningAlgorithm.DFALearner;
import de.learnlib.api.oracle.MembershipOracle;
import net.automatalib.SupportsGrowingAlphabet;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;

public class AAARLearnerDFA<L extends DFALearner<CI> & SupportsGrowingAlphabet<CI>, AI, CI>
        extends AbstractAAARLearner<L, DFA<?, AI>, DFA<?, CI>, AI, CI, Boolean> {

    public AAARLearnerDFA(LearnerProvider<L, DFA<?, CI>, CI, Boolean> learnerProvider,
                          MembershipOracle<CI, Boolean> o,
                          CI initialConcrete,
                          Function<CI, AI> abstractor) {
        super(learnerProvider, o, initialConcrete, abstractor);
    }

    @Override
    public DFA<?, AI> getHypothesisModel() {
        final DFA<?, CI> concrete = super.getConcreteHypothesisModel();
        final CompactDFA<AI> result = new CompactDFA<>(super.abs);

        super.copyAbstract(concrete, result);

        return result;
    }
}

