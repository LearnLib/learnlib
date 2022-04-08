package de.learnlib.algorithms.oml.ttt.dt;

import java.util.Collection;

public interface Children<I, D> {

    DTNode<I, D> child(D out);

    D key(DTNode<I, D> child);

    void addChild(D out, DTNode<I, D> child);

    void replace(DTLeaf<I, D> oldNode, DTInnerNode<I, D> newNode);

    //DTLeaf<I, D> newChild(D out, DTInnerNode<I, D> parent, PTNode<I> prefix);

    Collection<DTNode<I, D>> all();

}
