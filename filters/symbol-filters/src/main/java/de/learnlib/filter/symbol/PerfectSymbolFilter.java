package de.learnlib.filter.symbol;


import de.learnlib.filter.SymbolFilter;
import de.learnlib.filter.SymbolFilterResponse;
import net.automatalib.word.Word;

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
}
