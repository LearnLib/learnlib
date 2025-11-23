package de.learnlib.algorithm.lstar.mmlt.cex.results;

import net.automatalib.automaton.mmlt.TimerInfo;

/**
 * The provided timer should become one-shot.
 *
 * @param <I> Input type for non-delaying inputs
 * @param <O> Output symbol type
 */
public class MissingOneShotResult<I, O> extends CexAnalysisResult<I, O> {
    private final Integer location;
    private final TimerInfo<?, O> timeout;

    public MissingOneShotResult(Integer location, TimerInfo<?, O> timeout) {
        this.location = location;
        this.timeout = timeout;
    }

    public Integer getLocation() {
        return location;
    }

    public TimerInfo<?, O> getTimeout() {
        return timeout;
    }
}
