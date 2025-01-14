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
package de.learnlib.algorithm.observationpack.hypothesis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import de.learnlib.datastructure.discriminationtree.model.AbstractWordBasedDTNode;
import net.automatalib.common.util.array.ArrayStorage;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;

public class HState<I, O, SP, TP> {

    private final @Nullable HTransition<I, O, SP, TP> treeIncoming;
    private final int id;
    private final int depth;
    private final ArrayStorage<HTransition<I, O, SP, TP>> transitions;
    private final List<HTransition<I, O, SP, TP>> nonTreeIncoming = new ArrayList<>();
    private AbstractWordBasedDTNode<I, O, HState<I, O, SP, TP>> dtLeaf;
    private SP property;

    public HState(int alphabetSize) {
        this(alphabetSize, 0, null);
    }

    public HState(int initialAlphabetSize, int id, @Nullable HTransition<I, O, SP, TP> treeIncoming) {
        this.id = id;
        this.treeIncoming = treeIncoming;
        this.depth = (treeIncoming == null) ? 0 : treeIncoming.getSource().depth + 1;
        this.transitions = new ArrayStorage<>(initialAlphabetSize);
    }

    public AbstractWordBasedDTNode<I, O, HState<I, O, SP, TP>> getDTLeaf() {
        return dtLeaf;
    }

    public void setDTLeaf(AbstractWordBasedDTNode<I, O, HState<I, O, SP, TP>> dtLeaf) {
        this.dtLeaf = dtLeaf;
    }

    public @Nullable HTransition<I, O, SP, TP> getTreeIncoming() {
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

    public HTransition<I, O, SP, TP> getTransition(int transIdx) {
        return transitions.get(transIdx);
    }

    public void setTransition(int transIdx, HTransition<I, O, SP, TP> transition) {
        transitions.set(transIdx, transition);
    }

    public Collection<HTransition<I, O, SP, TP>> getOutgoingTransitions() {
        return Collections.unmodifiableList(transitions);
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

    public boolean ensureInputCapacity(int capacity) {
        return this.transitions.ensureCapacity(capacity);
    }

    @Override
    public String toString() {
        return "q" + id;
    }
}
