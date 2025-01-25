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
package de.learnlib.algorithm.procedural.sba;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import de.learnlib.algorithm.procedural.SymbolWrapper;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.query.Query;
import net.automatalib.alphabet.ProceduralInputAlphabet;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;

class ProceduralMembershipOracle<I> implements MembershipOracle<SymbolWrapper<I>, Boolean> {

    private final ProceduralInputAlphabet<I> alphabet;
    private final MembershipOracle<I, Boolean> delegate;
    private final I procedure;
    private final ATManager<I> atManager;

    ProceduralMembershipOracle(ProceduralInputAlphabet<I> alphabet,
                               MembershipOracle<I, Boolean> delegate,
                               I procedure,
                               ATManager<I> atManager) {
        this.alphabet = alphabet;
        this.delegate = delegate;
        this.procedure = procedure;
        this.atManager = atManager;
    }

    @Override
    public void processQueries(Collection<? extends Query<SymbolWrapper<I>, Boolean>> collection) {
        final List<Query<I, Boolean>> transformedQueries = new ArrayList<>(collection.size());

        for (Query<SymbolWrapper<I>, Boolean> q : collection) {
            if (isWellDefined(q)) {
                transformedQueries.add(new TransformedQuery(q));
            } else {
                q.answer(false);
            }
        }

        this.delegate.processQueries(transformedQueries);
    }

    private boolean isWellDefined(Query<SymbolWrapper<I>, Boolean> q) {
        final Iterator<SymbolWrapper<I>> iter = q.getInput().iterator();

        while (iter.hasNext()) {
            final SymbolWrapper<I> wrapper = iter.next();
            if (!wrapper.isContinuable()) {
                return !iter.hasNext();
            }
        }

        return true;
    }

    private Word<I> transformLocalQuery(Word<SymbolWrapper<I>> query) {
        final WordBuilder<I> builder = new WordBuilder<>();
        builder.append(atManager.getAccessSequence(this.procedure));

        final Iterator<SymbolWrapper<I>> iter = query.iterator();
        while (iter.hasNext()) {
            final SymbolWrapper<I> w = iter.next();
            final I i = w.getDelegate();
            builder.append(i);
            if (alphabet.isCallSymbol(i) && iter.hasNext()) {
                assert w.isContinuable();
                builder.append(atManager.getTerminatingSequence(i));
                builder.append(alphabet.getReturnSymbol());
            }
        }

        return builder.toWord();
    }

    private class TransformedQuery extends Query<I, Boolean> {

        private final Query<SymbolWrapper<I>, Boolean> originalQuery;
        private final Word<I> transformedQuery;

        TransformedQuery(Query<SymbolWrapper<I>, Boolean> originalQuery) {
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
