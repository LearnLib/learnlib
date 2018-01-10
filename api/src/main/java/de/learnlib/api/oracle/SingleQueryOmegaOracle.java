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

import java.util.Collection;
import java.util.List;
import java.util.Set;

import de.learnlib.api.query.OmegaQuery;
import net.automatalib.commons.util.Pair;
import net.automatalib.words.Word;

/**
 * An {@link OmegaMembershipOracle} that answers single queries.
 *
 * @author Jeroen Meijer
 *
 * @see OmegaMembershipOracle
 * @see SingleQueryOracle
 */
public interface SingleQueryOmegaOracle<S, I, D> extends OmegaMembershipOracle<S, I, D> {

    @Override
    default void processQuery(OmegaQuery<S, I, D> query) {
        Pair<D, List<S>> output = answerQuery(query.getPrefix(), query.getSuffix(), query.getIndices());
        query.answer(output.getFirst());
        query.setStates(output.getSecond());
    }

    @Override
    default void processQueries(Collection<? extends OmegaQuery<S, I, D>> queries) {
        queries.forEach(this::processQuery);
    }

    @Override
    Pair<D, List<S>> answerQuery(Word<I> prefix, Word<I> suffix, Set<Integer> indices);

    interface SingleQueryOmegaOracleDFA<S, I> extends SingleQueryOmegaOracle<S, I, Boolean>, DFAOmegaMembershipOracle<S, I> {}

    interface SingleQueryOmegaOracleMealy<S, I, O> extends SingleQueryOmegaOracle<S, I, Word<O>>, MealyOmegaMembershipOracle<S, I, O> {}
}
