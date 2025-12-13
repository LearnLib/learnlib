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
package de.learnlib.filter;

import net.automatalib.word.Word;

/**
 * A symbol filter allows one to incorporate additional external knowledge by predicting whether a given transition
 * (identified by an input symbol) is ignorable in a given state (identified by an access sequence). <i>Ignorable</i>
 * typically means that the symbol triggers a (silent) self-loop in the considered state. This information can be used
 * by, e.g., learning algorithms to avoid posing redundant queries.
 * <p>
 * Note that a symbol filter is not required to answer queries correctly. In particular, an initially ignored transition
 * may turn out to be relevant to the system behavior. As a result, learners that want to support these kinds of
 * semantics need to be able to handle the potentially resulting nondeterministic query behavior.
 *
 * @param <U>
 *         input symbol type of the prefix
 * @param <V>
 *         input symbol type of the transition label
 */
@FunctionalInterface
public interface SymbolFilter<U, V> {

    /**
     * Predicts whether the provided symbol is ignorable in the state that is addressed by the given prefix.
     *
     * @param prefix
     *         the prefix identifying the state
     * @param symbol
     *         the input symbol identifying the transition
     *
     * @return {@link FilterResponse#IGNORE} if the symbol is considered ignorable, {@link FilterResponse#ACCEPT}
     * otherwise
     */
    FilterResponse query(Word<U> prefix, V symbol);
}
