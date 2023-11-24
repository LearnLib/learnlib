package de.learnlib.algorithm.lsharp.ads;

import java.util.HashMap;
import java.util.Map;

import org.checkerframework.checker.nullness.qual.Nullable;

public class ADSNode<I, O> {
    private final @Nullable I input;
    private final Map<O, ADSNode<I, O>> children;
    private final Integer score;

    public ADSNode() {
        this.input = null;
        this.children = new HashMap<>();
        this.score = 0;
    }

    public ADSNode(I input, Map<O, ADSNode<I, O>> children, Integer score) {
        this.input = input;
        this.children = children;
        this.score = score;
    }

    public Integer getScore() {
        return score;
    }

    public @Nullable I getInput() {
        return input;
    }

    public @Nullable ADSNode<I, O> getChildNode(O lastOutput) {
        return this.children.getOrDefault(lastOutput, null);
    }
}
