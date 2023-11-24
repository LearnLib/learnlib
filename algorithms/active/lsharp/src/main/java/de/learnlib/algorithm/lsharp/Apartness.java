package de.learnlib.algorithm.lsharp;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.checkerframework.checker.nullness.qual.Nullable;

import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.common.util.Pair;
import net.automatalib.common.util.Triple;
import net.automatalib.word.Word;

public class Apartness {

    public static <S extends Comparable<S>, I, O> @Nullable Word<I> computeWitness(ObservationTree<S, I, O> tree, S s1,
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

    public static <S extends Comparable<S>, I, O> boolean accStatesAreApart(ObservationTree<S, I, O> tree, Word<I> s1a,
            Word<I> s2a) {
        S s1 = tree.getSucc(tree.defaultState(), s1a);
        Objects.requireNonNull(s1);
        S s2 = tree.getSucc(tree.defaultState(), s2a);
        Objects.requireNonNull(s2);
        return statesAreApart(tree, s1, s2);
    }

    public static <S extends Comparable<S>, I, O> boolean treeAndHypStatesApart(ObservationTree<S, I, O> tree, S st,
            LSState sh, MealyMachine<LSState, I, ?, O> fsm) {
        return treeAndHypShowsStatesAreApart(tree, st, sh, fsm) != null;
    }

    public static <S extends Comparable<S>, I, O> boolean treeAndHypStatesApartSunkBounded(
            ObservationTree<S, I, O> tree, S st, LSState sh, MealyMachine<LSState, I, ?, O> fsm, O sinkOutput,
            Integer depth) {
        return treeAndHypShowsStatesAreApartSunkDepth(tree, st, sh, fsm, sinkOutput, depth) != null;
    }

    public static <S extends Comparable<S>, I, O> @Nullable S treeAndHypShowsStatesAreApartSunkDepth(
            ObservationTree<S, I, O> tree, S st, LSState sh, MealyMachine<LSState, I, ?, O> fsm, O sinkOutput,
            Integer depth) {
        ArrayDeque<Triple<S, LSState, Integer>> queue = new ArrayDeque<>();
        queue.push(Triple.of(st, sh, 0));
        while (!queue.isEmpty()) {
            Triple<S, LSState, Integer> triple = queue.pop();
            S q = triple.getFirst();
            LSState r = triple.getSecond();
            Integer d = triple.getThird();

            for (I i : tree.getInputAlphabet()) {
                Pair<O, S> stepFree = tree.getOutSucc(q, i);
                if (stepFree != null) {
                    LSState dh = fsm.getSuccessor(r, i);
                    Objects.requireNonNull(dh);
                    O outHyp = fsm.getOutput(r, i);
                    Objects.requireNonNull(outHyp);

                    if (outHyp.equals(stepFree.getFirst())) {
                        if (stepFree.getFirst().equals(sinkOutput)) {
                            continue;
                        }
                        if (d + 1 == depth) {
                            continue;
                        }
                        queue.push(Triple.of(stepFree.getSecond(), dh, d + 1));
                    } else {
                        return stepFree.getSecond();
                    }
                }
            }
        }

        return null;
    }

    public static <S extends Comparable<S>, I, O> boolean treeAndHypStatesApartSunk(ObservationTree<S, I, O> tree, S st,
            LSState sh, MealyMachine<LSState, I, ?, O> fsm, O sinkOutput) {
        return treeAndHypShowsStatesAreApartSunk(tree, st, sh, fsm, sinkOutput) != null;
    }

    public static <S extends Comparable<S>, I, O> @Nullable S treeAndHypShowsStatesAreApartSunk(
            ObservationTree<S, I, O> tree, S st, LSState sh, MealyMachine<LSState, I, ?, O> fsm, O sinkOutput) {
        ArrayDeque<Pair<S, LSState>> queue = new ArrayDeque<>();
        queue.push(Pair.of(st, sh));
        while (!queue.isEmpty()) {
            Pair<S, LSState> pair = queue.pop();
            S q = pair.getFirst();
            LSState r = pair.getSecond();

            for (I i : tree.getInputAlphabet()) {
                Pair<O, S> stepFree = tree.getOutSucc(q, i);
                if (stepFree != null) {
                    LSState dh = fsm.getSuccessor(r, i);
                    Objects.requireNonNull(dh);
                    O outHyp = fsm.getOutput(r, i);
                    Objects.requireNonNull(outHyp);

                    if (outHyp.equals(stepFree.getFirst())) {
                        if (stepFree.getFirst().equals(sinkOutput)) {
                            continue;
                        }
                        queue.push(Pair.of(stepFree.getSecond(), dh));
                    } else {
                        return stepFree.getSecond();
                    }
                }
            }
        }

        return null;
    }

    public static <S extends Comparable<S>, I, O> @Nullable Word<I> treeAndHypComputeWitness(
            ObservationTree<S, I, O> tree, S st, MealyMachine<LSState, I, ?, O> fsm, LSState sh) {
        S s = treeAndHypShowsStatesAreApart(tree, st, sh, fsm);
        if (s == null) {
            return null;
        }

        return tree.getTransferSeq(s, st);
    }

    public static <S extends Comparable<S>, I, O> @Nullable S treeAndHypShowsStatesAreApart(
            ObservationTree<S, I, O> tree, S st, LSState sh, MealyMachine<LSState, I, ?, O> fsm) {
        ArrayDeque<Pair<S, LSState>> queue = new ArrayDeque<>();
        queue.push(Pair.of(st, sh));
        while (!queue.isEmpty()) {
            Pair<S, LSState> pair = queue.pop();
            S q = pair.getFirst();
            LSState r = pair.getSecond();

            for (I i : tree.getInputAlphabet()) {
                Pair<O, S> stepFree = tree.getOutSucc(q, i);
                if (stepFree != null) {
                    LSState dh = fsm.getSuccessor(r, i);
                    Objects.requireNonNull(dh);
                    O outHyp = fsm.getOutput(r, i);
                    Objects.requireNonNull(outHyp);

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
            ObservationTree<S, I, O> tree, S x, S y, I i) {
        Pair<O, S> s1 = step(tree, x, i);
        Pair<O, S> s2 = step(tree, y, i);

        if (s1 == null || s2 == null) {
            return null;
        }

        return Pair.of(s1, s2);

    }

    public static <S extends Comparable<S>, I, O> @Nullable S showsStatesAreApart(ObservationTree<S, I, O> tree, S s1,
            S s2) {
        ArrayDeque<Pair<S, S>> workList = new ArrayDeque<>();
        workList.add(Pair.of(s1, s2));
        while (!workList.isEmpty()) {
            Pair<S, S> pair = workList.pop();
            S fst = pair.getFirst();
            S snd = pair.getSecond();
            List<Pair<Pair<O, S>, Pair<O, S>>> iter = tree.getInputAlphabet().stream()
                    .map(i -> treeRespPairInput(tree, fst, snd, i)).filter(i -> i != null).collect(Collectors.toList());
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
