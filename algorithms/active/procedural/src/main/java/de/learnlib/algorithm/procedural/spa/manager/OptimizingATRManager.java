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
package de.learnlib.algorithm.procedural.spa.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.learnlib.AccessSequenceTransformer;
import de.learnlib.algorithm.procedural.spa.ATRManager;
import net.automatalib.alphabet.ProceduralInputAlphabet;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.common.util.HashUtil;
import net.automatalib.common.util.collection.IteratorUtil;
import net.automatalib.util.automaton.cover.Covers;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * An optimizing {@link ATRManager} that continuously scans positive counterexamples and procedural models in order to
 * find shorter access sequences, terminating sequences, and return sequences.
 *
 * @param <I>
 *         input symbol type
 */
public class OptimizingATRManager<I> implements ATRManager<I> {

    private final Map<I, Word<I>> accessSequences;
    private final Map<I, Word<I>> returnSequences;
    private final Map<I, Word<I>> terminatingSequences;

    private final ProceduralInputAlphabet<I> alphabet;

    public OptimizingATRManager(ProceduralInputAlphabet<I> alphabet) {
        this.alphabet = alphabet;

        this.accessSequences = new HashMap<>(HashUtil.capacity(alphabet.getNumCalls()));
        this.returnSequences = new HashMap<>(HashUtil.capacity(alphabet.getNumCalls()));
        this.terminatingSequences = new HashMap<>(HashUtil.capacity(alphabet.getNumCalls()));
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
        final Set<I> newProcedures =
                new HashSet<>(HashUtil.capacity(this.alphabet.getNumCalls() - this.terminatingSequences.size()));

        this.extractPotentialTerminatingSequences(input, newProcedures);
        this.extractPotentialAccessAndReturnSequences(input);

        return newProcedures;
    }

    @Override
    public void scanProcedures(Map<I, ? extends DFA<?, I>> procedures,
                               Map<I, ? extends AccessSequenceTransformer<I>> providers,
                               Collection<I> inputs) {
        if (!procedures.isEmpty()) {

            boolean foundImprovements = false;
            boolean stable = false;

            while (!stable) {
                stable = true;
                for (Map.Entry<I, ? extends DFA<?, I>> entry : procedures.entrySet()) {
                    final I i = entry.getKey();
                    final DFA<?, I> automaton = entry.getValue();
                    final Word<I> currentTS = getTerminatingSequence(i);
                    assert providers.containsKey(i);
                    final Word<I> hypTS = getShortestHypothesisTS(automaton, providers.get(i), inputs);

                    if (hypTS != null && hypTS.size() < currentTS.size()) {
                        terminatingSequences.put(i, hypTS);
                        stable = false;
                        foundImprovements = true;
                    }
                }
            }

            if (foundImprovements) {
                optimizeSequences(this.accessSequences);
                optimizeSequences(this.terminatingSequences);
                optimizeSequences(this.returnSequences);
            }
        }
    }

    private <S> @Nullable Word<I> getShortestHypothesisTS(DFA<S, I> hyp,
                                                          AccessSequenceTransformer<I> asTransformer,
                                                          Collection<I> inputs) {
        return IteratorUtil.stream(Covers.stateCoverIterator(hyp, inputs))
                           .filter(hyp::accepts)
                           .map(asTransformer::transformAccessSequence)
                           .map(as -> this.alphabet.expand(as, terminatingSequences::get))
                           .min(Comparator.comparingInt(Word::size))
                           .orElse(null);
    }

    private void optimizeSequences(Map<I, Word<I>> sequences) {
        for (Map.Entry<I, Word<I>> entry : sequences.entrySet()) {
            final Word<I> currentSequence = entry.getValue();
            final Word<I> minimized = minifyWellMatched(currentSequence);

            if (minimized.size() < currentSequence.size()) {
                sequences.put(entry.getKey(), minimized);
            }
        }
    }

    private void extractPotentialTerminatingSequences(Word<I> input, Set<I> newProcedures) {
        for (int i = 0; i < input.size(); i++) {
            final I sym = input.getSymbol(i);

            if (this.alphabet.isCallSymbol(sym)) {

                final int returnIdx = this.alphabet.findReturnIndex(input, i + 1);
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

    private void extractPotentialAccessAndReturnSequences(Word<I> input) {

        final List<I> asBuilder = new ArrayList<>(input.size());
        final List<I> rsBuilder = new ArrayList<>(input.size());
        rsBuilder.addAll(minifyWellMatched(input).asList());

        for (int i = 0; i < input.size(); i++) {

            final I sym = input.getSymbol(i);
            asBuilder.add(sym);

            if (this.alphabet.isCallSymbol(sym)) {

                final int returnIdx = this.alphabet.findReturnIndex(rsBuilder, 1);
                final List<I> potentialFinSeq = rsBuilder.subList(returnIdx, rsBuilder.size());

                final Word<I> currentAccSeq = this.accessSequences.get(sym);
                final Word<I> currentFinSeq = this.returnSequences.get(sym);

                if (currentAccSeq == null || currentFinSeq == null ||
                    (asBuilder.size() + potentialFinSeq.size()) < (currentAccSeq.size() + currentFinSeq.size())) {

                    this.accessSequences.put(sym, Word.fromList(asBuilder));
                    this.returnSequences.put(sym, Word.fromList(potentialFinSeq));
                }
            } else if (this.alphabet.isReturnSymbol(sym)) {
                // update asBuilder
                final int callIdx = this.alphabet.findCallIndex(asBuilder, asBuilder.size() - 1);
                final I procedure = asBuilder.get(callIdx);
                asBuilder.subList(callIdx + 1, asBuilder.size() - 1).clear();
                asBuilder.addAll(callIdx + 1, getTerminatingSequence(procedure).asList());
            }

            rsBuilder.remove(0);

            if (this.alphabet.isCallSymbol(sym)) {
                // updateRSBuilder
                final int rsBuilderReturnIdx = this.alphabet.findReturnIndex(rsBuilder, 0);
                final int inputReturnIdx = this.alphabet.findReturnIndex(input, i + 1);
                rsBuilder.subList(0, rsBuilderReturnIdx).clear();
                rsBuilder.addAll(0, minifyWellMatched(input.subWord(i + 1, inputReturnIdx)).asList());
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

            if (this.alphabet.isCallSymbol(sym)) {
                final int returnIdx = this.alphabet.findReturnIndex(input, i + 1);

                if (returnIdx > -1) {
                    wb.append(terminatingSequences.get(sym));
                    wb.append(alphabet.getReturnSymbol());
                    i = returnIdx; // next loop iteration starts _after_ the return symbol
                }
            }
        }

        return wb.toWord();
    }

}
