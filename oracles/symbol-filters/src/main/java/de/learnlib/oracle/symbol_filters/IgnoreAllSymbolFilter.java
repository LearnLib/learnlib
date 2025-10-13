package de.learnlib.oracle.symbol_filters;

import de.learnlib.symbol_filter.SymbolFilter;
import de.learnlib.symbol_filter.SymbolFilterResponse;
import net.automatalib.alphabet.time.mmlt.LocalTimerMealySemanticInputSymbol;
import net.automatalib.alphabet.time.mmlt.NonDelayingInput;
import net.automatalib.word.Word;

/**
 * A pass-through filter that ignores all inputs.
 *
 * @param <U> Type for symbols in the prefix of the considered states
 * @param <V> Type of the queried symbols
 */
public class IgnoreAllSymbolFilter<U, V> implements SymbolFilter<U, V> {
    @Override
    public SymbolFilterResponse query(Word<U> prefix, V symbol) {
        return SymbolFilterResponse.IGNORE;
    }

    @Override
    public void update(Word<U> prefix, V symbol, SymbolFilterResponse response) {
        throw new IllegalStateException("Not supported.");
    }
}
