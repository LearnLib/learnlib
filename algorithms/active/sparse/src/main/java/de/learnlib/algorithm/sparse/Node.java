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

import java.util.BitSet;
import java.util.List;

/**
 * For table compression and to cache suffix selection,
 * fringe rows do not store observations, but instead map to some node.
 * Each node is associated with a set of suffix-output pairs,
 * potentially representing multiple rows with identical observations.
 * Nodes are either leaves or separators.
 */
class Node<S, I, O> { // type parameters required for safe casting

    /**
     * Identifiers of suffix-output pairs associated with this node.
     */
    final List<Integer> cellIds;

    /**
     * Bit vector encoding which core rows remain compatible
     * with the observations at this node.
     * Rows are represented by their index.
     */
    final BitSet remRows;

    protected Node(List<Integer> cellIds) {
        this(cellIds, new BitSet());
    }

    protected Node(List<Integer> cellIds, BitSet remRows) {
        this.cellIds = cellIds;
        this.remRows = remRows;
    }
}
