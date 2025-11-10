package de.learnlib.algorithm.lstar.mmlt.cex.results;

import net.automatalib.symbol.time.TimedInput;
import net.automatalib.word.Word;

/**
 * The target at the identified transition is incorrect due to a missing discriminator.
 *
 * @param <S> Location type
 * @param <I> Input type for non-delaying inputs
 * @param <O> Output symbol type
 */
public class MissingDiscriminatorResult<S, I, O> extends CexAnalysisResult<S, I, O> {
    private final S location;
    private final TimedInput<I> input;
    private final Word<TimedInput<I>> discriminator;

    public MissingDiscriminatorResult(S location, TimedInput<I> input, Word<TimedInput<I>> discriminator) {
        this.location = location;
        this.input = input;
        this.discriminator = discriminator;
    }

    public S getLocation() {
        return location;
    }

    public TimedInput<I> getInput() {
        return input;
    }

    public Word<TimedInput<I>> getDiscriminator() {
        return discriminator;
    }

}
