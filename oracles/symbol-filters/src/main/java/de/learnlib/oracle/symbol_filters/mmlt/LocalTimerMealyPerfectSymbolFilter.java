package de.learnlib.oracle.symbol_filters.mmlt;


import de.learnlib.oracle.symbol_filters.PerfectSymbolFilter;
import de.learnlib.symbol_filter.SymbolFilterResponse;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.symbol.time.InputSymbol;
import net.automatalib.automaton.mmlt.MMLT;
import net.automatalib.word.Word;

/**
 * A symbol filter for MMLTs that correctly accepts and ignores all transitions
 * that silently self-loop.
 *
 * @param <I> Input type for non-delaying inputs
 * @param <O> Output symbol type
 */
public class LocalTimerMealyPerfectSymbolFilter<I, O> extends PerfectSymbolFilter<TimedInput<I>, InputSymbol<I>> {

    private final MMLT<?, I, ?, O> automaton;

    public LocalTimerMealyPerfectSymbolFilter(MMLT<?, I, ?, O> automaton) {
        this.automaton = automaton;
    }

    @Override
    protected SymbolFilterResponse isIgnorable(Word<TimedInput<I>> prefix, InputSymbol<I> symbol) {
        return LocalTimerMealySymbolFilterUtil.isIgnorable(this.automaton, prefix, symbol);
    }

}
