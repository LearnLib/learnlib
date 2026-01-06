/* Copyright (C) 2013-2026 TU Dortmund University
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
package de.learnlib.algorithm.sparse;

import java.util.Collections;
import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Leaves can be split or unsplit. An unsplit leaf has a single compatible core row to which it points. As new core rows
 * emerge, the observations of the leaf may not suffice anymore to uniquely assign it to some core row. Then, it becomes
 * split. Split leaves cache suffix selection by reference to separators. Leaves remember how many core rows and
 * suffixes existed at their last visit. This information is used as a logical timestamp to check if the separator is
 * still known to be optimal or if it needs to be recomputed.
 */
class Leaf<S, I, O> extends Node<S, I, O> {

    /**
     * Core row associated with this leaf (null if split, see {@link Leaf}).
     */
    @Nullable CoreRow<S, I, O> cRow;

    /**
     * Separator cached by this leaf (see {@link Leaf}).
     */
    @Nullable Separator<S, I, O> sep;

    private int lastNumCRows;
    private int lastNumSufs;

    private Leaf(int numCRows, int numSufs, List<Integer> cellIds) {
        super(cellIds);
        this.lastNumCRows = numCRows;
        this.lastNumSufs = numSufs;
    }

    /**
     * Creates split leaf without observations (see {@link Leaf}).
     */
    Leaf() {
        this(0, 0, Collections.emptyList());
        // timestamps will be updated automatically
        cRow = null;
    }

    /**
     * Creates unsplit leaf associated with the given core row and observations (see {@link Leaf}).
     */
    Leaf(CoreRow<S, I, O> cRow, int numCRows, int numSufs, List<Integer> cellIds) {
        this(numCRows, numSufs, cellIds);
        this.cRow = cRow;
        remRows.set(cRow.idx);
    }

    /**
     * See {@link Leaf}.
     */
    boolean isUnsplit() {
        return cRow != null;
    }

    void update(List<CoreRow<S, I, O>> cRows) {
        assert lastNumCRows <= cRows.size();
        if (lastNumCRows == cRows.size()) {
            return;
        }

        final int numSufs = cRows.get(0).sufToOut.size();
        if (numSufs > lastNumSufs) {
            lastNumSufs = numSufs;
            sep = null;
        }

        // since suffixes and core rows grow monotonically,
        // the separator only needs to be recomputed whenever
        // new compatible core prefixes emerge or the suffix set grows

        for (int i = lastNumCRows; i < cRows.size(); i++) {
            final CoreRow<S, I, O> c = cRows.get(i);
            if (c.cellIds.containsAll(cellIds)) {
                remRows.set(c.idx);
                cRow = null; // split leaf
                sep = null;
            }
        }

        lastNumCRows = cRows.size();
    }
}
