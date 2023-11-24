package de.learnlib.algorithm.lsharp.ads;

import org.checkerframework.checker.nullness.qual.Nullable;

import net.automatalib.common.util.Pair;

public class ArenaNode<T, P> {
    public @Nullable Pair<P, Integer> parent;
    public T value;

    public ArenaNode(Pair<P, Integer> parent, T value) {
        this.parent = parent;
        this.value = value;
    }

    public ArenaNode(T value) {
        this(null, value);
    }

    public void update(T value) {
        this.value = value;
    }
}
