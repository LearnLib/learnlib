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
package de.learnlib.datastructure.pta.pta;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterators;
import net.automatalib.automata.MutableDeterministic;
import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.commons.util.Pair;
import net.automatalib.commons.util.functions.FunctionsUtil;
import net.automatalib.graphs.Graph;
import net.automatalib.util.automata.Automata;
import net.automatalib.visualization.DefaultVisualizationHelper;
import net.automatalib.visualization.VisualizationHelper;
import net.automatalib.words.Alphabet;

/**
 * Base class for prefix tree acceptors.
 *
 * @param <SP>
 *         state property type
 * @param <TP>
 *         transition property type
 * @param <S>
 *         state type
 *
 * @author Malte Isberner
 */
@ParametersAreNonnullByDefault
public class BasePTA<SP, TP, S extends AbstractBasePTAState<SP, TP, S>>
        implements UniversalDeterministicAutomaton<S, Integer, PTATransition<S>, SP, TP> {

    @Nonnegative
    protected final int alphabetSize;
    @Nonnull
    protected final S root;

    /**
     * Constructor.
     *
     * @param alphabetSize
     *         the size of the input alphabet
     * @param root
     *         the root state
     */
    public BasePTA(@Nonnegative int alphabetSize, S root) {
        this.alphabetSize = alphabetSize;
        this.root = Objects.requireNonNull(root);
    }

    /**
     * Retrieves the state reached by the given word (represented as an {@code int} array). If there is no path for the
     * given word in the PTA, {@code null} is returned.
     *
     * @param word
     *         the word
     *
     * @return the state reached by this word, or {@code null} if there is no path for the given word in the PTA
     */
    @Nullable
    public S getState(int[] word) {
        S curr = root;
        int len = word.length;
        for (int i = 0; i < len && curr != null; i++) {
            curr = curr.getSuccessor(word[i]);
        }
        return curr;
    }

    /**
     * Adds a sample to the PTA, and sets the property of the last reached (or inserted) state accordingly.
     *
     * @param sample
     *         the word to add to the PTA
     * @param lastProperty
     *         the property of the last state to set
     */
    public void addSample(int[] sample, SP lastProperty) {
        S target = getOrCreateState(sample);
        if (!target.tryMergeStateProperty(lastProperty)) {
            throw new IllegalStateException();
        }
    }

    /**
     * Retrieves the state reached by the given word (represented as an {@code int} array). If there is no path for the
     * word in the PTA, it will be added to the PTA on-the-fly.
     *
     * @param word
     *         the word
     *
     * @return the state reached by this word, which might have been newly created (along with all required predecessor
     * states)
     */
    @Nonnull
    public S getOrCreateState(int[] word) {
        S curr = root;
        for (int sym : word) {
            curr = curr.getOrCreateSuccessor(sym, alphabetSize);
        }

        return curr;
    }

    public void addSampleWithStateProperties(int[] sample, List<? extends SP> lastStateProperties) {
        int sampleLen = sample.length;
        int skip = sampleLen + 1 - lastStateProperties.size();
        if (skip < 0) {
            throw new IllegalArgumentException();
        }

        S curr = getRoot();
        int i = 0;
        while (i < skip) {
            int sym = sample[i++];
            curr = curr.getOrCreateSuccessor(sym, alphabetSize);
        }

        Iterator<? extends SP> spIt = lastStateProperties.iterator();

        while (i < sampleLen) {
            if (!curr.tryMergeStateProperty(spIt.next())) {
                throw new IllegalArgumentException();
            }
            int sym = sample[i++];
            curr = curr.getOrCreateSuccessor(sym, alphabetSize);
        }

        if (!curr.tryMergeStateProperty(spIt.next())) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Retrieves the root of the PTA.
     *
     * @return the root state
     */
    @Nonnull
    public S getRoot() {
        return root;
    }

    public void addSampleWithTransitionProperties(int[] sample, List<? extends TP> lastTransitionProperties) {
        int sampleLen = sample.length;
        int skip = sampleLen - lastTransitionProperties.size();
        if (skip < 0) {
            throw new IllegalArgumentException();
        }

        S curr = getRoot();
        int i = 0;
        while (i < skip) {
            int sym = sample[i++];
            curr = curr.getOrCreateSuccessor(sym, alphabetSize);
        }

        Iterator<? extends TP> tpIt = lastTransitionProperties.iterator();
        while (i < sampleLen) {
            int sym = sample[i++];
            if (!curr.tryMergeTransitionProperty(sym, alphabetSize, tpIt.next())) {
                throw new IllegalArgumentException();
            }
            curr = curr.getOrCreateSuccessor(sym, alphabetSize);
        }
    }

    public <I> void toAutomaton(MutableDeterministic<?, I, ?, ? super SP, ? super TP> automaton, Alphabet<I> alphabet) {
        toAutomaton(automaton, alphabet, sp -> sp, tp -> tp);
    }

    public <S2, I, SP2, TP2> void toAutomaton(MutableDeterministic<S2, I, ?, ? super SP2, ? super TP2> automaton,
                                              Alphabet<I> alphabet,
                                              Function<? super SP, ? extends SP2> spExtractor,
                                              Function<? super TP, ? extends TP2> tpExtractor) {

        final Function<? super SP, ? extends SP2> safeSPExtractor = FunctionsUtil.safeDefault(spExtractor);
        final Function<? super TP, ? extends TP2> safeTPExtractor = FunctionsUtil.safeDefault(tpExtractor);

        Map<S, S2> resultStates = new HashMap<>();

        Queue<Pair<S, S2>> queue = new ArrayDeque<>();

        SP2 initProp = safeSPExtractor.apply(root.getStateProperty());
        S2 resultInit = automaton.addInitialState(initProp);
        queue.add(new Pair<>(root, resultInit));

        Pair<S, S2> curr;
        while ((curr = queue.poll()) != null) {
            S ptaState = curr.getFirst();
            S2 resultState = curr.getSecond();

            for (int i = 0; i < alphabetSize; i++) {
                S ptaSucc = ptaState.getSuccessor(i);
                if (ptaSucc != null) {
                    S2 resultSucc = resultStates.get(ptaSucc);
                    if (resultSucc == null) {
                        SP2 prop = safeSPExtractor.apply(ptaSucc.getStateProperty());
                        resultSucc = automaton.addState(prop);
                        resultStates.put(ptaSucc, resultSucc);
                        queue.offer(new Pair<>(ptaSucc, resultSucc));
                    }
                    I sym = alphabet.getSymbol(i);
                    TP2 transProp = safeTPExtractor.apply(ptaState.getTransProperty(i));
                    automaton.setTransition(resultState, sym, resultSucc, transProp);
                }
            }
        }

        Automata.invasiveMinimize(automaton, alphabet);
    }

    public <I> Graph<S, PTATransition<S>> graphView(Alphabet<I> alphabet) {
        return new Graph<S, PTATransition<S>>() {

            @Override
            public Collection<PTATransition<S>> getOutgoingEdges(S node) {
                return IntStream.range(0, alphabetSize)
                                .filter(i -> node.getSuccessor(i) != null)
                                .mapToObj(i -> new PTATransition<>(node, i))
                                .collect(Collectors.toList());
            }

            @Override
            public S getTarget(PTATransition<S> edge) {
                return edge.getTarget();
            }

            @Override
            public Collection<S> getNodes() {
                return bfsStates();
            }

            @Override
            public Iterator<S> iterator() {
                return bfsIterator();
            }

            @Override
            public VisualizationHelper<S, PTATransition<S>> getVisualizationHelper() {
                return new DefaultVisualizationHelper<S, PTATransition<S>>() {

                    @Override
                    public boolean getEdgeProperties(S src,
                                                     PTATransition<S> edge,
                                                     S tgt,
                                                     Map<String, String> properties) {
                        if (!super.getEdgeProperties(src, edge, tgt, properties)) {
                            return false;
                        }
                        properties.put(EdgeAttrs.LABEL, String.valueOf(alphabet.getSymbol(edge.getIndex())));
                        return true;
                    }
                };
            }
        };
    }

    /**
     * Retrieves a list of all states in this PTA that are reachable from the root state. The states will be returned in
     * breadth-first order.
     *
     * @return a breadth-first ordered list of all states in this PTA
     */
    @Nonnull
    public List<S> bfsStates() {
        List<S> stateList = new ArrayList<>();
        Set<S> visited = new HashSet<>();

        int ptr = 0;
        stateList.add(root);
        visited.add(root);
        int numStates = 1;

        while (ptr < numStates) {
            S curr = stateList.get(ptr++);
            for (int i = 0; i < alphabetSize; i++) {
                S succ = curr.getSuccessor(i);
                if (succ != null && visited.add(succ)) {
                    stateList.add(succ);
                    numStates++;
                }
            }
        }

        return stateList;
    }

    /**
     * Retrieves an iterator that can be used for iterating over all states in this PTA that are reachable from the root
     * state in a breadth-first order.
     *
     * @return an iterator for iterating over all states in this PTA
     */
    @Nonnull
    public Iterator<S> bfsIterator() {
        Set<S> visited = new HashSet<>();
        final Deque<S> bfsQueue = new ArrayDeque<>();
        bfsQueue.add(root);
        visited.add(root);

        return new AbstractIterator<S>() {

            @Override
            protected S computeNext() {
                S next = bfsQueue.poll();
                if (next == null) {
                    return endOfData();
                }
                for (int i = 0; i < alphabetSize; i++) {
                    S child = next.getSuccessor(i);
                    if (child != null && visited.add(child)) {
                        bfsQueue.offer(child);
                    }
                }
                return next;
            }
        };
    }

    @Override
    public S getSuccessor(PTATransition<S> transition) {
        return transition.getTarget();
    }

    @Override
    public S getSuccessor(S state, Integer input) {
        return state.getSuccessor(input);
    }

    @Override
    public Iterator<S> iterator() {
        return bfsIterator();
    }

    @Override
    public Collection<S> getStates() {
        return bfsStates();
    }

    @Override
    public int size() {
        return countStates();
    }

    /**
     * Counts the number of states in this PTA. Note that this method might require a complete traversal of the PTA.
     *
     * @return the number of states in the PTA reachable from the root state
     */
    @Nonnegative
    public int countStates() {
        return Iterators.size(bfsIterator());
    }

    @Override
    public S getInitialState() {
        return getRoot();
    }

    @Override
    public PTATransition<S> getTransition(S state, Integer input) {
        return new PTATransition<>(state, input);
    }

    @Override
    public SP getStateProperty(S state) {
        return state.getStateProperty();
    }

    @Override
    public TP getTransitionProperty(PTATransition<S> transition) {
        return transition.getSource().getTransProperty(transition.getIndex());
    }
}
