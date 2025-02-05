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
package de.learnlib.algorithm.adt.ads;

import java.util.ArrayDeque;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

import de.learnlib.algorithm.adt.adt.ADTLeafNode;
import de.learnlib.algorithm.adt.adt.ADTNode;
import de.learnlib.algorithm.adt.api.PartialTransitionAnalyzer;
import de.learnlib.algorithm.adt.util.ADTUtil;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.concept.StateIDs;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.common.smartcollection.ReflexiveMapView;
import net.automatalib.common.util.HashUtil;
import net.automatalib.common.util.Pair;
import net.automatalib.util.automaton.ads.ADS;
import net.automatalib.util.automaton.ads.ADSUtil;
import net.automatalib.util.automaton.ads.BacktrackingSearch;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A variant of the backtracking ADS search (see {@link ADS}, {@link BacktrackingSearch}), that works on partially
 * defined automata. It tries to find an ADS based on defined transitions and successively closes transitions if no ADS
 * has been found.
 *
 * @param <S>
 *         (hypothesis) state type
 * @param <I>
 *         input alphabet type
 * @param <O>
 *         output alphabet type
 */
public final class DefensiveADS<S, I, O> {

    private final MealyMachine<S, I, ?, O> automaton;
    private final Alphabet<I> alphabet;
    private final Set<S> states;
    private final PartialTransitionAnalyzer<S, I> partialTransitionAnalyzer;
    /**
     * The states, whose outgoing {@link #refinementInput}-transitions need to be closed.
     */
    private @Nullable Set<S> refinementStates;
    /**
     * The output for which the outgoing transitions of {@link #refinementStates} are undefined.
     */
    private @Nullable I refinementInput;

    private DefensiveADS(MealyMachine<S, I, ?, O> automaton,
                         Alphabet<I> alphabet,
                         Set<S> states,
                         PartialTransitionAnalyzer<S, I> partialTransitionAnalyzer) {
        this.automaton = automaton;
        this.alphabet = alphabet;
        this.states = states;
        this.partialTransitionAnalyzer = partialTransitionAnalyzer;
    }

    /**
     * Compute an adaptive distinguishing sequence (as an ADT) for the given automaton and the given set of states.
     *
     * @param automaton
     *         the automaton for which an ADS should be computed
     * @param alphabet
     *         the input alphabet of the automaton
     * @param states
     *         the set of states which should be distinguished by the computed ADS
     * @param pta
     *         the analyzer to inspect and close partial transitions
     * @param <S>
     *         (hypothesis) state type
     * @param <I>
     *         input alphabet type
     * @param <O>
     *         output alphabet type
     *
     * @return {@code Optional.empty()} if there exists no ADS that distinguishes the given states, a valid ADS
     * otherwise.
     */
    public static <S, I, O> Optional<ADTNode<S, I, O>> compute(MealyMachine<S, I, ?, O> automaton,
                                                               Alphabet<I> alphabet,
                                                               Set<S> states,
                                                               PartialTransitionAnalyzer<S, I> pta) {

        return new DefensiveADS<>(automaton, alphabet, states, pta).compute();
    }

    private Optional<ADTNode<S, I, O>> compute() {

        final Map<S, S> initialMapping = new ReflexiveMapView<>(states);
        Optional<ADTNode<S, I, O>> interMediateResult = compute(initialMapping);

        while (!interMediateResult.isPresent()) {

            // we encountered open transitions that can be closed
            if (refinementStates != null && refinementInput != null) {
                for (S s : refinementStates) {
                    this.partialTransitionAnalyzer.closeTransition(s, this.refinementInput);
                }
                this.refinementStates = null;
                this.refinementInput = null;
            } else { // no ADS found
                break;
            }

            // retry with updated hypothesis
            interMediateResult = compute(initialMapping);
        }

        return interMediateResult;
    }

