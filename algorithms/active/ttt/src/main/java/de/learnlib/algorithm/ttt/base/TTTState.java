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
import net.automatalib.common.util.array.ArrayStorage;
import net.automatalib.word.Word;

/**
 * A state in a {@link AbstractTTTHypothesis}.
 *
 * @param <I>
 *         input symbol
 */
public class TTTState<I, D> implements AccessSequenceProvider<I> {

    final int id;

    private final ArrayStorage<TTTTransition<I, D>> transitions;
    private final TTTTransition<I, D> parentTransition;

    AbstractBaseDTNode<I, D> dtLeaf;

    public TTTState(int initialAlphabetSize, TTTTransition<I, D> parentTransition, int id) {
        this.id = id;
        this.parentTransition = parentTransition;
        this.transitions = new ArrayStorage<>(initialAlphabetSize);
    }

    /**
     * Checks whether this state is the initial state (i.e., the root of the spanning tree).
     *
     * @return {@code true} if this state is the initial state, {@code false} otherwise
     */
    public boolean isRoot() {
        return getParentTransition() == null;
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

    public void setTransition(int idx, TTTTransition<I, D> transition) {
        transitions.set(idx, transition);
    }

    public TTTTransition<I, D> getTransition(int idx) {
        return transitions.get(idx);
    }

    public Iterable<TTTTransition<I, D>> getTransitions() {
        return transitions;
    }

    public boolean ensureInputCapacity(int capacity) {
        return this.transitions.ensureCapacity(capacity);
    }
}
