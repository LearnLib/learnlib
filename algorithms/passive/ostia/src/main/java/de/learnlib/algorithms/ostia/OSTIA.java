/* Copyright (C) 2013-2020 TU Dortmund
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
package de.learnlib.algorithms.ostia;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import de.learnlib.api.algorithm.PassiveLearningAlgorithm;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.automata.transducers.SubsequentialTransducer;
import net.automatalib.commons.smartcollections.IntSeq;
import net.automatalib.commons.util.Pair;
import net.automatalib.words.Alphabet;
import net.automatalib.words.GrowingAlphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.GrowingMapAlphabet;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Implementation of the "onward subsequential transducer inference algorithm" (OSTIA) from the paper <a
 * href="https://doi.org/10.1109/34.211465">Learning Subsequential Transducers for Pattern Recognition Interpretation
 * Tasks</a> by Oncina, García and Vidal.
 *
 * @author Aleksander Mendoza-Drosik
 * @author frohme
 */
public class OSTIA<I, O> implements PassiveLearningAlgorithm<SubsequentialTransducer<?, I, ?, O>, I, Word<O>> {

    private final Alphabet<I> inputAlphabet;
    private final GrowingAlphabet<O> outputAlphabet;
    private final State root;
    private boolean hasBeenComputed;

    public OSTIA(Alphabet<I> inputAlphabet) {
        this.inputAlphabet = inputAlphabet;
        this.outputAlphabet = new GrowingMapAlphabet<>();
        this.root = new State(inputAlphabet.size());
        this.hasBeenComputed = false;
    }

    @Override
    public void addSamples(Collection<? extends DefaultQuery<I, Word<O>>> samples) {
        for (DefaultQuery<I, Word<O>> sample : samples) {
            final Word<O> output = sample.getOutput();
            this.outputAlphabet.addAll(output.asList());
            buildPttOnward(root,
                           sample.getInput().asIntSeq(inputAlphabet),
                           IntQueue.asQueue(output.asIntSeq(outputAlphabet)));
        }
    }

    @Override
    public SubsequentialTransducer<?, I, ?, O> computeModel() {
        if (!hasBeenComputed) {
            hasBeenComputed = true;
            ostia(root);
        }
        return new OSSTWrapper<>(root, inputAlphabet, outputAlphabet);
    }

    public static State buildPtt(int alphabetSize, Iterator<Pair<IntSeq, IntSeq>> informant) {
        final State root = new State(alphabetSize);
        while (informant.hasNext()) {
            Pair<IntSeq, IntSeq> inout = informant.next();
            buildPttOnward(root, inout.getFirst(), IntQueue.asQueue(inout.getSecond()));
        }
        return root;
    }

    private static void buildPttOnward(State ptt, IntSeq input, IntQueue output) {
        State pttIter = ptt;
        IntQueue outputIter = output;

        for (int i = 0; i < input.size(); i++) {//input index
            final int symbol = input.get(i);
            final Edge edge;
            if (pttIter.transitions[symbol] == null) {
                edge = new Edge();
                edge.out = outputIter;
                edge.target = new State(pttIter.transitions.length);
                pttIter.transitions[symbol] = edge;
                outputIter = null;
            } else {
                edge = pttIter.transitions[symbol];
                IntQueue commonPrefixEdge = edge.out;
                IntQueue commonPrefixEdgePrev = null;
                IntQueue commonPrefixInformant = outputIter;
                while (commonPrefixEdge != null && commonPrefixInformant != null &&
                       commonPrefixEdge.value == commonPrefixInformant.value) {
                    commonPrefixInformant = commonPrefixInformant.next;
                    commonPrefixEdgePrev = commonPrefixEdge;
                    commonPrefixEdge = commonPrefixEdge.next;
                }
                /*
                informant=x
                edge.out=y
                ->
                informant=lcp(x,y)^-1 x
                edge=lcp(x,y)
                pushback=lcp(x,y)^-1 y
                */
                if (commonPrefixEdgePrev == null) {
                    edge.out = null;
                } else {
                    commonPrefixEdgePrev.next = null;
                }
                edge.target.prependButIgnoreMissingStateOutput(commonPrefixEdge);
                outputIter = commonPrefixInformant;
            }
            pttIter = edge.target;
        }
        if (pttIter.out != null && !IntQueue.eq(pttIter.out.str, outputIter)) {
            throw new IllegalArgumentException("For input '" + input + "' the state output is '" + pttIter.out.str +
                                               "' but training sample has remaining suffix '" + outputIter + '\'');
        }
        pttIter.out = new Out(outputIter);
    }

