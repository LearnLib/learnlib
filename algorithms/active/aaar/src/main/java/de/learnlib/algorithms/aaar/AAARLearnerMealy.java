package de.learnlib.algorithms.aaar;

import de.learnlib.algorithms.aaar.abstraction.InitialAbstraction;
import de.learnlib.api.algorithm.LearningAlgorithm.MealyLearner;
import de.learnlib.api.oracle.MembershipOracle;
import net.automatalib.SupportsGrowingAlphabet;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.words.Word;

public class AAARLearnerMealy<L extends MealyLearner<CI, O> & SupportsGrowingAlphabet<CI>, AI, CI, O>
        extends AbstractAAARLearner<L, MealyMachine<?, AI, ?, O>, MealyMachine<?, CI, ?, O>, AI, CI, Word<O>> {

    public AAARLearnerMealy(LearnerProvider<L, MealyMachine<?, CI, ?, O>, CI, Word<O>> learnerProvider,
                            InitialAbstraction<AI, CI> initial,
                            MembershipOracle<CI, Word<O>> o) {
        super(learnerProvider, initial, o);
    }

    @Override
    public MealyMachine<?, AI, ?, O> getHypothesisModel() {
        MealyMachine<?, CI, ?, O> concrete = super.getConcreteHypothesisModel();
        CompactMealy<AI, O> result = new CompactMealy<>(super.abs);

        super.copyAbstract(concrete, result);

        return result;
    }
}

