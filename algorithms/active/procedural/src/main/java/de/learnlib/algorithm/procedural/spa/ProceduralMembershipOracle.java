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
package de.learnlib.algorithm.procedural.spa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.learnlib.oracle.MembershipOracle;
import de.learnlib.query.Query;
import net.automatalib.alphabet.ProceduralInputAlphabet;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;

class ProceduralMembershipOracle<I> implements MembershipOracle<I, Boolean> {

    private final ProceduralInputAlphabet<I> alphabet;
    private final MembershipOracle<I, Boolean> delegate;
    private final I procedure;
    private final ATRManager<I> atrManager;

    ProceduralMembershipOracle(ProceduralInputAlphabet<I> alphabet,
                               MembershipOracle<I, Boolean> delegate,
                               I procedure,
                               ATRManager<I> atrManager) {
        this.alphabet = alphabet;
        this.delegate = delegate;
        this.procedure = procedure;
        this.atrManager = atrManager;
    }

    @Override
    public void processQuery(Query<I, Boolean> query) {
        this.delegate.processQuery(new TransformedQuery(query));
    }

    @Override
    public void processQueries(Collection<? extends Query<I, Boolean>> collection) {
        final List<Query<I, Boolean>> transformedQueries = new ArrayList<>(collection.size());

        for (Query<I, Boolean> q : collection) {
            transformedQueries.add(new TransformedQuery(q));
        }

        this.delegate.processQueries(transformedQueries);
    }

    private Word<I> transformLocalQuery(Word<I> query) {
        final WordBuilder<I> builder = new WordBuilder<>();
        builder.append(atrManager.getAccessSequence(this.procedure));

        for (I i : query) {
            if (alphabet.isInternalSymbol(i)) {
                builder.append(i);
            } else if (alphabet.isCallSymbol(i)) {
                builder.append(i);
                builder.append(atrManager.getTerminatingSequence(i));
                builder.append(alphabet.getReturnSymbol());
            } else { // return symbol
                throw new IllegalStateException("Systems should not query return symbol");
            }
        }

        builder.append(atrManager.getReturnSequence(this.procedure));

        return builder.toWord();
    }

    private class TransformedQuery extends Query<I, Boolean> {

        private final Query<I, Boolean> originalQuery;
        private final Word<I> transformedQuery;

        TransformedQuery(Query<I, Boolean> originalQuery) {
            this.originalQuery = originalQuery;
            this.transformedQuery = transformLocalQuery(originalQuery.getInput());
        }

        @Override
        public void answer(Boolean output) {
            originalQuery.answer(output);
        }

        @Override
        public Word<I> getPrefix() {
            return Word.epsilon();
        }

        @Override
        public Word<I> getSuffix() {
            return this.transformedQuery;
        }

        @Override
        public Word<I> getInput() {
            return this.transformedQuery;
        }
    }
}
