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

import java.util.HashMap;
import java.util.Map;

import de.learnlib.filter.FilterResponse;
import de.learnlib.filter.RefutableSymbolFilter;
import de.learnlib.filter.SymbolFilter;
import net.automatalib.word.Word;

/**
 * Wrapper for a symbol filter that caches previous responses and allows caller to update these.
 *
 * @param <U>
 *         input symbol type of the prefix
 * @param <V>
 *         input symbol type of the transition label
 */
public class CachedSymbolFilter<U, V> implements RefutableSymbolFilter<U, V> {

    private final Map<Word<U>, Map<V, Boolean>> previousResponses; // prefix -> (input -> legal/ignore)
    private final SymbolFilter<U, V> delegate;

    public CachedSymbolFilter(SymbolFilter<U, V> delegate) {
        this.delegate = delegate;
        this.previousResponses = new HashMap<>();
    }

    @Override
    public FilterResponse query(Word<U> prefix, V symbol) {
        Boolean oldResponse = this.previousResponses.computeIfAbsent(prefix, k -> new HashMap<>()).get(symbol);
        if (oldResponse != null) {
            return oldResponse ? FilterResponse.ACCEPT : FilterResponse.IGNORE;
        }

        FilterResponse res = delegate.query(prefix, symbol);
        this.update(prefix, symbol, res);
        return res;
    }

    @Override
    public void accept(Word<U> prefix, V symbol) {
        this.update(prefix, symbol, FilterResponse.ACCEPT);
    }

    private void update(Word<U> prefix, V symbol, FilterResponse response) {
        this.previousResponses.computeIfAbsent(prefix, k -> new HashMap<>())
                              .put(symbol, response == FilterResponse.ACCEPT);
    }
}
