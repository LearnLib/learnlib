package de.learnlib.oracle.symbol_filters.mmlt;


import de.learnlib.oracle.symbol_filters.PerfectSymbolFilter;
import de.learnlib.symbol_filter.SymbolFilterResponse;
import net.automatalib.alphabet.time.mmlt.LocalTimerMealySemanticInputSymbol;
import net.automatalib.alphabet.time.mmlt.NonDelayingInput;
import net.automatalib.automaton.time.mmlt.LocalTimerMealy;
import net.automatalib.word.Word;

/**
 * A symbol filter for MMLTs that correctly accepts and ignores all transitions
 * that silently self-loop.
 *
 * @param <S> Location type
 * @param <I> Input type for non-delaying inputs
 * @param <O> Output symbol type
 */
public class LocalTimerMealyPerfectSymbolFilter<S, I, O> extends PerfectSymbolFilter<LocalTimerMealySemanticInputSymbol<I>, NonDelayingInput<I>> {

    private final LocalTimerMealy<S, I, O> automaton;

    public LocalTimerMealyPerfectSymbolFilter(LocalTimerMealy<S, I, O> automaton) {
        this.automaton = automaton;
    }

    @Override
    protected SymbolFilterResponse isIgnorable(Word<LocalTimerMealySemanticInputSymbol<I>> prefix, NonDelayingInput<I> symbol) {
        return LocalTimerMealySymbolFilterUtil.isIgnorable(this.automaton, prefix, symbol);
    }

}
