package de.learnlib.symbol_filter;

import net.automatalib.alphabet.time.mmlt.LocalTimerMealySemanticInputSymbol;
import net.automatalib.alphabet.time.mmlt.NonDelayingInput;
import net.automatalib.word.Word;

/**
 * Interface for a symbol filter that can be used to speed-up the learning of MMLTs.
 *
 * @param <I> Input type for non-delaying inputs
 * @param <O> Output symbol type
 */
public interface SymbolFilter<I, O> {

    /**
     * Predicts whether the provided symbol is not ignorable in the configuration
     * identified by the provided prefix.
     * <p>
     * "Ignorable" means the symbol belongs to a silent self-loop.
     * <p>
     * Predictions may not be correct, i.e., an accepted symbol may be actually ignorable and an ignored symbol
     * may be actually not ignorable.
     *
     * @param prefix Configuration prefix. May contain time steps.
     * @param symbol Queried transition
     * @return IGNORE if the symbol is considered ignorable, ACCEPT if it is not.
     */
    SymbolFilterResponse query(Word<LocalTimerMealySemanticInputSymbol<I>> prefix, NonDelayingInput<I> symbol);

    /**
     * Sets the response of the filter for the given transition to the provided response.
     *
     * @param prefix   Configuration prefix.
     * @param symbol   Queried transition
     * @param response New response
     */
    void update(Word<LocalTimerMealySemanticInputSymbol<I>> prefix, NonDelayingInput<I> symbol, SymbolFilterResponse response);
}
