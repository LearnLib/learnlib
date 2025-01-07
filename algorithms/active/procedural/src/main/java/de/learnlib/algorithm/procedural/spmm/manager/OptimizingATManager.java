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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import net.automatalib.util.automaton.cover.Covers;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * An optimizing {@link ATManager} that continuously scans positive counterexamples and procedural models in order to
 * find shorter access sequences and terminating sequences.
 *
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbol type
 */
public class OptimizingATManager<I, O> implements ATManager<I, O> {

    private final Map<I, Word<I>> accessSequences;
    private final Map<I, Word<I>> terminatingSequences;

    private final ProceduralInputAlphabet<I> inputAlphabet;
    private final O errorOutput;

    public OptimizingATManager(ProceduralInputAlphabet<I> inputAlphabet, O errorOutput) {
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
        final Set<I> newCalls =
                new HashSet<>(HashUtil.capacity(this.inputAlphabet.getNumCalls() - this.accessSequences.size()));
        final Set<I> newTerms =
                new HashSet<>(HashUtil.capacity(this.inputAlphabet.getNumCalls() - this.terminatingSequences.size()));

        this.extractPotentialTerminatingSequences(counterexample, newTerms);
        this.extractPotentialAccessSequences(counterexample, newCalls);

        return Pair.of(newCalls, newTerms);
    }

    @Override
    public Set<I> scanProcedures(Map<I, ? extends MealyMachine<?, SymbolWrapper<I>, ?, O>> procedures,
                                 Map<I, ? extends AccessSequenceTransformer<SymbolWrapper<I>>> providers,
                                 Collection<SymbolWrapper<I>> inputs) {

        final Set<I> newTS = new HashSet<>(HashUtil.capacity(procedures.size()));
        if (!procedures.isEmpty()) {

            final SymbolWrapper<I> returnSymbol = inputs.stream()
                                                        .filter(i -> Objects.equals(i.getDelegate(),
                                                                                    inputAlphabet.getReturnSymbol()))
                                                        .findAny()
                                                        .orElseThrow(IllegalArgumentException::new);
            boolean foundImprovements = false;
            boolean stable = false;

            while (!stable) {
                stable = true;
                for (Entry<I, ? extends MealyMachine<?, SymbolWrapper<I>, ?, O>> entry : procedures.entrySet()) {
                    final I i = entry.getKey();
                    final MealyMachine<?, SymbolWrapper<I>, ?, O> automaton = entry.getValue();
                    final Word<I> currentTS = terminatingSequences.get(i);
                    assert providers.containsKey(i);
                    final Word<I> hypTS = getShortestHypothesisTS(automaton, providers.get(i), inputs, returnSymbol);

                    if (hypTS != null && (currentTS == null || hypTS.size() < currentTS.size())) {

                        if (currentTS == null) {
                            newTS.add(i);
                        }

                        terminatingSequences.put(i, hypTS);
                        stable = false;
                        foundImprovements = true;
                    }
                }
            }

            if (foundImprovements) {
                optimizeSequences(this.accessSequences);
                optimizeSequences(this.terminatingSequences);
            }
        }

        return newTS;
    }

    private <S> @Nullable Word<I> getShortestHypothesisTS(MealyMachine<S, SymbolWrapper<I>, ?, O> hyp,
                                                          AccessSequenceTransformer<SymbolWrapper<I>> asTransformer,
                                                          Collection<SymbolWrapper<I>> inputs,
                                                          SymbolWrapper<I> returnSymbol) {
        final Iterator<Word<SymbolWrapper<I>>> iter = Covers.stateCoverIterator(hyp, inputs);
        Word<I> result = null;

        while (iter.hasNext()) {
            final Word<SymbolWrapper<I>> cover = iter.next();
            final Word<SymbolWrapper<I>> as = asTransformer.transformAccessSequence(cover);
            final Word<SymbolWrapper<I>> asReturn = as.append(returnSymbol);

            if (!Objects.equals(this.errorOutput, hyp.computeOutput(asReturn).lastSymbol())) {
                final Word<I> ts =
                        this.inputAlphabet.expand(as.transform(SymbolWrapper::getDelegate), terminatingSequences::get);
                if (result == null || result.size() > ts.size()) {
                    result = ts;
                }
            }
        }

        return result;
    }

