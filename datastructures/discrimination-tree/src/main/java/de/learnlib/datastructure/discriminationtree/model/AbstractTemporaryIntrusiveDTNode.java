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

import java.util.Objects;

import de.learnlib.datastructure.discriminationtree.SplitData;
import de.learnlib.datastructure.list.IntrusiveList;
import de.learnlib.datastructure.list.IntrusiveListElem;

/**
 * An extension of the {@link AbstractDTNode} that adds the concept of temporary splitters as well as linking
 * discrimination tree nodes outside of their regular tree structure. Currently used by the TTT algorithm (both regular
 * and VPDA) and the DT algorithm (VPDA variant).
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
 *
 * @author frohme
 */
public abstract class AbstractTemporaryIntrusiveDTNode<DSCR, O, D, T extends IntrusiveList<?>, N extends AbstractTemporaryIntrusiveDTNode<DSCR, O, D, T, N>>
        extends AbstractDTNode<DSCR, O, D, N> implements IntrusiveListElem<N> {

    protected SplitData<O, T> splitData;

    protected IntrusiveListElem<N> prevElement;
    protected N nextElement;

    // LEAF NODE DATA
    protected boolean temp;

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
        return (discriminator != null);
    }

    public boolean isBlockRoot() {
        return (prevElement != null);
    }

    public void removeFromBlockList() {
        if (prevElement != null) {
            prevElement.setNextElement(nextElement);
            if (nextElement != null) {
                getNextElement().prevElement = prevElement;
            }
            nextElement = null;
            prevElement = null;
        }
    }

    @Override
    public N getNextElement() {
        return nextElement;
    }

    @Override
    public void setNextElement(N nextBlock) {
        this.nextElement = nextBlock;
    }

    public IntrusiveListElem<N> getPrevElement() {
        return prevElement;
    }

    public void setPrevElement(IntrusiveListElem<N> prevElement) {
        this.prevElement = prevElement;
    }
}
