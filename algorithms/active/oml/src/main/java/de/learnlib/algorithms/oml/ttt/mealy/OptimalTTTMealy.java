package de.learnlib.algorithms.oml.ttt.mealy;


import de.learnlib.algorithms.oml.ttt.OptimalTTT;
import de.learnlib.algorithms.oml.ttt.dt.DTLeaf;
import de.learnlib.algorithms.oml.ttt.dt.DecisionTree;
import de.learnlib.api.algorithm.LearningAlgorithm;
import de.learnlib.api.oracle.MembershipOracle;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

public class OptimalTTTMealy<I, O> extends OptimalTTT<MealyMachine<?, I, ?, O>, I, Word<O>>
        implements LearningAlgorithm.MealyLearner<I, O> {

    private final HypothesisMealy<I, O> hypothesis;

    private final DecisionTreeMealy<I, O> dtree;

    Alphabet<I> sigma;

    public OptimalTTTMealy(MembershipOracle<I, Word<O>> mqs, MembershipOracle<I, Word<O>> ceqs, Alphabet<I> sigma) {
        super(ceqs);
        dtree = new DecisionTreeMealy<>(mqs, sigma, strie.root());
        DTLeaf<I, Word<O>> dtRoot = new DTLeaf<>(null, dtree, ptree.root());
        dtree.setRoot(dtRoot);
        ptree.root().setState(dtRoot);
        for (I a : sigma) {
            dtree.sift(ptree.root().append(a));
        }
        hypothesis = new HypothesisMealy<>(ptree, dtree);

        this.sigma = sigma;
    }
    @Override
    protected int maxSearchIndex(int ceLength) {
        return ceLength-1;
    }

    @Override
    protected Word<O> hypOutput(Word<I> word, int length) {
        return hypothesis.computeOutput(word).suffix(length);
    }

    @Override
    protected DTLeaf<I, Word<O>> getState(Word<I> prefix) {
        return hypothesis.getState(prefix);
    }

    @Override
    protected MealyMachine<?, I, ?, O> hypothesis() {
//        System.out.println("Init: " + hypothesis.getInitialState());
//        for (DTLeaf<I, Word<O>> s : hypothesis.getStates()) {
//            for (I a : sigma) {
//                MealyTransition<I, O> t = hypothesis.getTransition(s, a);
//                System.out.println(s + " - " + a + " / " +
//                        hypothesis.getTransitionOutput(t) + " -> " +
//                        hypothesis.getSuccessor(t));
//            }
//        }
        return hypothesis;
    }

    @Override
    protected DecisionTree<I, Word<O>> dtree() {
        return dtree;
    }

    @Override
    protected Word<O> suffix(Word<O> output, int length) {
        return output.suffix(length);
    }

}
