package de.learnlib.algorithm.lstar.mmlt.cex.results;


import net.automatalib.symbol.time.InputSymbol;

/**
 * The specified symbol is considered to be falsely ignored by the symbol filter.
 *
 * @param <I> Input type for non-delaying inputs
 * @param <O> Output symbol type
 */
public class FalseIgnoreResult<I, O> extends CexAnalysisResult<I, O> {
    private final Integer location;
    private final InputSymbol<I> symbol;

    public FalseIgnoreResult(Integer location, InputSymbol<I> symbol) {
        this.location = location;
        this.symbol = symbol;
    }

    public Integer getLocation() {
        return location;
    }

    public InputSymbol<I> getSymbol() {
        return symbol;
    }
}
