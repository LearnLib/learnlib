package de.learnlib.algorithms.aaar;

import java.util.function.Function;

import de.learnlib.api.algorithm.LearningAlgorithm.MealyLearner;
import de.learnlib.api.oracle.MembershipOracle;
import net.automatalib.SupportsGrowingAlphabet;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.words.Word;

public class AAARLearnerMealy<L extends MealyLearner<CI, O> & SupportsGrowingAlphabet<CI>, AI, CI, O>
        extends AbstractAAARLearner<L, MealyMachine<?, AI, ?, O>, MealyMachine<?, CI, ?, O>, AI, CI, Word<O>> {

    public AAARLearnerMealy(LearnerProvider<L, MealyMachine<?, CI, ?, O>, CI, Word<O>> learnerProvider,
                            MembershipOracle<CI, Word<O>> o,
                            CI initialConcrete,
                            Function<CI, AI> abstractor) {
        super(learnerProvider, o, initialConcrete, abstractor);
    }

    @Override
    public MealyMachine<?, AI, ?, O> getHypothesisModel() {
        final MealyMachine<?, CI, ?, O> concrete = super.getConcreteHypothesisModel();
        final CompactMealy<AI, O> result = new CompactMealy<>(super.abs);

        super.copyAbstract(concrete, result);

        return result;
    }
}

