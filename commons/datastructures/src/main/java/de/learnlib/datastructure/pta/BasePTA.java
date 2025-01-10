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
package de.learnlib.datastructure.pta;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import de.learnlib.datastructure.pta.visualization.PTAVisualizationHelper;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.FiniteAlphabetAutomaton;
import net.automatalib.automaton.UniversalDeterministicAutomaton;
import net.automatalib.automaton.graph.TransitionEdge;
import net.automatalib.automaton.graph.TransitionEdge.Property;
import net.automatalib.automaton.graph.UniversalAutomatonGraphView;
import net.automatalib.common.smartcollection.IntSeq;
import net.automatalib.common.util.collection.AbstractSimplifiedIterator;
import net.automatalib.graph.UniversalGraph;
import net.automatalib.visualization.VisualizationHelper;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Base class for prefix tree acceptors.
 *
 * @param <S>
 *         state type
 * @param <SP>
 *         state property type
 * @param <TP>
 *         transition property type
 */
public class BasePTA<S extends AbstractBasePTAState<S, SP, TP>, SP, TP>
        implements UniversalDeterministicAutomaton<S, Integer, PTATransition<S>, SP, TP>,
                   FiniteAlphabetAutomaton<S, Integer, PTATransition<S>> {

    private final int alphabetSize;
    private final S root;

    /**
     * Constructor.
     *
     * @param alphabetSize
     *         the size of the input alphabet
     * @param root
     *         the root state
     */
    public BasePTA(int alphabetSize, S root) {
        this.alphabetSize = alphabetSize;
        this.root = Objects.requireNonNull(root);
    }

    /**
     * Retrieves the root of the PTA.
     *
     * @return the root state
     */
    public S getRoot() {
        return root;
    }

    @Override
    public Alphabet<Integer> getInputAlphabet() {
        return Alphabets.integers(0, alphabetSize - 1);
    }

    /**
     * Adds a sample to the PTA, and sets the property of the last reached (or inserted) state accordingly.
     *
     * @param sample
     *         the word to add to the PTA
     * @param lastProperty
     *         the property of the last state to set
     */
    public void addSample(IntSeq sample, SP lastProperty) {
        S target = getOrCreateState(sample);
        target.mergeStateProperty(lastProperty);
    }

    /**
     * Retrieves the state reached by the given word. If there is no path for the word in the PTA, it will be added to
     * the PTA on-the-fly.
     *
     * @param word
     *         the word
     *
     * @return the state reached by this word, which might have been newly created (along with all required predecessor
     * states)
     */
    public S getOrCreateState(IntSeq word) {
        S curr = root;
        for (int sym : word) {
            curr = curr.getOrCreateSuccessor(sym, alphabetSize);
        }

        return curr;
    }

    public void addSampleWithStateProperties(IntSeq sample, List<? extends SP> lastStateProperties) {
        int sampleLen = sample.size();
        int skip = sampleLen + 1 - lastStateProperties.size();
        if (skip < 0) {
            throw new IllegalArgumentException();
        }

        S curr = getRoot();
        int i = 0;
        while (i < skip) {
            int sym = sample.get(i++);
            curr = curr.getOrCreateSuccessor(sym, alphabetSize);
        }

        Iterator<? extends SP> spIt = lastStateProperties.iterator();

        while (i < sampleLen) {
            curr.mergeStateProperty(spIt.next());
            int sym = sample.get(i++);
            curr = curr.getOrCreateSuccessor(sym, alphabetSize);
        }

        curr.mergeStateProperty(spIt.next());
    }

    public void addSampleWithTransitionProperties(IntSeq sample, List<? extends TP> lastTransitionProperties) {
        int sampleLen = sample.size();
        int skip = sampleLen - lastTransitionProperties.size();
        if (skip < 0) {
            throw new IllegalArgumentException();
        }

        S curr = getRoot();
        int i = 0;
        while (i < skip) {
            int sym = sample.get(i++);
            curr = curr.getOrCreateSuccessor(sym, alphabetSize);
        }

        Iterator<? extends TP> tpIt = lastTransitionProperties.iterator();
        while (i < sampleLen) {
            int sym = sample.get(i++);
            curr.mergeTransitionProperty(sym, alphabetSize, tpIt.next());
            curr = curr.getOrCreateSuccessor(sym, alphabetSize);
        }
    }

    @Override
    public S getSuccessor(PTATransition<S> transition) {
        return transition.getTarget();
    }

    @Override
    public @Nullable S getSuccessor(S state, Integer input) {
        return state.getSuccessor(input);
    }

    @Override
    public Iterator<S> iterator() {
        Set<S> visited = new HashSet<>();
        final Deque<S> bfsQueue = new ArrayDeque<>();
        bfsQueue.add(root);
        visited.add(root);

        return new AbstractSimplifiedIterator<S>() {

            @Override
            protected boolean calculateNext() {
                final S next = bfsQueue.poll();
                if (next == null) {
                    return false;
                }
                super.nextValue = next;
                for (int i = 0; i < alphabetSize; i++) {
                    final S child = next.getSuccessor(i);
                    if (child != null && visited.add(child)) {
                        bfsQueue.offer(child);
                    }
                }
                return true;
            }
        };
    }

    @Override
    public Collection<S> getStates() {
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

    @Override
    public S getInitialState() {
        return getRoot();
    }

    @Override
    public @Nullable PTATransition<S> getTransition(S state, Integer input) {
        if (state.getSuccessor(input) == null) {
            return null;
        }

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

    @Override
    public UniversalGraph<S, TransitionEdge<Integer, PTATransition<S>>, SP, Property<Integer, TP>> transitionGraphView(
            Collection<? extends Integer> inputs) {
        return new UniversalAutomatonGraphView<S, Integer, PTATransition<S>, SP, TP, BasePTA<S, SP, TP>>(this, inputs) {

            @Override
            public VisualizationHelper<S, TransitionEdge<Integer, PTATransition<S>>> getVisualizationHelper() {
                return BasePTA.this.getVisualizationHelper();
            }
        };
    }

    protected VisualizationHelper<S, TransitionEdge<Integer, PTATransition<S>>> getVisualizationHelper() {
        return new PTAVisualizationHelper<>(this);
    }
}
