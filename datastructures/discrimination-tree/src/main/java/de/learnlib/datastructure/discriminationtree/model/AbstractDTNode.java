/* Copyright (C) 2013-2018 TU Dortmund
 * This file is part of LearnLib, http://www.learnlib.de/.
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
package de.learnlib.datastructure.discriminationtree.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * An abstract super class (DAO) for aggregating several information stored in a node of an discrimination tree.
 *
 * @param <DSCR>
 *         discriminator type
 * @param <O>
 *         output symbol type
 * @param <D>
 *         data type
 * @param <N>
 *         (recursive) node type
 *
 * @author frohme
 */
public abstract class AbstractDTNode<DSCR, O, D, N extends AbstractDTNode<DSCR, O, D, N>> implements Serializable {

    protected final N parent;
    protected final O parentOutcome;
    protected final int depth;
    protected Map<O, N> children;
    protected DSCR discriminator;
    protected D data;

    public AbstractDTNode(D data) {
        this(null, null, data);
    }

    protected AbstractDTNode(N parent, O parentOutcome, D data) {
        this.parent = parent;
        this.parentOutcome = parentOutcome;
        this.depth = (parent != null) ? parent.depth + 1 : 0;
        this.data = data;
    }

    public boolean isRoot() {
        return parent == null;
    }

    public N getParent() {
        return parent;
    }

    public DSCR getDiscriminator() {
        return discriminator;
    }

    public void setDiscriminator(DSCR discriminator) {
        this.discriminator = discriminator;
    }

    public SplitResult split(DSCR discriminator, O oldOut, O newOut) {
        return this.split(discriminator, oldOut, newOut, null);
    }

    public SplitResult split(DSCR discriminator, O oldOut, O newOut, D newData) {
        assert this.isLeaf();
        assert !Objects.equals(oldOut, newOut);

        this.children = createChildMap();

        final N nodeOld = addChild(oldOut, this.data);
        final N nodeNew = addChild(newOut, newData);

        this.data = null;
        this.discriminator = discriminator;

        return new SplitResult(nodeOld, nodeNew);
    }

    public boolean isLeaf() {
        return (children == null);
    }

    protected abstract Map<O, N> createChildMap();

    protected N addChild(O outcome, D data) {
        final N child = createChild(outcome, data);
        children.put(outcome, child);
        return child;
    }

    protected abstract N createChild(O outcome, D data);

    public N child(O out) {
        return child(out, null);
    }

    public N child(O out, D defaultData) {
        assert !isLeaf();

        N result = getChild(out);
        if (result == null) {
            result = addChild(out, defaultData);
        }
        return result;
    }

    public N getChild(O out) {
        return children.get(out);
    }

    public Collection<N> getChildren() {
        return children.values();
    }

    public Collection<Map.Entry<O, N>> getChildEntries() {
        return children.entrySet();
    }

    public void replaceChildren(Map<O, N> repChildren) {
        this.children = repChildren;
    }

    public int getDepth() {
        return depth;
    }

    public D getData() {
        assert isLeaf();
        return data;
    }

    public void setData(D data) {
        assert isLeaf();
        this.data = data;
    }

    public O subtreeLabel(N descendant) {
        N curr = descendant;

        while (curr.depth > this.depth + 1) {
            curr = curr.parent;
        }

        if (curr.parent != this) {
            return null;
        }

        return curr.getParentOutcome();
    }

    public O getParentOutcome() {
        return parentOutcome;
    }

    public class SplitResult {

        public final N nodeOld;
        public final N nodeNew;

        public SplitResult(N nodeOld, N nodeNew) {
            this.nodeOld = nodeOld;
            this.nodeNew = nodeNew;
        }
    }

}
