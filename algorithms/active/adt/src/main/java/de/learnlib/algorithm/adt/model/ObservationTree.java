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
package de.learnlib.algorithm.adt.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import de.learnlib.algorithm.LearningAlgorithm;
import de.learnlib.algorithm.adt.adt.ADTNode;
import de.learnlib.algorithm.adt.util.ADTUtil;
import de.learnlib.filter.cache.mealy.AdaptiveQueryCache;
import de.learnlib.oracle.AdaptiveMembershipOracle;
import de.learnlib.query.AdaptiveQuery;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.SupportsGrowingAlphabet;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.common.util.Pair;
import net.automatalib.util.automaton.equivalence.NearLinearEquivalenceTest;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A class, that stores observations of the system under learning in a tree-like structure. Can be used to <ul> <li>
 * Store output behavior information about the system under learning </li> <li> Query output behavior of the system
 * under learning if it has been stored before (i.e. cache) </li> <li> Find separating words of hypothesis states based
 * on the stored output behavior information </li> </ul>
 *
 * @param <S>
 *         (hypothesis) state type
 * @param <I>
 *         input alphabet type
 * @param <O>
 *         output alphabet type
 */
public class ObservationTree<S, I, O> implements AdaptiveMembershipOracle<I, O>, SupportsGrowingAlphabet<I> {

    private final Alphabet<I> alphabet;

    private final AdaptiveMembershipOracle<I, O> delegate;

    private final AdaptiveQueryCache<I, O> cache;
    private final Integer init;

    private final Map<S, Integer> nodeToObservationMap;

    public ObservationTree(Alphabet<I> alphabet, AdaptiveMembershipOracle<I, O> delegate, boolean useCache) {
        this.cache = new AdaptiveQueryCache<>(delegate, alphabet);
        this.init = this.cache.getInit();

        if (useCache) {
            this.delegate = this.cache;
        } else {
            this.delegate = delegate;
        }

        this.alphabet = alphabet;
        this.nodeToObservationMap = new HashMap<>();
    }

    /**
     * Initialize the observation tree with initial hypothesis state. Usually used during
     * {@link LearningAlgorithm#startLearning()}
     *
     * @param state
     *         the initial state of the hypothesis
     */
    public void initialize(S state) {
        this.nodeToObservationMap.put(state, this.init);
    }

    /**
     * Extended initialization method, that allows to initialize the observation tree with several hypothesis states.
     *
     * @param states
     *         The hypothesis states to initialize the observation tree with
     * @param asFunction
     *         Function to compute the access sequence of a node
     * @param outputFunction
     *         Function to compute the output of the access sequences
     */
    public void initialize(Collection<S> states,
                           Function<S, Word<I>> asFunction,
                           Function<Word<I>, Word<O>> outputFunction) {
        for (S s : states) {
            final Word<I> as = asFunction.apply(s);
            final Integer treeNode = this.addTrace(this.init, as, outputFunction.apply(as));
            this.nodeToObservationMap.put(s, treeNode);
        }
    }

    /**
     * Store input/output information about a hypothesis state in the internal data structure.
     *
     * @param state
     *         the hypothesis state for which information should be stored
     * @param input
     *         the input sequence applied when in the given state
     * @param output
     *         the observed output sequence
     */
    public void addTrace(S state, Word<I> input, Word<O> output) {
        this.addTrace(this.nodeToObservationMap.get(state), input, output);
    }

    private Integer addTrace(Integer state, Word<I> input, Word<O> output) {
        return this.cache.insert(state, input, output);
    }

    /**
     * See {@link #addState(Object, Word, Object)}. Convenience method that stores all information that the traces of
     * the given {@link ADTNode} holds.
     *
     * @param state
     *         the hypothesis state for which information should be stored
     * @param adtNode
     *         the {@link ADTNode} whose traces should be stored
     */
    public void addTrace(S state, ADTNode<S, I, O> adtNode) {

        final Integer internalState = this.nodeToObservationMap.get(state);

        ADTNode<S, I, O> adsIter = adtNode;

        while (adsIter != null) {
            final Pair<Word<I>, Word<O>> trace = ADTUtil.buildTraceForNode(adsIter);
            this.addTrace(internalState, trace.getFirst(), trace.getSecond());

            adsIter = ADTUtil.getStartOfADS(adsIter).getParent();
        }
    }

    /**
     * Registers a new hypothesis state at the observation tree. It is expected to register states in the order of their
     * discovery, meaning whenever a new state is added, information about all prefixes of its access sequence are
     * already stored. Therefore, providing only the output of the last symbol of its access sequence is sufficient.
     *
     * @param newState
     *         the hypothesis state in question
     * @param accessSequence
     *         the access sequence of the hypothesis state in the system under learning
     * @param output
     *         the output of the last symbol of the access sequence.
     */
    public void addState(S newState, Word<I> accessSequence, O output) {
        final Word<I> prefix = accessSequence.prefix(accessSequence.length() - 1);
        final I sym = accessSequence.lastSymbol();

        final Integer pred = this.cache.getCache().getState(prefix);
        final Integer target = this.cache.insert(pred, Word.fromLetter(sym), Word.fromLetter(output));

        this.nodeToObservationMap.put(newState, target);
    }

    /**
     * Find a separating word for two hypothesis states, after applying given input sequence first.
     *
     * @param s1
     *         first state
     * @param s2
     *         second state
     * @param prefix
     *         input sequence
     *
     * @return A {@link Word} separating the two states reached after applying the prefix to s1 and s2, or {@code null}
     * if no separating word exists.
     */
    public @Nullable Word<I> findSeparatingWord(S s1, S s2, Word<I> prefix) {

        final MealyMachine<Integer, I, ?, O> cache = this.cache.getCache();

        final Integer n1 = this.nodeToObservationMap.get(s1);
        final Integer n2 = this.nodeToObservationMap.get(s2);

        final Integer s1Succ = cache.getSuccessor(n1, prefix);
        final Integer s2Succ = cache.getSuccessor(n2, prefix);

        if (s1Succ != null && s2Succ != null) {
            return NearLinearEquivalenceTest.findSeparatingWord(cache, s1Succ, s2Succ, alphabet, true);
        }

        return null;
    }

    /**
     * Find a separating word for two hypothesis states.
     *
     * @param s1
     *         first state
     * @param s2
     *         second state
     *
     * @return A {@link Word} separating the two words. {@code null} if no such word is found.
     */
    public @Nullable Word<I> findSeparatingWord(S s1, S s2) {

        final Integer n1 = this.nodeToObservationMap.get(s1);
        final Integer n2 = this.nodeToObservationMap.get(s2);

        return NearLinearEquivalenceTest.findSeparatingWord(this.cache.getCache(), n1, n2, this.alphabet, true);
    }

    /**
     * Computes the output of the system under learning when applying the given input sequence in the given hypothesis
     * state. Requires the input/output behavior information to be stored before.
     *
     * @param s
     *         the hypothesis state
     * @param input
     *         the input sequence of interest
     *
     * @return the previously stored output behavior of the system under learning
     */
    public Word<O> trace(S s, Word<I> input) {
        final Integer q = this.nodeToObservationMap.get(s);
        return this.cache.getCache().computeStateOutput(q, input);
    }

    @Override
    public void processQueries(Collection<? extends AdaptiveQuery<I, O>> queries) {
        this.delegate.processQueries(queries);
    }

    @Override
    public void addAlphabetSymbol(I i) {
        this.alphabet.asGrowingAlphabetOrThrowException().add(i);
        this.cache.addAlphabetSymbol(i);
    }
}
