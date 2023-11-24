package de.learnlib.algorithm.lsharp;

import org.checkerframework.checker.nullness.qual.Nullable;

import net.automatalib.common.util.Pair;

public interface TransitionInformation<I, O> {
    public @Nullable Pair<O, LSState> getOutSucc(I input);

    public void addTrans(I input, O output, LSState d);
}
