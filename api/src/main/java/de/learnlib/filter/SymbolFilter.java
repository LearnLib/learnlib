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
public interface SymbolFilter<U, V> {

    /**
     * Predicts whether the provided symbol is ignorable in the state
     * that is addressed by the given prefix.
     * <p>
     * <i>ignorable</i> typically means that the symbol triggers a silent self-loop in the considered state.
     * However, the semantics may vary depending on the concrete implementation.
     * <p>
     * Predictions may not be correct, i.e., an accepted symbol may be actually ignorable and an ignored symbol
     * may be actually not ignorable.
     *
     * @param prefix State prefix.
     * @param symbol Input of the queried transition.
     * @return IGNORE if the symbol is considered ignorable, ACCEPT if it is not.
     */
    SymbolFilterResponse query(Word<U> prefix, V symbol);
}
