package de.learnlib.algorithms.oml.ttt.dt;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.learnlib.algorithms.oml.ttt.pt.PTNode;
import de.learnlib.algorithms.oml.ttt.st.STNode;

/**
 *
 * @param <I>
 */
public class DTLeaf<I, D> extends DTNode<I, D> {

    private final List<PTNode<I>> shortPrefixes = new LinkedList<>();
    private final List<PTNode<I>> longPrefixes  = new LinkedList<>();


    public DTLeaf(DTInnerNode<I, D> parent, DecisionTree<I, D> tree, PTNode<I> u) {
        super(parent, tree);
        shortPrefixes.add(u);
    }

    public List<PTNode<I>> getShortPrefixes() {
        return shortPrefixes;
    }

    public void addShortPrefix(PTNode<I> u) {
        shortPrefixes.add(u);
    }

    @Override
    void sift(PTNode<I> prefix) {
        prefix.setState(this);
        this.longPrefixes.add(prefix);
    }

    @Override
    void leaves(List<DTLeaf<I, D>> list) {
        list.add(this);
    }

    public boolean refineIfPossible() {
        PTNode<I> ref = shortPrefixes.get(0);
        for (int i=1; i<shortPrefixes.size(); i++) {
            if (refineIfPossible(ref, shortPrefixes.get(i))) {
                return true;
            }
        }
        return false;
    }

    private boolean refineIfPossible(PTNode<I> u1, PTNode<I> u2) {
        for (I a : tree.sigma()) {
            //System.out.println(u1.word() + " : " + u2.word());
            DTLeaf<I, D> ua1 = u1.succ(a).state();
            DTLeaf<I, D> ua2 = u2.succ(a).state();

            if (ua1 != ua2) {
                split(u1, u2, a);
                return true;
            }
        }
        return false;
    }

    public void makeShortPrefix(PTNode<I> uNew) {
        assert !shortPrefixes.contains(uNew);
        assert longPrefixes.contains(uNew);
        longPrefixes.remove(uNew);
        shortPrefixes.add(uNew);

        for (I a : tree.sigma()) {
            PTNode<I> ua = uNew.append(a);
            //System.out.println("Adding prefix: " + ua.word());
            tree.root().sift(ua);
        }
    }

    public void split(PTNode<I> u1, PTNode<I> u2, I a) {
        //System.out.println("Splitting " + u1.word() + " and " + u2.word());
        DTLeaf<I, D> ua1 = u1.succ(a).state();
        DTLeaf<I, D> ua2 = u2.succ(a).state();
        DTNode<I, D> n = lca(ua1, ua2);
        STNode<I> av;
        if (n instanceof DTInnerNode) {
            av = ((DTInnerNode<I, D>) n).suffix().prepend(a);
        } else {
            av = tree.newSuffix(a);
        }

        Children<I, D> newChildren = tree.newChildren();
        DTInnerNode<I, D> newInner = new DTInnerNode<>(parent, tree, newChildren, av);
        LinkedHashMap<D, DTLeaf<I, D>> newLeaves = new LinkedHashMap<>();

        for (PTNode<I> uOther : shortPrefixes) {
            // FIXME: We could safe some queries here in the dfa case ...
            D out = tree.query(uOther, av);
            DTLeaf<I,D> leaf = newLeaves.get(out);
            if (leaf == null) {
                leaf = new DTLeaf<>(newInner, tree, uOther);
                newLeaves.put(out, leaf);
            } else {
                leaf.addShortPrefix(uOther);
            }
            uOther.setState(leaf);
        }

        for (Map.Entry<D, DTLeaf<I, D>> e : newLeaves.entrySet()) {
            newChildren.addChild(e.getKey(), e.getValue());
        }

        if (this != tree.root()) {
            parent.replace(this, newInner);
        }
        else {
            tree.setRoot(newInner);
        }

        for (PTNode<I> ua : longPrefixes) {
            newInner.sift(ua);
        }

    }

    private DTNode<I, D> lca(DTLeaf<I, D> n1, DTLeaf<I, D> n2) {
        Iterator<DTNode<I, D>> p1 = n1.path().iterator();
        Iterator<DTNode<I, D>> p2 = n2.path().iterator();
        DTNode<I, D> lca = tree.root();
        while (p1.hasNext() && p2.hasNext()) {
            DTNode<I, D> t1 = p1.next();
            DTNode<I, D> t2 = p2.next();
            if (t1 != t2) {
                break;
            }
            lca = t1;
        }
        return lca;
    }
}
