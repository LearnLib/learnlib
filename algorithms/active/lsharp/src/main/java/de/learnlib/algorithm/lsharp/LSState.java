package de.learnlib.algorithm.lsharp;

public class LSState implements Comparable<LSState> {
    private final Integer base;

    public LSState(Integer base) {
        this.base = base;
    }

    public Integer raw() {
        return this.base;
    }

    public int compareTo(LSState to) {
        return Integer.compare(this.base, to.raw());
    };

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof LSState))
            return false;

        LSState state = (LSState) o;

        return raw() != null ? raw().equals(state.raw()) : state.raw() == null;
    }

    @Override
    public int hashCode() {
        return base != null ? base.hashCode() : 0;
    }
}
