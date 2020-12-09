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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.learnlib.api.algorithm.PassiveLearningAlgorithm;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.automata.transducers.impl.compact.SubsequentialTransducer;
import net.automatalib.commons.util.Pair;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

public class OSTIA<I, O> implements PassiveLearningAlgorithm<SubsequentialTransducer<?, I, ?, O>, I, Word<O>> {

    private final Alphabet<I> inputAlphabet;
    private final Alphabet<O> outputAlphabet;
    private final int alphabetSize;

    private final List<DefaultQuery<I, Word<O>>> samples;

    public OSTIA(Alphabet<I> inputAlphabet, Alphabet<O> outputAlphabet) {
        this.inputAlphabet = inputAlphabet;
        this.outputAlphabet = outputAlphabet;
        this.alphabetSize = inputAlphabet.size();
        this.samples = new ArrayList<>();
    }

    @Override
    public void addSamples(Collection<? extends DefaultQuery<I, Word<O>>> samples) {
        this.samples.addAll(samples);
    }

    @Override
    public SubsequentialTransducer<?, I, ?, O> computeModel() {
        final List<Pair<IntSeq, IntSeq>> informant = new ArrayList<>(this.samples.size());

        for (DefaultQuery<I, Word<O>> sample : this.samples) {
            final IntSeq inSeq = IntSeq.seq(sample.getInput().stream().mapToInt(inputAlphabet).toArray());
            final IntSeq outSeq = IntSeq.seq(sample.getOutput().stream().mapToInt(outputAlphabet).toArray());
            informant.add(Pair.of(inSeq, outSeq));
        }

        final State root = buildPtt(this.alphabetSize, informant.iterator());
        ostia(root);
        return new OSSTWrapper<>(root, inputAlphabet, outputAlphabet);

    }

    public static boolean hasCycle(IntQueue q) {
        final HashSet<IntQueue> elements = new HashSet<>();
        while (q != null) {
            if (!elements.add(q)) {
                return true;
            }
            q = q.next;
        }
        return false;
    }

    static IntQueue concat(IntQueue q, IntQueue tail) {
        assert !hasCycle(q) && !hasCycle(tail);
        if (q == null) {
            return tail;
        }
        final IntQueue first = q;
        while (q.next != null) {
            q = q.next;
        }
        q.next = tail;
        assert !hasCycle(first);
        return first;
    }

    static IntQueue copyAndConcat(IntQueue q, IntQueue tail) {
        assert !hasCycle(q) && !hasCycle(tail);
        if (q == null) {
            return tail;
        }
        final IntQueue root = new IntQueue();
        root.value = q.value;
        IntQueue curr = root;
        q = q.next;
        while (q != null) {
            curr.next = new IntQueue();
            curr = curr.next;
            curr.value = q.value;
            q = q.next;
        }
        curr.next = tail;
        assert !hasCycle(root);
        return root;
    }

    /**
     * builds onward prefix tree transducer
     */
    private static void buildPttOnward(State ptt, IntSeq input, IntQueue output) {
        for (int i = 0; i < input.size(); i++) {//input index
            final int symbol = input.get(i);
            if (ptt.transitions[symbol] == null) {
                final Edge edge = ptt.transitions[symbol] = new Edge();
                edge.out = output;
                output = null;
                ptt = edge.target = new State(ptt.transitions.length);
            } else {
                final Edge edge = ptt.transitions[symbol];
                IntQueue commonPrefixEdge = edge.out;
                IntQueue commonPrefixEdgePrev = null;
                IntQueue commonPrefixInformant = output;
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
                edge.target.prepend(commonPrefixEdge);
                output = commonPrefixInformant;
                ptt = edge.target;
            }
        }
        if (ptt.out != null && !eq(ptt.out.str, output)) {
            throw new IllegalArgumentException();
        }
        ptt.out = new Out(output);
    }

    private static boolean eq(IntQueue a, IntQueue b) {
        while (a != null && b != null) {
            if (a.value != b.value) {
                return false;
            }
            a = a.next;
            b = b.next;
        }
        return a == null && b == null;
    }

    private static IntQueue asQueue(IntSeq str, int offset) {
        IntQueue q = null;
        for (int i = str.size() - 1; i >= offset; i--) {
            IntQueue next = new IntQueue();
            next.value = str.get(i);
            next.next = q;
            q = next;
        }
        assert !hasCycle(q);
        return q;
    }

    public static State buildPtt(int alphabetSize, Iterator<Pair<IntSeq, IntSeq>> informant) {
        final State root = new State(alphabetSize);
        while (informant.hasNext()) {
            Pair<IntSeq, IntSeq> inout = informant.next();
            buildPttOnward(root, inout.getFirst(), asQueue(inout.getSecond(), 0));
        }
        return root;
    }

    static void addBlueStates(State parent, java.util.Queue<Blue> blue) {
        for (int i = 0; i < parent.transitions.length; i++) {
            if (parent.transitions[i] != null) { blue.add(new Blue(parent, i)); }
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
        final java.util.Queue<Blue> blue = new LinkedList<>();
        final ArrayList<State> red = new ArrayList<>();
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
        final HashMap<State, State> merged = new HashMap<>();
        final ArrayList<Blue> reachedBlueStates = new ArrayList<>();
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
                                     HashMap<State, State> mergedStates,
                                     ArrayList<Blue> reachedBlueStates) {
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
            } else if (!eq(mergedRedState.out.str, mergedBlueState.out.str)) {
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
                    assert commonPrefixBluePrev == null ?
                            commonPrefixBlue == transitionBlue.out :
                            commonPrefixBluePrev.next == commonPrefixBlue;
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

    static ArrayList<Integer> run(State init, Iterator<Integer> input) {
        ArrayList<Integer> output = new ArrayList<>();
        while (input.hasNext()) {
            final Edge edge = init.transitions[input.next()];
            if (edge == null) {
                return null;
            }
            init = edge.target;
            IntQueue q = edge.out;
            while (q != null) {
                output.add(q.value);
                q = q.next;
            }
        }
        if (init.out == null) {
            return null;
        }
        IntQueue q = init.out.str;
        while (q != null) {
            output.add(q.value);
            q = q.next;
        }
        return output;
    }

}

