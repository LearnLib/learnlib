package de.learnlib.algorithm.lsharp;

import java.util.HashMap;

import org.checkerframework.checker.nullness.qual.Nullable;

import net.automatalib.common.util.Pair;

public class MapTransitions<I, O> implements TransitionInformation<I, O> {
    private HashMap<I, Pair<O, LSState>> trans;

    public MapTransitions(Integer inSize) {
        trans = new HashMap<>(inSize);
    }

    @Override
    public @Nullable Pair<O, LSState> getOutSucc(I input) {
        return trans.getOrDefault(input, null);
    }

    @Override
    public void addTrans(I input, O output, LSState d) {
        Pair<O, LSState> out = trans.put(input, Pair.of(output, d));
        assert out == null;
    }

}
