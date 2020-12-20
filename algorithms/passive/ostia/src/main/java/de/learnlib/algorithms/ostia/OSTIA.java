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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import de.learnlib.api.algorithm.PassiveLearningAlgorithm;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.automata.transducers.SubsequentialTransducer;
import net.automatalib.commons.smartcollections.IntSeq;
import net.automatalib.commons.util.Pair;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * @author Aleksander Mendoza-Drosik
 * @author frohme
 */
public class OSTIA<I, O> implements PassiveLearningAlgorithm<SubsequentialTransducer<?, I, ?, O>, I, Word<O>> {

    private final Alphabet<I> inputAlphabet;
    private final Alphabet<O> outputAlphabet;
    private final State root;

    public OSTIA(Alphabet<I> inputAlphabet, Alphabet<O> outputAlphabet) {
        this.inputAlphabet = inputAlphabet;
        this.outputAlphabet = outputAlphabet;
        this.root = new State(inputAlphabet.size());
    }

    @Override
    public void addSamples(Collection<? extends DefaultQuery<I, Word<O>>> samples) {
        for (DefaultQuery<I, Word<O>> sample : samples) {
            buildPttOnward(root,
                           IntSeq.of(sample.getInput(), inputAlphabet),
                           IntQueue.asQueue(IntSeq.of(sample.getOutput(), outputAlphabet)));
        }
    }

    @Override
    public SubsequentialTransducer<?, I, ?, O> computeModel() {
        ostia(root);
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

    private static void addBlueStates(State parent, java.util.Queue<Blue> blue) {
        for (int i = 0; i < parent.transitions.length; i++) {
            if (parent.transitions[i] != null) {
                blue.add(new Blue(parent, i));
            }
        }
    }

    static Edge[] copyTransitions(Edge[] transitions) {
        final Edge[] copy = new Edge[transitions.length];
        for (int i = 0; i < copy.length; i++) {
            copy[i] = transitions[i] == null ? null : new Edge(transitions[i]);
        }
        return copy;
    }

    public static void ostia(State transducer) {
        final Queue<Blue> blue = new LinkedList<>();
        final List<State> red = new ArrayList<>();
        red.add(transducer);
        addBlueStates(transducer, blue);
        blue:
        while (!blue.isEmpty()) {
            final Blue next = blue.poll();
            final State blueState = next.state();
            for (State redState : red) {
                if (ostiaMerge(next, redState, blue)) {
                    continue blue;
                }
            }
            addBlueStates(blueState, blue);
            red.add(blueState);
        }
    }

    private static boolean ostiaMerge(Blue blue, State redState, java.util.Queue<Blue> blueToVisit) {
        final Map<State, State> merged = new HashMap<>();
        final List<Blue> reachedBlueStates = new ArrayList<>();
        if (ostiaFold(redState, null, blue.parent, blue.symbol, merged, reachedBlueStates)) {
            for (Map.Entry<State, State> mergedRedState : merged.entrySet()) {
                mergedRedState.getKey().assign(mergedRedState.getValue());
            }
            blueToVisit.addAll(reachedBlueStates);
            return true;
        }
        return false;
    }

    private static boolean ostiaFold(State red,
                                     IntQueue pushedBack,
                                     State blueParent,
                                     int symbolIncomingToBlue,
                                     Map<State, State> mergedStates,
                                     List<Blue> reachedBlueStates) {
        final State mergedRedState = mergedStates.computeIfAbsent(red, State::new);
        final State blueState = blueParent.transitions[symbolIncomingToBlue].target;
        final State mergedBlueState = new State(blueState);
        assert !mergedStates.containsKey(blueState);
        mergedStates.computeIfAbsent(blueParent, State::new).transitions[symbolIncomingToBlue].target = red;
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
                    reachedBlueStates.add(new Blue(blueState, i));
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
                        if (!ostiaFold(transitionRed.target,
                                       commonPrefixBlue,
                                       mergedBlueState,
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

}

