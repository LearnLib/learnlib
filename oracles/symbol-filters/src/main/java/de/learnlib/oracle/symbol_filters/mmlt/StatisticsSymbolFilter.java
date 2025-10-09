package de.learnlib.oracle.symbol_filters.mmlt;

import de.learnlib.statistic.container.DummyStatsContainer;
import de.learnlib.statistic.container.LearnerStatsProvider;
import de.learnlib.statistic.container.StatsContainerX;
import de.learnlib.symbol_filter.SymbolFilter;
import de.learnlib.symbol_filter.SymbolFilterResponse;
import net.automatalib.alphabet.time.mmlt.LocalTimerMealySemanticInputSymbol;
import net.automatalib.alphabet.time.mmlt.NonDelayingInput;
import net.automatalib.automaton.time.mmlt.LocalTimerMealy;
import net.automatalib.word.Word;

/**
 * Collects various statistics on symbol filtering, including false accepts + false ignores.
 *
 * @param <I> Input type for non-delaying inputs
 */
public class StatisticsSymbolFilter<S, I, O> implements SymbolFilter<I, O>, LearnerStatsProvider {

    private final SymbolFilter<I, O> delegate;
    private final PerfectSymbolFilter<S, I, O> perfectFilter;
    private StatsContainerX stats = new DummyStatsContainer();


    public StatisticsSymbolFilter(SymbolFilter<I, O> delegate, LocalTimerMealy<S, I, O> sulModel) {
        this.delegate = delegate;
        this.perfectFilter = new PerfectSymbolFilter<>(sulModel);
    }

    @Override
    public SymbolFilterResponse query(Word<LocalTimerMealySemanticInputSymbol<I>> prefix, NonDelayingInput<I> symbol) {
        stats.increaseCounter("cnt_isf_queries", "Filter: queries");

        SymbolFilterResponse filterResponse = this.delegate.query(prefix, symbol);
        SymbolFilterResponse expectedResponse = this.perfectFilter.query(prefix, symbol);

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
    public void update(Word<LocalTimerMealySemanticInputSymbol<I>> prefix, NonDelayingInput<I> symbol, SymbolFilterResponse response) {
        delegate.update(prefix, symbol, response);
    }

    @Override
    public void setStatsContainer(StatsContainerX container) {
        this.stats = container;
    }
}
