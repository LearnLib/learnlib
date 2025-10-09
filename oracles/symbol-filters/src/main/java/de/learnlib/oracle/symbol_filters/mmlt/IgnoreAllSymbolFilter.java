package de.learnlib.oracle.symbol_filters.mmlt;

import de.learnlib.symbol_filter.SymbolFilter;
import de.learnlib.symbol_filter.SymbolFilterResponse;
import net.automatalib.alphabet.time.mmlt.LocalTimerMealySemanticInputSymbol;
import net.automatalib.alphabet.time.mmlt.NonDelayingInput;
import net.automatalib.word.Word;

/**
 * A symbol filter that ignores all symbols.
 *
 * @param <I> Input type for non-delaying inputs
 */
public class IgnoreAllSymbolFilter<I, O> implements SymbolFilter<I, O> {
    @Override
    public SymbolFilterResponse query(Word<LocalTimerMealySemanticInputSymbol<I>> prefix, NonDelayingInput<I> symbol) {
        return SymbolFilterResponse.IGNORE;
    }

    @Override
    public void update(Word<LocalTimerMealySemanticInputSymbol<I>> prefix, NonDelayingInput<I> symbol, SymbolFilterResponse response) {
        throw new IllegalStateException("Not supported.");
    }

}
