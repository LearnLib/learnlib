/* Copyright (C) 2013-2018 TU Dortmund
 * This file is part of LearnLib, http://www.learnlib.de/.
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
package de.learnlib.api.oracle;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.automatalib.words.Word;

/**
 * @param <I>
 *         input symbol type
 * @param <D>
 *         output domain type
 *
 * @author Malte Isberner
 */
@ParametersAreNonnullByDefault
public interface QueryAnswerer<I, D> {

    @Nullable
    default D answerQuery(Word<I> input) {
        return answerQuery(Word.epsilon(), input);
    }

    @Nullable
    D answerQuery(Word<I> prefix, Word<I> suffix);

    @Nonnull
    default MembershipOracle<I, D> asOracle() {
        return queries -> queries.forEach(q -> q.answer(answerQuery(q.getPrefix(), q.getSuffix())));
    }
}
