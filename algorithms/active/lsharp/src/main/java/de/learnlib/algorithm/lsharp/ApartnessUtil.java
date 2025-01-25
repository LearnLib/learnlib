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
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.common.util.Pair;
import net.automatalib.word.Word;
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

    public static <S extends Comparable<S>, I, O> @Nullable Word<I> treeAndHypComputeWitness(ObservationTree<S, I, O> tree,
                                                                                             S st,
                                                                                             MealyMachine<LSState, I, ?, O> fsm,
                                                                                             LSState sh) {
        S s = treeAndHypShowsStatesAreApart(tree, st, sh, fsm);
        if (s == null) {
            return null;
        }

        return tree.getTransferSeq(s, st);
    }

    public static <S extends Comparable<S>, I, O> @Nullable S treeAndHypShowsStatesAreApart(ObservationTree<S, I, O> tree,
                                                                                            S st,
                                                                                            LSState sh,
                                                                                            MealyMachine<LSState, I, ?, O> fsm) {
        Deque<Pair<S, LSState>> queue = new ArrayDeque<>();
        queue.push(Pair.of(st, sh));
        while (!queue.isEmpty()) {
            Pair<S, LSState> pair = queue.pop();
            S q = pair.getFirst();
            LSState r = pair.getSecond();

            for (I i : tree.getInputAlphabet()) {
                Pair<O, S> stepFree = tree.getOutSucc(q, i);
                if (stepFree != null) {
                    LSState dh = fsm.getSuccessor(r, i);
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
            List<Pair<Pair<O, S>, Pair<O, S>>> iter = tree.getInputAlphabet()
                                                          .stream()
                                                          .map(i -> treeRespPairInput(tree, fst, snd, i))
                                                          .filter(Objects::nonNull)
                                                          .collect(Collectors.toList());
            for (Pair<Pair<O, S>, Pair<O, S>> iterPair : iter) {
                Pair<O, S> fstOD = iterPair.getFirst();
                Pair<O, S> sndOD = iterPair.getSecond();

                O fstO = fstOD.getFirst();
                S fstD = fstOD.getSecond();
                O sndO = sndOD.getFirst();
                S sndD = sndOD.getSecond();
                if (fstO.equals(sndO)) {
                    workList.push(Pair.of(fstD, sndD));
                } else {
                    return fstD;
                }
            }
        }

        return null;
    }
}
