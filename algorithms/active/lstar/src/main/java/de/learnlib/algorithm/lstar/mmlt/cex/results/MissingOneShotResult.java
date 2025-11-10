package de.learnlib.algorithm.lstar.mmlt.cex.results;

import net.automatalib.automaton.mmlt.MealyTimerInfo;

/**
 * The provided timer should become one-shot.
 *
 * @param <S> Location type
 * @param <I> Input type for non-delaying inputs
 * @param <O> Output symbol type
 */
public class MissingOneShotResult<S, I, O> extends CexAnalysisResult<S, I, O> {
    private final S location;
    private final MealyTimerInfo<?, O> timeout;

    public MissingOneShotResult(S location, MealyTimerInfo<?, O> timeout) {
        this.location = location;
        this.timeout = timeout;
    }

    public S getLocation() {
        return location;
    }

    public MealyTimerInfo<?, O> getTimeout() {
        return timeout;
    }
}
