package de.learnlib.algorithm.lstar.mmlt.filter;

import de.learnlib.filter.symbol.AbstractStatisticsSymbolFilter;
import de.learnlib.statistic.StatisticsCollector;
import de.learnlib.filter.SymbolFilter;
import de.learnlib.filter.FilterResponse;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.symbol.time.InputSymbol;
import net.automatalib.automaton.mmlt.MMLT;
import net.automatalib.word.Word;

public class MMLTStatisticsSymbolFilter<I, O> extends AbstractStatisticsSymbolFilter<TimedInput<I>, InputSymbol<I>> {

    private final MMLT<?, I, ?, O> automaton;

    public MMLTStatisticsSymbolFilter(MMLT<?, I, ?, O> automaton, SymbolFilter<TimedInput<I>, InputSymbol<I>> delegate, StatisticsCollector stats) {
        super(delegate);
        this.automaton = automaton;
    }

    @Override
    protected FilterResponse isIgnorable(Word<TimedInput<I>> prefix, InputSymbol<I> symbol) {
        return MMLTSymbolFilterUtil.isIgnorable(this.automaton, prefix, symbol);
    }

}
