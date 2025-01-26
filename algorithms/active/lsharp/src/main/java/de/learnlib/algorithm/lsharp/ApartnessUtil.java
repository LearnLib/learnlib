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
package de.learnlib.algorithm.lsharp;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.common.util.Pair;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class ApartnessUtil {

    private ApartnessUtil() {
        // prevent instantiation
    }

    public static <S extends Comparable<S>, I, O> @Nullable Word<I> computeWitness(ObservationTree<S, I, O> tree,
                                                                                   S s1,
                                                                                   S s2) {
        S t = showsStatesAreApart(tree, s1, s2);
        if (t == null) {
            return null;
        }

        return tree.getTransferSeq(t, s1);
    }

    public static <S extends Comparable<S>, I, O> boolean statesAreApart(ObservationTree<S, I, O> tree, S s1, S s2) {
        return showsStatesAreApart(tree, s1, s2) != null;
    }

    public static <S extends Comparable<S>, I, O> boolean accStatesAreApart(ObservationTree<S, I, O> tree,
                                                                            Word<I> s1a,
                                                                            Word<I> s2a) {
        S s1 = tree.getSucc(tree.defaultState(), s1a);
        assert s1 != null;
        S s2 = tree.getSucc(tree.defaultState(), s2a);
        assert s2 != null;
        return statesAreApart(tree, s1, s2);
    }

    public static <S1 extends Comparable<S1>, S2, I, O> @Nullable Word<I> treeAndHypComputeWitness(ObservationTree<S1, I, O> tree,
                                                                                                   S1 st,
                                                                                                   MealyMachine<S2, I, ?, O> fsm,
                                                                                                   S2 sh) {
        S1 s = treeAndHypShowsStatesAreApart(tree, st, sh, fsm);
        if (s == null) {
            return null;
        }

        return tree.getTransferSeq(s, st);
    }

    public static <S1 extends Comparable<S1>, S2, I, O> @Nullable S1 treeAndHypShowsStatesAreApart(ObservationTree<S1, I, O> tree,
                                                                                                   S1 st,
                                                                                                   S2 sh,
                                                                                                   MealyMachine<S2, I, ?, O> fsm) {
        Deque<Pair<S1, S2>> queue = new ArrayDeque<>();
        queue.push(Pair.of(st, sh));
        while (!queue.isEmpty()) {
            @SuppressWarnings("nullness") // false positive https://github.com/typetools/checker-framework/issues/399
            @NonNull Pair<S1, S2> pair = queue.poll();
            S1 q = pair.getFirst();
            S2 r = pair.getSecond();

            for (I i : tree.getInputAlphabet()) {
                Pair<O, S1> stepFree = tree.getOutSucc(q, i);
                if (stepFree != null) {
                    S2 dh = fsm.getSuccessor(r, i);
                    assert dh != null;
                    O outHyp = fsm.getOutput(r, i);
                    assert outHyp != null;

                    if (outHyp.equals(stepFree.getFirst())) {
                        queue.push(Pair.of(stepFree.getSecond(), dh));
                    } else {
                        return stepFree.getSecond();
                    }
                }
            }
        }

        return null;
    }

    private static <S extends Comparable<S>, I, O> @Nullable Pair<O, S> step(ObservationTree<S, I, O> tree, S x, I i) {
        return tree.getOutSucc(x, i);
    }

    private static <S extends Comparable<S>, I, O> @Nullable Pair<Pair<O, S>, Pair<O, S>> treeRespPairInput(
            ObservationTree<S, I, O> tree,
            S x,
            S y,
            I i) {
        Pair<O, S> s1 = step(tree, x, i);
        Pair<O, S> s2 = step(tree, y, i);

        if (s1 == null || s2 == null) {
            return null;
        }

        return Pair.of(s1, s2);

    }

    public static <S extends Comparable<S>, I, O> @Nullable S showsStatesAreApart(ObservationTree<S, I, O> tree,
                                                                                  S s1,
                                                                                  S s2) {
        Deque<Pair<S, S>> workList = new ArrayDeque<>();
        workList.add(Pair.of(s1, s2));
        while (!workList.isEmpty()) {
            Pair<S, S> pair = workList.pop();
            S fst = pair.getFirst();
            S snd = pair.getSecond();

            for (I i : tree.getInputAlphabet()) {
                Pair<Pair<O, S>, Pair<O, S>> p = treeRespPairInput(tree, fst, snd, i);
                if (p != null) {
                    Pair<O, S> fstOD = p.getFirst();
                    Pair<O, S> sndOD = p.getSecond();

                    O fstO = fstOD.getFirst();
                    S fstD = fstOD.getSecond();
                    O sndO = sndOD.getFirst();
                    S sndD = sndOD.getSecond();
                    if (Objects.equals(fstO, sndO)) {
                        workList.push(Pair.of(fstD, sndD));
                    } else {
                        return fstD;
                    }
                }
            }
        }

        return null;
    }
}
