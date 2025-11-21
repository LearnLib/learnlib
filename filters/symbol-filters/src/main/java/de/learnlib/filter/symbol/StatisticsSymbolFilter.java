package de.learnlib.filter.symbol;

import de.learnlib.filter.MutableSymbolFilter;
import de.learnlib.statistic.Statistics;
import de.learnlib.statistic.StatsContainer;
import de.learnlib.filter.SymbolFilter;
import de.learnlib.filter.SymbolFilterResponse;
import net.automatalib.word.Word;

/**
 * Collects various statistics on symbol filtering, including false accepts + false ignores.
 *
 * @param <U>
 *         Type for symbols in the prefix of the considered states
 * @param <V>
 *         Type of the queried symbols
 */
public abstract class StatisticsSymbolFilter<U, V> implements MutableSymbolFilter<U, V> {

    private final SymbolFilter<U, V> delegate;
    private final StatsContainer stats;

    public StatisticsSymbolFilter(SymbolFilter<U, V> delegate) {
        this.delegate = delegate;
        this.stats = Statistics.getContainer();
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
    public void accept(Word<U> prefix, V symbol) {
        if (delegate instanceof MutableSymbolFilter<U,V> mut) {
            mut.accept(prefix, symbol);
        } else {
            throw new UnsupportedOperationException("delegate filter does not support updates");
        }
    }
}
