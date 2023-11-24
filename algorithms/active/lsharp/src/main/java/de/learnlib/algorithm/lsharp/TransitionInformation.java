package de.learnlib.algorithm.lsharp;

import org.checkerframework.checker.nullness.qual.Nullable;

import net.automatalib.common.util.Pair;

public interface TransitionInformation<I, O> {
    @Nullable
    Pair<O, LSState> getOutSucc(I input);

    void addTrans(I input, O output, LSState d);
}
