package de.learnlib.filter.symbol;

import de.learnlib.filter.SymbolFilter;
import de.learnlib.filter.SymbolFilterResponse;
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
}
