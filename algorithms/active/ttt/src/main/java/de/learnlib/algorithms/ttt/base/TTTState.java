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
package de.learnlib.algorithms.ttt.base;

import java.io.Serializable;

import de.learnlib.api.AccessSequenceProvider;
import net.automatalib.commons.util.array.ResizingObjectArray;
import net.automatalib.words.Word;

/**
 * A state in a {@link AbstractTTTHypothesis}.
 *
 * @param <I>
 *         input symbol
 *
 * @author Malte Isberner
 */
public class TTTState<I, D> implements AccessSequenceProvider<I>, Serializable {

    final int id;

    private final ResizingObjectArray transitions;
    private final TTTTransition<I, D> parentTransition;

    AbstractBaseDTNode<I, D> dtLeaf;

    public TTTState(int initialAlphabetSize, TTTTransition<I, D> parentTransition, int id) {
        this.id = id;
        this.parentTransition = parentTransition;
        this.transitions = new ResizingObjectArray(initialAlphabetSize);
    }

    /**
     * Checks whether this state is the initial state (i.e., the root of the spanning tree).
     *
     * @return {@code true} if this state is the initial state, {@code false} otherwise
     */
    public boolean isRoot() {
        return (getParentTransition() == null);
    }

    public TTTTransition<I, D> getParentTransition() {
        return parentTransition;
    }

    /**
     * Retrieves the discrimination tree leaf associated with this state.
     *
     * @return the discrimination tree leaf associated with this state
     */
    public AbstractBaseDTNode<I, D> getDTLeaf() {
        return dtLeaf;
    }

    @Override
    public Word<I> getAccessSequence() {
        if (getParentTransition() != null) {
            return getParentTransition().getAccessSequence();
        }
        return Word.epsilon(); // root
    }

    @Override
    public String toString() {
        return "s" + id;
    }

    public void setTransition(final int idx, final TTTTransition<I, D> transition) {
        transitions.array[idx] = transition;
    }

    @SuppressWarnings("unchecked")
    public TTTTransition<I, D> getTransition(final int idx) {
        return (TTTTransition<I, D>) transitions.array[idx];
    }

    @SuppressWarnings("unchecked")
    public TTTTransition<I, D>[] getTransitions() {
        return (TTTTransition<I, D>[]) transitions.array;
    }

    /**
     * See {@link ResizingObjectArray#ensureCapacity(int)}.
     */
    public boolean ensureInputCapacity(int capacity) {
        return this.transitions.ensureCapacity(capacity);
    }
}
