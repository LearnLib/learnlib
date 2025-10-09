package de.learnlib.algorithm.lstar.mmlt.cex.results;


import net.automatalib.alphabet.time.mmlt.LocalTimerMealySemanticInputSymbol;
import net.automatalib.alphabet.time.mmlt.NonDelayingInput;

/**
 * There should be a local reset at the specified transition.
 *
 * @param <S> Location type
 * @param <I> Input type for non-delaying inputs
 * @param <O> Output symbol type
 */
public class MissingResetResult<S, I, O> extends CexAnalysisResult<S, I, O> {
    private final S location;
    private final NonDelayingInput<I> input;

    public MissingResetResult(S location, NonDelayingInput<I> input) {
        this.location = location;
        this.input = input;
    }

    public S getLocation() {
        return location;
    }

    public LocalTimerMealySemanticInputSymbol<I> getInput() {
        return input;
    }
}
