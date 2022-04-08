package de.learnlib.algorithms.oml.ttt.dfa;

import de.learnlib.algorithms.oml.ttt.OptimalTTT;
import de.learnlib.algorithms.oml.ttt.dt.DTInnerNode;
import de.learnlib.algorithms.oml.ttt.dt.DTLeaf;
import de.learnlib.algorithms.oml.ttt.dt.DecisionTree;
import de.learnlib.api.oracle.MembershipOracle;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

public class OptimalTTTDFA<I> extends OptimalTTT<DFA<?, I>, I, Boolean> {

    private final HypothesisDFA<I> hypothesis;

    private final DecisionTreeDFA<I> dtree;

    public OptimalTTTDFA(MembershipOracle<I, Boolean> mqs, MembershipOracle<I, Boolean> ceqs, Alphabet<I> sigma) {
        super(ceqs);
        dtree = new DecisionTreeDFA<>(mqs, sigma, strie.root());
        DTInnerNode<I,Boolean> dtRoot = new DTInnerNode<>(null, dtree, new ChildrenDFA<>(), strie.root());
        dtree.setRoot(dtRoot);
        hypothesis = new HypothesisDFA<>(ptree, dtree);
    }

    @Override
    protected int maxSearchIndex(int ceLength) {
        return ceLength;
    }

    @Override
    protected DTLeaf<I, Boolean> getState(Word<I> prefix) {
        return hypothesis.getState(prefix);
    }

    @Override
    protected DFA<?, I> hypothesis() {
        return hypothesis;
    }

    @Override
    protected DecisionTree<I, Boolean> dtree() {
        return dtree;
    }

    @Override
    protected Boolean suffix(Boolean output, int length) {
        return output;
    }

    @Override
    protected Boolean hypOutput(Word<I> word, int length) {
        return hypOutput(word);
    }

    protected Boolean hypOutput(Word<I> word) {
        DTLeaf<I, Boolean> s = getState(word);
        return dtree.isAccepting(s);
    }
}
