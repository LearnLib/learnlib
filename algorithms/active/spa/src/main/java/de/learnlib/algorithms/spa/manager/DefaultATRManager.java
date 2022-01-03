/* Copyright (C) 2013-2022 TU Dortmund
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
package de.learnlib.algorithms.spa.manager;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import de.learnlib.algorithms.spa.ATRManager;
import de.learnlib.api.AccessSequenceTransformer;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.words.SPAAlphabet;
import net.automatalib.words.Word;

public class DefaultATRManager<I> implements ATRManager<I> {

    private final Map<I, Word<I>> accessSequences;
    private final Map<I, Word<I>> returnSequences;
    private final Map<I, Word<I>> terminatingSequences;

    private final SPAAlphabet<I> alphabet;

    public DefaultATRManager(final SPAAlphabet<I> alphabet) {
        this.alphabet = alphabet;

        this.accessSequences = Maps.newHashMapWithExpectedSize(alphabet.getNumCalls());
        this.returnSequences = Maps.newHashMapWithExpectedSize(alphabet.getNumCalls());
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
    public Word<I> getReturnSequence(I procedure) {
        assert this.returnSequences.containsKey(procedure);
        return this.returnSequences.get(procedure);
    }

    @Override
    public Set<I> scanPositiveCounterexample(Word<I> input) {
        final Set<I> result = Sets.newHashSetWithExpectedSize(alphabet.getNumCalls() - accessSequences.size());

        for (int i = 0; i < input.size(); i++) {
            final I sym = input.getSymbol(i);

            if (this.alphabet.isCallSymbol(sym) && !this.accessSequences.containsKey(sym)) {

                final int returnIdx = this.alphabet.findReturnIndex(input, i + 1);

                this.accessSequences.put(sym, input.prefix(i + 1));
                this.terminatingSequences.put(sym, input.subWord(i + 1, returnIdx));
                this.returnSequences.put(sym, input.subWord(returnIdx));

                result.add(sym);
            }
        }

        return result;
    }

    @Override
    public void scanRefinedProcedures(Map<I, ? extends DFA<?, I>> procedures,
                                      Map<I, ? extends AccessSequenceTransformer<I>> providers,
                                      Collection<I> inputs) {
        // do nothing
    }
}
