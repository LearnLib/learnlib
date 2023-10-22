/* Copyright (C) 2013-2023 TU Dortmund
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
package de.learnlib.datastructure.pta;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.google.common.collect.AbstractIterator;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.UniversalDeterministicAutomaton;
import net.automatalib.automaton.concept.InputAlphabetHolder;
import net.automatalib.automaton.graph.TransitionEdge;
import net.automatalib.automaton.graph.TransitionEdge.Property;
import net.automatalib.automaton.graph.UniversalAutomatonGraphView;
import net.automatalib.automaton.visualization.AutomatonVisualizationHelper;
import net.automatalib.graph.UniversalGraph;
import net.automatalib.visualization.VisualizationHelper;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Base class for prefix tree acceptors.
 *
 * @param <S>
 *         state type
 * @param <I>
 *         input symbol type
 * @param <SP>
 *         state property type
 * @param <TP>
 *         transition property type
 */
public class BasePTA<S extends AbstractBasePTAState<S, SP, TP>, I, SP, TP>
        implements UniversalDeterministicAutomaton<S, I, PTATransition<S>, SP, TP>, InputAlphabetHolder<I> {

    private final Alphabet<I> alphabet;
    private final int alphabetSize;
    private final S root;

    /**
     * Constructor.
     *
     * @param alphabet
     *         the input alphabet
     * @param root
     *         the root state
     */
    public BasePTA(Alphabet<I> alphabet, S root) {
        this.alphabet = alphabet;
        this.alphabetSize = alphabet.size();
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
    public Alphabet<I> getInputAlphabet() {
        return alphabet;
    }

    /**
     * Adds a sample to the PTA, and sets the property of the last reached (or inserted) state accordingly.
     *
     * @param sample
     *         the word to add to the PTA
     * @param lastProperty
     *         the property of the last state to set
     */
    public void addSample(Word<I> sample, SP lastProperty) {
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
    public S getOrCreateState(Word<I> word) {
        S curr = root;
        for (I sym : word) {
            curr = curr.getOrCreateSuccessor(alphabet.getSymbolIndex(sym), alphabetSize);
        }

        return curr;
    }

    public void addSampleWithStateProperties(Word<I> sample, List<? extends SP> lastStateProperties) {
        int sampleLen = sample.size();
        int skip = sampleLen + 1 - lastStateProperties.size();
        if (skip < 0) {
            throw new IllegalArgumentException();
        }

        S curr = getRoot();
        int i = 0;
        while (i < skip) {
            I sym = sample.getSymbol(i++);
            curr = curr.getOrCreateSuccessor(alphabet.getSymbolIndex(sym), alphabetSize);
        }

        Iterator<? extends SP> spIt = lastStateProperties.iterator();

        while (i < sampleLen) {
            curr.mergeStateProperty(spIt.next());
            I sym = sample.getSymbol(i++);
            curr = curr.getOrCreateSuccessor(alphabet.getSymbolIndex(sym), alphabetSize);
        }

        curr.mergeStateProperty(spIt.next());
    }

    public void addSampleWithTransitionProperties(Word<I> sample, List<? extends TP> lastTransitionProperties) {
        int sampleLen = sample.size();
        int skip = sampleLen - lastTransitionProperties.size();
        if (skip < 0) {
            throw new IllegalArgumentException();
        }

        S curr = getRoot();
        int i = 0;
        while (i < skip) {
            I sym = sample.getSymbol(i++);
            curr = curr.getOrCreateSuccessor(alphabet.getSymbolIndex(sym), alphabetSize);
        }

        Iterator<? extends TP> tpIt = lastTransitionProperties.iterator();
        while (i < sampleLen) {
            I sym = sample.getSymbol(i++);
            int idx = alphabet.getSymbolIndex(sym);
            curr.mergeTransitionProperty(idx, alphabetSize, tpIt.next());
            curr = curr.getOrCreateSuccessor(idx, alphabetSize);
        }
    }

    @Override
    public S getSuccessor(PTATransition<S> transition) {
        return transition.getTarget();
    }

    @Override
    public @Nullable S getSuccessor(S state, I input) {
        return state.getSuccessor(alphabet.getSymbolIndex(input));
    }

    @Override
    public Iterator<S> iterator() {
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
                for (int i = 0; i < alphabet.size(); i++) {
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
    public @Nullable PTATransition<S> getTransition(S state, I input) {
        if (state.getSuccessor(alphabet.getSymbolIndex(input)) == null) {
            return null;
        }

        return new PTATransition<>(state, alphabet.getSymbolIndex(input));
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
    public UniversalGraph<S, TransitionEdge<I, PTATransition<S>>, SP, Property<I, TP>> transitionGraphView(Collection<? extends I> inputs) {
        return new UniversalAutomatonGraphView<S, I, PTATransition<S>, SP, TP, BasePTA<S, I, SP, TP>>(this, inputs) {

            @Override
            public VisualizationHelper<S, TransitionEdge<I, PTATransition<S>>> getVisualizationHelper() {
                return new AutomatonVisualizationHelper<S, I, PTATransition<S>, BasePTA<S, I, SP, TP>>(BasePTA.this) {

                    @Override
                    public boolean getNodeProperties(S node, Map<String, String> properties) {
                        final SP property = node.getProperty();
                        properties.put(NodeAttrs.LABEL, property == null ? "" : property.toString());
                        return super.getNodeProperties(node, properties);
                    }
                };
            }
        };
    }
}
