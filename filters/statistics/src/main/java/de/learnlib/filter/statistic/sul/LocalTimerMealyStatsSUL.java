package de.learnlib.filter.statistic.sul;

import de.learnlib.statistic.container.LearnerStatsProvider;
import de.learnlib.statistic.container.StatsContainer;
import de.learnlib.sul.LocalTimerMealySUL;
import net.automatalib.alphabet.time.mmlt.LocalTimerMealyOutputSymbol;
import net.automatalib.alphabet.time.mmlt.NonDelayingInput;
import net.automatalib.alphabet.time.mmlt.TimeStepSequence;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;


/**
 * Wrapper for an MMLT SUL that gathers various statistics on queries sent to this SUL.
 *
 * @param <I> Input type for non-delaying inputs
 * @param <O> Output symbol type
 */
public class LocalTimerMealyStatsSUL<I, O> extends LocalTimerMealySUL<I, O> implements LearnerStatsProvider {
    private final LocalTimerMealySUL<I, O> delegate;
    private StatsContainer stats;

    @Nullable
    private final String name;

    public LocalTimerMealyStatsSUL(LocalTimerMealySUL<I, O> delegate, StatsContainer stats) {
        this(delegate, stats, null);
    }

    public LocalTimerMealyStatsSUL(LocalTimerMealySUL<I, O> delegate, StatsContainer stats, String name) {
        this.delegate = delegate;
        this.stats = stats;
        this.name = name;
    }

    public long getResetCount() {
        if (this.stats == null) {
            throw new IllegalStateException("No stats container set up.");
        }
        return this.stats.getCount(withPrefix("sul_resets_counter")).get();
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
    public LocalTimerMealyOutputSymbol<O> step(NonDelayingInput<I> input) {
        stats.increaseCounter(withPrefix("sul_untimed_syms_counter"),
                withPrefix("Total untimed symbols"));
        return this.delegate.step(input);
    }

    @Override
    public @Nullable LocalTimerMealyOutputSymbol<O> timeoutStep(long maxTime) {
        LocalTimerMealyOutputSymbol<O> res = this.delegate.timeoutStep(maxTime);
        if (res == null) {
            // Waited until maxTime, no timeout occurred:
            stats.increaseCounter(withPrefix("sul_total_time"),
                    withPrefix("Total query time"), maxTime);
        } else {
            stats.increaseCounter(withPrefix("sul_total_time"),
                    withPrefix("Total query time"), res.getDelay());
        }

        return res;
    }

    @Override
    public Word<LocalTimerMealyOutputSymbol<O>> collectTimeouts(TimeStepSequence<I> input) {
        stats.increaseCounter(withPrefix("sul_total_time"),
                withPrefix("Total query time"),
                input.getTimeSteps());
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
