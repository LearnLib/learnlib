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
package de.learnlib.algorithm.lambda.lstar;

import java.util.List;
import java.util.Map;
import java.util.Set;

import de.learnlib.Resumable;
import net.automatalib.word.Word;

/**
 * State class for making {@link LLambdaDFA} and {@link LLambdaMealy} {@link Resumable resumable}.
 *
 * @param <I>
 *         input symbol type
 * @param <D>
 *         output domain type
 */
public class LLambdaState<I, D> {

    private final Set<Word<I>> shortPrefixes;
    private final Map<Word<I>, List<D>> rows;
    private final List<Word<I>> suffixes;

    LLambdaState(Set<Word<I>> shortPrefixes, Map<Word<I>, List<D>> rows, List<Word<I>> suffixes) {
        this.shortPrefixes = shortPrefixes;
        this.rows = rows;
        this.suffixes = suffixes;
    }

    Set<Word<I>> getShortPrefixes() {
        return shortPrefixes;
    }

    Map<Word<I>, List<D>> getRows() {
        return rows;
    }

    List<Word<I>> getSuffixes() {
        return suffixes;
    }
}
