package de.learnlib.filter.symbol;


import de.learnlib.filter.SymbolFilter;
import de.learnlib.filter.SymbolFilterResponse;
import net.automatalib.word.Word;

import java.util.Random;

/**
 * A symbol filter that falsely answers a query with a specified probability.
 *
 * @param <U> Type for symbols in the prefix of the considered states
 * @param <V> Type of the queried symbols
 */
public abstract class RandomSymbolFilter<U, V> implements SymbolFilter<U, V> {

    private final double inaccurateProb;
    private final Random random;

    public RandomSymbolFilter(double inaccurateProb, Random random) {
        if (inaccurateProb > 1 || inaccurateProb < 0) {
            throw new IllegalArgumentException("Ratios must be between zero and 1 (inclusive).");
        }

        this.inaccurateProb = inaccurateProb;
        this.random = random;
    }

    protected abstract SymbolFilterResponse isIgnorable(Word<U> prefix, V symbol);

    @Override
    public SymbolFilterResponse query(Word<U> prefix, V symbol) {
        boolean ignorable = isIgnorable(prefix, symbol) == SymbolFilterResponse.IGNORE;

        // Randomly misclassify:
        if (this.random.nextDouble() <= this.inaccurateProb) {
            ignorable = !ignorable;
        }

        if (ignorable) {
            return SymbolFilterResponse.IGNORE;
        } else {
            return SymbolFilterResponse.ACCEPT;
        }
    }
}