    private Optional<ADTNode<S, I, O>> compute(Map<S, S> mapping) {

        final long maximumSplittingWordLength =
                ADSUtil.computeMaximumSplittingWordLength(automaton.size(), mapping.size(), this.states.size());
        final Queue<Word<I>> splittingWordCandidates = new ArrayDeque<>();
        final StateIDs<S> stateIds = automaton.stateIDs();
        final Set<BitSet> cache = new HashSet<>();

        splittingWordCandidates.add(Word.epsilon());

        while (!splittingWordCandidates.isEmpty()) {

            @SuppressWarnings("nullness") // false positive https://github.com/typetools/checker-framework/issues/399
            final @NonNull Word<I> prefix = splittingWordCandidates.poll();
            final Map<S, S> currentToInitialMapping = new LinkedHashMap<>(HashUtil.capacity(mapping.size()));

            for (Entry<S, S> e : mapping.entrySet()) {
                currentToInitialMapping.put(automaton.getSuccessor(e.getKey(), prefix), e.getValue());
            }

            final BitSet currentSetAsBitSet = new BitSet();
            for (S s : currentToInitialMapping.keySet()) {
                currentSetAsBitSet.set(stateIds.getStateId(s));
            }

            if (cache.contains(currentSetAsBitSet)) {
                continue;
            }

            oneSymbolFuture:
            for (I i : this.alphabet) {

                //check for missing transitions
                final Set<S> statesWithMissingTransitions = new LinkedHashSet<>();
                for (S s : currentToInitialMapping.keySet()) {
                    if (!this.partialTransitionAnalyzer.isTransitionDefined(s, i)) {
                        statesWithMissingTransitions.add(s);
                    }
                }

                // if we encountered undefined transitions, stop further search
                if (!statesWithMissingTransitions.isEmpty()) {

                    // override existing refinement candidate, if we can assure progress with fewer transitions to refine
                    if (refinementStates == null || statesWithMissingTransitions.size() < refinementStates.size()) {
                        refinementStates = statesWithMissingTransitions;
                        refinementInput = i;
                    }

                    continue;
                }

                // compute successors
                final Map<O, Map<S, S>> successors = new HashMap<>();

                for (Map.Entry<S, S> entry : currentToInitialMapping.entrySet()) {
                    final S current = entry.getKey();
                    final S nextState = automaton.getSuccessor(current, i);
                    final O nextOutput = automaton.getOutput(current, i);

                    final Map<S, S> nextMapping;
                    if (successors.containsKey(nextOutput)) {
                        nextMapping = successors.get(nextOutput);
                    } else {
                        nextMapping = new HashMap<>();
                        successors.put(nextOutput, nextMapping);
                    }

                    // invalid input
                    if (nextMapping.put(nextState, entry.getValue()) != null) {
                        continue oneSymbolFuture;
                    }
                }

                //splitting word
                if (successors.size() > 1) {
                    final Map<O, ADTNode<S, I, O>> results = new HashMap<>();

                    for (Map.Entry<O, Map<S, S>> entry : successors.entrySet()) {

                        final Map<S, S> currentMapping = entry.getValue();

                        final BitSet currentNodeAsBitSet = new BitSet();
                        for (S s : currentMapping.keySet()) {
                            currentNodeAsBitSet.set(stateIds.getStateId(s));
                        }

                        if (cache.contains(currentNodeAsBitSet)) {
                            continue oneSymbolFuture;
                        }

                        final Optional<ADTNode<S, I, O>> succ;
                        if (currentMapping.size() > 1) {
                            succ = compute(currentMapping);
                        } else {
                            final S s = currentMapping.values().iterator().next();
                            succ = Optional.of(new ADTLeafNode<>(null, s));
                        }

                        if (!succ.isPresent()) {
                            cache.add(currentNodeAsBitSet);
                            continue oneSymbolFuture;
                        }

                        results.put(entry.getKey(), succ.get());
                    }

                    // create ADS (if we haven't continued until here)
                    final Pair<ADTNode<S, I, O>, ADTNode<S, I, O>> ads =
                            ADTUtil.buildADSFromTrace(automaton, prefix.append(i), mapping.keySet().iterator().next());
                    final ADTNode<S, I, O> head = ads.getFirst();
                    final ADTNode<S, I, O> tail = ads.getSecond();

                    for (Map.Entry<O, ADTNode<S, I, O>> entry : results.entrySet()) {
                        entry.getValue().setParent(tail);
                        tail.getChildren().put(entry.getKey(), entry.getValue());
                    }

                    return Optional.of(head);
                } else if (prefix.length() < maximumSplittingWordLength) { // no splitting word
                    splittingWordCandidates.add(prefix.append(i));
                }
            }

            cache.add(currentSetAsBitSet);
        }

        return Optional.empty();
    }
}
