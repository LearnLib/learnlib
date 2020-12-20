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
package net.automatalib.automata.transducers;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import net.automatalib.commons.util.Pair;
import net.automatalib.commons.util.mappings.Mapping;
import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.util.automata.Automata;
import net.automatalib.util.automata.copy.AutomatonCopyMethod;
import net.automatalib.util.automata.copy.AutomatonLowLevelCopy;
import net.automatalib.words.Word;

/**
 * Utility methods of {@link SubsequentialTransducer}s.
 *
 * @author frohme
 */
public final class SubsequentialTransducers {

    private SubsequentialTransducers() {
        // prevent initialization
    }

    /**
     * Constructs a new <i>onward</i> subsequential transducer for a given {@link SubsequentialTransducer SST}. In an
     * onward SST, for each state except the initial state, the longest common prefix over the state output and the
     * outputs of all outgoing transitions of a state is {@link Word#epsilon() epsilon}. This can be achieved by pushing
     * back the longest common prefix to the transition outputs of the incoming transitions of each state.
     *
     * @param sst
     *         the original SST
     * @param inputs
     *         the alphabet symbols to consider for this transformation
     * @param out
     *         the target automaton to write the onward form to
     *
     * @return {@code out}, for convenience
     */
    public static <S1, S2, I, T1, T2, O, A extends MutableSubsequentialTransducer<S2, I, T2, O>> A toOnwardSST(
            SubsequentialTransducer<S1, I, T1, O> sst,
            Collection<? extends I> inputs,
            A out) {
        return toOnwardSST(sst, inputs, out, true);
    }

    /**
     * Constructs a new <i>onward</i> subsequential transducer for a given {@link SubsequentialTransducer SST}. In an
     * onward SST, for each state except the initial state, the longest common prefix over the state output and the
     * outputs of all outgoing transitions of a state is {@link Word#epsilon() epsilon}. This can be achieved by pushing
     * back the longest common prefix to the transition outputs of the incoming transitions of each state.
     *
     * @param sst
     *         the original SST
     * @param inputs
     *         the alphabet symbols to consider for this transformation
     * @param out
     *         the target automaton to write the onward form to
     * @param minimize
     *         a flag indicating whether the final result should be minimized
     *
     * @return {@code out}, for convenience
     */
    public static <S1, S2, I, T1, T2, O, A extends MutableSubsequentialTransducer<S2, I, T2, O>> A toOnwardSST(
            SubsequentialTransducer<S1, I, T1, O> sst,
            Collection<? extends I> inputs,
            A out,
            boolean minimize) {

        assert out.size() == 0;
        AutomatonLowLevelCopy.copy(AutomatonCopyMethod.STATE_BY_STATE, sst, inputs, out);

        final Mapping<S2, Set<Pair<S2, I>>> incomingTransitions = getIncomingTransitions(out, inputs);
        final Deque<S2> queue = new ArrayDeque<>(out.getStates());

        final S2 oldInit = out.getInitialState();

        if (!incomingTransitions.get(oldInit).isEmpty()) {
            // copy initial state to prevent push-back of prefixes for the initial state.
            out.setInitial(oldInit, false);
            final S2 newInit = out.addInitialState(out.getStateProperty(oldInit));

            for (I i : inputs) {
                T2 oldT = out.getTransition(oldInit, i);
                if (oldT != null) {
                    final S2 succ = out.getSuccessor(oldT);
                    out.addTransition(newInit, i, succ, out.getTransitionProperty(oldT));
                    incomingTransitions.get(succ).add(Pair.of(newInit, i));
                }
            }
        }

        while (!queue.isEmpty()) {
            final S2 s = queue.pop();
            if (Objects.equals(s, out.getInitialState())) {
                continue;
            }
            final Word<O> lcp = computeLCP(out, inputs, s);

            if (!lcp.isEmpty()) {
                final Word<O> oldStateProperty = out.getStateProperty(s);
                final Word<O> newStateProperty = oldStateProperty.subWord(lcp.length());

                out.setStateProperty(s, newStateProperty);

                for (I i : inputs) {
                    final T2 t = out.getTransition(s, i);
                    if (t != null) {
                        final Word<O> oldTransitionProperty = out.getTransitionProperty(t);
                        final Word<O> newTransitionProperty = oldTransitionProperty.subWord(lcp.length());

                        out.setTransitionProperty(t, newTransitionProperty);
                    }
                }

                for (Pair<S2, I> trans : incomingTransitions.get(s)) {
                    final T2 t = out.getTransition(trans.getFirst(), trans.getSecond());

                    final Word<O> oldTransitionProperty = out.getTransitionProperty(t);
                    final Word<O> newTransitionProperty = oldTransitionProperty.concat(lcp);

                    out.setTransitionProperty(t, newTransitionProperty);
                    if (!queue.contains(trans.getFirst())) {
                        queue.add(trans.getFirst());
                    }
                }
            }
        }

        return minimize ? Automata.invasiveMinimize(out, inputs) : out;
    }

    /**
     * Checks whether a given {@link SubsequentialTransducer} is <i>onward</i>, i.e. if for each state (sans initial
     * state) the longest common prefix over its state property and the transition properties of its outgoing edges
     * equals {@link Word#epsilon() epsilon}.
     *
     * @param sst
     *         the SST to check
     * @param inputs
     *         the input symbols to consider for this check
     *
     * @return {@code true} if {@code sst} is onward, {@code false} otherwise
     */
    public static <S, I, T, O> boolean isOnwardSST(SubsequentialTransducer<S, I, T, O> sst,
                                                   Collection<? extends I> inputs) {

        for (S s : sst) {
            if (Objects.equals(s, sst.getInitialState())) {
                continue;
            }

            final Word<O> lcp = computeLCP(sst, inputs, s);
            if (!lcp.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    private static <S, I, T> Mapping<S, Set<Pair<S, I>>> getIncomingTransitions(SubsequentialTransducer<S, I, T, ?> sst,
                                                                                Collection<? extends I> inputs) {

        final MutableMapping<S, Set<Pair<S, I>>> result = sst.createStaticStateMapping();

        for (S s : sst) {
            result.put(s, new HashSet<>());
        }

        for (S s : sst) {
            for (I i : inputs) {
                final T t = sst.getTransition(s, i);

                if (t != null) {
                    final S succ = sst.getSuccessor(t);
                    result.get(succ).add(Pair.of(s, i));
                }
            }
        }

        return result;
    }

    private static <S, I, T, O> Word<O> computeLCP(SubsequentialTransducer<S, I, T, O> sst,
                                                   Collection<? extends I> inputs,
                                                   S s) {

        Word<O> lcp = sst.getStateProperty(s);

        for (I i : inputs) {
            T t = sst.getTransition(s, i);
            if (t != null) {
                lcp = lcp.longestCommonPrefix(sst.getTransitionProperty(s, i));
            }
        }

        return lcp;
    }

}
