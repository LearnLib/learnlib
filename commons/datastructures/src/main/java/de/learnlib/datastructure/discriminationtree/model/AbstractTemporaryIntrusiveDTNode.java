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
package de.learnlib.datastructure.discriminationtree.model;

import java.util.Objects;

import de.learnlib.datastructure.discriminationtree.SplitData;
import de.learnlib.datastructure.list.IntrusiveListEntry;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * An extension of the {@link AbstractDTNode} that adds the concept of temporary splitters as well as linking
 * discrimination tree nodes outside their regular tree structure. Currently used by the TTT algorithm (both regular
 * and VPA) and the DT algorithm (VPA variant).
 *
 * @param <DSCR>
 *         discriminator type
 * @param <O>
 *         output symbol type
 * @param <D>
 *         node data type
 * @param <T>
 *         link structure type
 * @param <N>
 *         node type
 */
public abstract class AbstractTemporaryIntrusiveDTNode<DSCR, O, D, T, N extends AbstractTemporaryIntrusiveDTNode<DSCR, O, D, T, N>>
        extends AbstractDTNode<DSCR, O, D, N> implements IntrusiveListEntry<N> {

    private IntrusiveListEntry<N> prevElement;
    private IntrusiveListEntry<N> nextElement;

    private SplitData<O, T> splitData;

    // LEAF NODE DATA
    private boolean temp;

    public AbstractTemporaryIntrusiveDTNode(N parent, O parentOutcome, D data) {
        super(parent, parentOutcome, data);
    }

    public void setChild(O label, N newChild) {
        assert newChild.parent == this;
        assert Objects.equals(newChild.getParentOutcome(), label);

        children.put(label, newChild);
    }

    public boolean isTemp() {
        return temp;
    }

    public void setTemp(boolean temp) {
        this.temp = temp;
    }

    public SplitData<O, T> getSplitData() {
        return splitData;
    }

    public void setSplitData(SplitData<O, T> splitData) {
        this.splitData = splitData;
    }

    public N anyChild() {
        assert isInner();
        return children.values().iterator().next();
    }

    public boolean isInner() {
        return discriminator != null;
    }

    public boolean isBlockRoot() {
        return prevElement != null;
    }

    @Override
    public @Nullable IntrusiveListEntry<N> getNext() {
        return nextElement;
    }

    @Override
    public void setNext(@Nullable IntrusiveListEntry<N> nextBlock) {
        this.nextElement = nextBlock;
    }

    @Override
    public @Nullable IntrusiveListEntry<N> getPrev() {
        return prevElement;
    }

    @Override
    public void setPrev(@Nullable IntrusiveListEntry<N> prevElement) {
        this.prevElement = prevElement;
    }
}