    private static void addBlueStates(State parent, Queue<Blue> blue) {
        for (int i = 0; i < parent.transitions.length; i++) {
            if (parent.transitions[i] != null) {
                assert !contains(blue, parent.transitions[i].target);
                assert parent.transitions[i].target != parent;
                blue.add(new Blue(parent, i));
            }
        }
    }

    public static void ostia(State transducer) {
        final Queue<Blue> blue = new LinkedList<>();
        final Set<State> red = new LinkedHashSet<>();
        assert isTree(transducer, new HashSet<>());
        red.add(transducer);
        addBlueStates(transducer, blue);
        assert uniqueItems(blue);
        assert disjoint(blue, red);
        assert validateBlueAndRed(transducer, red, blue);
        blue:
        while (!blue.isEmpty()) {
            final Blue next = blue.poll();
            final State blueState = next.state();
            assert isTree(blueState, new HashSet<>());
            assert uniqueItems(blue);
            assert !contains(blue, blueState);
            assert disjoint(blue, red);
            for (State redState : red) {
                if (ostiaMerge(next, redState, blue, red)) {
                    assert disjoint(blue, red);
                    assert uniqueItems(blue);
                    continue blue;
                }
            }
            assert isTree(blueState, new HashSet<>());
            assert uniqueItems(blue);
            addBlueStates(blueState, blue);
            assert uniqueItems(blue);
            assert !contains(blue, blueState);
            assert disjoint(blue, red);
            red.add(blueState);
            assert disjoint(blue, red);
            assert validateBlueAndRed(transducer, red, blue);
        }
    }

    private static boolean ostiaMerge(Blue blue, State redState, Queue<Blue> blueToVisit, Set<State> red) {
        final Map<State, State.Copy> merged = new HashMap<>();
        final List<Blue> reachedBlueStates = new ArrayList<>();
        if (ostiaFold(redState, null, blue.parent, blue.symbol, merged, reachedBlueStates)) {
            for (Map.Entry<State, State.Copy> mergedRedState : merged.entrySet()) {
                assert mergedRedState.getKey() == mergedRedState.getValue().original;
                mergedRedState.getValue().assign();
            }
            for (Blue reachedBlueCandidate : reachedBlueStates) {
                if (red.contains(reachedBlueCandidate.parent)) {
                    assert !contains(blueToVisit, reachedBlueCandidate.state());
                    blueToVisit.add(reachedBlueCandidate);
                }
            }
            return true;
        }
        return false;
    }

