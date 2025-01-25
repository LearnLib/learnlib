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
package de.learnlib.oracle;

import java.util.Collection;

import de.learnlib.query.Query;
import net.automatalib.word.Word;

/**
 * Base interface for oracles whose semantic is defined in terms of directly answering single queries (like a {@link
 * QueryAnswerer}), and that cannot profit from batch processing of queries.
 * <p>
 * Implementing this class instead of directly implementing {@link MembershipOracle} means that the {@link
 * #answerQuery(Word, Word)} instead of the {@link #processQueries(Collection)} method needs to be implemented.
 *
 * @param <I>
 *         input symbol type
 * @param <D>
 *         output domain type
 */
public interface SingleQueryOracle<I, D> extends MembershipOracle<I, D> {

    @Override
    default void processQuery(Query<I, D> query) {
        D output = answerQuery(query.getPrefix(), query.getSuffix());
        query.answer(output);
    }

    @Override
    default void processQueries(Collection<? extends Query<I, D>> queries) {
        queries.forEach(this::processQuery);
    }

    @Override
    D answerQuery(Word<I> prefix, Word<I> suffix);

    interface SingleQueryOracleDFA<I> extends SingleQueryOracle<I, Boolean>, DFAMembershipOracle<I> {}

    interface SingleQueryOracleMealy<I, O> extends SingleQueryOracle<I, Word<O>>, MealyMembershipOracle<I, O> {}

    interface SingleQueryOracleMoore<I, O> extends SingleQueryOracle<I, Word<O>>, MooreMembershipOracle<I, O> {}

}
