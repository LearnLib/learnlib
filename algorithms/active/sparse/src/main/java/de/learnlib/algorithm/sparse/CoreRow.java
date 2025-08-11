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

import net.automatalib.word.Word;

import java.util.*;

class CoreRow<S, I, O> extends Row<S, I, O> {
    final S state; // hypothesis state associated with this row
    final int idx; // index in core row list
    final Map<Word<I>, Word<O>> sufToOut; // maps suffixes to the outputs contained in this row
    final Set<Integer> cellIds; // also store identifiers of suffix-output pairs for fast compatability checking

    CoreRow(Word<I> prefix, S state, int idx) {
        super(prefix);
        this.state = state;
        this.idx = idx;
        sufToOut = new HashMap<>();
        cellIds = new HashSet<>(); // use HashSet to enable fast containment checks
    }

    void addSuffix(Word<I> suf, Word<O> out, int cell) {
        sufToOut.put(suf, out);
        cellIds.add(cell);
    }
}
