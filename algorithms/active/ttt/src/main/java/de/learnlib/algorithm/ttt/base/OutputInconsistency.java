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

import net.automatalib.word.Word;

/**
 * Class for representing output inconsistencies within the TTT algorithm.
 *
 * @param <I>
 *         input symbol type
 * @param <D>
 *         output domain type
 */
public class OutputInconsistency<I, D> {

    public final TTTState<I, D> srcState;
    public final Word<I> suffix;
    public final D targetOut;

    public OutputInconsistency(TTTState<I, D> srcState, Word<I> suffix, D targetOut) {
        this.srcState = srcState;
        this.suffix = suffix;
        this.targetOut = targetOut;
    }

}
