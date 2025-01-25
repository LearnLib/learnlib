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
package de.learnlib.algorithm.procedural.spmm.manager;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import de.learnlib.AccessSequenceTransformer;
import de.learnlib.algorithm.procedural.SymbolWrapper;
import de.learnlib.algorithm.procedural.spmm.ATManager;
import de.learnlib.query.DefaultQuery;
import net.automatalib.alphabet.ProceduralInputAlphabet;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.common.util.HashUtil;
import net.automatalib.common.util.Pair;
import net.automatalib.word.Word;

/**
 * A default {@link ATManager} that only extracts initial access sequences and terminating sequences from positive
 * counterexamples.
 *
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbol type
 */
public class DefaultATManager<I, O> implements ATManager<I, O> {

    private final Map<I, Word<I>> accessSequences;
    private final Map<I, Word<I>> terminatingSequences;

    private final ProceduralInputAlphabet<I> inputAlphabet;
    private final O errorOutput;

    public DefaultATManager(ProceduralInputAlphabet<I> inputAlphabet, O errorOutput) {
        this.inputAlphabet = inputAlphabet;
        this.errorOutput = errorOutput;

        this.accessSequences = new HashMap<>(HashUtil.capacity(inputAlphabet.getNumCalls()));
        this.terminatingSequences = new HashMap<>(HashUtil.capacity(inputAlphabet.getNumCalls()));
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
    public Pair<Set<I>, Set<I>> scanCounterexample(DefaultQuery<I, Word<O>> counterexample) {
        final Set<I> newCalls = new HashSet<>(HashUtil.capacity(inputAlphabet.getNumCalls() - accessSequences.size()));
        final Set<I> newTerms =
                new HashSet<>(HashUtil.capacity(inputAlphabet.getNumCalls() - terminatingSequences.size()));

        final Word<I> input = counterexample.getInput();
        final Word<O> output = counterexample.getOutput();

        for (int i = 0; i < input.size(); i++) {
            final I sym = input.getSymbol(i);

            if (this.inputAlphabet.isCallSymbol(sym)) {

                if (!this.accessSequences.containsKey(sym)) {
                    this.accessSequences.put(sym, input.prefix(i + 1));
                    newCalls.add(sym);
                }

                final int returnIdx = inputAlphabet.findReturnIndex(input, i + 1);

                if (returnIdx > 0 && !this.terminatingSequences.containsKey(sym) &&
                    !Objects.equals(this.errorOutput, output.getSymbol(returnIdx))) {
                    this.terminatingSequences.put(sym, input.subWord(i + 1, returnIdx));
                    newTerms.add(sym);
                }
            }
        }

        return Pair.of(newCalls, newTerms);
    }

    @Override
    public Set<I> scanProcedures(Map<I, ? extends MealyMachine<?, SymbolWrapper<I>, ?, O>> procedures,
                                 Map<I, ? extends AccessSequenceTransformer<SymbolWrapper<I>>> providers,
                                 Collection<SymbolWrapper<I>> inputs) {
        return Collections.emptySet();
    }
}