    private static boolean ostiaFold(State red,
                                     IntQueue pushedBack,
                                     State blueParent,
                                     int symbolIncomingToBlue,
                                     Map<State, State.Copy> mergedStates,
                                     List<Blue> reachedBlueStates) {
        final State blueState = blueParent.transitions[symbolIncomingToBlue].target;
        assert red != blueState;
        assert !mergedStates.containsKey(blueState);
        final State.Copy mergedRedState = mergedStates.computeIfAbsent(red, State.Copy::new);
        final State.Copy mergedBlueState = new State.Copy(blueState);
        mergedStates.computeIfAbsent(blueParent, State.Copy::new).transitions[symbolIncomingToBlue].target = red;
        final State prevBlue = mergedStates.put(blueState, mergedBlueState);
        assert prevBlue == null;
        mergedBlueState.prepend(pushedBack);
        if (mergedBlueState.out != null) {
            if (mergedRedState.out == null) {
                mergedRedState.out = mergedBlueState.out;
            } else if (!IntQueue.eq(mergedRedState.out.str, mergedBlueState.out.str)) {
                return false;
            }
        }
        for (int i = 0; i < mergedRedState.transitions.length; i++) {
            final Edge transitionBlue = mergedBlueState.transitions[i];
            if (transitionBlue != null) {
                final Edge transitionRed = mergedRedState.transitions[i];
                if (transitionRed == null) {
                    mergedRedState.transitions[i] = new Edge(transitionBlue);
                    reachedBlueStates.add(new Blue(red, i));
                } else {
                    IntQueue commonPrefixRed = transitionRed.out;
                    IntQueue commonPrefixBlue = transitionBlue.out;
                    IntQueue commonPrefixBluePrev = null;
                    while (commonPrefixBlue != null && commonPrefixRed != null &&
                           commonPrefixBlue.value == commonPrefixRed.value) {
                        commonPrefixBluePrev = commonPrefixBlue;
                        commonPrefixBlue = commonPrefixBlue.next;
                        commonPrefixRed = commonPrefixRed.next;
                    }
                    assert commonPrefixBluePrev == null || commonPrefixBluePrev.next == commonPrefixBlue;
                    if (commonPrefixRed == null) {
                        if (commonPrefixBluePrev == null) {
                            transitionBlue.out = null;
                        } else {
                            commonPrefixBluePrev.next = null;
                        }
                        assert mergedBlueState.transitions[i] != null;
                        assert mergedBlueState.transitions[i].target == blueState.transitions[i].target;
                        if (!ostiaFold(transitionRed.target,
                                       commonPrefixBlue,
                                       blueState,
                                       i,
                                       mergedStates,
                                       reachedBlueStates)) {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static @Nullable IntSeq run(State init, IntSeq input) {
        final List<Integer> output = new ArrayList<>();
        State iter = init;
        for (int i = 0; i < input.size(); i++) {
            final Edge edge = iter.transitions[input.get(i)];
            if (edge == null) {
                return null;
            }
            iter = edge.target;
            IntQueue q = edge.out;
            while (q != null) {
                output.add(q.value);
                q = q.next;
            }
        }
        if (iter.out == null) {
            return null;
        }
        IntQueue q = iter.out.str;
        while (q != null) {
            output.add(q.value);
            q = q.next;
        }
        return IntSeq.of(output);
    }

    // Assertion methods

    private static boolean disjoint(Queue<Blue> blue, Set<State> red) {
        for (Blue b : blue) {
            if (red.contains(b.state())) {
                return false;
            }
        }
        return true;
    }

    private static boolean contains(Queue<Blue> blue, State state) {
        for (Blue b : blue) {
            if (state.equals(b.state())) {
                return true;
            }
        }
        return false;
    }

    private static boolean uniqueItems(Queue<Blue> blue) {
        final Set<State> unique = new HashSet<>();
        for (Blue b : blue) {
            if (!unique.add(b.state())) {
                return false;
            }
        }
        return true;
    }

    private static boolean validateBlueAndRed(State root, Set<State> red, Queue<Blue> blue) {
        final Set<State> reachable = new HashSet<>();
        isTree(root, reachable);
        for (State r : red) {
            for (Edge edge : r.transitions) {
                assert edge == null || contains(blue, edge.target) ^ red.contains(edge.target);
            }
            assert reachable.contains(r);
        }
        for (Blue b : blue) {
            assert red.contains(b.parent);
            assert reachable.contains(b.state());
        }
        return true;
    }

    private static boolean isTree(State root, Set<State> nodes) {
        final Queue<State> toVisit = new LinkedList<>();
        toVisit.add(root);
        boolean isTree = true;
        while (!toVisit.isEmpty()) {
            final State s = toVisit.poll();
            if (nodes.add(s)) {
                for (Edge edge : s.transitions) {
                    if (edge != null) {
                        toVisit.add(edge.target);
                    }
                }
            } else {
                isTree = false;
            }

        }
        return isTree;
    }

}

