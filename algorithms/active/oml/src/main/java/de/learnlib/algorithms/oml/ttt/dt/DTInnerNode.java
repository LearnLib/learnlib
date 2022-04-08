package de.learnlib.algorithms.oml.ttt.dt;

import java.util.List;

import de.learnlib.algorithms.oml.ttt.pt.PTNode;
import de.learnlib.algorithms.oml.ttt.st.STNode;

/**
 *
 * @param <I>
 */
public class DTInnerNode<I, D> extends DTNode<I, D> {

    private final STNode<I> suffix;

    private final Children<I, D> children;

    public DTInnerNode(DTInnerNode<I, D> parent, DecisionTree<I, D> tree, Children<I, D> children, STNode<I> suffix) {
        super(parent, tree);
        this.children = children;
        this.suffix = suffix;

    }

    public Children<I, D> getChildren() {
        return children;
    }

    void sift(PTNode<I> prefix) {
        D out = tree.query(prefix, suffix);
        DTNode<I, D> succ = children.child(out);
        if (succ != null) {
            succ.sift(prefix);
        }
        else {
            //System.out.println("New short prefix (sifting): " + prefix.word());
            DTLeaf<I, D> newLeaf = new DTLeaf<>(this, tree, prefix);
            children.addChild(out, newLeaf);
            prefix.setState( newLeaf );

            for (I a : tree.sigma()) {
                PTNode<I> ua = prefix.append(a);
                //System.out.println("Adding prefix: " + ua.word());
                tree.root().sift(ua);
            }
        }
    }

    @Override
    void leaves(List<DTLeaf<I, D>> list) {
        for (DTNode<I, D> n : children.all()) {
            n.leaves(list);
        }
    }

    void replace(DTLeaf<I, D> oldNode, DTInnerNode<I, D> newNode) {
        this.children.replace(oldNode, newNode);
    }

    STNode<I> suffix() {
        return suffix;
    }
}
