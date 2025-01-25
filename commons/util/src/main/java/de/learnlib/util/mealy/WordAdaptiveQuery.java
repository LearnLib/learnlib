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
package de.learnlib.util.mealy;

import de.learnlib.oracle.AdaptiveMembershipOracle;
import de.learnlib.query.AdaptiveQuery;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;

/**
 * Wraps a given (non-empty) {@link Word} as an {@link AdaptiveQuery} so that it can be answered by an
 * {@link AdaptiveMembershipOracle}.
 *
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbol type
 */
public class WordAdaptiveQuery<I, O> implements AdaptiveQuery<I, O> {

    private final WordBuilder<O> builder;
    private final Word<I> query;

    private int idx;

    public WordAdaptiveQuery(Word<I> query) {
        this.builder = new WordBuilder<>(query.length());
        this.query = query;
        this.idx = 0;
    }

    @Override
    public I getInput() {
        return query.getSymbol(idx);
    }

    @Override
    public Response processOutput(O out) {
        idx++;
        builder.append(out);

        return idx == query.length() ? Response.FINISHED : Response.SYMBOL;
    }

    public Word<O> getOutput() {
        return builder.toWord();
    }
}

