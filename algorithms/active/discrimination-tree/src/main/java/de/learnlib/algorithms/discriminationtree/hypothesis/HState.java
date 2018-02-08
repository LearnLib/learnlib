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
package de.learnlib.algorithms.discriminationtree.hypothesis;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import de.learnlib.datastructure.discriminationtree.model.AbstractWordBasedDTNode;
import net.automatalib.commons.util.array.ResizingObjectArray;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

public class HState<I, O, SP, TP> implements Serializable {

    private final HTransition<I, O, SP, TP> treeIncoming;
    private final int id;
    private final int depth;
    private final ResizingObjectArray transitions;
    private final List<HTransition<I, O, SP, TP>> nonTreeIncoming = new ArrayList<>();
    private AbstractWordBasedDTNode<I, O, HState<I, O, SP, TP>> dtLeaf;
    private SP property;

    public HState(int alphabetSize) {
        this(alphabetSize, 0, null);
    }

    @SuppressWarnings("unchecked")
    public HState(int initialAlphabetSize, int id, HTransition<I, O, SP, TP> treeIncoming) {
        this.id = id;
        this.treeIncoming = treeIncoming;
        this.depth = (treeIncoming == null) ? 0 : treeIncoming.getSource().depth + 1;
        this.transitions = new ResizingObjectArray(initialAlphabetSize);
    }

    public AbstractWordBasedDTNode<I, O, HState<I, O, SP, TP>> getDTLeaf() {
        return dtLeaf;
    }

    public void setDTLeaf(AbstractWordBasedDTNode<I, O, HState<I, O, SP, TP>> dtLeaf) {
        this.dtLeaf = dtLeaf;
    }

    public HTransition<I, O, SP, TP> getTreeIncoming() {
        return treeIncoming;
    }

    public void appendAccessSequence(List<? super I> symList) {
        if (treeIncoming == null) {
            return;
        }
        treeIncoming.getSource().appendAccessSequence(symList);
        symList.add(treeIncoming.getSymbol());
    }

    public Word<I> getAccessSequence() {
        if (treeIncoming == null) {
            return Word.epsilon();
        }
        WordBuilder<I> wb = new WordBuilder<>(depth);
        appendAccessSequence(wb);
        return wb.toWord();
    }

    public SP getProperty() {
        return property;
    }

    public void setProperty(SP property) {
        this.property = property;
    }

    public int getId() {
        return id;
    }

    @SuppressWarnings("unchecked")
    public HTransition<I, O, SP, TP> getTransition(int transIdx) {
        return (HTransition<I, O, SP, TP>) transitions.array[transIdx];
    }

    public void setTransition(int transIdx, HTransition<I, O, SP, TP> transition) {
        transitions.array[transIdx] = transition;
    }

    @SuppressWarnings("unchecked")
    public Collection<HTransition<I, O, SP, TP>> getOutgoingTransitions() {
        return Collections.unmodifiableList(Arrays.asList((HTransition<I, O, SP, TP>[]) transitions.array));
    }

    public int getDepth() {
        return depth;
    }

    public void addNonTreeIncoming(HTransition<I, O, SP, TP> trans) {
        nonTreeIncoming.add(trans);
    }

    public void fetchNonTreeIncoming(Collection<? super HTransition<I, O, SP, TP>> target) {
        target.addAll(nonTreeIncoming);
        nonTreeIncoming.clear();
    }

    /**
     * See {@link ResizingObjectArray#ensureCapacity(int)}.
     */
    public boolean ensureInputCapacity(int capacity) {
        return this.transitions.ensureCapacity(capacity);
    }

    @Override
    public String toString() {
        return "q" + id;
    }
}
