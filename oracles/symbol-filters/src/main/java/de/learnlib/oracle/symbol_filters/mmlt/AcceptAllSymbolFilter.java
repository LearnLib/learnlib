package de.learnlib.oracle.symbol_filters.mmlt;


import de.learnlib.symbol_filter.SymbolFilter;
import de.learnlib.symbol_filter.SymbolFilterResponse;
import net.automatalib.alphabet.time.mmlt.LocalTimerMealySemanticInputSymbol;
import net.automatalib.alphabet.time.mmlt.NonDelayingInput;
import net.automatalib.word.Word;

/**
 * A pass-through filter that accepts all inputs.
 *
 * @param <I> Input type for non-delaying inputs
 */
public class AcceptAllSymbolFilter<I, O> implements SymbolFilter<I, O> {
    @Override
    public SymbolFilterResponse query(Word<LocalTimerMealySemanticInputSymbol<I>> prefix, NonDelayingInput<I> symbol) {
        return SymbolFilterResponse.ACCEPT;
    }

    @Override
    public void update(Word<LocalTimerMealySemanticInputSymbol<I>> prefix, NonDelayingInput<I> symbol, SymbolFilterResponse response) {
        throw new IllegalStateException("Not supported.");
    }
}
