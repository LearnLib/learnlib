package de.learnlib.filter.symbol;


import de.learnlib.filter.MutableSymbolFilter;
import de.learnlib.filter.SymbolFilter;
import de.learnlib.filter.SymbolFilterResponse;
import net.automatalib.word.Word;

import java.util.HashMap;
import java.util.Map;

/**
 * Wrapper for a symbol filter that caches previous responses + allows caller to update these.
 *
 * @param <U> Type for symbols in the prefix of the considered states
 * @param <V> Type of the queried symbols
 */
public class CachedSymbolFilter<U, V> implements MutableSymbolFilter<U, V> {
    private final Map<Word<U>, Map<V, Boolean>> previousResponses; // prefix -> (input -> legal/ignore)
    private final SymbolFilter<U, V> delegate;

    public CachedSymbolFilter(SymbolFilter<U, V> delegate) {
        this.delegate = delegate;
        this.previousResponses = new HashMap<>();
    }

    @Override
    public SymbolFilterResponse query(Word<U> prefix, V symbol) {
        this.previousResponses.putIfAbsent(prefix, new HashMap<>());
        var oldResponse = this.previousResponses.get(prefix).get(symbol);
        if (oldResponse != null) {
            return (oldResponse) ? SymbolFilterResponse.ACCEPT : SymbolFilterResponse.IGNORE;
        }

        var res = delegate.query(prefix, symbol);
        this.update(prefix, symbol, res);
        return res;
    }

    @Override
    public void accept(Word<U> prefix, V symbol) {
        this.update(prefix, symbol, SymbolFilterResponse.ACCEPT);
    }

    private void update(Word<U> prefix, V symbol, SymbolFilterResponse response) {
        this.previousResponses.putIfAbsent(prefix, new HashMap<>());
        this.previousResponses.get(prefix).put(symbol, (response == SymbolFilterResponse.ACCEPT));
    }
}
