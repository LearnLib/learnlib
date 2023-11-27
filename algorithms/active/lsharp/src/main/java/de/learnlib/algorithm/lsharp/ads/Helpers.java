/* Copyright (C) 2013-2023 TU Dortmund
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

package de.learnlib.algorithm.lsharp.ads;

import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;

import net.automatalib.common.util.Pair;

// We specify the implementations to guarantee they are Serializable.
@SuppressWarnings("PMD.LooseCoupling")
public class Helpers<I> implements Serializable {
    public final HashSet<Integer> nodesInTree = new HashSet<>();
    public final PriorityQueue<Pair<Integer, Integer>> partition = new PriorityQueue<>(new PairComparator());
    public final HashSet<Integer> dependent = new HashSet<>();
    public final PriorityQueue<Pair<Integer, Integer>> dependentPrioQueue = new PriorityQueue<>(new PairComparator());
    public final HashMap<Integer, HashSet<Pair<Integer, I>>> transitionsTo = new HashMap<>();
    public final HashSet<Integer> analysedIndices = new HashSet<>();
    public final HashMap<Integer, BestNode<I>> bestNode = new HashMap<>();

    public void addTransitionFromToVia(Integer src, Integer dst, I via) {
        this.transitionsTo.computeIfAbsent(dst, k -> new HashSet<>()).add(Pair.of(src, via));
    }

    public Integer scoreOf(Integer r) {
        return this.bestNode.get(r).score;
    }

    private class PairComparator implements Comparator<Pair<Integer, Integer>>, Serializable {
        @Override
        public int compare(Pair<Integer, Integer> p1, Pair<Integer, Integer> p2) {
            if (p1.getSecond() < p2.getSecond()) {
                return 1;
            } else if (p1.getSecond() > p2.getSecond()) {
                return -1;
            }
            return 0;
        }
    }

}
