package de.learnlib.algorithm.lsharp.ads;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.checkerframework.checker.nullness.qual.Nullable;

import de.learnlib.algorithm.lsharp.ObservationTree;
import de.learnlib.algorithm.lsharp.ads.ADSStatus.Code;
import net.automatalib.common.util.Pair;
import net.automatalib.common.util.Triple;

public class ADSTree<S extends Comparable<S>, I, O> implements ADS<I, O> {
    private ADSNode<I, O> initialNode;
    private ADSNode<I, O> currentNode;

    public ADSTree(ObservationTree<S, I, O> tree, List<S> currentBlock, @Nullable O sinkOut) {
        ADSNode<I, O> initialNode = this.constructADS(tree, currentBlock, sinkOut);
        Objects.requireNonNull(initialNode);
        this.initialNode = initialNode;
        this.currentNode = initialNode;
    }

    public Integer getScore() {
        return this.initialNode.getScore();
    }

    private <A, B> Pair<List<A>, List<B>> unzip(List<Pair<A, B>> list) {
        List<A> lA = new LinkedList<>();
        List<B> lB = new LinkedList<>();
        for (Pair<A, B> pair : list) {
            lA.add(pair.getFirst());
            lB.add(pair.getSecond());
        }

        return Pair.of(lA, lB);
    }

    private <A, B> HashMap<A, B> toMap(List<Pair<A, B>> list) {
        HashMap<A, B> map = new HashMap<>();
        for (Pair<A, B> pair : list) {
            map.put(pair.getFirst(), pair.getSecond());
        }
        return map;
    }

    public ADSNode<I, O> constructADS(ObservationTree<S, I, O> tree, List<S> currentBlock, O sinkOut) {
        Integer blockSize = currentBlock.size();

        if (blockSize == 1) {
            return new ADSNode<>();
        }

        HashMap<I, Pair<Integer, Integer>> splitScore = new HashMap<>();
        I maxInput = this.maximalBaseInput(tree, currentBlock, splitScore).getFirst();

        HashMap<O, List<S>> oPartitions = this.partitionOnOutput(tree, currentBlock, maxInput);
        Integer ui = oPartitions.values().stream().map(p -> p.size()).collect(Collectors.summingInt(x -> x));
        Integer maxRec = oPartitions.entrySet().stream().map(e -> {
            O o = e.getKey();
            List<S> oPart = e.getValue();
            Integer uIO = oPart.size();
            Integer childScore = o.equals(sinkOut) ? 0 : (this.constructADS(tree, oPart, sinkOut)).getScore();
            return (Integer) this.computeRegScore(uIO, ui, childScore);
        }).collect(Collectors.summingInt(x -> x));

        Integer maxInputScore = maxRec;
        List<I> inputsToKeep = splitScore.entrySet().parallelStream()
                .filter(e -> e.getValue().getFirst() + e.getValue().getSecond() >= maxInputScore).map(e -> e.getKey())
                .collect(Collectors.toList());

        assert !inputsToKeep.isEmpty();

        ADSNode<I, O> subtreeInfo = inputsToKeep.parallelStream().map(i -> {
            HashMap<O, List<S>> innerOPartitions = this.partitionOnOutput(tree, currentBlock, i);
            Integer innerUI = innerOPartitions.values().stream().map(p -> p.size())
                    .collect(Collectors.summingInt(x -> x));

            Pair<List<Integer>, List<Pair<O, ADSNode<I, O>>>> pair = unzip(
                    innerOPartitions
                            .entrySet().stream().map(e -> (Pair<Integer, Pair<O, ADSNode<I, O>>>) this
                                    .computeOSubtree(tree, e.getKey(), e.getValue(), sinkOut, innerUI))
                            .collect(Collectors.toList()));

            List<Integer> oScores = pair.getFirst();
            List<Pair<O, ADSNode<I, O>>> data = pair.getSecond();
            Integer iScore = oScores.stream().collect(Collectors.summingInt(x -> x));
            return (Triple<I, Integer, List<Pair<O, ADSNode<I, O>>>>) Triple.of(i, iScore, data);
        }).filter(triple -> triple.getSecond() >= maxInputScore)
                .max((a, b) -> (Integer.compare(a.getSecond(), b.getSecond()))).map(t -> {
                    HashMap<O, ADSNode<I, O>> children = toMap(t.getThird());
                    return new ADSNode<I, O>(t.getFirst(), children, t.getSecond());
                }).orElse(null);

        Objects.requireNonNull(subtreeInfo);
        return subtreeInfo;
    }