    private void optimizeSequences(Map<I, Word<I>> sequences) {
        for (Entry<I, Word<I>> entry : sequences.entrySet()) {
            final Word<I> currentSequence = entry.getValue();
            final Word<I> minimized = minifyWellMatched(currentSequence);

            if (minimized.size() < currentSequence.size()) {
                sequences.put(entry.getKey(), minimized);
            }
        }
    }

    private void extractPotentialTerminatingSequences(DefaultQuery<I, Word<O>> counterexample, Set<I> newProcedures) {

        final Word<I> input = counterexample.getInput();
        final Word<O> output = counterexample.getOutput();

        for (int i = 0; i < input.size(); i++) {
            final I sym = input.getSymbol(i);

            if (this.inputAlphabet.isCallSymbol(sym)) {

                final int returnIdx = inputAlphabet.findReturnIndex(input, i + 1);

                if (returnIdx > 0 && !Objects.equals(this.errorOutput, output.getSymbol(returnIdx))) {
                    final Word<I> potentialTermSeq = input.subWord(i + 1, returnIdx);
                    final Word<I> currentTermSeq = this.terminatingSequences.get(sym);

                    if (currentTermSeq == null) {
                        newProcedures.add(sym);
                        this.terminatingSequences.put(sym, potentialTermSeq);
                    } else if (potentialTermSeq.size() < currentTermSeq.size()) {
                        this.terminatingSequences.put(sym, potentialTermSeq);
                    }
                }
            }
        }
    }

    private void extractPotentialAccessSequences(DefaultQuery<I, Word<O>> counterexample, Set<I> newCalls) {

        final Word<I> input = counterexample.getInput();
        final Word<O> output = counterexample.getOutput();
        final List<I> asBuilder = new ArrayList<>(input.size());

        for (int i = 0; i < input.size(); i++) {

            final I sym = input.getSymbol(i);

            asBuilder.add(sym);

            if (this.inputAlphabet.isCallSymbol(sym)) {

                if (Objects.equals(this.errorOutput, output.getSymbol(i))) {
                    return;
                }

                final Word<I> currentAccSeq = this.accessSequences.get(sym);

                if (currentAccSeq == null) {
                    newCalls.add(sym);
                    this.accessSequences.put(sym, Word.fromList(asBuilder));
                } else if (asBuilder.size() < currentAccSeq.size()) {
                    this.accessSequences.put(sym, Word.fromList(asBuilder));
                }
            } else if (this.inputAlphabet.isReturnSymbol(sym)) {
                // update asBuilder
                final int callIdx = inputAlphabet.findCallIndex(asBuilder, asBuilder.size() - 1);
                final I procedure = asBuilder.get(callIdx);
                final Word<I> ts = terminatingSequences.get(procedure);

                assert ts != null;

                asBuilder.subList(callIdx + 1, asBuilder.size()).clear();
                asBuilder.addAll(ts.asList());
                asBuilder.add(inputAlphabet.getReturnSymbol());
            }
        }
    }

    @SuppressWarnings("PMD.AvoidReassigningLoopVariables") // we want to skip ahead here
    private Word<I> minifyWellMatched(Word<I> input) {

        if (input.isEmpty()) {
            return Word.epsilon();
        }

        final WordBuilder<I> wb = new WordBuilder<>(input.size());

        for (int i = 0; i < input.size(); i++) {

            final I sym = input.getSymbol(i);

            wb.append(sym);

            if (this.inputAlphabet.isCallSymbol(sym)) {
                final int returnIdx = inputAlphabet.findReturnIndex(input, i + 1);

                if (returnIdx > -1) {
                    wb.append(terminatingSequences.get(sym));
                    wb.append(inputAlphabet.getReturnSymbol());
                    i = returnIdx; // next loop iteration starts _after_ the return symbol
                }
            }
        }

        return wb.toWord();
    }

}
