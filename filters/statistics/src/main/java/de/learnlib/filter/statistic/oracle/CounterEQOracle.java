package de.learnlib.filter.statistic.oracle;

import java.util.Collection;

import de.learnlib.oracle.EquivalenceOracle;
import de.learnlib.query.DefaultQuery;
import de.learnlib.statistic.Statistics;
import de.learnlib.statistic.StatsContainer;
import org.checkerframework.checker.nullness.qual.Nullable;

public class CounterEQOracle<A, I, D> implements EquivalenceOracle<A, I, D> {

    private final EquivalenceOracle<A, I, D> delegate;
    private final StatsContainer stats;
    private final String prefix;

    public CounterEQOracle(EquivalenceOracle<A, I, D> delegate) {
        this(delegate, "");
    }

    public CounterEQOracle(EquivalenceOracle<A, I, D> delegate, String prefix) {
        this.delegate = delegate;
        this.prefix = prefix;
        this.stats = Statistics.getContainer();
    }

    @Override
    public @Nullable DefaultQuery<I, D> findCounterExample(A hypothesis, Collection<? extends I> inputs) {
        final String suffix = prefix.isEmpty() ? "" : " from '" + prefix + '\'';

        stats.startOrResumeClock(prefix + "-cex-dur", "Duration of CEX search" + suffix);
        final DefaultQuery<I, D> cex = this.delegate.findCounterExample(hypothesis, inputs);
        stats.pauseClock(prefix + "-cex-dur");
        if (cex != null) {
            stats.increaseCounter(prefix + "-cex-cnt", "Found CEX" + suffix);
        }
        return cex;
    }
}
