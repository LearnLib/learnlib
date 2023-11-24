package de.learnlib.algorithm.lsharp.ads;

import org.checkerframework.checker.nullness.qual.Nullable;

public class BestNode<I> {
    public Integer score;
    public @Nullable I input;
    public @Nullable Integer next;

    public BestNode() {
        this.score = Integer.MAX_VALUE;
        this.input = null;
        this.next = null;
    }

    public BestNode(@Nullable I input, @Nullable Integer next, Integer score) {
        this.input = input;
        this.score = score;
        this.next = next;
    }

    public void update(@Nullable I input, @Nullable Integer next, Integer score) {
        this.input = input;
        this.score = score;
        this.next = next;
    }
}
