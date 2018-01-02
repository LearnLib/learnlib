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

import java.awt.Color;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.function.Consumer;

import com.google.common.collect.Sets;
import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.commons.util.Pair;
import net.automatalib.commons.util.array.RichArray;

public class RedBlueMerge<SP, TP, S extends AbstractBlueFringePTAState<SP, TP, S>> {

    private final AbstractBlueFringePTA<SP, TP, S> pta;
    private final RichArray<S>[] succMod;
    private final RichArray<TP>[] transPropMod;
    private final RichArray<SP> propMod;
    private final int alphabetSize;
    private final S qr;
    private final S qb;
    private boolean merged;

    @SuppressWarnings("unchecked")
    public RedBlueMerge(AbstractBlueFringePTA<SP, TP, S> pta, S qr, S qb) {
        if (!qr.isRed()) {
            throw new IllegalArgumentException("Merge target must be a red state");
        }
        if (!qb.isBlue()) {
            throw new IllegalArgumentException("Merge source must be a blue state");
        }

        this.pta = pta;

        int numRedStates = pta.getNumRedStates();
        this.succMod = new RichArray[numRedStates];
        this.transPropMod = new RichArray[numRedStates];
        this.propMod = new RichArray<>(numRedStates);
        this.alphabetSize = pta.alphabetSize;

        this.qr = qr;
        this.qb = qb;
    }

    public S getRedState() {
        return qr;
    }

    public S getBlueState() {
        return qb;
    }

    public boolean merge() {
        this.merged = true;
        if (!mergeRedProperties(qr, qb)) {
            return false;
        }
        updateRedTransition(qb.parent, qb.parentInput, qr);

        Deque<FoldRecord<S>> stack = new ArrayDeque<>();
        stack.push(new FoldRecord<>(qr, qb));

        FoldRecord<S> curr;
        while ((curr = stack.peek()) != null) {
            int i = ++curr.i;

            if (i == alphabetSize) {
                stack.pop();
                continue;
            }

            S q = curr.q;
            S r = curr.r;

            S rSucc = r.getSuccessor(i);
            if (rSucc != null) {
                S qSucc = getSucc(q, i);
                if (qSucc != null) {
                    if (qSucc.isRed()) {
                        if (!mergeRedProperties(qSucc, rSucc)) {
                            return false;
                        }
                    } else {
                        SP rSuccSP = rSucc.property, qSuccSP = qSucc.property;

                        SP newSP = null;
                        if (qSuccSP == null && rSuccSP != null) {
                            newSP = rSuccSP;
                        } else if (rSuccSP != null) { // && qSucc.property != null
                            if (!Objects.equals(qSuccSP, rSuccSP)) {
                                return false;
                            }
                        }

                        RichArray<TP> newTPs = null;
                        RichArray<TP> rSuccTPs = rSucc.transProperties;
                        RichArray<TP> qSuccTPs = qSucc.transProperties;

                        if (rSuccTPs != null) {
                            if (qSuccTPs != null) {
                                RichArray<TP> mergedTPs = mergeTransProperties(qSuccTPs, rSuccTPs);
                                if (mergedTPs == null) {
                                    return false;
                                } else if (mergedTPs != qSuccTPs) {
                                    newTPs = mergedTPs;
                                }
                            } else {
                                newTPs = rSuccTPs.clone();
                            }
                        }

                        if (newSP != null || newTPs != null) {
                            qSucc = cloneTopSucc(qSucc, i, stack, newTPs);
                            if (newSP != null) {
                                qSucc.property = newSP;
                            }
                        }
                    }

                    stack.push(new FoldRecord<>(qSucc, rSucc));
                } else {
                    if (q.isRed()) {
                        updateRedTransition(q, i, rSucc, r.getTransProperty(i));
                    } else {
                        q = cloneTop(q, stack);
                        assert q.isCopy;
                        q.setForeignSuccessor(i, rSucc, alphabetSize);
                    }
                }
            }
        }

        return true;
    }

    private S cloneTopSucc(S succ, int i, Deque<FoldRecord<S>> stack, RichArray<TP> newTPs) {
        S succClone = (newTPs != null) ? succ.copy(newTPs) : succ.copy();
        if (succClone == succ) {
            return succ;
        }
        S top = stack.peek().q;
        if (top.isRed()) {
            updateRedTransition(top, i, succClone);
        } else {
            S topClone = cloneTop(top, stack);
            topClone.setForeignSuccessor(i, succClone, alphabetSize);
        }
        return succClone;
    }

