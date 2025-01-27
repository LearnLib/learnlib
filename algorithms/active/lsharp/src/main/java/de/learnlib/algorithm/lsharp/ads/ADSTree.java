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
package de.learnlib.algorithm.lsharp.ads;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import de.learnlib.algorithm.lsharp.ObservationTree;
import net.automatalib.common.util.HashUtil;
import net.automatalib.common.util.Pair;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class ADSTree<S extends Comparable<S>, I, O> implements ADS<I, O> {

    private final ADSNode<I, O> initialNode;
    private ADSNode<I, O> currentNode;

    public ADSTree(ObservationTree<S, I, O> tree, Collection<S> currentBlock, @Nullable O sinkOut) {
        ADSNode<I, O> initialNode = constructADS(tree, currentBlock, sinkOut);
        this.initialNode = initialNode;
        this.currentNode = initialNode;
    }

    public int getScore() {
        return this.initialNode.getScore();
    }

    private static <A, B> Map<A, B> toMap(List<Pair<A, B>> list) {
        Map<A, B> map = new HashMap<>(HashUtil.capacity(list.size()));
        for (Pair<A, B> pair : list) {
            map.put(pair.getFirst(), pair.getSecond());
        }
        return map;
    }

    public static <S extends Comparable<S>, I, O> ADSNode<I, O> constructADS(ObservationTree<S, I, O> tree,
                                                                             Collection<S> currentBlock,
                                                                             @Nullable O sinkOut) {
        int blockSize = currentBlock.size();

        if (blockSize == 1) {
            return new ADSNode<>();
        }

        Map<I, Pair<Integer, Integer>> splitScore = new HashMap<>();
        I maxInput = maximalBaseInput(tree, currentBlock, splitScore).getFirst();

        Map<O, List<S>> oPartitions = partitionOnOutput(tree, currentBlock, maxInput);
        int ui = computeUI(oPartitions);

        int maxInputScore = 0;
        for (Entry<O, List<S>> e : oPartitions.entrySet()) {
            O o = e.getKey();
            List<S> oPart = e.getValue();
            int uIO = oPart.size();
            int childScore = Objects.equals(o, sinkOut) ? 0 : constructADS(tree, oPart, sinkOut).getScore();
            maxInputScore += computeRegScore(uIO, ui, childScore);
        }

        List<I> inputsToKeep = new ArrayList<>(splitScore.size());
        for (Entry<I, Pair<Integer, Integer>> e : splitScore.entrySet()) {
            I key = e.getKey();
            Pair<Integer, Integer> value = e.getValue();
            if (value.getFirst() + value.getSecond() >= maxInputScore) {
                inputsToKeep.add(key);
            }
        }

        assert !inputsToKeep.isEmpty();

        int bestIScore = -1;
        I bestInput = null;
        List<Pair<O, ADSNode<I, O>>> bestChildren = null;

        for (I i : inputsToKeep) {
            Map<O, List<S>> innerOPartitions = partitionOnOutput(tree, currentBlock, i);
            int innerUI = computeUI(innerOPartitions);
            int iScore = 0;
            List<Pair<O, ADSNode<I, O>>> children = new ArrayList<>(innerOPartitions.size());

            for (Entry<O, List<S>> e : innerOPartitions.entrySet()) {
                Pair<Integer, Pair<O, ADSNode<I, O>>> pair =
                        computeOSubtree(tree, e.getKey(), e.getValue(), sinkOut, innerUI);
                iScore += pair.getFirst();
                children.add(pair.getSecond());
            }

            if (iScore >= maxInputScore && iScore >= bestIScore) {
                bestIScore = iScore;
                bestInput = i;
                bestChildren = children;
            }
        }

        assert bestInput != null && bestChildren != null;

        return new ADSNode<>(bestInput, toMap(bestChildren), bestIScore);
    }

    public static <S extends Comparable<S>, I, O> Pair<Integer, Pair<O, ADSNode<I, O>>> computeOSubtree(ObservationTree<S, I, O> tree,
                                                                                                        O o,
                                                                                                        List<S> oPart,
                                                                                                        @Nullable O sinkOut,
                                                                                                        int ui) {
        ADSNode<I, O> oSubtree = Objects.equals(o, sinkOut) ? new ADSNode<>() : constructADS(tree, oPart, sinkOut);
        int oChildScore = oSubtree.getScore();
        int uio = oPart.size();
        Pair<O, ADSNode<I, O>> oChild = Pair.of(o, oSubtree);
        int oScore = computeRegScore(uio, ui, oChildScore);
        return Pair.of(oScore, oChild);
    }

    private static int computeRegScore(int uio, int ui, int childScore) {
        return uio * (ui - uio) + childScore;
    }

    private static <S, O> int computeUI(Map<O, List<S>> oPartitions) {
        int ui = 0;
        for (List<S> value : oPartitions.values()) {
            ui += value.size();
        }
        return ui;
    }

    private static <S extends Comparable<S>, I, O> Map<O, List<S>> partitionOnOutput(ObservationTree<S, I, O> tree,
                                                                                     Collection<S> block,
                                                                                     I input) {
        Map<O, List<S>> map = new HashMap<>();
        for (S s : block) {
            Pair<O, S> succ = tree.getOutSucc(s, input);
            if (succ != null) {
                map.computeIfAbsent(succ.getFirst(), k -> new ArrayList<>()).add(succ.getSecond());
            }
        }
        return map;
    }

    public static <S extends Comparable<S>, I, O> Pair<I, Integer> maximalBaseInput(ObservationTree<S, I, O> tree,
                                                                                    Collection<S> currentBlock,
                                                                                    Map<I, Pair<Integer, Integer>> splitScore) {
        I retInput = tree.getInputAlphabet().getSymbol(0);
        int retPairs = 0;
        for (I i : tree.getInputAlphabet()) {
            Map<O, List<S>> oPartition = partitionOnOutput(tree, currentBlock, i);
            List<Integer> stateNums = new ArrayList<>(oPartition.size());
            int maxRec = 0;
            int ui = 0;

            for (List<S> value : oPartition.values()) {
                int size = value.size();
                stateNums.add(size);
                maxRec += size * (size - 1);
                ui += size;
            }

            int numApartPairs = 0;
            for (Integer uio : stateNums) {
                numApartPairs += uio * (ui - uio);
            }

            splitScore.put(i, Pair.of(numApartPairs, maxRec));

            if (numApartPairs > retPairs) {
                retInput = i;
                retPairs = numApartPairs;
            }
        }

        return Pair.of(retInput, retPairs);
    }

    @Override
    public @Nullable I nextInput(@Nullable O previousSymbol) {
        if (previousSymbol != null) {
            ADSNode<I, O> childNode = currentNode.getChildNode(previousSymbol);
            if (childNode == null) {
                return null;
            }
            this.currentNode = childNode;
        }

        return this.currentNode.getInput();
    }

    @Override
    public void resetToRoot() {
        this.currentNode = initialNode;
    }

}
