package de.learnlib.oracle.symbol_filters.mmlt;

import de.learnlib.oracle.symbol_filters.StatisticsSymbolFilter;
import de.learnlib.symbol_filter.SymbolFilter;
import de.learnlib.symbol_filter.SymbolFilterResponse;
import net.automatalib.alphabet.time.mmlt.LocalTimerMealySemanticInputSymbol;
import net.automatalib.alphabet.time.mmlt.NonDelayingInput;
import net.automatalib.automaton.time.mmlt.LocalTimerMealy;
import net.automatalib.word.Word;

public class LocalTimerMealyStatisticsSymbolFilter<S, I, O> extends StatisticsSymbolFilter<LocalTimerMealySemanticInputSymbol<I>, NonDelayingInput<I>> {

    private final LocalTimerMealy<S, I, O> automaton;

    public LocalTimerMealyStatisticsSymbolFilter(LocalTimerMealy<S, I, O> automaton, SymbolFilter<LocalTimerMealySemanticInputSymbol<I>, NonDelayingInput<I>> delegate) {
        super(delegate);
        this.automaton = automaton;
    }

    @Override
    protected SymbolFilterResponse isIgnorable(Word<LocalTimerMealySemanticInputSymbol<I>> prefix, NonDelayingInput<I> symbol) {
        return LocalTimerMealySymbolFilterUtil.isIgnorable(this.automaton, prefix, symbol);
    }

}
