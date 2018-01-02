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
package de.learnlib.filter.reuse;

import net.automatalib.words.Word;

/**
 * Required interface for the {@link ReuseOracle}. An implementation needs to provide the ability to answer queries with
 * respect to a system state class S and an input and must be able to reset to SUL to an initial state.
 * <p>
 * The {@link ReuseOracle} decides whether a full membership query needs to be answered including a reset (via {@link
 * #processQuery(Word)}) or if there is a system state available that is able to save the reset with some prefix (via
 * {@link #continueQuery(Word, Object)}).
 *
 * @param <S>
 *         system state class
 * @param <I>
 *         input symbol class
 * @param <O>
 *         output symbol class
 *
 * @author Oliver Bauer
 */
public interface ReuseCapableOracle<S, I, O> {

    /**
     * This method will be invoked whenever a system state s was found for reusage when a new membership query is
     * processed. Please note that only a saved reset can be ensured.
     *
     * @param trace
     *         The query to consider (mostly a real suffix from a membership query).
     * @param s
     *         A system state that corresponds to an already answered prefix.
     *
     * @return A query result consisting of the output to the input and the resulting system state.
     */
    QueryResult<S, O> continueQuery(Word<I> trace, S s);

    /**
     * An implementation needs to provide a fresh system state, process the whole query and return a {@link QueryResult}
     * with the resulting system state ({@link QueryResult#newState}) and the SUL output to that query ({@link
     * QueryResult#output}).
     * <p>
     * This method will be invoked if no available system state was found and can be seen as a 'normal membership
     * query'.
     *
     * @param trace
     *         The query to consider.
     *
     * @return A query result consisting of the output to the input and the resulting system state.
     */
    QueryResult<S, O> processQuery(Word<I> trace);

    final class QueryResult<S, O> {

        public final Word<O> output;
        public final S newState;

        public QueryResult(Word<O> output, S newState) {
            super();
            this.output = output;
            this.newState = newState;
        }
    }
}
