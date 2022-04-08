package de.learnlib.algorithms.oml.ttt.mealy;

import java.util.Collection;

import de.learnlib.algorithms.oml.ttt.dt.DTLeaf;
import de.learnlib.algorithms.oml.ttt.pt.PTNode;
import de.learnlib.algorithms.oml.ttt.pt.PrefixTree;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.words.Word;


final class HypothesisMealy<I, O> implements MealyMachine<DTLeaf<I, Word<O>>, I, MealyTransition<I, O>, O> {

    private final PrefixTree<I> ptree;

    private final DecisionTreeMealy<I, O> dtree;

    HypothesisMealy(PrefixTree<I> ptree, DecisionTreeMealy<I, O> dtree) {
        this.ptree = ptree;
        this.dtree = dtree;
    }

    @Override
    public Collection<DTLeaf<I, Word<O>>> getStates() {
        return dtree.leaves();
    }

    @Override
    public O getTransitionOutput(MealyTransition<I, O> o) {
        return dtree.getOutput(o.source, o.input).lastSymbol();
    }

    @Override
    public MealyTransition<I, O> getTransition(DTLeaf<I, Word<O>> iWordDTLeaf, I i) {
        return new MealyTransition<>(iWordDTLeaf, i);
    }

    @Override
    public DTLeaf<I, Word<O>> getSuccessor(MealyTransition<I, O> o) {
        PTNode<I> u = o.source.getShortPrefixes().get(0);
        assert u != null;
        PTNode<I> ua = u.succ(o.input);
        assert ua != null;
        DTLeaf<I, Word<O>> dst = ua.state();
        assert dst != null;
        return dst;
    }

    @Override
    public DTLeaf<I, Word<O>> getInitialState() {
        return ptree.root().state();
    }
}
