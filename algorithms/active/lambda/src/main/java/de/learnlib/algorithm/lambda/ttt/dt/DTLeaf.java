/* Copyright (C) 2013-2025 TU Dortmund University
 * This file is part of LearnLib <https://learnlib.de>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.learnlib.algorithm.lambda.ttt.dt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.learnlib.algorithm.lambda.ttt.pt.PTNode;
import de.learnlib.algorithm.lambda.ttt.st.STNode;
import org.checkerframework.checker.nullness.qual.Nullable;

public class DTLeaf<I, D> extends AbstractDTNode<I, D> {

    private final List<PTNode<I, D>> shortPrefixes;
    private final List<PTNode<I, D>> longPrefixes;

    public DTLeaf(@Nullable DTInnerNode<I, D> parent, AbstractDecisionTree<I, D> tree, PTNode<I, D> u) {
        super(parent, tree);
        this.shortPrefixes = new ArrayList<>();
        this.longPrefixes = new ArrayList<>();
        shortPrefixes.add(u);
    }

    public List<PTNode<I, D>> getShortPrefixes() {
        return shortPrefixes;
    }

    public void addShortPrefix(PTNode<I, D> u) {
        shortPrefixes.add(u);
    }

    @Override
    void sift(PTNode<I, D> prefix) {
        prefix.setState(this);
        this.longPrefixes.add(prefix);
    }

    @Override
    void leaves(List<DTLeaf<I, D>> list) {
        list.add(this);
    }

    public boolean refineIfPossible() {
        PTNode<I, D> ref = shortPrefixes.get(0);
        for (int i = 1; i < shortPrefixes.size(); i++) {
            if (refineIfPossible(ref, shortPrefixes.get(i))) {
                return true;
            }
        }
        return false;
    }

    private boolean refineIfPossible(PTNode<I, D> u1, PTNode<I, D> u2) {
        I bestA = null;
        int vLength = 0;
        for (I a : tree.getAlphabet()) {
            PTNode<I, D> s1 = u1.succ(a);
            PTNode<I, D> s2 = u2.succ(a);
            assert s1 != null && s2 != null;
            DTLeaf<I, D> ua1 = s1.state();
            DTLeaf<I, D> ua2 = s2.state();

            if (ua1 != ua2) {
                int l = ((DTInnerNode<I, ?>) lca(ua1, ua2)).suffix().word().length();
                if (bestA == null || l < vLength) {
                    vLength = l;
                    bestA = a;
                }
            }
        }
        if (bestA != null) {
            split(u1, u2, bestA);
            return true;
        }
        return false;
    }

    public void makeShortPrefix(PTNode<I, D> uNew) {
        assert !shortPrefixes.contains(uNew);
        assert longPrefixes.contains(uNew);
        longPrefixes.remove(uNew);
        shortPrefixes.add(uNew);

        for (I a : tree.getAlphabet()) {
            PTNode<I, D> ua = uNew.append(a);
            tree.root().sift(ua);
        }
    }

    public void split(PTNode<I, D> u1, PTNode<I, D> u2, I a) {
        PTNode<I, D> s1 = u1.succ(a);
        PTNode<I, D> s2 = u2.succ(a);
        assert s1 != null && s2 != null;
        DTLeaf<I, D> ua1 = s1.state();
        DTLeaf<I, D> ua2 = s2.state();
        AbstractDTNode<I, D> n = lca(ua1, ua2);
        STNode<I> av;
        if (n instanceof DTInnerNode) {
            av = ((DTInnerNode<I, D>) n).suffix().prepend(a);
        } else {
            av = tree.newSuffix(a);
        }

        Children<I, D> newChildren = tree.newChildren();
        DTInnerNode<I, D> newInner = new DTInnerNode<>(parent, tree, newChildren, av);
        Map<D, DTLeaf<I, D>> newLeaves = new HashMap<>();

        for (PTNode<I, D> uOther : shortPrefixes) {
            // FIXME: We could safe some queries here in the dfa case ...
            D out = tree.query(uOther, av);
            DTLeaf<I, D> leaf = newLeaves.get(out);
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

        if (parent != null) {
            parent.replace(this, newInner);
        } else {
            tree.setRoot(newInner);
        }

        for (PTNode<I, D> ua : longPrefixes) {
            newInner.sift(ua);
        }

    }

    private AbstractDTNode<I, D> lca(DTLeaf<I, D> n1, DTLeaf<I, D> n2) {
        Iterator<AbstractDTNode<I, D>> p1 = n1.path().iterator();
        Iterator<AbstractDTNode<I, D>> p2 = n2.path().iterator();
        AbstractDTNode<I, D> lca = tree.root();
        while (p1.hasNext() && p2.hasNext()) {
            AbstractDTNode<I, D> t1 = p1.next();
            AbstractDTNode<I, D> t2 = p2.next();
            if (t1 != t2) {
                break;
            }
            lca = t1;
        }
        return lca;
    }
}
