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
import de.learnlib.query.Query;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;

/**
 * Wraps a given (non-empty) {@link Query} as an {@link AdaptiveQuery} so that it can be answered by an
 * {@link AdaptiveMembershipOracle}.
 *
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbol type
 */
public class PresetAdaptiveQuery<I, O> implements AdaptiveQuery<I, O> {

    private final WordBuilder<O> builder;
    private final Query<I, Word<O>> query;

    private final Word<I> prefix;
    private final Word<I> suffix;

    private int prefixIdx;
    private int suffixIdx;

    public PresetAdaptiveQuery(Query<I, Word<O>> query) {
        this.builder = new WordBuilder<>();
        this.query = query;
        this.prefix = query.getPrefix();
        this.suffix = query.getSuffix();
        this.prefixIdx = 0;
        this.suffixIdx = 0;
    }

    @Override
    public I getInput() {
        if (prefixIdx < prefix.size()) {
            return prefix.getSymbol(prefixIdx);
        } else {
            return suffix.getSymbol(suffixIdx);
        }
    }

    @Override
    public Response processOutput(O out) {
        if (prefixIdx < prefix.size()) {
            prefixIdx++;
        } else {
            suffixIdx++;
            builder.add(out);

            if (suffixIdx >= suffix.size()) {
                query.answer(builder.toWord());
                return Response.FINISHED;
            }
        }

        return Response.SYMBOL;
    }
}

