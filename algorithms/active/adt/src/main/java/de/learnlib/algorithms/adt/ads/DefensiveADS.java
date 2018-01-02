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
package de.learnlib.algorithms.adt.ads;

import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.ParametersAreNonnullByDefault;

import de.learnlib.algorithms.adt.adt.ADTLeafNode;
import de.learnlib.algorithms.adt.adt.ADTNode;
import de.learnlib.algorithms.adt.api.PartialTransitionAnalyzer;
import de.learnlib.algorithms.adt.util.ADTUtil;
import net.automatalib.automata.concepts.StateIDs;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.commons.util.Pair;
import net.automatalib.util.automata.ads.ADSUtil;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

/**
 * A variant of the backtracking ADS search (see {@link net.automatalib.util.automata.ads.ADS}, {@link
 * net.automatalib.util.automata.ads.BacktrackingSearch}), that works on partially defined automata. It tries to find an
 * ADS based on defined transitions and successively closes transitions if no ADS has been found.
 *
 * @param <S>
 *         (hypothesis) state type
 * @param <I>
 *         input alphabet type
 * @param <O>
 *         output alphabet type
 *
 * @author frohme
 */
@ParametersAreNonnullByDefault
public final class DefensiveADS<S, I, O> {

    private final MealyMachine<S, I, ?, O> automaton;
    private final Alphabet<I> alphabet;
    private final Set<S> states;
    private final PartialTransitionAnalyzer<S, I> partialTransitionAnalyzer;
    /**
     * The states, whose outgoing {@link #refinementInput}-transitions need to be closed.
     */
    private Set<S> refinementStates;
    /**
     * The output for which the outgoing transitions of {@link #refinementStates} are undefined.
     */
    private I refinementInput;

    private DefensiveADS(final MealyMachine<S, I, ?, O> automaton,
                         final Alphabet<I> alphabet,
                         final Set<S> states,
                         final PartialTransitionAnalyzer<S, I> partialTransitionAnalyzer) {
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
    public static <S, I, O> Optional<ADTNode<S, I, O>> compute(final MealyMachine<S, I, ?, O> automaton,
                                                               final Alphabet<I> alphabet,
                                                               final Set<S> states,
                                                               final PartialTransitionAnalyzer<S, I> pta) {

        return new DefensiveADS<>(automaton, alphabet, states, pta).compute();
    }

    private Optional<ADTNode<S, I, O>> compute() {

        final Map<S, S> initialMapping =
                states.stream().collect(Collectors.toMap(Function.identity(), Function.identity()));

        Optional<ADTNode<S, I, O>> interMediateResult = compute(initialMapping);

        while (!interMediateResult.isPresent()) {

            // we encountered open transitions that can be closed
            if (refinementStates != null && refinementInput != null) {
                for (final S s : refinementStates) {
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

    private Optional<ADTNode<S, I, O>> compute(final Map<S, S> mapping) {

        final long maximumSplittingWordLength =
                ADSUtil.computeMaximumSplittingWordLength(automaton.size(), mapping.size(), this.states.size());
        final Queue<Word<I>> splittingWordCandidates = new LinkedList<>();
        final StateIDs<S> stateIds = automaton.stateIDs();
        final Set<BitSet> cache = new HashSet<>();

        splittingWordCandidates.add(Word.epsilon());

        candidateLoop:
        while (!splittingWordCandidates.isEmpty()) {

            final Word<I> prefix = splittingWordCandidates.poll();
            final Map<S, S> currentToInitialMapping = mapping.keySet()
                                                             .stream()
                                                             .collect(Collectors.toMap(x -> automaton.getSuccessor(x,
                                                                                                                   prefix),
                                                                                       mapping::get));
            final BitSet currentSetAsBitSet = new BitSet();
            for (final S s : currentToInitialMapping.keySet()) {
                currentSetAsBitSet.set(stateIds.getStateId(s));
            }

            if (cache.contains(currentSetAsBitSet)) {
                continue candidateLoop;
            }

            oneSymbolFuture:
            for (final I i : this.alphabet) {

                //check for missing transitions
                final Set<S> statesWithMissingTransitions = new HashSet<>();
                for (final S s : currentToInitialMapping.keySet()) {
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

                    continue oneSymbolFuture;
                }

                // compute successors
                final Map<O, Map<S, S>> successors = new HashMap<>();

                for (final Map.Entry<S, S> entry : currentToInitialMapping.entrySet()) {
                    final S current = entry.getKey();
                    final S nextState = automaton.getSuccessor(current, i);
                    final O nextOutput = automaton.getOutput(current, i);

                    final Map<S, S> nextMapping;
                    if (!successors.containsKey(nextOutput)) {
                        nextMapping = new HashMap<>();
                        successors.put(nextOutput, nextMapping);
                    } else {
                        nextMapping = successors.get(nextOutput);
                    }

                    // invalid input
                    if (nextMapping.put(nextState, entry.getValue()) != null) {
                        continue oneSymbolFuture;
                    }
                }

                //splitting word
                if (successors.size() > 1) {
                    final Map<O, ADTNode<S, I, O>> results = new HashMap<>();

                    for (final Map.Entry<O, Map<S, S>> entry : successors.entrySet()) {

                        final Map<S, S> currentMapping = entry.getValue();

                        final BitSet currentNodeAsBitSet = new BitSet();
                        for (final S s : currentMapping.keySet()) {
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

                    for (final Map.Entry<O, ADTNode<S, I, O>> entry : results.entrySet()) {
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
