package de.learnlib.oracle.symbol_filters.mmlt;


import de.learnlib.symbol_filter.SymbolFilter;
import de.learnlib.symbol_filter.SymbolFilterResponse;
import net.automatalib.alphabet.time.mmlt.LocalTimerMealySemanticInputSymbol;
import net.automatalib.alphabet.time.mmlt.NonDelayingInput;
import net.automatalib.word.Word;

import java.util.HashMap;
import java.util.Map;

/**
 * Wrapper for a symbol filter that caches previous responses + allows caller to update these.
 *
 * @param <I> Input type for non-delaying inputs
 * @param <O> Output symbol type
 */
public class CachedSymbolFilter<I, O> implements SymbolFilter<I, O> {
    private final Map<Word<LocalTimerMealySemanticInputSymbol<I>>, Boolean> previousResponses; // transition -> legal/ignore
    private final SymbolFilter<I, O> delegate;

    public CachedSymbolFilter(SymbolFilter<I, O> delegate) {
        this.delegate = delegate;
        this.previousResponses = new HashMap<>();
    }

    @Override
    public SymbolFilterResponse query(Word<LocalTimerMealySemanticInputSymbol<I>> prefix, NonDelayingInput<I> symbol) {
        var oldResponse = this.previousResponses.get(prefix.append(symbol));
        if (oldResponse != null) {
            return (oldResponse) ? SymbolFilterResponse.ACCEPT : SymbolFilterResponse.IGNORE;
        }

        var res = delegate.query(prefix, symbol);
        this.previousResponses.put(prefix.append(symbol), res == SymbolFilterResponse.ACCEPT);
        return res;
    }

    @Override
    public void update(Word<LocalTimerMealySemanticInputSymbol<I>> prefix, NonDelayingInput<I> symbol, SymbolFilterResponse response) {
        if (!this.previousResponses.containsKey(prefix.append(symbol))) {
            throw new IllegalArgumentException("Can only update response if already queried.");
        }
        this.previousResponses.put(prefix.append(symbol), (response == SymbolFilterResponse.ACCEPT));
    }
}
