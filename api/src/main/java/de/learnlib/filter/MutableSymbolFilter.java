package de.learnlib.filter;

import net.automatalib.word.Word;

/**
 * Interface for a symbol filter.
 * A symbol filter predicts whether a given transition is ignorable in a given state.
 * This information can be used to avoid redundant queries.
 * A symbol filter may answer incorrectly.
 *
 * @param <U> Type for symbols in the prefix of the considered states
 * @param <V> Type of the queried symbols
 */
public interface MutableSymbolFilter<U, V> extends SymbolFilter<U, V> {

    /**
     * Sets the response of the filter for the given transition to the provided response.
     *
     * @param prefix
     *         State prefix.
     * @param symbol
     *         Input of the transition that should be updated.
     */
    void accept(Word<U> prefix, V symbol);
}
