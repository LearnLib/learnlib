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

import java.util.*;

import de.learnlib.api.algorithm.PassiveLearningAlgorithm;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.automata.transducers.impl.compact.CompactMealyTransition;
import net.automatalib.automata.transducers.impl.compact.CompactSST;
import net.automatalib.automata.transducers.impl.compact.SubsequentialTransducer;
import net.automatalib.commons.util.Pair;
import net.automatalib.visualization.Visualization;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

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
        final State root = new State(alphabetSize);
        this.samples.sort(Comparator.comparingInt(a -> a.getInput().length()));//these samples must be sorted
        //with respect to length or otherwise building prefix-tree-transducer won't work
        for (DefaultQuery<I, Word<O>> sample : this.samples) {
            final IntSeq inSeq = IntSeq.seq(sample.getInput().stream().mapToInt(inputAlphabet).toArray());
            final IntSeq outSeq = IntSeq.seq(sample.getOutput().stream().mapToInt(outputAlphabet).toArray());
            buildPttOnward(root, inSeq, asQueue(outSeq, 0));
        }
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
            throw new IllegalArgumentException("For input "+input.toString()+" the state output is "+IntQueue.str(ptt.out.str)+" but training sample has remaining suffix "+IntQueue.str(output));
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

    public static Word<String> dequeueLongestCommonPrefix(CompactSST<Character, String> automaton,int stateIdx) {
        Word<String> lcp = automaton.getStateProperty(stateIdx);
        for (int symbol=0;symbol<automaton.numInputs();symbol++) {
            @Nullable CompactMealyTransition<Word<String>> outgoing = automaton.getTransition(stateIdx,symbol);
            if(outgoing==null)continue;
            if (lcp == null){
                lcp = outgoing.getOutput();
            }else{
                lcp = outgoing.getOutput().longestCommonPrefix(lcp);
            }
        }
        if(lcp==null||lcp.length()==0)return null;
        final Word<String> stateOut = automaton.getStateProperty(stateIdx);
        if(stateOut!=null){
            automaton.setStateProperty(stateIdx,stateOut.subWord(lcp.length()));
        }
        for (int symbol=0;symbol<automaton.numInputs();symbol++) {
            @Nullable CompactMealyTransition<Word<String>> outgoing = automaton.getTransition(stateIdx, symbol);
            if (outgoing != null) {
                automaton.setStateProperty(stateIdx,outgoing.getOutput().subWord(lcp.length()));
            }
        }
        return lcp;
    }


    public static void onwardForm(CompactSST<Character, String> automaton) {
        final HashMap<Integer,ArrayList<Pair<Integer,Integer>>> allStates = new HashMap<>();
        for(int source:automaton.getStates()){
            allStates.put(source,new ArrayList<>());
        }
        for(int source:automaton.getStates()){
            for(int symbol=0;symbol<automaton.numInputs();symbol++){
                final @Nullable CompactMealyTransition<Word<String>> outgoing = automaton.getTransition(source,symbol);
                if(outgoing!=null){
                    allStates.get(outgoing.getSuccId()).add(Pair.of(symbol,source));
                }
            }
        }
        final Queue<Integer> modified = new LinkedList<>();
        final HashSet<Integer> modifiedLookup = new HashSet<>();
        for(Map.Entry<Integer, ArrayList<Pair<Integer, Integer>>> stateAndReversedTransitions:allStates.entrySet()){
            final int target = stateAndReversedTransitions.getKey();
            final Word<String> dequeued = dequeueLongestCommonPrefix(automaton,target);
            if(dequeued!=null){
                for(Pair<Integer, Integer> symbolAndSource: stateAndReversedTransitions.getValue()){
                    final int symbol = symbolAndSource.getFirst();
                    final int source = symbolAndSource.getSecond();
                    final @Nullable CompactMealyTransition<Word<String>> incoming = automaton.getTransition(source,symbol);
                    assert incoming!=null;
                    automaton.setTransitionProperty(incoming,incoming.getOutput().concat(dequeued));
                    if(modifiedLookup.add(source))modified.add(source);
                }
            }
        }
        while(!modified.isEmpty()){
            final int target = modified.poll();
            modifiedLookup.remove(target);
            final Word<String> dequeued = dequeueLongestCommonPrefix(automaton,target);
            if(dequeued!=null){
                for(Pair<Integer, Integer> symbolAndSource: allStates.get(target)){
                    final int symbol = symbolAndSource.getFirst();
                    final int source = symbolAndSource.getSecond();
                    final @Nullable CompactMealyTransition<Word<String>> incoming = automaton.getTransition(source,symbol);
                    assert incoming!=null;
                    automaton.setTransitionProperty(incoming,incoming.getOutput().concat(dequeued));
                    if(modifiedLookup.add(source))modified.add(source);
                }
            }
        }
    }

    public static void onwardForm(State transducer) {
        final HashMap<State,ArrayList<Pair<Integer,State>>> allStates = new HashMap<>();
        final Stack<State> toVisit = new Stack<>();
        toVisit.push(transducer);
        allStates.put(transducer,new ArrayList<>());
        while(!toVisit.isEmpty()){
            final State s = toVisit.pop();
            for(Edge outgoing:s.transitions){
                if(outgoing!=null && !allStates.containsKey(outgoing.target)){
                    allStates.put(outgoing.target,new ArrayList<>());
                    toVisit.push(outgoing.target);
                }
            }
        }
        for(State source:allStates.keySet()){
            for(int symbol=0;symbol<source.transitions.length;symbol++){
                final Edge outgoing = source.transitions[symbol];
                if(outgoing!=null){
                    allStates.get(outgoing.target).add(Pair.of(symbol,source));
                }
            }
        }
        final Queue<State> modified = new LinkedList<>();
        final HashSet<State> modifiedLookup = new HashSet<>();
        for(Map.Entry<State, ArrayList<Pair<Integer, State>>> stateAndReversedTransitions:allStates.entrySet()){
            final State target = stateAndReversedTransitions.getKey();
            final IntQueue dequeued = target.dequeueLongestCommonPrefix();
            if(dequeued!=null){
                for(Pair<Integer, State> symbolAndSource: stateAndReversedTransitions.getValue()){
                    final int symbol = symbolAndSource.getFirst();
                    final State source = symbolAndSource.getSecond();
                    source.transitions[symbol].out = IntQueue.concatAndCopy(source.transitions[symbol].out,dequeued);
                    if(modifiedLookup.add(source))modified.add(source);
                }
            }
        }
        while(!modified.isEmpty()){
            final State target = modified.poll();
            modifiedLookup.remove(target);
            final IntQueue dequeued = target.dequeueLongestCommonPrefix();
            if(dequeued!=null) {
                for (Pair<Integer, State> symbolAndSource : allStates.get(target)) {
                    final int symbol = symbolAndSource.getFirst();
                    final State source = symbolAndSource.getSecond();
                    source.transitions[symbol].out = IntQueue.concatAndCopy(source.transitions[symbol].out, dequeued);
                    if (modifiedLookup.add(source)) modified.add(source);
                }
            }
        }
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

