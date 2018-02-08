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
package de.learnlib.util.mealy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.Query;
import net.automatalib.words.Word;

/**
 * Word-to-Symbol-Oracle adapter.
 * <p>
 * Wraps an oracle which uses {@link Word}s as its output to an oracle which only yields the last symbol of each
 * output.
 *
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbol type
 *
 * @author Malte Isberner
 */
@ParametersAreNonnullByDefault
final class SymbolOracleWrapper<I, O> implements MembershipOracle<I, O> {

    private final MembershipOracle<I, Word<O>> wordOracle;

    /**
     * Constructor.
     *
     * @param wordOracle
     *         the {@link MembershipOracle} returning output words.
     */
    SymbolOracleWrapper(MembershipOracle<I, Word<O>> wordOracle) {
        this.wordOracle = wordOracle;
    }

    @Override
    public void processQueries(Collection<? extends Query<I, O>> queries) {
        List<LastSymbolQuery<I, O>> lsQueries = new ArrayList<>(queries.size());
        for (Query<I, O> qry : queries) {
            lsQueries.add(new LastSymbolQuery<>(qry));
        }

        wordOracle.processQueries(lsQueries);
    }

    @ParametersAreNonnullByDefault
    private static final class LastSymbolQuery<I, O> extends Query<I, Word<O>> {

        private final Query<I, O> originalQuery;

        LastSymbolQuery(Query<I, O> originalQuery) {
            this.originalQuery = originalQuery;
        }

        @Override
        public void answer(Word<O> output) {
            if (output == null) {
                throw new IllegalArgumentException("Query answer words must not be null");
            }
            originalQuery.answer(output.isEmpty() ? null : output.lastSymbol());
        }

        @Override
        @Nonnull
        public Word<I> getPrefix() {
            return originalQuery.getPrefix();
        }

        @Override
        @Nonnull
        public Word<I> getSuffix() {
            return originalQuery.getSuffix();
        }

        @Override
        public String toString() {
            return originalQuery.toString();
        }

    }

}
