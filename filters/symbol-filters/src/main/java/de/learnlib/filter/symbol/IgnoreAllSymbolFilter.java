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
package de.learnlib.filter.symbol;

import de.learnlib.filter.FilterResponse;
import de.learnlib.filter.SymbolFilter;
import net.automatalib.word.Word;

/**
 * A pass-through filter that ignores all inputs.
 *
 * @param <U>
 *         input symbol type of the prefix
 * @param <V>
 *         input symbol type of the transition label
 */
public class IgnoreAllSymbolFilter<U, V> implements SymbolFilter<U, V> {

    @Override
    public FilterResponse query(Word<U> prefix, V symbol) {
        return FilterResponse.IGNORE;
    }
}
