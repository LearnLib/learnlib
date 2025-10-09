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
package de.learnlib.algorithm.sparse;

import java.util.Collections;
import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;

class Leaf<S, I, O> extends Node<S, I, O> {

    final @Nullable CoreRow<S, I, O> cRow;
    private boolean split;
    private int lastNumCRows;
    private int lastNumSufs;

    /**
     * Split leafs always remember how many core rows and suffixes the table contained
     * at their last visit. This information is used as a logical timestamp to check
     * if the separator is still guaranteed to be optimal or if it needs to be recomputed.
     */
    @Nullable Separator<S, I, O> sep;

    /**
     * Creates split leaf without observations.
     */
    Leaf() {
        super(Collections.emptyList());
        cRow = null;
        split = true;
        lastNumCRows = 0;
        lastNumSufs = 0;
        // timestamps will be updated automatically
    }

    /**
     * Creates unsplit leaf associated with the given core row and observations.
     */
    Leaf(CoreRow<S, I, O> cRow, int numCRows, int numSufs, List<Integer> cellIds) {
        super(cellIds);
        this.cRow = cRow;
        remRows.set(cRow.idx);
        split = false;
        lastNumCRows = numCRows;
        lastNumSufs = numSufs;
    }

    boolean isUnsplit() {
        return !split;
    }

    void update(List<CoreRow<S, I, O>> cRows, int numSufs) {
        assert lastNumCRows <= cRows.size();
        if (lastNumCRows == cRows.size()) {
            assert lastNumSufs == numSufs;
            return;
        } else if (numSufs > lastNumSufs) {
            lastNumSufs = numSufs;
            sep = null;
        }

        // Since suffixes and core rows grow monotonically,
        // the separator only needs to be recomputed whenever
        // new compatible core prefixes emerge or the suffix set grows.

        for (int i = lastNumCRows; i < cRows.size(); i++) {
            final CoreRow<S, I, O> c = cRows.get(i);
            if (c.cellIds.containsAll(cellsIds)) {
                remRows.set(c.idx);
                split = true;
                sep = null;
            }
        }

        lastNumCRows = cRows.size();
    }
}
