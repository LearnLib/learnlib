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
package de.learnlib.algorithms.procedural.sba.manager;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import de.learnlib.algorithms.procedural.SymbolWrapper;
import de.learnlib.algorithms.procedural.sba.ATManager;
import de.learnlib.api.AccessSequenceTransformer;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.commons.util.Pair;
import net.automatalib.words.ProceduralInputAlphabet;
import net.automatalib.words.Word;

public class DefaultATManager<I> implements ATManager<I> {

    private final Map<I, Word<I>> accessSequences;
    private final Map<I, Word<I>> terminatingSequences;

    private final ProceduralInputAlphabet<I> alphabet;

    public DefaultATManager(final ProceduralInputAlphabet<I> alphabet) {
        this.alphabet = alphabet;

        this.accessSequences = Maps.newHashMapWithExpectedSize(alphabet.getNumCalls());
        this.terminatingSequences = Maps.newHashMapWithExpectedSize(alphabet.getNumCalls());
    }

    @Override
    public Word<I> getAccessSequence(I procedure) {
        assert this.accessSequences.containsKey(procedure);
        return this.accessSequences.get(procedure);
    }

    @Override
    public Word<I> getTerminatingSequence(I procedure) {
        assert this.terminatingSequences.containsKey(procedure);
        return this.terminatingSequences.get(procedure);
    }

    @Override
    public Pair<Set<I>, Set<I>> scanPositiveCounterexample(Word<I> input) {
        final Set<I> newCalls = Sets.newHashSetWithExpectedSize(alphabet.getNumCalls() - accessSequences.size());
        final Set<I> newTerms = Sets.newHashSetWithExpectedSize(alphabet.getNumCalls() - terminatingSequences.size());

        for (int i = 0; i < input.size(); i++) {
            final I sym = input.getSymbol(i);

            if (this.alphabet.isCallSymbol(sym)) {

                if (!this.accessSequences.containsKey(sym)) {
                    this.accessSequences.put(sym, input.prefix(i + 1));
                    newCalls.add(sym);
                }

                final int returnIdx = this.alphabet.findReturnIndex(input, i + 1);

                if (returnIdx > 0 && !this.terminatingSequences.containsKey(sym)) {
                    this.terminatingSequences.put(sym, input.subWord(i + 1, returnIdx));
                    newTerms.add(sym);
                }
            }
        }

        return Pair.of(newCalls, newTerms);
    }

    @Override
    public Set<I> scanRefinedProcedures(Map<I, ? extends DFA<?, SymbolWrapper<I>>> procedures,
                                        Map<I, ? extends AccessSequenceTransformer<SymbolWrapper<I>>> providers,
                                        Collection<SymbolWrapper<I>> inputs) {
        return Collections.emptySet();
    }
}
