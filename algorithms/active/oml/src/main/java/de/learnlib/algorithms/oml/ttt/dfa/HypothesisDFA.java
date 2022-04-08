/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.learnlib.algorithms.oml.ttt.dfa;

import java.util.Collection;

import de.learnlib.algorithms.oml.ttt.dt.DTLeaf;
import de.learnlib.algorithms.oml.ttt.pt.PTNode;
import de.learnlib.algorithms.oml.ttt.pt.PrefixTree;
import net.automatalib.automata.fsa.DFA;

/**
 *
 * @author falk
 */
final class HypothesisDFA<I> implements DFA<DTLeaf<I, Boolean>, I> {
    
    private final PrefixTree<I> ptree;
    
    private final DecisionTreeDFA<I> dtree;

    public HypothesisDFA(PrefixTree<I> ptree, DecisionTreeDFA<I> dtree) {
        this.ptree = ptree;
        this.dtree = dtree;
    }
    
    @Override
    public DTLeaf<I, Boolean> getTransition(DTLeaf<I, Boolean> s, I a) {
        PTNode<I> u = s.getShortPrefixes().get(0);
        assert u != null;
        PTNode<I> ua = u.succ(a);
        assert ua != null;
        DTLeaf<I, Boolean> dst = ua.state();
        assert dst != null;
        return dst;
    }

    @Override
    public boolean isAccepting(DTLeaf<I, Boolean> s) {
        assert s != null;
        return dtree.isAccepting(s);
    }

    @Override
    public DTLeaf<I, Boolean> getInitialState() {
        return ptree.root().state();
    }

    @Override
    public Collection<DTLeaf<I, Boolean>> getStates() {
        return dtree.leaves();
    }

    
}
