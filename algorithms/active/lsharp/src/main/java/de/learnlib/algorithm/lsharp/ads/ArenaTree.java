package de.learnlib.algorithm.lsharp.ads;

import java.util.LinkedList;
import java.util.function.Function;

import net.automatalib.common.util.Pair;

public class ArenaTree<T, P> {
    public LinkedList<ArenaNode<T, P>> arena;

    public ArenaTree() {
        this.arena = new LinkedList<>();
    }

    public Integer size() {
        return this.arena.size();
    }

    public Integer node(T value) {
        Integer idx = this.size();
        this.arena.add(new ArenaNode<T, P>(value));
        return idx;
    }

    public Integer nodeWithParent(T value, Integer pIndex, P input) {
        Integer idx = this.arena.size();
        this.arena.add(new ArenaNode<T, P>(Pair.of(input, pIndex), value));
        return idx;
    }

    public T get(Integer index) {
        return this.arena.get(index).value;
    }

    public <X> X apply(Integer index, Function<T, X> func) {
        return func.apply(this.get(index));
    }

}
