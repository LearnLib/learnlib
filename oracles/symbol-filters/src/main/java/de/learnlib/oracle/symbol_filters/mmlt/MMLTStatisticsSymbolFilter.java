package de.learnlib.oracle.symbol_filters.mmlt;

import de.learnlib.oracle.symbol_filters.StatisticsSymbolFilter;
import de.learnlib.statistic.StatsContainer;
import de.learnlib.symbol_filter.SymbolFilter;
import de.learnlib.symbol_filter.SymbolFilterResponse;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.symbol.time.InputSymbol;
import net.automatalib.automaton.mmlt.MMLT;
import net.automatalib.word.Word;

public class MMLTStatisticsSymbolFilter<I, O> extends StatisticsSymbolFilter<TimedInput<I>, InputSymbol<I>> {

    private final MMLT<?, I, ?, O> automaton;

    public MMLTStatisticsSymbolFilter(MMLT<?, I, ?, O> automaton, SymbolFilter<TimedInput<I>, InputSymbol<I>> delegate, StatsContainer stats) {
        super(delegate);
        this.automaton = automaton;
    }

    @Override
    protected SymbolFilterResponse isIgnorable(Word<TimedInput<I>> prefix, InputSymbol<I> symbol) {
        return MMLTSymbolFilterUtil.isIgnorable(this.automaton, prefix, symbol);
    }

}
