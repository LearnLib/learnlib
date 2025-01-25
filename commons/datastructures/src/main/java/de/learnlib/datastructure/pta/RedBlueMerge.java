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
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.function.Consumer;

import net.automatalib.automaton.UniversalDeterministicAutomaton;
import net.automatalib.common.util.array.ArrayStorage;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class RedBlueMerge<S extends AbstractBlueFringePTAState<S, SP, TP>, SP, TP> {

    private final AbstractBlueFringePTA<S, SP, TP> pta;
    private final ArrayStorage<ArrayStorage<S>> succMod;
    private final ArrayStorage<ArrayStorage<TP>> transPropMod;
    private final ArrayStorage<SP> propMod;
    private final int alphabetSize;
    private final S qr;
    private final S qb;

    public RedBlueMerge(AbstractBlueFringePTA<S, SP, TP> pta, S qr, S qb) {
        this(pta, qr, qb, validateInputs(pta, qr, qb));
    }

    // utility constructor to prevent finalizer attacks, see SEI CERT Rule OBJ-11
    private RedBlueMerge(AbstractBlueFringePTA<S, SP, TP> pta, S qr, S qb, int numRedStates) {
        this.pta = pta;
        this.succMod = new ArrayStorage<>(numRedStates);
        this.transPropMod = new ArrayStorage<>(numRedStates);
        this.propMod = new ArrayStorage<>(numRedStates);
        this.alphabetSize = pta.getInputAlphabet().size();

        this.qr = qr;
        this.qb = qb;
    }

    private static <S extends AbstractBlueFringePTAState<S, SP, TP>, SP, TP> int validateInputs(AbstractBlueFringePTA<S, SP, TP> pta,
                                                                                                S qr,
                                                                                                S qb) {
        if (!qr.isRed()) {
            throw new IllegalArgumentException("Merge target must be a red state");
        }
        if (!qb.isBlue()) {
            throw new IllegalArgumentException("Merge source must be a blue state");
        }

        return pta.getNumRedStates();
    }

    public S getRedState() {
        return qr;
    }

    public S getBlueState() {
        return qb;
    }

    public boolean merge() {
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
                        } else if (rSuccSP != null && !rSuccSP.equals(qSuccSP)) {
                            return false;
                        }

                        ArrayStorage<TP> newTPs = null;
                        ArrayStorage<TP> rSuccTPs = rSucc.transProperties;
                        ArrayStorage<TP> qSuccTPs = qSucc.transProperties;

                        if (rSuccTPs != null) {
                            if (qSuccTPs != null) {
                                ArrayStorage<TP> mergedTPs = mergeTransProperties(qSuccTPs, rSuccTPs);
                                if (mergedTPs == null) {
                                    return false;
                                } else if (mergedTPs != qSuccTPs) {
                                    newTPs = mergedTPs;
                                }
                            } else {
                                newTPs = new ArrayStorage<>(rSuccTPs);
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

    private S cloneTopSucc(S succ, int i, Deque<FoldRecord<S>> stack, @Nullable ArrayStorage<TP> newTPs) {
        S succClone = (newTPs != null) ? succ.copy(newTPs) : succ.copy();
        FoldRecord<S> peek = stack.peek();
        assert peek != null;
        S top = peek.q;
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
            assert currSrcClone.successors != null;
            currSrcClone.successors.set(currRec.i, currTgt);
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

    private @Nullable ArrayStorage<TP> getTransProperties(S q) {
        if (q.isRed()) {
            int qId = q.id;
            ArrayStorage<TP> props = transPropMod.get(qId);
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

    private @Nullable S getSucc(S q, int i) {
        if (q.isRed()) {
            int qId = q.id;
            ArrayStorage<S> modSuccs = succMod.get(qId);
            if (modSuccs != null) {
                return modSuccs.get(i);
            }
        }
        return q.getSuccessor(i);
    }

    private void updateRedTransition(S redSrc, int input, S tgt) {
        updateRedTransition(redSrc, input, tgt, null);
    }

    private void updateRedTransition(S redSrc, int input, S tgt, @Nullable TP transProp) {
        assert redSrc.isRed();

        int id = redSrc.id;
        ArrayStorage<S> newSuccs = succMod.get(id);
        if (newSuccs == null) {
            if (redSrc.successors == null) {
                newSuccs = new ArrayStorage<>(alphabetSize);
            } else {
                newSuccs = new ArrayStorage<>(redSrc.successors);
            }
            succMod.set(id, newSuccs);
        }
        newSuccs.set(input, tgt);
        if (transProp != null) {
            ArrayStorage<TP> newTransProps = transPropMod.get(id);
            if (newTransProps == null) {
                if (redSrc.transProperties == null) {
                    newTransProps = new ArrayStorage<>(alphabetSize);
                } else {
                    newTransProps = new ArrayStorage<>(redSrc.transProperties);
                }
                transPropMod.set(id, newTransProps);
            }
            newTransProps.set(input, transProp);
        }
    }

    private boolean mergeRedProperties(S qr, S qb) {
        return mergeRedStateProperty(qr, qb) && mergeRedTransProperties(qr, qb);
    }

    private boolean mergeRedTransProperties(S qr, S qb) {
        assert qr.isRed();

        ArrayStorage<TP> qbProps = qb.transProperties;
        if (qbProps == null) {
            return true;
        }
        ArrayStorage<TP> qrProps = getTransProperties(qr);
        ArrayStorage<TP> mergedProps = qbProps;
        if (qrProps != null) {
            mergedProps = mergeTransProperties(qrProps, qbProps);
            if (mergedProps == null) {
                return false;
            }
        }
        if (mergedProps != qrProps) {
            transPropMod.set(qr.id, mergedProps);
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
        propMod.set(qr.id, qbProp);
        return true;
    }

    /**
     * Merges two non-null transition property arrays. The behavior of this method is as follows:
     * <ul>
     *     <li>if {@code tps1} subsumes {@code tps2}, then {@code tps1} is returned.</li>
     *     <li>otherwise, if {@code tps1} and {@code tps2} can be merged, a new {@link ArrayStorage} containing the result of the merge is returned.</li>
     *     <li>otherwise (i.e., if no merge is possible), {@code null} is returned.</li>
     * </ul>
     */
    @SuppressWarnings("PMD.ReturnEmptyCollectionRatherThanNull") // null is semantically different from an empty list
    private @Nullable ArrayStorage<TP> mergeTransProperties(ArrayStorage<TP> tps1, ArrayStorage<TP> tps2) {
        int len = tps1.size();
        int i;

        ArrayStorage<TP> tps1OrCopy = tps1;

        for (i = 0; i < len; i++) {
            TP tp1 = tps1OrCopy.get(i);
            TP tp2 = tps2.get(i);
            if (tp2 != null) {
                if (tp1 != null) {
                    if (!Objects.equals(tp1, tp2)) {
                        return null;
                    }
                } else {
                    tps1OrCopy = new ArrayStorage<>(tps1);
                    tps1OrCopy.set(i++, tp2);
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
                    tps1OrCopy.set(i, tp2);
                }
            }
        }

        return tps1OrCopy;
    }

    public void apply(AbstractBlueFringePTA<S, SP, TP> pta, Consumer<? super PTATransition<S>> newFrontierConsumer) {
        int alphabetSize = pta.getInputAlphabet().size();

        for (int i = 0; i < succMod.size(); i++) {
            S redState = pta.redStates.get(i);
            assert redState.isRed();
            ArrayStorage<S> newSuccs = succMod.get(i);
            if (newSuccs != null) {
                int len = newSuccs.size();
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
            ArrayStorage<TP> newTransProps = transPropMod.get(i);
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
            ArrayStorage<S> succs = curr.successors;
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

    /**
     * Returns an automaton-based view of the merge. If the merge was not yet {@link #merge() tried}, this view is equal
     * to the unmodified PTA.
     *
     * @return the automaton-based view of this merge
     */
    public UniversalDeterministicAutomaton<S, Integer, ?, SP, TP> toMergedAutomaton() {
        return new UniversalDeterministicAutomaton<S, Integer, PTATransition<S>, SP, TP>() {

            private @MonotonicNonNull Set<S> states;

            @Override
            public S getSuccessor(PTATransition<S> transition) {
                return Objects.requireNonNull(RedBlueMerge.this.getSucc(transition.getSource(), transition.getIndex()));
            }

            @Override
            public SP getStateProperty(S state) {
                return RedBlueMerge.this.getStateProperty(state);
            }

            @Override
            public TP getTransitionProperty(PTATransition<S> transition) {
                ArrayStorage<TP> properties = RedBlueMerge.this.getTransProperties(transition.getSource());

                if (properties != null) {
                    return properties.get(transition.getIndex());
                }

                return null;
            }

            @Override
            public @Nullable PTATransition<S> getTransition(S state, Integer input) {
                final S succ = RedBlueMerge.this.getSucc(state, input);
                return succ == null ? null : new PTATransition<>(state, input);
            }

            @Override
            public Collection<S> getStates() {

                if (states != null) {
                    return states;
                }

                states = new LinkedHashSet<>();
                final Queue<S> discoverQueue = new ArrayDeque<>();

                S initialState = getInitialState();
                assert initialState != null;
                discoverQueue.add(initialState);
                states.add(initialState);

                S iter;

                while ((iter = discoverQueue.poll()) != null) {
                    for (int i = 0; i < alphabetSize; i++) {
                        final S succ = getSuccessor(iter, i);

                        if (succ != null && states.add(succ)) {
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

    static final class FoldRecord<S extends AbstractBlueFringePTAState<S, ?, ?>> {

        public final S r;
        public S q;
        public int i = -1;

        FoldRecord(S q, S r) {
            this.q = q;
            this.r = r;
        }
    }
}
