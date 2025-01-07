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

import de.learnlib.AccessSequenceProvider;
import de.learnlib.datastructure.list.AbstractIntrusiveListEntryImpl;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A transition in a {@link AbstractTTTHypothesis}.
 *
 * @param <I>
 *         input symbol type
 */
public class TTTTransition<I, D> extends AbstractIntrusiveListEntryImpl<TTTTransition<I, D>>
        implements AccessSequenceProvider<I> {

    private final TTTState<I, D> source;
    private final I input;
    // NON-TREE TRANSITION
    AbstractBaseDTNode<I, D> nonTreeTarget;
    // TREE TRANSITION
    private TTTState<I, D> treeTarget;

    public TTTTransition(TTTState<I, D> source, I input) {
        this.source = source;
        this.input = input;
    }

    public TTTState<I, D> getTreeTarget() {
        assert isTree();

        return treeTarget;
    }

    public boolean isTree() {
        return treeTarget != null;
    }

    public AbstractBaseDTNode<I, D> getNonTreeTarget() {
        assert !isTree();

        return nonTreeTarget;
    }

    void setNonTreeTarget(AbstractBaseDTNode<I, D> nonTreeTarget) {
        this.nonTreeTarget = nonTreeTarget;
        nonTreeTarget.getIncoming().add(this);
    }

    public AbstractBaseDTNode<I, D> getDTTarget() {
        if (treeTarget != null) {
            return treeTarget.dtLeaf;
        }
        return nonTreeTarget;
    }

    public TTTState<I, D> getTarget() {
        if (treeTarget != null) {
            return treeTarget;
        }

        assert nonTreeTarget.isLeaf() :
                "transition target is not a leaf, but is a " + (nonTreeTarget.isTemp() ? "temp" : "non-temp") +
                " node with discriminator: " + nonTreeTarget.getDiscriminator();
        assert nonTreeTarget.getData() != null;
        return nonTreeTarget.getData();
    }

    public TTTState<I, D> getSource() {
        return source;
    }

    public I getInput() {
        return input;
    }

    protected @Nullable Object getProperty() {
        return null;
    }

    @Override
    public Word<I> getAccessSequence() {
        WordBuilder<I> wb = new WordBuilder<>(); // FIXME capacity hint

        TTTTransition<I, D> curr = this;

        while (curr != null) {
            wb.add(curr.input);
            curr = curr.source.getParentTransition();
        }

        return wb.reverse().toWord();
    }

    void makeTree(TTTState<I, D> treeTarget) {
        removeFromList();
        this.treeTarget = treeTarget;
        this.nonTreeTarget = null;
    }

    @Override
    public TTTTransition<I, D> getElement() {
        return this;
    }
}
