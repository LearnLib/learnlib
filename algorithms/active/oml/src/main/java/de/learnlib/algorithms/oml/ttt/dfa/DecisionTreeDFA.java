/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.learnlib.algorithms.oml.ttt.dfa;


import de.learnlib.algorithms.oml.ttt.dt.Children;
import de.learnlib.algorithms.oml.ttt.dt.DTInnerNode;
import de.learnlib.algorithms.oml.ttt.dt.DTLeaf;
import de.learnlib.algorithms.oml.ttt.dt.DTNode;
import de.learnlib.algorithms.oml.ttt.dt.DecisionTree;
import de.learnlib.algorithms.oml.ttt.pt.PTNode;
import de.learnlib.algorithms.oml.ttt.st.STNode;
import de.learnlib.api.oracle.MembershipOracle;
import net.automatalib.words.Alphabet;

/**
 *
 * @author falk
 */
class DecisionTreeDFA<I> extends DecisionTree<I, Boolean> {

    DecisionTreeDFA(MembershipOracle<I, Boolean> mqOracle, Alphabet<I> sigma, STNode<I> stRoot) {
        super(mqOracle, sigma, stRoot);
    }

    boolean isAccepting(DTLeaf<I, Boolean> s) {
        DTNode<I, Boolean> n = s.path().get(1);
        return root().getChildren().key(n);
    }

    @Override
    protected Children<I, Boolean> newChildren() {
        return new ChildrenDFA<>();
    }

    @Override
    protected Boolean query(PTNode<I> prefix, STNode<I> suffix) {
        return mqOracle.answerQuery(prefix.word(), suffix.word());
    }

    private DTInnerNode<I, Boolean> root() {
        return (DTInnerNode<I, Boolean>) root;
    }

}
