package de.learnlib.filter.cache.mmlt;

import de.learnlib.statistic.container.LearnerStatsProvider;
import de.learnlib.statistic.container.StatsContainer;
import de.learnlib.sul.LocalTimerMealySUL;
import net.automatalib.symbol.time.TimedOutput;
import net.automatalib.symbol.time.InputSymbol;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Avoids redundant queries for timeouts.
 * <p>
 * Assume we waited maxDelay for a timeout and observed no expiration.
 * Then, any consecutive timeout-input must also show no timer (assuming sufficient maxDelay).
 * Hence, we do not need to query the SUL for these.
 * <p>
 * We may observe a timeout again after any non-delaying input, as this may
 * trigger a location-change.
 *
 * @param <I> Input type for non-delaying inputs
 * @param <O> Output symbol type
 */
public class TimeoutReducerSUL<I, O> implements LocalTimerMealySUL<I, O>, LearnerStatsProvider {

    private final LocalTimerMealySUL<I, O> delegate;
    private final long maxDelay;

    /**
     * Delay since the last non-delaying input OR
     * timer expiration.
     */
    private long noTimeoutWaitingTime;

    private StatsContainer stats;

    public TimeoutReducerSUL(LocalTimerMealySUL<I, O> delegate, long maxDelay, StatsContainer stats) {
        this.delegate = delegate;
        this.maxDelay = maxDelay;
        this.stats = stats;
    }

    @Override
    public TimedOutput<O> step(InputSymbol<I> input) {
        this.noTimeoutWaitingTime = 0; // might observe expirations again
        return delegate.step(input);
    }

    @Override
    public @Nullable TimedOutput<O> timeoutStep(long maxTime) {
        if (this.noTimeoutWaitingTime >= this.maxDelay) {
            return null; // cannot observe expiration until non-delaying input
        }

        var result = delegate.timeoutStep(maxTime);

        if (result == null) {
            this.noTimeoutWaitingTime += maxTime;
        } else {
            this.noTimeoutWaitingTime = 0;
        }

        return result;
    }

    @Override
    public void pre() {
        delegate.pre();
        this.noTimeoutWaitingTime = 0;
    }

    @Override
    public void post() {
        delegate.post();
    }

    @Override
    public void setStatsContainer(StatsContainer container) {
        this.stats = container;
    }
}
