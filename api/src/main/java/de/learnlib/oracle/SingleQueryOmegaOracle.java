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

import de.learnlib.query.OmegaQuery;
import net.automatalib.common.util.Pair;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * An {@link OmegaMembershipOracle} that answers single queries.
 *
 * @see OmegaMembershipOracle
 * @see SingleQueryOracle
 */
public interface SingleQueryOmegaOracle<S, I, D> extends OmegaMembershipOracle<S, I, D> {

    @Override
    default void processQuery(OmegaQuery<I, D> query) {
        Pair<@Nullable D, Integer> output = answerQuery(query.getPrefix(), query.getLoop(), query.getRepeat());
        query.answer(output.getFirst(), output.getSecond());
    }

    @Override
    default void processQueries(Collection<? extends OmegaQuery<I, D>> queries) {
        queries.forEach(this::processQuery);
    }

    interface SingleQueryOmegaOracleDFA<S, I> extends SingleQueryOmegaOracle<S, I, Boolean>, DFAOmegaMembershipOracle<S, I> {}

    interface SingleQueryOmegaOracleMealy<S, I, O> extends SingleQueryOmegaOracle<S, I, Word<O>>, MealyOmegaMembershipOracle<S, I, O> {}
}
