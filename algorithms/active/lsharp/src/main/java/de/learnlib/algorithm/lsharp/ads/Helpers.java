package de.learnlib.algorithm.lsharp.ads;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;

import net.automatalib.common.util.Pair;

class PairComparator implements Comparator<Pair<Integer, Integer>> {
    public int compare(Pair<Integer, Integer> p1, Pair<Integer, Integer> p2) {
        if (p1.getSecond() < p2.getSecond())
            return 1;
        else if (p1.getSecond() > p2.getSecond())
            return -1;
        return 0;
    }
}

public class Helpers<I> {
    public HashSet<Integer> nodesInTree = new HashSet<>();
    public PriorityQueue<Pair<Integer, Integer>> partition = new PriorityQueue<>(new PairComparator());
    public HashSet<Integer> dependent = new HashSet<>();
    public PriorityQueue<Pair<Integer, Integer>> dependentPrioQueue = new PriorityQueue<>(new PairComparator());
    public HashMap<Integer, HashSet<Pair<Integer, I>>> transitionsTo = new HashMap<>();
    public HashSet<Integer> analysedIndices = new HashSet<>();
    public HashMap<Integer, BestNode<I>> bestNode = new HashMap<>();

    public Helpers() {
    }

    public void addTransitionFromToVia(Integer src, Integer dst, I via) {
        this.transitionsTo.computeIfAbsent(dst, k -> new HashSet<>()).add(Pair.of(src, via));
    }

    public Integer scoreOf(Integer r) {
        return this.bestNode.get(r).score;
    }
}
