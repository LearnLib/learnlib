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
package de.learnlib.algorithm.procedural.spmm;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import de.learnlib.algorithm.procedural.SymbolWrapper;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.query.Query;
import net.automatalib.alphabet.ProceduralInputAlphabet;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;

class ProceduralMembershipOracle<I, O> implements MembershipOracle<SymbolWrapper<I>, Word<O>> {

    private final ProceduralInputAlphabet<I> alphabet;
    private final MembershipOracle<I, Word<O>> delegate;
    private final I procedure;
    private final O errorSymbol;
    private final ATManager<I, O> atManager;

    ProceduralMembershipOracle(ProceduralInputAlphabet<I> alphabet,
                               MembershipOracle<I, Word<O>> delegate,
                               I procedure,
                               O errorSymbol,
                               ATManager<I, O> atManager) {
        this.alphabet = alphabet;
        this.delegate = delegate;
        this.procedure = procedure;
        this.errorSymbol = errorSymbol;
        this.atManager = atManager;
    }

    @Override
    public void processQueries(Collection<? extends Query<SymbolWrapper<I>, Word<O>>> collection) {
        final List<Query<I, Word<O>>> transformedQueries = new ArrayList<>(collection.size());

        for (Query<SymbolWrapper<I>, Word<O>> q : collection) {
            if (hasErrorInPrefix(q.getPrefix())) {
                q.answer(Word.fromList(Collections.nCopies(q.getSuffix().length(), errorSymbol)));
            } else {
                transformedQueries.add(new TransformedQuery(q));
            }
        }

        this.delegate.processQueries(transformedQueries);
    }

    private Word<I> transformPrefix(Word<SymbolWrapper<I>> query) {
        final WordBuilder<I> builder = new WordBuilder<>();
        builder.append(atManager.getAccessSequence(this.procedure));

        for (SymbolWrapper<I> wrapper : query) {
            final I i = wrapper.getDelegate();
            if (alphabet.isInternalSymbol(i)) {
                builder.append(i);
            } else if (alphabet.isCallSymbol(i)) {
                builder.append(i);
                builder.append(atManager.getTerminatingSequence(i));
                builder.append(alphabet.getReturnSymbol());
            } else { // return symbol
                throw new IllegalStateException("Prefixes should not contain return symbol");
            }
        }

        return builder.toWord();
    }

    private Word<I> transformSuffix(Word<SymbolWrapper<I>> query, BitSet indices) {
        final WordBuilder<I> builder = new WordBuilder<>();

        for (SymbolWrapper<I> wrapper : query) {
            final I i = wrapper.getDelegate();
            indices.set(builder.size());
            if (alphabet.isInternalSymbol(i)) {
                builder.append(i);
            } else if (alphabet.isCallSymbol(i)) {
                builder.append(i);
                if (wrapper.isContinuable()) {
                    builder.append(atManager.getTerminatingSequence(i));
                    builder.append(alphabet.getReturnSymbol());
                } else {
                    return builder.toWord();
                }
            } else { // return symbol
                builder.append(i);
                return builder.toWord();
            }
        }

        return builder.toWord();
    }

    private boolean hasErrorInPrefix(Word<SymbolWrapper<I>> prefix) {

        for (SymbolWrapper<I> wrapper : prefix) {
            if (!wrapper.isContinuable()) {
                return true;
            }
        }

        return false;
    }

    private class TransformedQuery extends Query<I, Word<O>> {

        private final Query<SymbolWrapper<I>, Word<O>> originalQuery;
        private final Word<I> transformedPrefix;
        private final Word<I> transformedSuffix;
        private final BitSet outputIndices;

        TransformedQuery(Query<SymbolWrapper<I>, Word<O>> originalQuery) {
            this.originalQuery = originalQuery;
            this.outputIndices = new BitSet();

            this.transformedPrefix = transformPrefix(originalQuery.getPrefix());
            this.transformedSuffix = transformSuffix(originalQuery.getSuffix(), outputIndices);
        }

        @Override
        public void answer(Word<O> output) {
            final List<O> out = outputIndices.stream().mapToObj(output::getSymbol).collect(Collectors.toList());

            // fill up with skipped symbols
            for (int i = out.size(); i < originalQuery.getSuffix().size(); i++) {
                out.add(errorSymbol);
            }

            this.originalQuery.answer(Word.fromList(out));
        }

        @Override
        public Word<I> getPrefix() {
            return this.transformedPrefix;
        }

        @Override
        public Word<I> getSuffix() {
            return this.transformedSuffix;
        }
    }
}
