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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.automatalib.word.Word;

/**
 * Each core row represents some hypothesis state and stores its outputs for all table suffixes.
 */
class CoreRow<S, I, O> extends Row<S, I, O> {

    /**
     * Hypothesis state associated with this row.
     */
    final S state;

    /**
     * Index of this row in the core row list. Used as a unique address.
     */
    final int idx;

    /**
     * Maps suffixes to their outputs.
     */
    final Map<Word<I>, Word<O>> sufToOut;

    /**
     * Identifiers of all suffix-output pairs in this row, used for fast compatibility checking.
     */
    final Set<Integer> cellIds;

    CoreRow(Word<I> prefix, S state, int idx) {
        super(prefix);
        this.state = state;
        this.idx = idx;
        sufToOut = new HashMap<>();
        cellIds = new HashSet<>(); // use HashSet for fast containment checks
    }

    void addSuffix(Word<I> suf, Word<O> out, int cell) {
        sufToOut.put(suf, out);
        cellIds.add(cell);
    }
}
