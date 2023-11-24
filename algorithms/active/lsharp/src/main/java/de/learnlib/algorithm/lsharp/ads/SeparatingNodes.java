package de.learnlib.algorithm.lsharp.ads;

import java.util.HashMap;

import org.checkerframework.checker.nullness.qual.Nullable;

import net.automatalib.common.util.Pair;

public class SeparatingNodes<K extends Comparable<K>, V> {
    public HashMap<Pair<K, K>, V> inner;

    public SeparatingNodes() {
        this.inner = new HashMap<>();
    }

    public Pair<K, K> makeKey(K x, K y) {
        return x.compareTo(y) < 0 ? Pair.of(x, y) : Pair.of(y, x);
    }

    public void insert(K s1, K s2, V value) {
        Pair<K, K> key = this.makeKey(s1, s2);
        this.inner.put(key, value);
    }

    public @Nullable V checkPair(K s1, K s2) {
        Pair<K, K> key = this.makeKey(s1, s2);
        return this.inner.computeIfAbsent(key, k -> null);
    }
}
