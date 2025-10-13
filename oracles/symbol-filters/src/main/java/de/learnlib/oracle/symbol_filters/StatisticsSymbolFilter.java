package de.learnlib.oracle.symbol_filters;

import de.learnlib.statistic.container.DummyStatsContainer;
import de.learnlib.statistic.container.LearnerStatsProvider;
import de.learnlib.statistic.container.StatsContainer;
import de.learnlib.symbol_filter.SymbolFilter;
import de.learnlib.symbol_filter.SymbolFilterResponse;
import net.automatalib.word.Word;

/**
 * Collects various statistics on symbol filtering, including false accepts + false ignores.
 *
 * @param <U> Type for symbols in the prefix of the considered states
 * @param <V> Type of the queried symbols
 */
public abstract class StatisticsSymbolFilter<U, V> implements SymbolFilter<U, V>, LearnerStatsProvider {

    private final SymbolFilter<U, V> delegate;
    private StatsContainer stats = new DummyStatsContainer();

    public StatisticsSymbolFilter(SymbolFilter<U, V> delegate, StatsContainer stats) {
        this.delegate = delegate;
        this.stats = stats;
    }

    protected abstract SymbolFilterResponse isIgnorable(Word<U> prefix, V symbol);

    @Override
    public SymbolFilterResponse query(Word<U> prefix, V symbol) {
        stats.increaseCounter("cnt_isf_queries", "Filter: queries");

        SymbolFilterResponse filterResponse = this.delegate.query(prefix, symbol);
        SymbolFilterResponse expectedResponse = this.isIgnorable(prefix, symbol);

        // Count false ignores, rejects + correct predictions:
        if (filterResponse.equals(SymbolFilterResponse.ACCEPT)) {
            if (filterResponse.equals(expectedResponse)) {
                stats.increaseCounter("cnt_isf_correct_accepts", "Filter: correct accepts");
            } else {
                stats.increaseCounter("cnt_isf_false_accepts", "Filter: false accepts");
            }
        } else {
            if (filterResponse.equals(expectedResponse)) {
                stats.increaseCounter("cnt_isf_correct_ignores", "Filter: correct ignores");
            } else {
                stats.increaseCounter("cnt_isf_false_ignores", "Filter: false ignores");
            }
        }

        return filterResponse;
    }

    @Override
    public void update(Word<U> prefix, V symbol, SymbolFilterResponse response) {
        delegate.update(prefix, symbol, response);
    }

    @Override
    public void setStatsContainer(StatsContainer container) {
        this.stats = container;
    }
}
