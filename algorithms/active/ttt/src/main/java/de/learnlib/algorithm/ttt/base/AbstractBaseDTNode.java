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
package de.learnlib.algorithm.ttt.base;

import java.util.Iterator;

import de.learnlib.datastructure.discriminationtree.iterators.DiscriminationTreeIterators;
import de.learnlib.datastructure.discriminationtree.model.AbstractTemporaryIntrusiveDTNode;
import de.learnlib.datastructure.list.IntrusiveList;
import de.learnlib.datastructure.list.IntrusiveListEntry;
import net.automatalib.word.Word;

public abstract class AbstractBaseDTNode<I, D>
        extends AbstractTemporaryIntrusiveDTNode<Word<I>, D, TTTState<I, D>, IntrusiveList<TTTTransition<I, D>>, AbstractBaseDTNode<I, D>>
        implements IntrusiveListEntry<AbstractBaseDTNode<I, D>> {

    private final IntrusiveList<TTTTransition<I, D>> incoming = new IntrusiveList<>();

    public AbstractBaseDTNode() {
        this(null, null);
    }

    public AbstractBaseDTNode(AbstractBaseDTNode<I, D> parent, D parentEdgeLabel) {
        super(parent, parentEdgeLabel, null);
    }

    public TTTState<I, D> anySubtreeState() {
        AbstractBaseDTNode<I, D> curr = this;
        while (!curr.isLeaf()) {
            curr = curr.anyChild();
        }
        return curr.data;
    }

    public Iterable<TTTState<I, D>> subtreeStates() {
        return this::subtreeStatesIterator;
    }

    public Iterator<TTTState<I, D>> subtreeStatesIterator() {
        return DiscriminationTreeIterators.transformingLeafIterator(this, AbstractBaseDTNode::getData);
    }

    public IntrusiveList<TTTTransition<I, D>> getIncoming() {
        return incoming;
    }

    public Iterator<AbstractBaseDTNode<I, D>> subtreeNodesIterator() {
        return DiscriminationTreeIterators.nodeIterator(this);
    }

    /**
     * Updates the {@link TTTTransition#nonTreeTarget} attribute to point to this node for all transitions in the
     * incoming list.
     */
    void updateIncoming() {
        for (TTTTransition<I, D> trans : incoming) {
            trans.nonTreeTarget = this;
        }
    }

    @Override
    public AbstractBaseDTNode<I, D> getElement() {
        return this;
    }
}
