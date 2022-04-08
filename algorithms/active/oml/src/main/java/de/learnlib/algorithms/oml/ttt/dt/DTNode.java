package de.learnlib.algorithms.oml.ttt.dt;

import java.util.LinkedList;
import java.util.List;

import de.learnlib.algorithms.oml.ttt.pt.PTNode;

public abstract class DTNode<I, D> {

    final DTInnerNode<I, D> parent;

    final DecisionTree<I, D> tree;

    public DTNode(DTInnerNode<I, D> parent, DecisionTree<I, D> tree) {
        this.parent = parent;
        this.tree = tree;
    }

    public List<DTNode<I, D>> path() {
        List<DTNode<I, D>> path = new LinkedList<>();
        this.path(path);
        return path;
    }

    void path(List<DTNode<I, D>> path) {
        path.add(0, this);
        if (this != tree.root()) {
            parent.path(path);
        }
    }

    abstract void sift(PTNode<I> prefix);

    abstract void leaves(List<DTLeaf<I, D>> list);

}

