package de.learnlib.algorithm;

import net.automatalib.automaton.mmlt.SymbolCombiner;

import java.util.Objects;

/**
 * Model-specific parameters for the MMLT-learner.
 * These are used by various filters, oracles, and the MMLT simulator.
 *
 * @param <O> Output symbol type
 */
public final class MMLTModelParams<O> {
    private final O silentOutput;
    private final SymbolCombiner<O> outputCombiner;
    private final long maxTimeoutWaitingTime;
    private long maxTimerQueryWaitingTime;

    /**
     * @param silentOutput             Silent output symbol
     * @param maxTimeoutWaitingTime    Maximum time to wait for a timeout in any configuration.
     *                                 If no timeout is observed after this time, the learner assumes that no timers are active.
     *                                 Hence, if this value is set too low, the learner will miss timeouts. This usually results in an
     *                                 incomplete model but can also trigger exceptions due to unsatisfied assumptions.
     * @param maxTimerQueryWaitingTime Maximum waiting time to wait when inferring timers for a location.
     *                                 This must be at least the max. time for a timeout.
     *                                 We recommend setting this value to at least twice the highest value of any timer
     *                                 in the SUL, if these values are known or can be estimated.
     *                                 This increases the likelihood of detecting
     *                                 non-periodic behavior during timer inference, and thus reduces
     *                                 the need for equivalence queries.
     * @param outputCombiner           Function for combining simultaneously occurring outputs of timers
     */
    public MMLTModelParams(O silentOutput,
                           long maxTimeoutWaitingTime,
                           long maxTimerQueryWaitingTime,
                           SymbolCombiner<O> outputCombiner) {
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

    public SymbolCombiner<O> outputCombiner() {
        return outputCombiner;
    }

    public void setMaxTimerQueryWaitingTime(long maxTimerQueryWaitingTime) {
        this.maxTimerQueryWaitingTime = maxTimerQueryWaitingTime;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (MMLTModelParams) obj;
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
