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
package de.learnlib.algorithms.adt.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import javax.annotation.ParametersAreNonnullByDefault;

import de.learnlib.algorithms.adt.adt.ADTNode;
import de.learnlib.algorithms.adt.util.ADTUtil;
import net.automatalib.automata.transout.impl.FastMealy;
import net.automatalib.automata.transout.impl.FastMealyState;
import net.automatalib.commons.util.Pair;
import net.automatalib.util.automata.equivalence.NearLinearEquivalenceTest;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

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
 *
 * @author frohme
 */
@ParametersAreNonnullByDefault
public class ObservationTree<S, I, O> {

    private final Alphabet<I> alphabet;

    private final FastMealy<I, O> observationTree;

    private final Map<S, FastMealyState<O>> nodeToObservationMap;

    public ObservationTree(final Alphabet<I> alphabet) {
        this.alphabet = alphabet;
        this.observationTree = new FastMealy<>(alphabet);
        this.nodeToObservationMap = new HashMap<>();
    }

    public FastMealy<I, O> getObservationTree() {
        return observationTree;
    }

    /**
     * Initialize the observation tree with initial hypothesis state. Usually used during {@link
     * de.learnlib.api.algorithm.LearningAlgorithm#startLearning()}
     *
     * @param state
     *         the initial state of the hypothesis
     */
    public void initialize(final S state) {
        final FastMealyState<O> init = this.observationTree.addInitialState();
        this.nodeToObservationMap.put(state, init);
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
    public void initialize(final Collection<S> states,
                           final Function<S, Word<I>> asFunction,
                           final Function<Word<I>, Word<O>> outputFunction) {
        final FastMealyState<O> init = this.observationTree.addInitialState();

        for (final S s : states) {
            final Word<I> as = asFunction.apply(s);
            final FastMealyState<O> treeNode = this.addTrace(init, as, outputFunction.apply(as));
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
    public void addTrace(final S state, final Word<I> input, final Word<O> output) {
        this.addTrace(this.nodeToObservationMap.get(state), input, output);
    }

    private FastMealyState<O> addTrace(final FastMealyState<O> state, final Word<I> input, final Word<O> output) {

        assert input.length() == output.length() : "Traces differ in length";

        final Iterator<I> inputIter = input.iterator();
        final Iterator<O> outputIter = output.iterator();
        FastMealyState<O> iter = state;

        while (inputIter.hasNext()) {

            final I nextInput = inputIter.next();
            final O nextOuput = outputIter.next();
            final FastMealyState<O> nextState;

            if (this.observationTree.getTransition(iter, nextInput) == null) {
                nextState = this.observationTree.addState();
                this.observationTree.addTransition(iter, nextInput, nextState, nextOuput);
            } else {
                assert this.observationTree.getOutput(iter, nextInput).equals(nextOuput) : "Inconsistent observations";
                nextState = this.observationTree.getSuccessor(iter, nextInput);
            }

            iter = nextState;
        }

        return iter;
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
    public void addTrace(final S state, final ADTNode<S, I, O> adtNode) {

        final FastMealyState<O> internalState = this.nodeToObservationMap.get(state);

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
     * already stored. Therefore providing only the output of the last symbol of its access sequence is sufficient.
     *
     * @param newState
     *         the hypothesis state in question
     * @param accessSequence
     *         the access sequence of the hypothesis state in the system under learning
     * @param output
     *         the output of the last symbol of the access sequence.
     */
    public void addState(final S newState, final Word<I> accessSequence, final O output) {
        final Word<I> prefix = accessSequence.prefix(accessSequence.length() - 1);
        final I sym = accessSequence.lastSymbol();

        final FastMealyState<O> pred =
                this.observationTree.getSuccessor(this.observationTree.getInitialState(), prefix);
        final FastMealyState<O> target;

        if (pred.getTransition(alphabet.getSymbolIndex(sym)) == null) {
            target = this.observationTree.addState();
            this.observationTree.addTransition(pred, sym, target, output);
        } else {
            target = this.observationTree.getSuccessor(pred, sym);
        }

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
     * @return A {@link Word} separating the two states reached after applying the prefix to s1 and s2. {@code
     * Optional.empty()} if not separating word exists.
     */
    public Optional<Word<I>> findSeparatingWord(final S s1, final S s2, final Word<I> prefix) {

        final FastMealyState<O> n1 = this.nodeToObservationMap.get(s1);
        final FastMealyState<O> n2 = this.nodeToObservationMap.get(s2);

        final FastMealyState<O> s1Succ = this.observationTree.getSuccessor(n1, prefix);
        final FastMealyState<O> s2Succ = this.observationTree.getSuccessor(n2, prefix);

        if (s1Succ != null && s2Succ != null) {
            final Word<I> sepWord =
                    NearLinearEquivalenceTest.findSeparatingWord(this.observationTree, s1Succ, s2Succ, alphabet, true);

            if (sepWord != null) {
                return Optional.of(sepWord);
            }
        }

        return Optional.empty();
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
    public Word<I> findSeparatingWord(final S s1, final S s2) {

        final FastMealyState<O> n1 = this.nodeToObservationMap.get(s1);
        final FastMealyState<O> n2 = this.nodeToObservationMap.get(s2);

        return NearLinearEquivalenceTest.findSeparatingWord(this.observationTree, n1, n2, this.alphabet, true);
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
    public Word<O> trace(final S s, final Word<I> input) {
        final FastMealyState<O> q = this.nodeToObservationMap.get(s);
        return this.observationTree.computeStateOutput(q, input);
    }

}