    public Pair<Integer, Pair<O, ADSNode<I, O>>> computeOSubtree(ObservationTree<S, I, O> tree, O o, List<S> oPart,
            O sinkOut, Integer ui) {
        ADSNode<I, O> oSubtree = o.equals(sinkOut) ? new ADSNode<>(null, new HashMap<>(), 0)
                : this.constructADS(tree, oPart, sinkOut);
        Integer oChildScore = oSubtree.getScore();
        int uio = oPart.size();
        Pair<O, ADSNode<I, O>> oChild = Pair.of(o, oSubtree);
        Integer oScore = this.computeRegScore(uio, ui, oChildScore);
        return Pair.of(oScore, oChild);
    }

    private Integer computeRegScore(Integer uio, Integer ui, Integer childScore) {
        return uio * (ui - uio) + childScore;
    }

    private HashMap<O, List<S>> partitionOnOutput(ObservationTree<S, I, O> tree, List<S> block, I input) {
        HashMap<O, List<S>> map = new HashMap<>();
        block.stream().map(s -> tree.getOutSucc(s, input)).filter(s -> s != null)
                .forEach(p -> map.computeIfAbsent(p.getFirst(), k -> new LinkedList<>()).add(p.getSecond()));
        return map;
    }

    public Pair<I, Integer> maximalBaseInput(ObservationTree<S, I, O> tree, List<S> currentBlock,
            HashMap<I, Pair<Integer, Integer>> splitScore) {
        I retInput = tree.getInputAlphabet().getSymbol(0);
        Integer retPairs = 0;
        for (I i : tree.getInputAlphabet()) {
            AtomicReference<Pair<List<Integer>, Integer>> pair = new AtomicReference<>(Pair.of(new LinkedList<>(), 0));
            partitionOnOutput(tree, currentBlock, i).entrySet().stream().map(e -> e.getValue().size()).forEach(in -> {
                pair.get().getFirst().add(in);
                pair.set(Pair.of(pair.get().getFirst(), pair.get().getSecond() + (in * (in - 1))));
            });
            List<Integer> stateNums = pair.get().getFirst();
            Integer maxRec = pair.get().getSecond();

            Integer ui = stateNums.stream().collect(Collectors.summingInt(x -> x));
            Integer numApartPairs = stateNums.stream().collect(Collectors.summingInt(uio -> uio * (ui - uio)));
            splitScore.put(i, Pair.of(numApartPairs, maxRec));

            if (numApartPairs > retPairs) {
                retInput = i;
                retPairs = numApartPairs;
            }
        }

        return Pair.of(retInput, retPairs);
    }

    public I nextInput(@Nullable O previousSymbol) throws ADSStatus {
        if (previousSymbol != null) {
            @Nullable
            ADSNode<I, O> childNode = currentNode.getChildNode(previousSymbol);
            if (childNode == null) {
                throw new ADSStatus(Code.UNEXPECTED);
            }
            this.currentNode = childNode;
        }

        @Nullable
        I outSymbol = this.currentNode.getInput();
        if (outSymbol == null) {
            throw new ADSStatus(Code.DONE);
        }
        return outSymbol;
    }

    public void resetToRoot() {
        this.currentNode = initialNode;
    }

    public Float identificationPower() {
        return this.getScore().floatValue();
    }

}
