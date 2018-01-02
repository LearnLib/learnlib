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
package de.learnlib.algorithms.discriminationtree.hypothesis.vpda;

import de.learnlib.api.AccessSequenceProvider;
import de.learnlib.datastructure.list.IntrusiveListElemImpl;
import net.automatalib.words.Word;

/**
 * @param <I>
 *         input symbol type
 *
 * @author Malte Isberner
 */
public abstract class AbstractHypTrans<I> extends IntrusiveListElemImpl<AbstractHypTrans<I>>
        implements AccessSequenceProvider<I> {

    private final Word<I> aseq;
    protected IntrusiveListElemImpl<AbstractHypTrans<I>> prev;
    private HypLoc<I> treeTarget;
    private DTNode<I> nonTreeTarget;

    public AbstractHypTrans(Word<I> aseq) {
        this.aseq = aseq;
    }

    public HypLoc<I> getTreeTarget() {
        assert isTree();
        return treeTarget;
    }

    public boolean isTree() {
        return treeTarget != null;
    }

    public HypLoc<I> getTargetLocation() {
        if (treeTarget != null) {
            return treeTarget;
        }
        assert nonTreeTarget.isLeaf() : "transition does not point to a leaf";
        assert nonTreeTarget.getData() != null;
        return nonTreeTarget.getData();
    }

    public DTNode<I> getNonTreeTarget() {
        assert !isTree();
        return nonTreeTarget;
    }

    public void setNonTreeTarget(DTNode<I> nonTreeTarget) {
        assert !isTree();
        this.nonTreeTarget = nonTreeTarget;
    }

    public DTNode<I> getTargetNode() {
        if (treeTarget != null) {
            return treeTarget.getLeaf();
        }
        return nonTreeTarget;
    }

    public Word<I> getAccessSequence() {
        return aseq;
    }

    public abstract boolean isInternal();

    public void makeTree(HypLoc<I> tgtLoc) {
        assert !isTree();
        this.treeTarget = tgtLoc;
        this.nonTreeTarget = null;
        removeFromList();
    }

    public void removeFromList() {
        if (next != null) {
            next.prev = prev;
        }
        if (prev != null) {
            prev.setNextElement(next);
        }
        prev = null;
        next = null;
    }
}
