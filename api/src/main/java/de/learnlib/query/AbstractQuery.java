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
package de.learnlib.query;

import net.automatalib.word.Word;

public abstract class AbstractQuery<I, D> extends Query<I, D> {

    protected final Word<I> prefix;
    protected final Word<I> suffix;

    public AbstractQuery(Word<I> queryWord) {
        this(Word.epsilon(), queryWord);
    }

    public AbstractQuery(Word<I> prefix, Word<I> suffix) {
        this.prefix = prefix;
        this.suffix = suffix;
    }

    public AbstractQuery(Query<I, ?> query) {
        this(query.getPrefix(), query.getSuffix());
    }

    @Override
    public Word<I> getPrefix() {
        return prefix;
    }

    @Override
    public Word<I> getSuffix() {
        return suffix;
    }

    /**
     * Returns the string representation of this query, including a possible answer. This method should be used by
     * classes extending {@link AbstractQuery} for their toString method to ensure output consistency.
     *
     * @param answer
     *         the output object to render as answer
     *
     * @return A string of the form {@code "Query[<prefix>|<suffix> / <answer>]"}. If the query has not been answered
     * yet, {@code <answer>} will be null.
     */
    public String toStringWithAnswer(D answer) {
        return "Query[" + prefix + '|' + suffix + " / " + answer + ']';
    }
}
