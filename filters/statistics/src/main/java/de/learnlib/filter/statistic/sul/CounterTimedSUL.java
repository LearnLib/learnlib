package de.learnlib.filter.statistic.sul;

import de.learnlib.statistic.container.LearnerStatsProvider;
import de.learnlib.statistic.container.StatsContainer;
import de.learnlib.sul.TimedSUL;
import net.automatalib.symbol.time.TimedOutput;
import net.automatalib.symbol.time.InputSymbol;
import net.automatalib.symbol.time.TimeStepSequence;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;


/**
 * Wrapper for an MMLT SUL that gathers various statistics on queries sent to this SUL.
 *
 * @param <I> Input type for non-delaying inputs
 * @param <O> Output symbol type
 */
public class CounterTimedSUL<I, O> implements TimedSUL<I, O>, LearnerStatsProvider {
    private final TimedSUL<I, O> delegate;
    private StatsContainer stats;

    @Nullable
    private final String name;

    public CounterTimedSUL(TimedSUL<I, O> delegate, StatsContainer stats) {
        this(delegate, stats, null);
    }

    public CounterTimedSUL(TimedSUL<I, O> delegate, StatsContainer stats, String name) {
        this.delegate = delegate;
        this.stats = stats;
        this.name = name;
    }

    @Override
    public void setStatsContainer(StatsContainer container) {
        this.stats = container;
    }

    private String withPrefix(String label) {
        if (this.name == null) {
            return label;
        }
        return this.name + ":" + label;
    }

    @Override
    public TimedOutput<O> step(InputSymbol<I> input) {
        stats.increaseCounter(withPrefix("sul_untimed_syms_counter"),
                withPrefix("Total untimed symbols"));
        return this.delegate.step(input);
    }

    @Override
    public @Nullable TimedOutput<O> timeoutStep(long maxTime) {
        TimedOutput<O> res = this.delegate.timeoutStep(maxTime);
        if (res == null) {
            // Waited until maxTime, no timeout occurred:
            stats.increaseCounter(withPrefix("sul_total_time"),
                    withPrefix("Total query time"), maxTime);
        } else {
            stats.increaseCounter(withPrefix("sul_total_time"),
                    withPrefix("Total query time"), res.delay());
        }

        return res;
    }

    @Override
    public Word<TimedOutput<O>> collectTimeouts(TimeStepSequence<I> input) {
        stats.increaseCounter(withPrefix("sul_total_time"),
                withPrefix("Total query time"),
                input.timeSteps());
        return this.delegate.collectTimeouts(input);
    }

    @Override
    public void pre() {
        this.delegate.pre();
        stats.increaseCounter(withPrefix("sul_resets_counter"),
                withPrefix("SUL resets"));
    }

    @Override
    public void post() {
        this.delegate.post();
    }


}
