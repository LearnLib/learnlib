package de.learnlib.algorithm.lstar.mmlt.cex.results;


import net.automatalib.symbol.time.TimedInput;
import net.automatalib.symbol.time.InputSymbol;

/**
 * There should be a local reset at the specified transition.
 *
 * @param <S> Location type
 * @param <I> Input type for non-delaying inputs
 * @param <O> Output symbol type
 */
public class MissingResetResult<S, I, O> extends CexAnalysisResult<S, I, O> {
    private final S location;
    private final InputSymbol<I> input;

    public MissingResetResult(S location, InputSymbol<I> input) {
        this.location = location;
        this.input = input;
    }

    public S getLocation() {
        return location;
    }

    public TimedInput<I> getInput() {
        return input;
    }
}
