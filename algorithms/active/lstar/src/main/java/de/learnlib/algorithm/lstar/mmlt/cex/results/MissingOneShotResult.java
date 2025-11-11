package de.learnlib.algorithm.lstar.mmlt.cex.results;

import net.automatalib.automaton.mmlt.MealyTimerInfo;

/**
 * The provided timer should become one-shot.
 *
 * @param <I> Input type for non-delaying inputs
 * @param <O> Output symbol type
 */
public class MissingOneShotResult<I, O> extends CexAnalysisResult<I, O> {
    private final Integer location;
    private final MealyTimerInfo<?, O> timeout;

    public MissingOneShotResult(Integer location, MealyTimerInfo<?, O> timeout) {
        this.location = location;
        this.timeout = timeout;
    }

    public Integer getLocation() {
        return location;
    }

    public MealyTimerInfo<?, O> getTimeout() {
        return timeout;
    }
}
