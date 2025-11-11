package de.learnlib.algorithm.lstar.mmlt.cex.results;


import net.automatalib.symbol.time.TimedInput;
import net.automatalib.symbol.time.InputSymbol;

/**
 * There should be a local reset at the specified transition.
 *
 * @param <I> Input type for non-delaying inputs
 * @param <O> Output symbol type
 */
public class MissingResetResult<I, O> extends CexAnalysisResult<I, O> {
    private final Integer location;
    private final InputSymbol<I> input;

    public MissingResetResult(Integer location, InputSymbol<I> input) {
        this.location = location;
        this.input = input;
    }

    public Integer getLocation() {
        return location;
    }

    public TimedInput<I> getInput() {
        return input;
    }
}
