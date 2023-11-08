/* Copyright (C) 2013-2023 TU Dortmund
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
package de.learnlib.oracle;

import java.util.Collection;

import de.learnlib.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.query.Query;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;

/**
 * Symbol query interface. Semantically similar to {@link MealyMembershipOracle}, but allows to pose queries
 * symbol-wise.
 *
 * @param <I>
 *         input alphabet type
 * @param <O>
 *         output alphabet type
 */
public interface SymbolQueryOracle<I, O> extends MealyMembershipOracle<I, O> {

    /**
     * Query the system under learning for a new symbol. <b>This is a stateful operation.</b>
     *
     * @param i
     *         the symbol to query
     *
     * @return the observed output
     */
    O query(I i);

    /**
     * Reset the system under learning.
     */
    void reset();

    @Override
    default void processQueries(Collection<? extends Query<I, Word<O>>> queries) {

        final WordBuilder<O> wb = new WordBuilder<>();

        for (Query<I, Word<O>> q : queries) {
            reset();

            for (I i : q.getPrefix()) {
                query(i);
            }

            for (I i : q.getSuffix()) {
                wb.append(query(i));
            }

            q.answer(wb.toWord());
            wb.clear();
        }
    }
}
