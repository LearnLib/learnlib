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
package de.learnlib.datastructure.discriminationtree;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import de.learnlib.datastructure.list.IntrusiveList;

/**
 * Data associated with a discrimination tree node while an enclosing subtree is being split.
 *
 * @param <O>
 *         output symbol type
 * @param <T>
 *         transition type
 *
 * @author Malte Isberner
 */
public class SplitData<O, T extends IntrusiveList<?>> {

    // TODO: HashSets/Maps are quite an overkill for booleans
    private final Set<O> marks = new HashSet<>();
    private final Map<O, T> incomingTransitions = new HashMap<>();
    private final Supplier<T> listSuppllier;
    private O stateLabel;

    public SplitData(Supplier<T> listSuppllier) {
        this.listSuppllier = listSuppllier;
    }

    /**
     * Mark this node with the given label. The result indicates whether it has been newly marked.
     *
     * @param label
     *         the label to mark this node with
     *
     * @return {@code true} if the node was previously unmarked (wrt. to the given label), {@code false} otherwise
     */
    public boolean mark(O label) {
        return marks.add(label);
    }

    public Set<O> getLabels() {
        return marks;
    }

    /**
     * Retrieves the state label associated with this split data.
     * <p>
     * <b>Note:</b> invoking this operation is illegal if no state label has previously been set.
     *
     * @return the state label
     */
    public O getStateLabel() {
        assert hasStateLabel();

        return stateLabel;
    }

    /**
     * Sets the state label associated with this split data.
     * <p>
     * <b>Note:</b> invoking this operation is illegal if a state label has already been set.
     *
     * @param label
     *         the state label
     */
    public void setStateLabel(O label) {
        assert !hasStateLabel();

        this.stateLabel = label;
    }

    /**
     * Checks whether there is a state label associated with this node, regardless of its value.
     *
     * @return {@code true} if there is a state label ({@code true} or {@code false}) associated with this node, {@code
     * false} otherwise
     */
    public boolean hasStateLabel() {
        return (stateLabel != null);
    }

    /**
     * Retrieves the list of incoming transitions for the respective label.
     * <p>
     * This method will always return a non-{@code null} value.
     *
     * @param label
     *         the label
     *
     * @return the (possibly empty) list associated with the given state label
     */
    public T getIncoming(O label) {
        return incomingTransitions.computeIfAbsent(label, k -> listSuppllier.get());
    }

    /**
     * Checks whether the corresponding node is marked with the given label.
     *
     * @param label
     *         the label
     *
     * @return {@code true} if the corresponding node is marked with the given label, {@code false} otherwise
     */
    public boolean isMarked(O label) {
        return marks.contains(label);
    }
}
