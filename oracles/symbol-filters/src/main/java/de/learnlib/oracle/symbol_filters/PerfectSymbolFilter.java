package de.learnlib.oracle.symbol_filters;


import de.learnlib.symbol_filter.SymbolFilter;
import de.learnlib.symbol_filter.SymbolFilterResponse;
import net.automatalib.word.Word;

import java.util.Random;

/**
 * A symbol filter that answers all queries correctly.
 *
 * @param <U> Type for symbols in the prefix of the considered states
 * @param <V> Type of the queried symbols
 */
public abstract class PerfectSymbolFilter<U, V> implements SymbolFilter<U, V> {

    protected abstract SymbolFilterResponse isIgnorable(Word<U> prefix, V symbol);

    @Override
    public SymbolFilterResponse query(Word<U> prefix, V symbol) {

        if (isIgnorable(prefix, symbol) == SymbolFilterResponse.IGNORE) {
            return SymbolFilterResponse.IGNORE;
        } else {
            return SymbolFilterResponse.ACCEPT;
        }
    }

    @Override
    public void update(Word<U> prefix, V symbol, SymbolFilterResponse response) {
        throw new IllegalStateException("Not supported.");
    }
}
