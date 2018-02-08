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

import java.util.Iterator;
import java.util.Map;

import de.learnlib.datastructure.discriminationtree.iterators.TransformingLeavesIterator;
import de.learnlib.datastructure.discriminationtree.model.AbstractTemporaryIntrusiveDTNode;
import de.learnlib.datastructure.discriminationtree.model.BooleanMap;
import de.learnlib.datastructure.list.IntrusiveListElem;

/**
 * @param <I>
 *         input symbol type
 *
 * @author Malte Isberner
 */
public class DTNode<I>
        extends AbstractTemporaryIntrusiveDTNode<ContextPair<I>, Boolean, HypLoc<I>, TransList<I>, DTNode<I>>
        implements IntrusiveListElem<DTNode<I>> {

    private final TransList<I> nonTreeIncoming = new TransList<>();

    public DTNode(DTNode<I> parent, boolean parentLabel) {
        this(parent, parentLabel, null);
    }

    public DTNode(DTNode<I> parent, boolean parentLabel, HypLoc<I> data) {
        super(parent, parentLabel, data);
    }

    public void updateIncoming() {
        for (AbstractHypTrans<I> inc : nonTreeIncoming) {
            assert !inc.isTree();
            inc.setNonTreeTarget(this);
        }
    }

    public void split(ContextPair<I> discriminator, Map<Boolean, DTNode<I>> children) {
        assert isLeaf();
        assert children.values().stream().allMatch(c -> c.parent == this);
        assert children.entrySet().stream().allMatch(e -> e.getKey().equals(e.getValue().getParentOutcome()));

        this.discriminator = discriminator;
        this.children = children;
    }

    public Iterator<HypLoc<I>> subtreeLocsIterator() {
        return new TransformingLeavesIterator<>(this, DTNode::getData);
    }

    public Iterable<HypLoc<I>> subtreeLocations() {
        return this::subtreeLocsIterator;
    }

    public void addIncoming(AbstractHypTrans<I> trans) {
        nonTreeIncoming.add(trans);
    }

    public TransList<I> getIncoming() {
        return nonTreeIncoming;
    }

    @Override
    protected Map<Boolean, DTNode<I>> createChildMap() {
        return new BooleanMap<>();
    }

    @Override
    protected DTNode<I> createChild(Boolean outcome, HypLoc<I> data) {
        return new DTNode<>(this, outcome, data);
    }
}