    private S cloneTop(S topState, Deque<FoldRecord<S>> stack) {
        assert !topState.isRed();

        S topClone = topState.copy();
        if (topClone == topState) {
            return topState;
        }
        S currTgt = topClone;

        Iterator<FoldRecord<S>> it = stack.iterator();
        FoldRecord<S> currRec = it.next();
        assert currRec.q == topState;
        currRec.q = topClone;

        assert it.hasNext();
        currRec = it.next();
        S currSrc = currRec.q;

        while (!currSrc.isRed()) {
            S currSrcClone = currSrc.copy();
            currSrcClone.successors.update(currRec.i, currTgt);
            if (currSrcClone == currSrc) {
                return topClone; // we're done
            }
            currRec.q = currSrcClone;
            currTgt = currSrcClone;

            assert it.hasNext();
            currRec = it.next();
            currSrc = currRec.q;
        }

        assert currSrc.isRed();
        updateRedTransition(currSrc, currRec.i, currTgt);

        return topClone;
    }

    private RichArray<TP> getTransProperties(S q) {
        if (q.isRed()) {
            int qId = q.id;
            RichArray<TP> props = transPropMod[qId];
            if (props != null) {
                return props;
            }
        }
        return q.transProperties;
    }

    private SP getStateProperty(S q) {
        if (q.isRed()) {
            int qId = q.id;
            SP prop = propMod.get(qId);
            if (prop != null) {
                return prop;
            }
        }
        return q.property;
    }

    private S getSucc(S q, int i) {
        if (q.isRed()) {
            int qId = q.id;
            RichArray<S> modSuccs = succMod[qId];
            if (modSuccs != null) {
                return modSuccs.get(i);
            }
        }
        return q.getSuccessor(i);
    }

    private void updateRedTransition(S redSrc, int input, S tgt) {
        updateRedTransition(redSrc, input, tgt, null);
    }

    private void updateRedTransition(S redSrc, int input, S tgt, TP transProp) {
        assert redSrc.isRed();

        int id = redSrc.id;
        RichArray<S> newSuccs = succMod[id];
        if (newSuccs == null) {
            if (redSrc.successors == null) {
                newSuccs = new RichArray<>(alphabetSize);
            } else {
                newSuccs = redSrc.successors.clone();
            }
            succMod[id] = newSuccs;
        }
        newSuccs.update(input, tgt);
        if (transProp != null) {
            RichArray<TP> newTransProps = transPropMod[id];
            if (newTransProps == null) {
                if (redSrc.transProperties == null) {
                    newTransProps = new RichArray<>(alphabetSize);
                } else {
                    newTransProps = redSrc.transProperties.clone();
                }
                transPropMod[id] = newTransProps;
            }
            newTransProps.update(input, transProp);
        }
    }

    private boolean mergeRedProperties(S qr, S qb) {
        return mergeRedStateProperty(qr, qb) && mergeRedTransProperties(qr, qb);
    }

    private boolean mergeRedTransProperties(S qr, S qb) {
        assert qr.isRed();

        RichArray<TP> qbProps = qb.transProperties;
        if (qbProps == null) {
            return true;
        }
        RichArray<TP> qrProps = getTransProperties(qr);
        RichArray<TP> mergedProps = qbProps;
        if (qrProps != null) {
            mergedProps = mergeTransProperties(qrProps, qbProps);
            if (mergedProps == null) {
                return false;
            }
        }
        if (mergedProps != qrProps) {
            transPropMod[qr.id] = mergedProps;
        }
        return true;
    }

    private boolean mergeRedStateProperty(S qr, S qb) {
        assert qr.isRed();

        SP qbProp = qb.property;
        if (qbProp == null) {
            return true;
        }
        SP qrProp = getStateProperty(qr);
        if (qrProp != null) {
            return Objects.equals(qbProp, qrProp);
        }
        propMod.update(qr.id, qbProp);
        return true;
    }

    /**
     * Merges two non-null transition property arrays. The behavior of this method is as follows: <ul> <li>if {@code
     * tps1} subsumes {@code tps2}, then {@code tps1} is returned.</li> <li>otherwise, if {@code tps1} and {@code tps2}
     * can be merged, a new {@link RichArray} containing the result of the merge is returned. <li>otherwise (i.e., if no
     * merge is possible), {@code null} is returned. </ul>
     */
    private RichArray<TP> mergeTransProperties(RichArray<TP> tps1, RichArray<TP> tps2) {
        int len = tps1.length;
        int i;

        RichArray<TP> tps1OrCopy = tps1;

        for (i = 0; i < len; i++) {
            TP tp1 = tps1OrCopy.get(i);
            TP tp2 = tps2.get(i);
            if (tp2 != null) {
                if (tp1 != null) {
                    if (!Objects.equals(tp1, tp2)) {
                        return null;
                    }
                } else {
                    tps1OrCopy = tps1.clone();
                    tps1OrCopy.update(i++, tp2);
                    break;
                }
            }
        }

        for (; i < len; i++) {
            TP tp1 = tps1OrCopy.get(i);
            TP tp2 = tps2.get(i);
            if (tp2 != null) {
                if (tp1 != null) {
                    if (!Objects.equals(tp1, tp2)) {
                        return null;
                    }
                } else {
                    tps1OrCopy.update(i, tp2);
                }
            }
        }

        return tps1OrCopy;
    }

