package de.learnlib.algorithm;

import net.automatalib.automaton.time.mmlt.AbstractSymbolCombiner;

import java.util.Objects;

/**
 * Model-specific parameters for the MMLT-learner.
 * These are used by various filters, oracles, and the MMLT simulator.
 *
 * @param <O> Output symbol type
 */
public final class LocalTimerMealyModelParams<O> {
    private final O silentOutput;
    private final AbstractSymbolCombiner<O> outputCombiner;
    private final long maxTimeoutWaitingTime;
    private long maxTimerQueryWaitingTime;

    /**
     * @param silentOutput             Silent output symbol
     * @param maxTimeoutWaitingTime    Maximum waiting time to wait for a timeout in any configuration
     * @param maxTimerQueryWaitingTime Maximum waiting time for timer queries
     * @param outputCombiner           Function for combining simultaneously occurring outputs of timers
     */
    public LocalTimerMealyModelParams(O silentOutput,
                                      long maxTimeoutWaitingTime,
                                      long maxTimerQueryWaitingTime,
                                      AbstractSymbolCombiner<O> outputCombiner) {
        this.silentOutput = silentOutput;
        this.maxTimeoutWaitingTime = maxTimeoutWaitingTime;
        this.maxTimerQueryWaitingTime = maxTimerQueryWaitingTime;
        this.outputCombiner = outputCombiner;
    }

    public O silentOutput() {
        return silentOutput;
    }

    public long maxTimeoutWaitingTime() {
        return maxTimeoutWaitingTime;
    }

    public long maxTimerQueryWaitingTime() {
        return maxTimerQueryWaitingTime;
    }

    public AbstractSymbolCombiner<O> outputCombiner() {
        return outputCombiner;
    }

    public void setMaxTimerQueryWaitingTime(long maxTimerQueryWaitingTime) {
        this.maxTimerQueryWaitingTime = maxTimerQueryWaitingTime;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (LocalTimerMealyModelParams) obj;
        return Objects.equals(this.silentOutput, that.silentOutput) &&
                this.maxTimeoutWaitingTime == that.maxTimeoutWaitingTime &&
                this.maxTimerQueryWaitingTime == that.maxTimerQueryWaitingTime &&
                Objects.equals(this.outputCombiner, that.outputCombiner);
    }

    @Override
    public int hashCode() {
        return Objects.hash(silentOutput, maxTimeoutWaitingTime, maxTimerQueryWaitingTime, outputCombiner);
    }

    @Override
    public String toString() {
        return "LocalTimerMealyModelParams[" +
                "silentOutput=" + silentOutput + ", " +
                "maxTimeoutWaitingTime=" + maxTimeoutWaitingTime + ", " +
                "maxTimerQueryWaitingTime=" + maxTimerQueryWaitingTime + ", " +
                "outputCombiner=" + outputCombiner + ']';
    }

}
