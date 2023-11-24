package de.learnlib.algorithm.lsharp.ads;

import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import net.automatalib.common.util.Pair;

class PairComparator implements Comparator<Pair<Integer, Integer>>, Serializable {
    public int compare(Pair<Integer, Integer> p1, Pair<Integer, Integer> p2) {
        if (p1.getSecond() < p2.getSecond()) {
            return 1;
        } else if (p1.getSecond() > p2.getSecond()) {
            return -1;
        }
        return 0;
    }
}

public class Helpers<I> {
    public Set<Integer> nodesInTree = new HashSet<>();
    public PriorityQueue<Pair<Integer, Integer>> partition = new PriorityQueue<>(new PairComparator());
    public Set<Integer> dependent = new HashSet<>();
    public PriorityQueue<Pair<Integer, Integer>> dependentPrioQueue = new PriorityQueue<>(new PairComparator());
    public Map<Integer, HashSet<Pair<Integer, I>>> transitionsTo = new HashMap<>();
    public Set<Integer> analysedIndices = new HashSet<>();
    public Map<Integer, BestNode<I>> bestNode = new HashMap<>();

    public void addTransitionFromToVia(Integer src, Integer dst, I via) {
        this.transitionsTo.computeIfAbsent(dst, k -> new HashSet<>()).add(Pair.of(src, via));
    }

    public Integer scoreOf(Integer r) {
        return this.bestNode.get(r).score;
    }
}
