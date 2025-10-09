package de.learnlib.algorithm.lstar.mmlt.cex.results;


import net.automatalib.alphabet.time.mmlt.NonDelayingInput;

/**
 * The specified symbol is considered to be falsely ignored by the symbol filter.
 *
 * @param <S> Location type
 * @param <I> Input type for non-delaying inputs
 * @param <O> Output symbol type
 */
public class FalseIgnoreResult<S, I, O> extends CexAnalysisResult<S, I, O> {
    private final S location;
    private final NonDelayingInput<I> symbol;

    public FalseIgnoreResult(S location, NonDelayingInput<I> symbol) {
        this.location = location;
        this.symbol = symbol;
    }

    public S getLocation() {
        return location;
    }

    public NonDelayingInput<I> getSymbol() {
        return symbol;
    }
}
