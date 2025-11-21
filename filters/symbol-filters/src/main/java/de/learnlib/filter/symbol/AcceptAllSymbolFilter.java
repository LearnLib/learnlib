package de.learnlib.filter.symbol;


import de.learnlib.filter.MutableSymbolFilter;
import de.learnlib.filter.SymbolFilterResponse;
import net.automatalib.word.Word;

/**
 * A pass-through filter that accepts all inputs.
 *
 * @param <U> Type for symbols in the prefix of the considered states
 * @param <V> Type of the queried symbols
 */
public class AcceptAllSymbolFilter<U, V> implements MutableSymbolFilter<U, V> {
    @Override
    public SymbolFilterResponse query(Word<U> prefix, V symbol) {
        return SymbolFilterResponse.ACCEPT;
    }

    @Override
    public void accept(Word<U> prefix, V symbol) {
        // we don't need to do anything because we always return ACCEPT anyway
    }
}