    public void apply(AbstractBlueFringePTA<SP, TP, S> pta, Consumer<? super PTATransition<S>> newFrontierConsumer) {
        int alphabetSize = pta.alphabetSize;

        for (int i = 0; i < succMod.length; i++) {
            S redState = pta.redStates.get(i);
            assert redState.isRed();
            RichArray<S> newSuccs = succMod[i];
            if (newSuccs != null) {
                int len = newSuccs.length;
                for (int j = 0; j < len; j++) {
                    S newSucc = newSuccs.get(j);
                    if (newSucc != null) {
                        redState.setForeignSuccessor(j, newSucc, alphabetSize);
                        Color c = newSucc.getColor();
                        if (c != Color.RED) {
                            newSucc.parent = redState;
                            newSucc.parentInput = j;
                            incorporate(newSucc);
                            if (c != Color.BLUE) {
                                newFrontierConsumer.accept(newSucc.makeBlue());
                            }
                        }
                    }
                }
            }

            SP newProp = propMod.get(i);
            if (newProp != null) {
                redState.property = newProp;
            }
            RichArray<TP> newTransProps = transPropMod[i];
            if (newTransProps != null) {
                redState.transProperties = newTransProps;
            }
        }
    }

    private void incorporate(S state) {
        if (!state.isCopy) {
            return;
        }
        state.isCopy = false;
        Deque<S> queue = new ArrayDeque<>();
        queue.offer(state);

        S curr;

        while ((curr = queue.poll()) != null) {
            RichArray<S> succs = curr.successors;
            if (succs == null) {
                continue;
            }
            for (int i = 0; i < alphabetSize; i++) {
                S succ = succs.get(i);
                if (succ != null) {
                    succ.parent = curr;
                    succ.parentInput = i;
                    if (succ.isCopy) {
                        succ.isCopy = false;
                        queue.offer(succ);
                    }
                }
            }
        }
    }

    public UniversalDeterministicAutomaton<S, Integer, ?, SP, TP> toMergedAutomaton() {
        if (!this.merged) {
            throw new IllegalStateException("#merge has not been called yet");
        }

        return new UniversalDeterministicAutomaton<S, Integer, Pair<S, Integer>, SP, TP>() {

            private Set<S> states;

            @Override
            public S getSuccessor(Pair<S, Integer> transition) {
                final S source = transition.getFirst();
                final Integer input = transition.getSecond();

                if (source.isRed() && succMod[source.id] != null) {
                    return succMod[source.id].get(input);
                }

                return pta.getSuccessor(source, input);
            }

            @Override
            public SP getStateProperty(S state) {
                if (state.isRed() && propMod.get(state.id) != null) {
                    return propMod.get(state.id);
                }

                return state.getStateProperty();
            }

            @Override
            public TP getTransitionProperty(Pair<S, Integer> transition) {
                final S source = transition.getFirst();
                final Integer input = transition.getSecond();

                if (source.isRed() && transPropMod[source.id] != null) {
                    return transPropMod[source.id].get(input);
                }

                return source.transProperties.get(input);
            }

            @Override
            public Pair<S, Integer> getTransition(S state, Integer input) {
                return new Pair<>(state, input);
            }

            @Override
            public Collection<S> getStates() {

                if (states != null) {
                    return states;
                }

                states = Sets.newHashSetWithExpectedSize(pta.size());
                final Queue<S> discoverQueue = new ArrayDeque<>();

                discoverQueue.add(getInitialState());

                S iter;

                while ((iter = discoverQueue.poll()) != null) {
                    states.add(iter);

                    for (int i = 0; i < alphabetSize; i++) {
                        final S succ = getSuccessor(iter, i);

                        if (succ != null && !states.contains(succ)) {
                            discoverQueue.add(succ);
                        }
                    }
                }

                return states;
            }

            @Override
            public S getInitialState() {
                return pta.getInitialState();
            }
        };
    }

    static final class FoldRecord<S extends AbstractBlueFringePTAState<?, ?, S>> {

        public final S r;
        public S q;
        public int i = -1;

        FoldRecord(S q, S r) {
            this.q = q;
            this.r = r;
        }
    }
}