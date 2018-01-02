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
import java.util.Collections;

import javax.annotation.ParametersAreNonnullByDefault;

import de.learnlib.api.query.DefaultQuery;
import de.learnlib.api.query.Query;
import net.automatalib.words.Word;

/**
 * Membership oracle interface. A membership oracle provides an elementary abstraction to a System Under Learning (SUL),
 * by allowing to pose {@link Query queries}: A query is a sequence of input symbols (divided into a prefix and a suffix
 * part, cf. {@link Query#getPrefix()} and {@link Query#getSuffix()}, in reaction to which the SUL produces a specific
 * observable behavior (outputting a word, acceptance/rejection etc.).
 *
 * @author Malte Isberner
 * @author Maik Merten
 * @see DefaultQuery
 */
@ParametersAreNonnullByDefault
public interface MembershipOracle<I, D> extends QueryAnswerer<I, D> {

    @Override
    default D answerQuery(Word<I> input) {
        return answerQuery(Word.epsilon(), input);
    }

    @Override
    default D answerQuery(Word<I> prefix, Word<I> suffix) {
        DefaultQuery<I, D> query = new DefaultQuery<>(prefix, suffix);
        processQuery(query);
        return query.getOutput();
    }

    /**
     * Processes a single query. When this method returns, the {@link Query#answer(Object)} method of the supplied
     * object will have been called with an argument reflecting the SUL response to the respective query.
     * <p>
     * The default implementation of this method will simply wrap the provided {@link Query} in a singleton {@link
     * Collection} using {@link Collections#singleton(Object)}. Implementations in subclasses should override this
     * method to circumvent the Collection object creation, if possible.
     *
     * @param query
     *         the query to process
     */
    default void processQuery(Query<I, D> query) {
        processQueries(Collections.singleton(query));
    }

    /**
     * Processes the specified collection of queries. When this method returns, each of the contained queries {@link
     * Query#answer(Object)} method should have been called with an argument reflecting the SUL response to the
     * respective query.
     *
     * @param queries
     *         the queries to process
     *
     * @see Query#answer(Object)
     */
    void processQueries(Collection<? extends Query<I, D>> queries);

    @Override
    default MembershipOracle<I, D> asOracle() {
        return this;
    }

    interface DFAMembershipOracle<I> extends MembershipOracle<I, Boolean> {}

    interface MealyMembershipOracle<I, O> extends MembershipOracle<I, Word<O>> {}
}
