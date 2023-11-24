package de.learnlib.algorithm.lsharp.ads;

import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;

public class SepSeq<T> {
    enum Status {
        INJ, NONINJ;
    }

    public List<T> seq;
    public @Nullable Status status;

    public SepSeq(Status status, List<T> seq) {
        this.seq = seq;
        this.status = status;
    }

    public Boolean isInjective() {
        if (this.status == null) {
            return false;
        }

        return this.status.compareTo(Status.INJ) == 0;
    }

    public Boolean isSet() {
        return this.status != null;
    }

    public List<T> get() {
        return this.seq;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof SepSeq<?>) {
            SepSeq<?> casted = (SepSeq<?>) other;
            return this.seq.equals(casted.seq) && this.status.equals(casted.status);
        }

        return false;
    }
}
