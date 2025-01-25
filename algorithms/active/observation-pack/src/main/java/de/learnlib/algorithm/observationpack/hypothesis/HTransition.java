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

import de.learnlib.datastructure.discriminationtree.model.AbstractWordBasedDTNode;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;
import org.checkerframework.checker.nullness.qual.EnsuresNonNullIf;
import org.checkerframework.checker.nullness.qual.Nullable;

public class HTransition<I, O, SP, TP> {

    // GENERAL PURPOSE FIELDS
    private final HState<I, O, SP, TP> source;
    private final I symbol;
    private TP property;

    // TREE EDGE FIELDS
    private @Nullable HState<I, O, SP, TP> treeTgt;

    // NON-TREE EDGE FIELDS
    private @Nullable AbstractWordBasedDTNode<I, O, HState<I, O, SP, TP>> dt;

    public HTransition(HState<I, O, SP, TP> source,
                       I symbol,
                       AbstractWordBasedDTNode<I, O, HState<I, O, SP, TP>> dtTgt) {
        this.source = source;
        this.symbol = symbol;
        this.treeTgt = null;
        this.dt = dtTgt;
    }

    public HState<I, O, SP, TP> getSource() {
        return source;
    }

    public I getSymbol() {
        return symbol;
    }

    public TP getProperty() {
        return property;
    }

    public void setProperty(TP property) {
        this.property = property;
    }

    public HState<I, O, SP, TP> getTreeTarget() {
        assert isTree();
        return treeTgt;
    }

    @EnsuresNonNullIf(expression = "treeTgt", result = true)
    public boolean isTree() {
        return treeTgt != null;
    }

    @EnsuresNonNullIf(expression = "dt", result = true)
    public boolean isNonTree() {
        return dt != null;
    }

    public AbstractWordBasedDTNode<I, O, HState<I, O, SP, TP>> getDT() {
        assert isNonTree();
        return dt;
    }

    public void setDT(AbstractWordBasedDTNode<I, O, HState<I, O, SP, TP>> dtNode) {
        assert !isTree();
        this.dt = dtNode;
    }

    public void makeTree(HState<I, O, SP, TP> treeTgt) {
        if (this.treeTgt != null) {
            throw new IllegalStateException(
                    "Cannot make transition [" + getAccessSequence() + "] a tree transition: already is");
        }

        this.treeTgt = treeTgt;
        this.dt = null;
    }

    public Word<I> getAccessSequence() {
        WordBuilder<I> wb = new WordBuilder<>(source.getDepth() + 1);
        source.appendAccessSequence(wb);
        wb.append(symbol);
        return wb.toWord();
    }

    public HState<I, O, SP, TP> nonTreeTarget() {
        assert isNonTree();
        return dt.getData();
    }

    public HState<I, O, SP, TP> currentTarget() {
        if (treeTgt != null) {
            return treeTgt;
        } else if (dt != null) {
            return dt.getData();
        } else {
            throw new IllegalStateException("Neither tree target not DT node provided");
        }
    }

}
