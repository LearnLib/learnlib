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

/**
 * Each fringe row represents a hypothesis transition.
 *
 * @param <S>
 *         state type
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbol type
 */
class FringeRow<S, I, O> extends Row<S, I, O> {

    /**
     * Source state.
     */
    final S srcState;
    /**
     * Input symbol.
     */
    final I transIn;
    /**
     * Output symbol (determined dynamically).
     */
    O transOut;
    /**
     * For compression, fringe rows do not store observations directly. instead, they point to some leaf in a tree
     * encoding their classification history. this trick avoids redundantly storing identical observations.
     */
    Leaf<S, I, O> leaf;

    FringeRow(Word<I> prefix, S srcState, Leaf<S, I, O> leaf) {
        super(prefix);
        this.srcState = srcState;
        this.transIn = prefix.lastSymbol();
        this.leaf = leaf;
    }
}
